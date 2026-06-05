package pe.edu.uni.fc.cc.citizenvoice.service;

public class OperationResult {
    private final boolean ok;
    private final String message;
    private final long proposalId;
    private final long signatures;
    private final String hash;

    private OperationResult(boolean ok, String message, long proposalId, long signatures, String hash) {
        this.ok = ok;
        this.message = message;
        this.proposalId = proposalId;
        this.signatures = signatures;
        this.hash = hash == null ? "" : hash;
    }

    public static OperationResult ok(String message, long proposalId, long signatures, String hash) {
        return new OperationResult(true, message, proposalId, signatures, hash);
    }

    public static OperationResult fail(String message, long proposalId, long signatures) {
        return new OperationResult(false, message, proposalId, signatures, "");
    }

    public boolean ok() { return ok; }
    public String message() { return message; }
    public long proposalId() { return proposalId; }
    public long signatures() { return signatures; }
    public String hash() { return hash; }
}
