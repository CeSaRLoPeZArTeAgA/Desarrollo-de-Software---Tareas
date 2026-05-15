const { buildApp } = require("./app");
const { getEnv } = require("./config/env");

async function main() {
  const env = getEnv();
  const app = await buildApp();

  await app.listen({
    port: env.port,
    host: env.host
  });

  app.log.info(`Servidor iniciado en http://${env.host}:${env.port}`);
}

main().catch((error) => {
  console.error("No se pudo iniciar el servidor:", error);
  process.exit(1);
});
