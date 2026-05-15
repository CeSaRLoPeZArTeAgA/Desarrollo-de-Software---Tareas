require("dotenv").config();

const path = require("path");

function getEnv() {
  return {
    port: Number(process.env.PORT || 3000),
    host: process.env.HOST || "0.0.0.0",
    dbFile: process.env.DB_FILE || path.join(process.cwd(), "data", "incidentes.sqlite"),
    uploadDir: process.env.UPLOAD_DIR || path.join(process.cwd(), "src", "public", "uploads")
  };
}

module.exports = { getEnv };
