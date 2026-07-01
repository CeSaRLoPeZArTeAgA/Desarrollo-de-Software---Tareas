from __future__ import annotations

import json


def test_adoption_search_returns_only_ong_catalog(client):
    response = client.post(
        "/api/search",
        data={
            "intent": "adoption",
            "metadata_json": json.dumps({"species": "perro", "breed": "labrador"}),
        },
    )

    assert response.status_code == 200
    results = response.json()["results"]
    assert results
    assert all(result["source"] == "ONG/Protectora" for result in results)


def test_sale_search_returns_only_certified_breeders(client):
    response = client.post(
        "/api/search",
        data={
            "intent": "sale",
            "metadata_json": json.dumps({"species": "perro", "breed": "labrador"}),
        },
    )

    assert response.status_code == 200
    results = response.json()["results"]
    assert results
    assert all(result["source"] == "Criadero certificado" for result in results)
    assert all(result["certified"] for result in results)


def test_verify_loss_search_reads_active_alerts(client):
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

    response = client.post(
        "/api/search",
        data={
            "intent": "verify_loss",
            "metadata_json": json.dumps({"species": "perro", "breed": "labrador"}),
        },
    )

    assert response.status_code == 200
    results = response.json()["results"]
    assert len(results) == 1
    assert results[0]["source"] == "Alerta activa"
    assert "Max" in results[0]["title"]
