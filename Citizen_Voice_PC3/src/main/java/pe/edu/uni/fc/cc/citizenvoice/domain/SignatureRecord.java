package pe.edu.uni.fc.cc.citizenvoice.domain;

public class SignatureRecord {
    private final long proposalId;
    private final String dni;
    private final String fullName;
    private final String signatureBase64;
    private final String payloadHashHex;
    private final String signedAt;

    public SignatureRecord(long proposalId, String dni, String fullName, String signatureBase64, String payloadHashHex, String signedAt) {
        this.proposalId = proposalId;
        this.dni = dni;
        this.fullName = fullName;
        this.signatureBase64 = signatureBase64;
        this.payloadHashHex = payloadHashHex;
        this.signedAt = signedAt;
    }

    public long proposalId() { return proposalId; }
    public String dni() { return dni; }
    public String fullName() { return fullName; }
    public String signatureBase64() { return signatureBase64; }
    public String payloadHashHex() { return payloadHashHex; }
    public String signedAt() { return signedAt; }
}
