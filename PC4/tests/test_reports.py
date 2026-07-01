from __future__ import annotations


def test_register_lost_pet_dispatches_alert_and_hides_owner(client):
    response = client.post(
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
        files={"photo": ("max.png", b"fake-png-content", "image/png")},
    )

    assert response.status_code == 200
    body = response.json()
    assert body["report"]["type"] == "lost_pet"
    assert body["report"]["status"] == "active"
    assert body["alert"]["recipients_count"] >= 1
    assert body["alert"]["latency_ok"] is True
    assert body["owner_public_view"]["display_name"] == "Dueño protegido por la plataforma"
    assert "cesar@example.com" not in str(body["owner_public_view"])


def test_register_sighting_anonymous(client):
    response = client.post(
        "/api/reports/sighting",
        data={
            "species": "gato",
            "breed": "mestizo",
            "description": "Gato visto en parque",
            "latitude": "-12.050",
            "longitude": "-77.030",
            "reporter_alias": "Anonimo",
        },
    )

    assert response.status_code == 200
    body = response.json()
    assert body["report"]["type"] == "sighting"
    assert body["report"]["reporter_alias"] == "Anonimo"


def test_invalid_coordinates_are_rejected(client):
    response = client.post(
        "/api/reports/lost",
        data={
            "pet_name": "Max",
            "species": "perro",
            "breed": "labrador",
            "description": "Collar rojo",
            "latitude": "-120.000",
            "longitude": "-77.049",
            "owner_name": "Cesar Lopez",
            "owner_email": "cesar@example.com",
            "owner_phone": "999888777",
        },
    )

    assert response.status_code == 400
    assert "latitud" in response.json()["detail"].lower()
