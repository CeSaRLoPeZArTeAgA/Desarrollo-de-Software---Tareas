# Patrones de diseño usados

## 1. Layered Architecture

El sistema se separa en capas:

```text
Rutas → Servicios → Repositorios → Base de datos
```

Ventaja:

- Evita mezclar lógica HTTP con lógica SQL.
- Hace el código más mantenible.
- Permite probar servicios sin depender directamente de la interfaz web.

---

## 2. Repository Pattern

Archivo:
```text
src/models/incidentRepository.js
```

El repositorio encapsula las operaciones de base de datos:
- Crear incidencia.
- Listar incidencias.
- Buscar por ID.
- Actualizar estado.
- Registrar evidencias.

Esto evita que las rutas tengan consultas SQL directamente.

---
## 3. Service Layer Pattern

Archivo:

```text
src/services/incidentService.js
```

El servicio contiene reglas de negocio:

- Validar tipos de incidencia.
- Validar estados.
- Asignar prioridad urgente a emergencias.
- Verificar que los campos obligatorios existan.

---
## 4. DTO / Schema Validation

Archivo:

```text
src/schemas/incidentSchemas.js
```
Los esquemas definen la forma esperada de los datos de entrada y salida.

---
## 5. Dependency Injection simple

Archivo:

```text
src/app.js
```

El repositorio se inyecta en el servicio:

```js
const repository = new IncidentRepository(db);
const service = new IncidentService(repository);
```

Así, el servicio depende de una abstracción práctica del repositorio y no crea directamente la base de datos.
