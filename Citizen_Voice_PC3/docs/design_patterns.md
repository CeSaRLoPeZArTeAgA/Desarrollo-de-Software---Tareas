# Patrones de diseño implementados

## Patrones creacionales

| Patron | Clase | Funcion en el sistema |
|---|---|---|
| Singleton | `ApplicationConfig`, `ApplicationContext`, `ProposalTemplateRegistry` | Mantienen una unica configuracion, un unico contexto de dependencias y un registro central de plantillas. |
| Factory Method | `RsaCryptoFactory`, `RsaKeyPairFactory` | Encapsulan la creacion del motor criptografico RSA y de pares de llaves. |
| Abstract Factory | `RepositoryAbstractFactory`, `FileRepositoryFactory` | Crea la familia completa de repositorios de la aplicacion. |
| Builder | `LegislativeArchiveBuilder` | Construye el expediente legislativo por etapas: propuesta, recursos y manifiesto de firmas. |
| Prototype | `ProposalDraftPrototype`, `ProposalTemplateRegistry` | Clona plantillas de propuestas legislativas. |

## Patrones estructurales

| Patron | Clase | Funcion en el sistema |
|---|---|---|
| Adapter | `ProtocolRsaSignatureAdapter` | Adapta el protocolo de firma digital del curso: SHA-256 + RSA modular. |
| Bridge | `SubmissionServiceBridge`, `CongressOfficeDigitalChannel` | Separa el envio del expediente del canal concreto de envio. |
| Composite | `ArchivePackage`, `ArchiveComponent`, `ProposalTextComponent`, `ResourcesComponent`, `SignatureManifestComponent` | Modela el expediente como composicion de partes. |
| Decorator | `HashingArchiveDecorator` | Agrega congelamiento criptografico SHA-256 sin modificar el expediente base. |
| Facade | `CitizenVoiceFacade` | Expone una interfaz unica para crear, firmar, verificar, congelar y enviar. |
| Flyweight | `PublicKeyFlyweightFactory` | Evita decodificar repetidamente las mismas llaves publicas. |
| Proxy | `PublicRegistryProxy` | Controla el acceso a la base publica y valida formato del DNI. |

## Firma digital usada

Se implemento el protocolo enseñado en Java en el material de seguridad:

```text
h = SHA-256(mensaje)
s = h^d mod n
h' = s^e mod n
firma valida si h' = SHA-256(mensaje)
```

Donde `(d,n)` proviene de la llave privada RSA y `(e,n)` proviene de la llave publica RSA.
