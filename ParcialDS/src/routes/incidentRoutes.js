const fs = require("fs");
const path = require("path");
const { pipeline } = require("stream/promises");
const { createIncidentSchema, updateStatusSchema } = require("../schemas/incidentSchemas");

function detectEvidenceType(mimeType) {
  if (mimeType.startsWith("image/")) {
    return "IMAGEN";
  }

  if (mimeType.startsWith("video/")) {
    return "VIDEO";
  }

  if (mimeType.startsWith("audio/")) {
    return "AUDIO";
  }

  return "LINK";
}

function safeFilename(filename) {
  return filename
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^a-zA-Z0-9._-]/g, "_");
}


//define todas las rutas de incidencias
async function incidentRoutes(fastify) {
  fastify.get("/health", async () => {
    return {
      ok: true,
      service: "incidentes-ciudadanos-fastify"
    };
  });

  fastify.get("/incidentes", async (request) => {
    const data = await fastify.incidentService.listIncidents({
      tipo: request.query.tipo,
      estado: request.query.estado
    });

    return {
      ok: true,
      data
    };
  });

  fastify.get("/incidentes/:id", async (request) => {
    const data = await fastify.incidentService.getIncidentById(request.params.id);

    return {
      ok: true,
      data
    };
  });

  fastify.post("/incidentes", { schema: createIncidentSchema }, async (request, reply) => {
    const data = await fastify.incidentService.createIncident(request.body);

    reply.code(201);

    return {
      ok: true,
      data
    };
  });
  
   // Permite registrar una incidencia con archivos reales
   fastify.post("/incidentes/multipart", async (request, reply) => {
    const fields = {};
    const evidencias = [];

    fs.mkdirSync(fastify.uploadDir, { recursive: true });

    for await (const part of request.parts()) {
      if (part.type === "file") {
        if (!part.filename) {
          continue;
        }

        const cleanName = safeFilename(part.filename);
        const storedName = `${Date.now()}-${Math.round(Math.random() * 1_000_000)}-${cleanName}`;
        const absolutePath = path.join(fastify.uploadDir, storedName);

        await pipeline(part.file, fs.createWriteStream(absolutePath));

        evidencias.push({
          tipo: detectEvidenceType(part.mimetype || "application/octet-stream"),
          url: `/uploads/${storedName}`,
          filename: part.filename
        });
      } else {
        fields[part.fieldname] = part.value;
      }
    }

    const data = await fastify.incidentService.createIncident({
      ...fields,
      lat: fields.lat ? Number(fields.lat) : undefined,
      lng: fields.lng ? Number(fields.lng) : undefined,
      evidencias
    });

    reply.code(201);

    return {
      ok: true,
      data
    };
  });

  fastify.patch("/incidentes/:id/estado", { schema: updateStatusSchema }, async (request) => {
    const data = await fastify.incidentService.updateStatus(
      request.params.id,
      request.body
    );

    return {
      ok: true,
      data
    };
  });
}

module.exports = { incidentRoutes };
