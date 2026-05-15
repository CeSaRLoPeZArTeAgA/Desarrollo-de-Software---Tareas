const path = require("path");
const Fastify = require("fastify");
const cors = require("@fastify/cors");
const multipart = require("@fastify/multipart");
const fastifyStatic = require("@fastify/static");

const { getEnv } = require("./config/env");
const { openDatabase } = require("./config/database");
const { IncidentRepository } = require("./models/incidentRepository");
const { IncidentService } = require("./services/incidentService");
const { incidentRoutes } = require("./routes/incidentRoutes");
const { isAppError } = require("./utils/errors");

async function buildApp(options = {}) {
  const env = getEnv();

  const app = Fastify({
    logger: options.logger ?? true
  });

  const dbFile = options.dbFile || env.dbFile;
  const uploadDir = options.uploadDir || env.uploadDir;

  const database = await openDatabase(dbFile);
  const repository = new IncidentRepository(database);
  const service = new IncidentService(repository);

  app.decorate("incidentService", service);
  app.decorate("uploadDir", uploadDir);

  await app.register(cors, {
    origin: true
  });

  await app.register(multipart, {
    limits: {
      fileSize: 25 * 1024 * 1024,
      files: 5
    }
  });

  await app.register(incidentRoutes, {
    prefix: "/api"
  });

  if (options.serveStatic !== false) {
    const publicDir = path.join(__dirname, "public");

    await app.register(fastifyStatic, {
      root: publicDir,
      prefix: "/"
    });
  }

  app.setErrorHandler((error, request, reply) => {
    if (isAppError(error)) {
      reply.code(error.statusCode).send({
        ok: false,
        error: error.message
      });
      return;
    }

    if (error.validation) {
      reply.code(400).send({
        ok: false,
        error: "Datos inválidos",
        details: error.validation
      });
      return;
    }

    request.log.error(error);

    reply.code(500).send({
      ok: false,
      error: "Error interno del servidor"
    });
  });

  app.addHook("onClose", async () => {
    await database.close();
  });

  return app;
}

module.exports = { buildApp };
