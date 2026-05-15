# Registro de Incidencias Ciudadanas

El sistema permite registrar incidencias en la vía pública, por ejemplo:

1. Fallas de alumbrado público
2. Problemas de basura acumulada
3. Casos de seguridad ciudadana o emergencia

Incluye:

- Backend con Fastify.
- API REST.
- Base de datos SQLite.
- Frontend web simple.
- Registro de evidencias mediante imágenes, videos o audios.
- Historial de estados de cada incidencia.
- Documentación de arquitectura, casos de uso, API, GitFlow y casos de prueba.

--- 
## 1. Requisitos

Instalar:

- Node.js 20 o superior.
- npm.
---
## 2. Instalación

Crear archivo de configuración:

```bash
cp .env.example .env
```

En Windows PowerShell:

```powershell
Copy-Item .env.example .env
```
---
## 3. Ejecutar el sistema

Modo normal:

```bash
npm start
```

Modo desarrollo:

```bash
npm run dev
```

Luego abrir en el navegador:

```text
http://localhost:3000
```
---

## 4. Ejecutar pruebas

```bash
npm test
```
---
## 5. Casos de uso implementados

### Caso de uso 1: Reportar falla de alumbrado público

Un ciudadano informa que una luminaria no funciona o presenta intermitencias.

Tipo usado en la API:

```text
ALUMBRADO_PUBLICO
```

### Caso de uso 2: Reportar basura acumulada

Un ciudadano informa acumulación de residuos en una calle, parque o avenida.

Tipo usado en la API:

```text
BASURA
```

### Caso de uso 3: Reportar emergencia o seguridad ciudadana

Un ciudadano reporta una situación de riesgo, emergencia o inseguridad.

Tipos usados en la API:

```text
SEGURIDAD_CIUDADANA
EMERGENCIA
```

---

## 6. API principal

Crear incidencia con JSON:

```bash
curl -X POST http://localhost:3000/api/incidentes \
  -H "Content-Type: application/json" \
  -d '{
    "tipo": "ALUMBRADO_PUBLICO",
    "titulo": "Poste de luz apagado",
    "descripcion": "El poste no funciona desde ayer.",
    "direccion": "Av. Principal 123",
    "distrito": "Lima",
    "prioridad": "MEDIA",
    "reportanteNombre": "Cesar",
    "reportanteContacto": "cesar@example.com",
    "evidencias": [
      {
        "tipo": "IMAGEN",
        "url": "https://ejemplo.com/foto.jpg"
      }
    ]
  }'
```

Listar incidencias:

```bash
curl http://localhost:3000/api/incidentes
```

Consultar una incidencia:

```bash
curl http://localhost:3000/api/incidentes/1
```

Actualizar estado:

```bash
curl -X PATCH http://localhost:3000/api/incidentes/1/estado \
  -H "Content-Type: application/json" \
  -d '{
    "estado": "EN_ATENCION",
    "comentario": "Cuadrilla asignada.",
    "actualizadoPor": "Operador municipal"
  }'
```

---

## 7. Flujo GitFlow 

```text
main       -> versión estable
develop    -> integración de avances
feature/*  -> nuevas funcionalidades
release/*  -> preparación de entrega
hotfix/*   -> correcciones urgentes
```