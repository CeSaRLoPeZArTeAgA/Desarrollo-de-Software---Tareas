from __future__ import annotations


def test_verified_caregiver_registration(client):
    response = client.post(
        "/api/caregivers",
        data={
            "full_name": "Ana Torres",
            "dni": "12345678",
            "role": "professional",
            "species_accepted": "perros, gatos",
            "size_accepted": "pequeño, mediano",
            "medication": "true",
            "accepts_lost_pet_alerts": "true",
        },
    )

    assert response.status_code == 200
    caregiver = response.json()["caregiver"]
    assert caregiver["status"] == "verified"
    assert caregiver["accepts_lost_pet_alerts"] is True


def test_pending_caregiver_if_identity_provider_does_not_validate(client):
    response = client.post(
        "/api/caregivers",
        data={
            "full_name": "Luis Perez",
            "dni": "12345677",
            "role": "solidary",
            "species_accepted": "perros",
            "size_accepted": "grande",
            "medication": "false",
            "accepts_lost_pet_alerts": "true",
        },
    )

    assert response.status_code == 200
    caregiver = response.json()["caregiver"]
    assert caregiver["status"] == "pending_validation"


def test_toggle_caregiver_alerts(client):
    created = client.post(
        "/api/caregivers",
        data={
            "full_name": "Ana Torres",
            "dni": "12345678",
            "role": "professional",
            "species_accepted": "perros, gatos",
            "size_accepted": "pequeño, mediano",
            "medication": "true",
            "accepts_lost_pet_alerts": "true",
        },
    ).json()["caregiver"]

    response = client.post(f"/api/caregivers/{created['id']}/alerts", data={"enabled": "false"})

    assert response.status_code == 200
    assert response.json()["caregiver"]["accepts_lost_pet_alerts"] is False
