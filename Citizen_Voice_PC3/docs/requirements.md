# Requisitos funcionales PC3

RF01. El sistema debe permitir crear propuestas normativas ciudadanas.

RF02. El sistema debe registrar el colectivo civil responsable de la propuesta.

RF03. El sistema debe permitir agregar recursos, comentarios o sustento a una propuesta.

RF04. El sistema debe generar una base general de 100 000 personas ficticias con DNI, nombres completos y par de llaves RSA.

RF05. El sistema debe crear simultaneamente una base privada con llaves privadas y una base publica con llaves publicas.

RF06. El sistema debe permitir firmar digitalmente una propuesta usando la llave privada asociada al DNI.

RF07. El sistema debe verificar cada firma usando la llave publica correspondiente al DNI.

RF08. El sistema debe rechazar firmas duplicadas para una misma propuesta.

RF09. El sistema debe tomar 25 000 ciudadanos aleatorios de la base general para ejecutar el flujo PC3.

RF10. Al alcanzar 25 000 firmas validas, el sistema debe congelar criptograficamente el expediente.

RF11. El congelamiento debe producir un hash SHA-256 del expediente.

RF12. El sistema debe registrar el envio del expediente congelado a la Oficina del Congreso.

RF13. El sistema debe exponer una API HTTP y una CLI para operacion local.

RF14. El sistema debe documentar patrones creacionales y estructurales.
