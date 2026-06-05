# Casos de prueba

| ID | Caso | Resultado esperado |
|---|---|---|
| CP01 | Generar 20 ciudadanos en modo prueba | Las bases privada y publica contienen 20 registros alineados. |
| CP02 | Firmar un payload RSA | La firma verifica con la llave publica. |
| CP03 | Alterar el payload firmado | La verificacion falla. |
| CP04 | Firmar dos veces con el mismo DNI | La segunda firma se rechaza. |
| CP05 | Ejecutar demo con umbral 5 | El sistema congela y envia el expediente. |
| CP06 | Revisar patrones | El catalogo contiene 5 creacionales y 7 estructurales. |
| CP07 | Verificar expediente congelado | Se genera archivo en data/archives. |
| CP08 | Verificar envio Congreso | Se genera archivo en data/submissions. |
| CP09 | Verificar Prototype | Clonar una plantilla no modifica la original. |
| CP10 | Verificar Decorator | El hash SHA-256 tiene 64 caracteres hexadecimales. |

Ejecucion:

```bash
./scripts/run-tests.sh
```

o en Windows:

```bat
scripts\run-tests.bat
```
