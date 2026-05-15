const TIPOS_INCIDENTE = [
  "ALUMBRADO_PUBLICO",
  "BASURA",
  "SEGURIDAD_CIUDADANA",
  "EMERGENCIA"
];

const PRIORIDADES = [
  "BAJA",
  "MEDIA",
  "ALTA",
  "URGENTE"
];

const ESTADOS = [
  "REGISTRADO",
  "VALIDADO",
  "EN_ATENCION",
  "RESUELTO",
  "RECHAZADO"
];

const TIPOS_EVIDENCIA = [
  "IMAGEN",
  "VIDEO",
  "AUDIO",
  "LINK"
];

const createIncidentSchema = {
  body: {
    type: "object",
    required: ["tipo", "titulo", "descripcion", "direccion", "distrito"],
    additionalProperties: true,
    properties: {
      tipo: { type: "string", enum: TIPOS_INCIDENTE },
      titulo: { type: "string", minLength: 3 },
      descripcion: { type: "string", minLength: 5 },
      direccion: { type: "string", minLength: 3 },
      distrito: { type: "string", minLength: 2 },
      lat: { type: "number" },
      lng: { type: "number" },
      prioridad: { type: "string", enum: PRIORIDADES },
      reportanteNombre: { type: "string" },
      reportanteContacto: { type: "string" },
      evidencias: {
        type: "array",
        items: {
          type: "object",
          required: ["tipo", "url"],
          properties: {
            tipo: { type: "string", enum: TIPOS_EVIDENCIA },
            url: { type: "string" },
            filename: { type: "string" }
          }
        }
      }
    }
  }
};

const updateStatusSchema = {
  body: {
    type: "object",
    required: ["estado"],
    additionalProperties: false,
    properties: {
      estado: { type: "string", enum: ESTADOS },
      comentario: { type: "string" },
      actualizadoPor: { type: "string" }
    }
  }
};

module.exports = {
  TIPOS_INCIDENTE,
  PRIORIDADES,
  ESTADOS,
  TIPOS_EVIDENCIA,
  createIncidentSchema,
  updateStatusSchema
};
