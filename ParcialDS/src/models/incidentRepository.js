
class IncidentRepository {
  constructor(database) {
    this.database = database;
  }

  async createIncident(incident, evidences = []) {
    const now = new Date().toISOString();

    await this.database.run("BEGIN TRANSACTION");

    try {
      const result = await this.database.run(
        `
        INSERT INTO incidents (
          tipo,
          titulo,
          descripcion,
          direccion,
          distrito,
          lat,
          lng,
          prioridad,
          estado,
          reportante_nombre,
          reportante_contacto,
          created_at,
          updated_at
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        `,
        [
          incident.tipo,
          incident.titulo,
          incident.descripcion,
          incident.direccion,
          incident.distrito,
          incident.lat ?? null,
          incident.lng ?? null,
          incident.prioridad,
          incident.estado,
          incident.reportanteNombre ?? null,
          incident.reportanteContacto ?? null,
          now,
          now
        ]
      );

      const incidentId = result.lastID;

      for (const evidence of evidences) {
        await this.database.run(
          `
          INSERT INTO incident_evidences (
            incident_id,
            tipo,
            url,
            filename,
            created_at
          )
          VALUES (?, ?, ?, ?, ?)
          `,
          [
            incidentId,
            evidence.tipo,
            evidence.url,
            evidence.filename ?? null,
            now
          ]
        );
      }

      await this.database.run(
        `
        INSERT INTO incident_status_history (
          incident_id,
          estado_anterior,
          estado_nuevo,
          comentario,
          actualizado_por,
          created_at
        )
        VALUES (?, ?, ?, ?, ?, ?)
        `,
        [
          incidentId,
          null,
          incident.estado,
          "Incidencia registrada por el ciudadano.",
          incident.reportanteNombre ?? "Ciudadano",
          now
        ]
      );

      await this.database.run("COMMIT");

      return this.findById(incidentId);
    } catch (error) {
      await this.database.run("ROLLBACK");
      throw error;
    }
  }

  async listIncidents(filters = {}) {
    const conditions = [];
    const params = [];

    if (filters.tipo) {
      conditions.push("tipo = ?");
      params.push(filters.tipo);
    }

    if (filters.estado) {
      conditions.push("estado = ?");
      params.push(filters.estado);
    }

    const where = conditions.length > 0 ? `WHERE ${conditions.join(" AND ")}` : "";

    return this.database.all(
      `
      SELECT
        id,
        tipo,
        titulo,
        descripcion,
        direccion,
        distrito,
        lat,
        lng,
        prioridad,
        estado,
        reportante_nombre AS reportanteNombre,
        reportante_contacto AS reportanteContacto,
        created_at AS createdAt,
        updated_at AS updatedAt
      FROM incidents
      ${where}
      ORDER BY datetime(created_at) DESC
      `,
      params
    );
  }

  async findById(id) {
    const incident = await this.database.get(
      `
      SELECT
        id,
        tipo,
        titulo,
        descripcion,
        direccion,
        distrito,
        lat,
        lng,
        prioridad,
        estado,
        reportante_nombre AS reportanteNombre,
        reportante_contacto AS reportanteContacto,
        created_at AS createdAt,
        updated_at AS updatedAt
      FROM incidents
      WHERE id = ?
      `,
      [id]
    );

    if (!incident) {
      return null;
    }

    const evidencias = await this.database.all(
      `
      SELECT
        id,
        tipo,
        url,
        filename,
        created_at AS createdAt
      FROM incident_evidences
      WHERE incident_id = ?
      ORDER BY id ASC
      `,
      [id]
    );

    const historial = await this.database.all(
      `
      SELECT
        id,
        estado_anterior AS estadoAnterior,
        estado_nuevo AS estadoNuevo,
        comentario,
        actualizado_por AS actualizadoPor,
        created_at AS createdAt
      FROM incident_status_history
      WHERE incident_id = ?
      ORDER BY id ASC
      `,
      [id]
    );

    return {
      ...incident,
      evidencias,
      historial
    };
  }

  async updateStatus(id, newStatus, comment, updatedBy) {
    const incident = await this.findById(id);

    if (!incident) {
      return null;
    }

    const now = new Date().toISOString();

    await this.database.run("BEGIN TRANSACTION");

    try {
      await this.database.run(
        `
        UPDATE incidents
        SET estado = ?, updated_at = ?
        WHERE id = ?
        `,
        [newStatus, now, id]
      );

      await this.database.run(
        `
        INSERT INTO incident_status_history (
          incident_id,
          estado_anterior,
          estado_nuevo,
          comentario,
          actualizado_por,
          created_at
        )
        VALUES (?, ?, ?, ?, ?, ?)
        `,
        [
          id,
          incident.estado,
          newStatus,
          comment ?? "Cambio de estado.",
          updatedBy ?? "Operador",
          now
        ]
      );

      await this.database.run("COMMIT");

      return this.findById(id);
    } catch (error) {
      await this.database.run("ROLLBACK");
      throw error;
    }
  }
}

module.exports = { IncidentRepository };
