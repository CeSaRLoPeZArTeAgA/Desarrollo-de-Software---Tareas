const test = require("node:test");
const assert = require("node:assert/strict");
const { buildApp } = require("../src/app");

async function createTestApp() {
  const app = await buildApp({
    logger: false,
    dbFile: ":memory:",
    serveStatic: false
  });

  return app;
}

test("crea una incidencia de alumbrado público", async () => {
  const app = await createTestApp();

  const response = await app.inject({
    method: "POST",
    url: "/api/incidentes",
    payload: {
      tipo: "ALUMBRADO_PUBLICO",
      titulo: "Poste apagado",
      descripcion: "El poste no funciona desde ayer.",
      direccion: "Av. Central 100",
      distrito: "Lima",
      prioridad: "MEDIA",
      reportanteNombre: "Cesar",
      reportanteContacto: "cesar@example.com"
    }
  });

  assert.equal(response.statusCode, 201);

  const body = response.json();

  assert.equal(body.ok, true);
  assert.equal(body.data.tipo, "ALUMBRADO_PUBLICO");
  assert.equal(body.data.estado, "REGISTRADO");

  await app.close();
});

test("una emergencia se guarda automáticamente con prioridad URGENTE", async () => {
  const app = await createTestApp();

  const response = await app.inject({
    method: "POST",
    url: "/api/incidentes",
    payload: {
      tipo: "EMERGENCIA",
      titulo: "Emergencia en avenida",
      descripcion: "Se requiere atención inmediata.",
      direccion: "Av. Principal 500",
      distrito: "Lima",
      prioridad: "MEDIA"
    }
  });

  assert.equal(response.statusCode, 201);

  const body = response.json();

  assert.equal(body.ok, true);
  assert.equal(body.data.prioridad, "URGENTE");

  await app.close();
});

test("actualiza el estado de una incidencia", async () => {
  const app = await createTestApp();

  const createResponse = await app.inject({
    method: "POST",
    url: "/api/incidentes",
    payload: {
      tipo: "BASURA",
      titulo: "Basura acumulada",
      descripcion: "Hay bolsas de basura en la esquina.",
      direccion: "Jr. Los Pinos 200",
      distrito: "Lima"
    }
  });

  const created = createResponse.json().data;

  const updateResponse = await app.inject({
    method: "PATCH",
    url: `/api/incidentes/${created.id}/estado`,
    payload: {
      estado: "EN_ATENCION",
      comentario: "Se asignó personal de limpieza.",
      actualizadoPor: "Operador municipal"
    }
  });

  assert.equal(updateResponse.statusCode, 200);

  const body = updateResponse.json();

  assert.equal(body.ok, true);
  assert.equal(body.data.estado, "EN_ATENCION");
  assert.equal(body.data.historial.length, 2);

  await app.close();
});

test("rechaza un tipo de incidencia inválido", async () => {
  const app = await createTestApp();

  const response = await app.inject({
    method: "POST",
    url: "/api/incidentes",
    payload: {
      tipo: "OTRO",
      titulo: "Caso inválido",
      descripcion: "Prueba de validación.",
      direccion: "Av. Prueba",
      distrito: "Lima"
    }
  });

  assert.equal(response.statusCode, 400);

  const body = response.json();

  assert.equal(body.ok, false);

  await app.close();
});
