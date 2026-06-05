package pe.edu.uni.fc.cc.citizenvoice.service;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PatternCatalog {
    private PatternCatalog() {}

    public static Map<String, String> catalog() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("Creacional - Singleton", "ApplicationConfig, ApplicationContext, ProposalTemplateRegistry");
        m.put("Creacional - Factory Method", "RsaCryptoFactory y RsaKeyPairFactory crean motores RSA");
        m.put("Creacional - Abstract Factory", "FileRepositoryFactory crea la familia de repositorios del sistema");
        m.put("Creacional - Builder", "LegislativeArchiveBuilder construye el expediente legislativo");
        m.put("Creacional - Prototype", "ProposalDraftPrototype permite clonar plantillas de propuestas");
        m.put("Estructural - Adapter", "ProtocolRsaSignatureAdapter adapta el protocolo SHA-256 + RSA modular");
        m.put("Estructural - Bridge", "SubmissionServiceBridge desacopla envio y canal Congreso digital");
        m.put("Estructural - Composite", "ArchivePackage agrupa texto, recursos y manifiesto de firmas");
        m.put("Estructural - Decorator", "HashingArchiveDecorator agrega SHA-256 al expediente");
        m.put("Estructural - Facade", "CitizenVoiceFacade concentra los casos de uso de la PC3");
        m.put("Estructural - Flyweight", "PublicKeyFlyweightFactory reutiliza llaves publicas decodificadas");
        m.put("Estructural - Proxy", "PublicRegistryProxy controla acceso a la base publica");
        return m;
    }
}
