# Arquitectura del sistema

## 1. Estilo arquitectónico

El proyecto usa una arquitectura por capas:

```text
Frontend Web
     ↓
API REST Fastify
     ↓
Servicio de negocio
     ↓
Repositorio
     ↓
Base de datos SQLite
```

---

## 2. Capas

## 2.1. Frontend

Ubicación:

```text
src/public/
```

Responsabilidades:

- Mostrar formulario de registro.
- Enviar datos al backend.
- Mostrar lista de incidencias.
- Permitir consulta rápida de los casos registrados.

Archivos principales:

```text
index.html
styles.css
app.js
```

---

## 2.2. Rutas HTTP

Ubicación:

```text
src/routes/incidentRoutes.js
```

Responsabilidades:

- Definir endpoints.
- Recibir peticiones HTTP.
- Delegar la lógica al servicio.

Ejemplo:

```text
POST /api/incidentes
GET /api/incidentes
GET /api/incidentes/:id
PATCH /api/incidentes/:id/estado
```

---

## 2.3. Servicio de negocio

Ubicación:

```text
src/services/incidentService.js
```

Responsabilidades:

- Validar reglas de negocio.
- Definir estados permitidos.
- Asignar prioridad urgente para emergencias.
- Coordinar operaciones del repositorio.

---

## 2.4. Repositorio

Ubicación:

```text
src/models/incidentRepository.js
```

Responsabilidades:

- Insertar incidencias en SQLite.
- Consultar incidencias.
- Registrar evidencias.
- Registrar historial de cambios.

---

## 2.5. Base de datos

Ubicación:

```text
src/config/database.js
```

Tablas:

```text
incidents
incident_evidences
incident_status_history
```

---

# 3. Flujo principal

```text
Ciudadano
   ↓
Formulario HTML
   ↓
POST /api/incidentes/multipart
   ↓
incidentRoutes
   ↓
incidentService
   ↓
incidentRepository
   ↓
SQLite
```

---

# 4. Diagrama simple

```text
+-------------------+
|   Ciudadano       |
+---------+---------+
          |
          v
+-------------------+
| Frontend Web      |
| HTML/CSS/JS       |
+---------+---------+
          |
          v
+-------------------+
| API Fastify       |
| Routes            |
+---------+---------+
          |
          v
+-------------------+
| Service Layer     |
| Reglas negocio    |
+---------+---------+
          |
          v
+-------------------+
| Repository Layer  |
| Consultas SQL     |
+---------+---------+
          |
          v
+-------------------+
| SQLite Database   |
+-------------------+
```
