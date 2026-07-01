from __future__ import annotations


def test_home_page_renders_with_uni_logo(client):
    response = client.get("/")

    assert response.status_code == 200
    assert "UNI Mascotas" in response.text
    assert "/static/img/logo_uni.png" in response.text


def test_favicon_route_returns_logo(client):
    response = client.get("/favicon.ico")

    assert response.status_code == 200
    assert response.headers["content-type"].startswith("image/png")


def test_ui_register_lost_pet_does_not_return_500(client):
    response = client.post(
        "/reports/lost",
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

    assert response.status_code == 200
    assert "Reporte #" in response.text


def test_ui_search_does_not_return_500(client):
    response = client.post(
        "/search",
        data={"intent": "adoption", "species": "perro", "breed": "labrador"},
    )

    assert response.status_code == 200
    assert "Resultados del buscador multipropósito" in response.text


def test_duplicate_caregiver_dni_returns_controlled_api_error(client):
    payload = {
        "full_name": "Ana Torres",
        "dni": "12345678",
        "role": "professional",
        "species_accepted": "perros, gatos",
        "size_accepted": "pequeño, mediano",
        "medication": "true",
        "accepts_lost_pet_alerts": "true",
    }
    first = client.post("/api/caregivers", data=payload)
    second = client.post("/api/caregivers", data=payload)

    assert first.status_code == 200
    assert second.status_code == 400
    assert "dni" in second.json()["detail"].lower()
