from __future__ import annotations

import json
from pathlib import Path
from typing import Any

from fastapi import FastAPI, File, Form, HTTPException, Request, UploadFile
from fastapi.responses import FileResponse, HTMLResponse, RedirectResponse
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates

from .chain import ValidationError
from .commands import (
    RegisterCaregiverCommand,
    RegisterLostPetCommand,
    RegisterSightingCommand,
    SearchByImageCommand,
    ToggleCaregiverAlertsCommand,
)
from .config import SettingsSingleton
from .facades import AlertMascotaFacade, BusquedaImagenFacade, CuidadorFacade
from .models import Caregiver, Report, SearchResult
from .observer import ConsoleNotificationObserver, EventBus, HistoryObserver
from .repository import SQLiteRepository

BASE_DIR = Path(__file__).resolve().parent
TEMPLATES = Jinja2Templates(directory=str(BASE_DIR / "templates"))


def render_template(request: Request, template_name: str, context: dict[str, Any]) -> HTMLResponse:
    """Renderiza plantillas de forma compatible con versiones nuevas de Starlette/FastAPI.

    En Starlette reciente la firma estable es:
        TemplateResponse(request, name, context)
    Usar esta forma evita el error ``TypeError: unhashable type: 'dict'`` que aparece
    cuando se ejecuta con versiones nuevas y se usa la firma antigua.
    """
    full_context = {"request": request, **context}
    return TEMPLATES.TemplateResponse(request, template_name, full_context)


def report_to_dict(report: Report) -> dict[str, Any]:
    return {
        "id": report.id,
        "type": report.report_type.value,
        "pet_name": report.pet.name,
        "species": report.pet.species,
        "breed": report.pet.breed,
        "description": report.description,
        "latitude": report.location.latitude,
        "longitude": report.location.longitude,
        "status": report.status.value,
        "radius_meters": report.radius_meters,
        "reporter_alias": report.reporter_alias,
    }


def caregiver_to_dict(caregiver: Caregiver) -> dict[str, Any]:
    return {
        "id": caregiver.id,
        "full_name": caregiver.full_name,
        "dni": caregiver.dni,
        "role": caregiver.role.value,
        "species_accepted": caregiver.species_accepted,
        "size_accepted": caregiver.size_accepted,
        "medication": caregiver.medication,
        "accepts_lost_pet_alerts": caregiver.accepts_lost_pet_alerts,
        "status": caregiver.status.value,
        "average_rating": caregiver.average_rating,
    }


def search_result_to_dict(result: SearchResult) -> dict[str, Any]:
    return {
        "source": result.source,
        "title": result.title,
        "description": result.description,
        "score": result.score,
        "certified": result.certified,
        "location": result.location,
    }


