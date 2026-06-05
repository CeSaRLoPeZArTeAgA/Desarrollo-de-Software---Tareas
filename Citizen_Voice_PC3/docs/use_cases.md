# Casos de uso

## CU01 - Crear propuesta normativa

Actor: colectivo civil.

Flujo principal: el colectivo ingresa titulo, descripcion y nombre del colectivo; el sistema registra la propuesta en estado ACTIVE.

## CU02 - Generar padron criptografico

Actor: administrador del sistema.

Flujo principal: el administrador ejecuta el seeder; el sistema genera 100 000 DNI ficticios y crea simultaneamente las bases privada y publica.

## CU03 - Firmar propuesta

Actor: ciudadano.

Flujo principal: el ciudadano firma una propuesta con su DNI; el sistema busca su llave privada, construye el payload canonico, firma con RSA, busca la llave publica y verifica la firma.

## CU04 - Ejecutar demostracion PC3

Actor: administrador o docente evaluador.

Flujo principal: el sistema selecciona 25 000 ciudadanos aleatorios, registra sus firmas validas y congela automaticamente la propuesta cuando se alcanza el umbral.

## CU05 - Congelar y enviar expediente

Actor: sistema.

Flujo principal: el sistema construye el expediente, calcula SHA-256, guarda el archivo congelado y registra el envio a la Oficina del Congreso.
