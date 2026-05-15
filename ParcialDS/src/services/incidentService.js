const {
  TIPOS_INCIDENTE,
  PRIORIDADES,
  ESTADOS,
  TIPOS_EVIDENCIA
} = require("../schemas/incidentSchemas");

const { AppError } = require("../utils/errors");

//Service Layer. Aqui van las reglas de negocio

// El controller se comunica con el repository a través de esta clase
class IncidentService {
  constructor(repository) {
    this.repository = repository;
  }

  async createIncident(payload) {
    this.validateRequiredText(payload.tipo, "tipo");
    this.validateRequiredText(payload.titulo, "titulo");
    this.validateRequiredText(payload.descripcion, "descripcion");
    this.validateRequiredText(payload.direccion, "direccion");
    this.validateRequiredText(payload.distrito, "distrito");

    if (!TIPOS_INCIDENTE.includes(payload.tipo)) {
      throw new AppError(`Tipo de incidencia inválido: ${payload.tipo}`, 400);
    }

    let prioridad = payload.prioridad || "MEDIA";

    if (!PRIORIDADES.includes(prioridad)) {
      throw new AppError(`Prioridad inválida: ${prioridad}`, 400);
    }
    
    //Si el ciudadano marca la incidencia como emergencia, el sistema fuerza prioridad URGENTE.
    if (payload.tipo === "EMERGENCIA") {
      prioridad = "URGENTE";
    }

    const evidencias = Array.isArray(payload.evidencias) ? payload.evidencias : [];

    for (const evidence of evidencias) {
      if (!TIPOS_EVIDENCIA.includes(evidence.tipo)) {
        throw new AppError(`Tipo de evidencia inválido: ${evidence.tipo}`, 400);
      }

      this.validateRequiredText(evidence.url, "url de evidencia");
    }

    const incident = {
      tipo: payload.tipo,
      titulo: payload.titulo.trim(),
      descripcion: payload.descripcion.trim(),
      direccion: payload.direccion.trim(),
      distrito: payload.distrito.trim(),
      lat: this.toNumberOrNull(payload.lat),
      lng: this.toNumberOrNull(payload.lng),
      prioridad,
      estado: "REGISTRADO",
      reportanteNombre: payload.reportanteNombre?.trim() || null,
      reportanteContacto: payload.reportanteContacto?.trim() || null
    };

    return this.repository.createIncident(incident, evidencias);
  }

  async listIncidents(filters = {}) {
    if (filters.tipo && !TIPOS_INCIDENTE.includes(filters.tipo)) {
      throw new AppError(`Tipo de incidencia inválido: ${filters.tipo}`, 400);
    }

    if (filters.estado && !ESTADOS.includes(filters.estado)) {
      throw new AppError(`Estado inválido: ${filters.estado}`, 400);
    }

    return this.repository.listIncidents(filters);
  }

  async getIncidentById(id) {
    const numericId = this.validateId(id);
    const incident = await this.repository.findById(numericId);

    if (!incident) {
      throw new AppError("Incidencia no encontrada", 404);
    }

    return incident;
  }

  async updateStatus(id, payload) {
    const numericId = this.validateId(id);

    if (!ESTADOS.includes(payload.estado)) {
      throw new AppError(`Estado inválido: ${payload.estado}`, 400);
    }

    const updated = await this.repository.updateStatus(
      numericId,
      payload.estado,
      payload.comentario,
      payload.actualizadoPor
    );

    if (!updated) {
      throw new AppError("Incidencia no encontrada", 404);
    }

    return updated;
  }

  validateRequiredText(value, fieldName) {
    if (typeof value !== "string" || value.trim().length === 0) {
      throw new AppError(`El campo ${fieldName} es obligatorio`, 400);
    }
  }

  validateId(id) {
    const numericId = Number(id);

    if (!Number.isInteger(numericId) || numericId <= 0) {
      throw new AppError("ID inválido", 400);
    }

    return numericId;
  }

  toNumberOrNull(value) {
    if (value === undefined || value === null || value === "") {
      return null;
    }

    const number = Number(value);

    if (Number.isNaN(number)) {
      return null;
    }

    return number;
  }
}

module.exports = { IncidentService };
