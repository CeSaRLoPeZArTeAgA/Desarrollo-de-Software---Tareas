const fs = require("fs");
const path = require("path");
const sqlite3 = require("sqlite3").verbose();

/**
 * Pequeña clase envoltorio para usar sqlite3 con Promises.
 * sqlite3 por defecto trabaja con callbacks; esta clase permite usar async/await.
 */
class Database {
  constructor(db) {
    this.db = db;
  }

  run(sql, params = []) {
    return new Promise((resolve, reject) => {
      this.db.run(sql, params, function onRun(error) {
        if (error) {
          reject(error);
          return;
        }

        resolve({
          lastID: this.lastID,
          changes: this.changes
        });
      });
    });
  }

  get(sql, params = []) {
    return new Promise((resolve, reject) => {
      this.db.get(sql, params, (error, row) => {
        if (error) {
          reject(error);
          return;
        }

        resolve(row);
      });
    });
  }

  all(sql, params = []) {
    return new Promise((resolve, reject) => {
      this.db.all(sql, params, (error, rows) => {
        if (error) {
          reject(error);
          return;
        }

        resolve(rows);
      });
    });
  }

  close() {
    return new Promise((resolve, reject) => {
      this.db.close((error) => {
        if (error) {
          reject(error);
          return;
        }

        resolve();
      });
    });
  }
}

/**
 * Abre la base de datos y crea las tablas si no existen.
 */
async function openDatabase(dbFile) {
  if (dbFile !== ":memory:") {
    const dir = path.dirname(dbFile);
    fs.mkdirSync(dir, { recursive: true });
  }

  const rawDb = await new Promise((resolve, reject) => {
    const db = new sqlite3.Database(dbFile, (error) => {
      if (error) {
        reject(error);
        return;
      }

      resolve(db);
    });
  });

  const database = new Database(rawDb);

  await database.run("PRAGMA foreign_keys = ON");

  await database.run(`
    CREATE TABLE IF NOT EXISTS incidents (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      tipo TEXT NOT NULL,
      titulo TEXT NOT NULL,
      descripcion TEXT NOT NULL,
      direccion TEXT NOT NULL,
      distrito TEXT NOT NULL,
      lat REAL,
      lng REAL,
      prioridad TEXT NOT NULL,
      estado TEXT NOT NULL,
      reportante_nombre TEXT,
      reportante_contacto TEXT,
      created_at TEXT NOT NULL,
      updated_at TEXT NOT NULL
    )
  `);

  await database.run(`
    CREATE TABLE IF NOT EXISTS incident_evidences (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      incident_id INTEGER NOT NULL,
      tipo TEXT NOT NULL,
      url TEXT NOT NULL,
      filename TEXT,
      created_at TEXT NOT NULL,
      FOREIGN KEY (incident_id) REFERENCES incidents(id) ON DELETE CASCADE
    )
  `);

  await database.run(`
    CREATE TABLE IF NOT EXISTS incident_status_history (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      incident_id INTEGER NOT NULL,
      estado_anterior TEXT,
      estado_nuevo TEXT NOT NULL,
      comentario TEXT,
      actualizado_por TEXT,
      created_at TEXT NOT NULL,
      FOREIGN KEY (incident_id) REFERENCES incidents(id) ON DELETE CASCADE
    )
  `);

  return database;
}

module.exports = { openDatabase };
