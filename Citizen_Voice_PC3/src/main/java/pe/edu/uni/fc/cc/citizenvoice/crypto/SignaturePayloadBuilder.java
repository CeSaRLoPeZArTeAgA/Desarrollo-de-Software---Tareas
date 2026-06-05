package pe.edu.uni.fc.cc.citizenvoice.crypto;

import pe.edu.uni.fc.cc.citizenvoice.domain.Proposal;
import pe.edu.uni.fc.cc.citizenvoice.util.HashUtil;

import java.nio.charset.StandardCharsets;

public final class SignaturePayloadBuilder {
    private SignaturePayloadBuilder() {}

    public static byte[] payload(Proposal proposal, String dni) {
        String canonical = "CITIZEN_VOICE_PC3|" +
                "proposalId=" + proposal.id() + "|" +
                "dni=" + dni + "|" +
                "titleHash=" + HashUtil.sha256Hex(proposal.title()) + "|" +
                "descriptionHash=" + HashUtil.sha256Hex(proposal.description()) + "|" +
                "collectiveHash=" + HashUtil.sha256Hex(proposal.collective());
        return canonical.getBytes(StandardCharsets.UTF_8);
    }
}