def create_app(repository: SQLiteRepository | None = None) -> FastAPI:
    settings = SettingsSingleton.get()
    repo = repository or SQLiteRepository(settings.database_path)
    event_bus = EventBus()
    history = HistoryObserver()
    console = ConsoleNotificationObserver()
    event_bus.subscribe("lost_pet_registered", history)
    event_bus.subscribe("lost_pet_registered", console)
    event_bus.subscribe("sighting_registered", history)
    event_bus.subscribe("sighting_registered", console)

    alert_facade = AlertMascotaFacade(repo, event_bus)
    search_facade = BusquedaImagenFacade(repo)
    caregiver_facade = CuidadorFacade(repo)

    app = FastAPI(
        title="UNI Mascotas",
        description="Sistema de mascotas perdidas, búsqueda por imagen y red de cuidadores usando patrones GoF.",
        version="1.0.0",
    )
    app.mount("/static", StaticFiles(directory=str(BASE_DIR / "static")), name="static")
    app.mount("/uploads", StaticFiles(directory=str(settings.upload_dir)), name="uploads")

    @app.get("/favicon.ico", include_in_schema=False)
    def favicon() -> FileResponse:
        return FileResponse(BASE_DIR / "static" / "img" / "logo_uni.png")

    @app.get("/", response_class=HTMLResponse)
    def home(request: Request) -> HTMLResponse:
        return render_template(
            request,
            "index.html",
            {
                "title": "UNI Huellitas",
                "reports": alert_facade.recent_reports(),
                "caregivers": caregiver_facade.list_caregivers(),
                "events": history.events[-8:],
                "message": None,
            },
        )

    @app.get("/adopcion", response_class=HTMLResponse)
    def adoption_page(request: Request) -> HTMLResponse:
        return render_template(
            request,
            "adoption.html",
            {
                "title": "Adopción · UNI Huellitas",
                "adoptions": repo.list_adoption_catalog(),
                "message": None,
            },
        )

    @app.get("/perdidos-y-encontrados", response_class=HTMLResponse)
    def lost_found_page(request: Request, tipo: str = "all", species: str = "") -> HTMLResponse:
        reports = repo.list_reports_filtered(report_type=tipo, species=species.strip() or None)
        return render_template(
            request,
            "lost_found.html",
            {
                "title": "Perdidos y Encontrados · UNI Huellitas",
                "reports": reports,
                "tipo": tipo,
                "species": species,
                "message": None,
            },
        )

    @app.get("/publicar-anuncio", response_class=HTMLResponse)
    def publish_page(request: Request) -> HTMLResponse:
        return render_template(
            request,
            "publish.html",
            {
                "title": "Publicar Anuncio · UNI Huellitas",
                "message": None,
            },
        )

    @app.get("/buscar-por-imagen", response_class=HTMLResponse)
    def image_search_page(request: Request) -> HTMLResponse:
        return render_template(
            request,
            "image_search.html",
            {
                "title": "Buscar por Imagen · UNI Huellitas",
                "message": None,
            },
        )

    @app.get("/cuidadores", response_class=HTMLResponse)
    def caregivers_page(request: Request) -> HTMLResponse:
        return render_template(
            request,
            "caregivers.html",
            {
                "title": "Cuidadores · UNI Huellitas",
                "caregivers": caregiver_facade.list_caregivers(),
                "message": None,
            },
        )

    @app.post("/reports/lost", response_class=HTMLResponse)
    async def ui_register_lost_pet(
        request: Request,
        pet_name: str = Form(...),
        species: str = Form(...),
        breed: str = Form(...),
        description: str = Form(...),
        latitude: float = Form(...),
        longitude: float = Form(...),
        owner_name: str = Form(...),
        owner_email: str = Form(...),
        owner_phone: str = Form(...),
        radius_meters: int = Form(1000),
        photo: UploadFile | None = File(None),
    ) -> HTMLResponse:
        try:
            result = RegisterLostPetCommand(
                alert_facade,
                {
                    "pet_name": pet_name,
                    "species": species,
                    "breed": breed,
                    "description": description,
                    "latitude": latitude,
                    "longitude": longitude,
                    "owner_name": owner_name,
                    "owner_email": owner_email,
                    "owner_phone": owner_phone,
                    "radius_meters": radius_meters,
                },
                photo.file if photo else None,
                photo.filename if photo else None,
            ).execute()
            message = f"Reporte #{result['report'].id} registrado. Alertas enviadas: {result['alert']['recipients_count']}."
        except (ValidationError, ValueError) as exc:
            message = f"Error: {exc}"
        return render_template(
            request,
            "lost_found.html",
            {
                "title": "Perdidos y Encontrados · UNI Huellitas",
                "reports": repo.list_reports_filtered(),
                "tipo": "all",
                "species": "",
                "message": message,
            },
        )

    @app.post("/reports/sighting", response_class=HTMLResponse)
    async def ui_register_sighting(
        request: Request,
        species: str = Form(...),
        breed: str = Form("desconocida"),
        description: str = Form(...),
        latitude: float = Form(...),
        longitude: float = Form(...),
        reporter_alias: str = Form("Ciudadano anónimo"),
        photo: UploadFile | None = File(None),
    ) -> HTMLResponse:
        try:
            report = RegisterSightingCommand(
                alert_facade,
                {
                    "species": species,
                    "breed": breed,
                    "description": description,
                    "latitude": latitude,
                    "longitude": longitude,
                    "reporter_alias": reporter_alias,
                },
                photo.file if photo else None,
                photo.filename if photo else None,
            ).execute()
            message = f"Avistamiento #{report.id} registrado correctamente."
        except (ValidationError, ValueError) as exc:
            message = f"Error: {exc}"
        return render_template(
            request,
            "lost_found.html",
            {
                "title": "Perdidos y Encontrados · UNI Huellitas",
                "reports": repo.list_reports_filtered(),
                "tipo": "all",
                "species": "",
                "message": message,
            },
        )

    @app.post("/search", response_class=HTMLResponse)
    async def ui_search(
        request: Request,
        intent: str = Form(...),
        species: str = Form(""),
        breed: str = Form(""),
        image: UploadFile | None = File(None),
    ) -> HTMLResponse:
        try:
            metadata_json = json.dumps({"species": species, "breed": breed})
            results = SearchByImageCommand(search_facade, intent, metadata_json).execute()
            message = f"Búsqueda ejecutada. Resultados: {len(results)}."
        except (ValidationError, ValueError) as exc:
            results = []
            message = f"Error: {exc}"
        return render_template(
            request,
            "search.html",
            {"title": "Resultados · UNI Huellitas", "results": results, "message": message},
        )

    @app.post("/caregivers", response_class=HTMLResponse)
    async def ui_register_caregiver(
        request: Request,
        full_name: str = Form(...),
        dni: str = Form(...),
        role: str = Form(...),
        species_accepted: str = Form(...),
        size_accepted: str = Form(...),
        medication: bool = Form(False),
        accepts_lost_pet_alerts: bool = Form(False),
    ) -> HTMLResponse:
        try:
            caregiver = RegisterCaregiverCommand(
                caregiver_facade,
                {
                    "full_name": full_name,
                    "dni": dni,
                    "role": role,
                    "species_accepted": species_accepted,
                    "size_accepted": size_accepted,
                    "medication": medication,
                    "accepts_lost_pet_alerts": accepts_lost_pet_alerts,
                },
            ).execute()
            message = f"Cuidador #{caregiver.id} registrado con estado {caregiver.status.value}."
        except (ValidationError, ValueError) as exc:
            message = f"Error: {exc}"
        return render_template(
            request,
            "caregivers.html",
            {
                "title": "Cuidadores · UNI Huellitas",
                "caregivers": caregiver_facade.list_caregivers(),
                "message": message,
            },
        )

    @app.post("/caregivers/{caregiver_id}/toggle")
    def ui_toggle_caregiver(caregiver_id: int, enabled: bool = Form(...)) -> RedirectResponse:
        ToggleCaregiverAlertsCommand(caregiver_facade, caregiver_id, enabled).execute()
        return RedirectResponse(url="/cuidadores", status_code=303)

    @app.post("/api/reports/lost")
    async def api_register_lost_pet(
        pet_name: str = Form(...),
        species: str = Form(...),
        breed: str = Form(...),
        description: str = Form(...),
        latitude: float = Form(...),
        longitude: float = Form(...),
        owner_name: str = Form(...),
        owner_email: str = Form(...),
        owner_phone: str = Form(...),
        radius_meters: int = Form(1000),
        photo: UploadFile | None = File(None),
    ) -> dict[str, Any]:
        try:
            result = RegisterLostPetCommand(
                alert_facade,
                {
                    "pet_name": pet_name,
                    "species": species,
                    "breed": breed,
                    "description": description,
                    "latitude": latitude,
                    "longitude": longitude,
                    "owner_name": owner_name,
                    "owner_email": owner_email,
                    "owner_phone": owner_phone,
                    "radius_meters": radius_meters,
                },
                photo.file if photo else None,
                photo.filename if photo else None,
            ).execute()
            owner_view = result["owner_public_view"]
            return {
                "report": report_to_dict(result["report"]),
                "alert": result["alert"],
                "owner_public_view": {"display_name": owner_view.display_name, "contact_channel": owner_view.contact_channel} if owner_view else None,
            }
        except (ValidationError, ValueError) as exc:
            raise HTTPException(status_code=400, detail=str(exc)) from exc

    @app.post("/api/reports/sighting")
    async def api_register_sighting(
        species: str = Form(...),
        breed: str = Form("desconocida"),
        description: str = Form(...),
        latitude: float = Form(...),
        longitude: float = Form(...),
        reporter_alias: str = Form("Ciudadano anónimo"),
        photo: UploadFile | None = File(None),
    ) -> dict[str, Any]:
        try:
            report = RegisterSightingCommand(
                alert_facade,
                {
                    "species": species,
                    "breed": breed,
                    "description": description,
                    "latitude": latitude,
                    "longitude": longitude,
                    "reporter_alias": reporter_alias,
                },
                photo.file if photo else None,
                photo.filename if photo else None,
            ).execute()
            return {"report": report_to_dict(report)}
        except (ValidationError, ValueError) as exc:
            raise HTTPException(status_code=400, detail=str(exc)) from exc

    @app.post("/api/search")
    async def api_search(intent: str = Form(...), metadata_json: str | None = Form(None)) -> dict[str, Any]:
        try:
            results = SearchByImageCommand(search_facade, intent, metadata_json).execute()
            return {"results": [search_result_to_dict(result) for result in results]}
        except (ValidationError, ValueError) as exc:
            raise HTTPException(status_code=400, detail=str(exc)) from exc

    @app.post("/api/caregivers")
    async def api_register_caregiver(
        full_name: str = Form(...),
        dni: str = Form(...),
        role: str = Form(...),
        species_accepted: str = Form(...),
        size_accepted: str = Form(...),
        medication: bool = Form(False),
        accepts_lost_pet_alerts: bool = Form(True),
    ) -> dict[str, Any]:
        try:
            caregiver = RegisterCaregiverCommand(
                caregiver_facade,
                {
                    "full_name": full_name,
                    "dni": dni,
                    "role": role,
                    "species_accepted": species_accepted,
                    "size_accepted": size_accepted,
                    "medication": medication,
                    "accepts_lost_pet_alerts": accepts_lost_pet_alerts,
                },
            ).execute()
            return {"caregiver": caregiver_to_dict(caregiver)}
        except (ValidationError, ValueError) as exc:
            raise HTTPException(status_code=400, detail=str(exc)) from exc

    @app.post("/api/caregivers/{caregiver_id}/alerts")
    async def api_toggle_caregiver_alerts(caregiver_id: int, enabled: bool = Form(...)) -> dict[str, Any]:
        caregiver = ToggleCaregiverAlertsCommand(caregiver_facade, caregiver_id, enabled).execute()
        if caregiver is None:
            raise HTTPException(status_code=404, detail="Cuidador no encontrado.")
        return {"caregiver": caregiver_to_dict(caregiver)}

    @app.get("/api/reports")
    async def api_reports() -> dict[str, Any]:
        return {"reports": [report_to_dict(report) for report in alert_facade.recent_reports()]}

    @app.get("/api/caregivers")
    async def api_caregivers() -> dict[str, Any]:
        return {"caregivers": [caregiver_to_dict(caregiver) for caregiver in caregiver_facade.list_caregivers()]}

    app.state.repository = repo
    app.state.event_history = history
    return app


app = create_app()
