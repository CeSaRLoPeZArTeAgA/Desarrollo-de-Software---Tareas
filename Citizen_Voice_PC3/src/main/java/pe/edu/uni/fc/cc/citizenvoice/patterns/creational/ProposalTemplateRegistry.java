package pe.edu.uni.fc.cc.citizenvoice.patterns.creational;

import java.util.LinkedHashMap;
import java.util.Map;

/** Singleton + Prototype Registry. */
public final class ProposalTemplateRegistry {
    private static final ProposalTemplateRegistry INSTANCE = new ProposalTemplateRegistry();
    private final Map<String, ProposalDraftPrototype> templates = new LinkedHashMap<>();

    private ProposalTemplateRegistry() {
        templates.put("transparencia-digital", new ProposalDraftPrototype(
                "Ley de Transparencia en Servicios Publicos Digitales",
                "Propuesta normativa para exigir trazabilidad, auditoria y rendicion de cuentas en plataformas digitales del Estado.",
                "Colectivo Ciudadano Digital"));
        templates.put("seguridad-ciudadana", new ProposalDraftPrototype(
                "Ley de Fortalecimiento de Alertas Ciudadanas",
                "Propuesta para integrar canales digitales seguros de reporte ciudadano y seguimiento legislativo.",
                "Colectivo Seguridad y Comunidad"));
    }

    public static ProposalTemplateRegistry getInstance() { return INSTANCE; }

    public ProposalDraftPrototype cloneTemplate(String key) {
        ProposalDraftPrototype prototype = templates.get(key);
        if (prototype == null) throw new IllegalArgumentException("Plantilla no encontrada: " + key);
        return prototype.clone();
    }

    public Map<String, ProposalDraftPrototype> list() { return new LinkedHashMap<>(templates); }
}

