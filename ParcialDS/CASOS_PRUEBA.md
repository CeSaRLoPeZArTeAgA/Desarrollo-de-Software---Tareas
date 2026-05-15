# Casos de prueba

## Caso de prueba 1: Registrar alumbrado público

Entrada:

```json
{
  "tipo": "ALUMBRADO_PUBLICO",
  "titulo": "Poste apagado",
  "descripcion": "El poste de la esquina no prende.",
  "direccion": "Av. Central 100",
  "distrito": "Lima",
  "prioridad": "MEDIA",
  "reportanteNombre": "Cesar",
  "reportanteContacto": "cesar@example.com"
}
```

Resultado esperado:

- Código HTTP `201`.
- Estado inicial `REGISTRADO`.
- Se crea historial inicial.

---

## Caso de prueba 2: Registrar emergencia

Entrada:

```json
{
  "tipo": "EMERGENCIA",
  "titulo": "Accidente en avenida",
  "descripcion": "Se requiere atención inmediata.",
  "direccion": "Av. Principal 500",
  "distrito": "Lima",
  "prioridad": "MEDIA",
  "reportanteNombre": "Vecino",
  "reportanteContacto": "999999999"
}
```

Resultado esperado:

- Código HTTP `201`.
- Prioridad final `URGENTE`.
- Estado inicial `REGISTRADO`.

---

## Caso de prueba 3: Actualizar estado

Entrada:

```json
{
  "estado": "EN_ATENCION",
  "comentario": "Se asignó personal.",
  "actualizadoPor": "Operador"
}
```

Resultado esperado:

- Código HTTP `200`.
- El estado cambia a `EN_ATENCION`.
- Se registra historial de cambio.

---

## Caso de prueba 4: Tipo inválido

Entrada:

```json
{
  "tipo": "OTRO",
  "titulo": "Prueba",
  "descripcion": "Prueba",
  "direccion": "Prueba",
  "distrito": "Prueba"
}
```

Resultado esperado:

- Código HTTP `400`.
- Mensaje de error indicando tipo inválido.
