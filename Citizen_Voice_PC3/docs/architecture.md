# Arquitectura Citizen Voice PC3 Java

## Capas

```text
API HTTP / CLI
    ↓
CitizenVoiceFacade
    ↓
Servicios criptograficos + repositorios + patrones estructurales
    ↓
Bases .db en data/
```

## Flujo principal PC3

```text
1. Administrador genera padron de 100 000 ciudadanos.
2. El sistema crea private_registry.db y public_registry.db simultaneamente.
3. El colectivo civil crea una propuesta normativa.
4. El flujo demo toma 25 000 ciudadanos aleatorios.
5. Cada ciudadano firma el payload canonico de la propuesta.
6. El sistema verifica la firma contra la llave publica.
7. Se registran solo firmas validas y no duplicadas.
8. Al llegar a 25 000 firmas, el sistema construye el expediente.
9. El expediente se congela con SHA-256.
10. El archivo congelado se envia a la Oficina del Congreso simulada.
```

## Archivos generados

```text
data/private_registry.db
Base privada: DNI, nombre completo, llave privada RSA.

data/public_registry.db
Base publica: DNI, nombre completo, llave publica RSA.

data/proposals.db
Propuestas normativas.

data/signatures.db
Firmas digitales validas.

data/resources.db
Sustentos, comentarios o recursos.

data/archives/proposal-{id}-frozen.txt
Expediente congelado.

data/submissions/congreso-propuesta-{id}.txt
Envio simulado a la Oficina del Congreso.
```
