from __future__ import annotations

from pet_alerts.repository import SQLiteRepository


def test_public_pages_do_not_require_login(client):
    for path in [
        "/",
        "/adopcion",
        "/perdidos-y-encontrados",
        "/buscar-por-imagen",
        "/cuidadores",
        "/publicar-anuncio",
    ]:
        response = client.get(path)
        assert response.status_code == 200
        assert "Iniciar sesión" not in response.text
        assert "Login" not in response.text


def test_adoption_page_uses_seed_catalog(client):
    response = client.get("/adopcion")
    assert response.status_code == 200
    assert "ONG Patitas UNI" in response.text
    assert "Adopción responsable" in response.text


def test_lost_found_filter_works_with_seedless_test_data(client):
    client.post(
        "/api/reports/lost",
        data={
            "pet_name": "Max",
            "species": "perro",
            "breed": "labrador",
            "description": "Collar rojo",
            "latitude": "-12.024",
            "longitude": "-77.049",
            "owner_name": "Cesar Lopez",
            "owner_email": "cesar@example.com",
            "owner_phone": "999888777",
            "radius_meters": "1000",
        },
    )
    response = client.get("/perdidos-y-encontrados?tipo=lost_pet&species=perro")
    assert response.status_code == 200
    assert "Max" in response.text
    assert "Perdido" in response.text


def test_demo_database_is_populated_when_seed_enabled(tmp_path):
    repo = SQLiteRepository(tmp_path / "demo.db", seed_demo=True)
    assert len(repo.list_reports_filtered()) >= 4
    assert len(repo.list_caregivers()) >= 3
    assert len(repo.list_adoption_catalog()) >= 3
