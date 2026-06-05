package pe.edu.uni.fc.cc.citizenvoice.patterns.structural;

import pe.edu.uni.fc.cc.citizenvoice.domain.SignatureRecord;
import java.util.List;

public class SignatureManifestComponent implements ArchiveComponent {
    private final List<SignatureRecord> signatures;
    public SignatureManifestComponent(List<SignatureRecord> signatures) { this.signatures = signatures; }

    @Override public String render() {
        StringBuilder sb = new StringBuilder("[MANIFIESTO DE FIRMAS DIGITALES]\n");
        sb.append("Total firmas validas: ").append(signatures.size()).append('\n');
        int shown = 0;
        for (SignatureRecord s : signatures) {
            if (shown >= 30) { sb.append("... firmas restantes omitidas en vista resumida ...\n"); break; }
            sb.append("- DNI: ").append(s.dni())
                    .append(" | Persona: ").append(s.fullName())
                    .append(" | Hash payload: ").append(s.payloadHashHex())
                    .append(" | Fecha: ").append(s.signedAt()).append('\n');
            shown++;
        }
        return sb.toString();
    }
}


