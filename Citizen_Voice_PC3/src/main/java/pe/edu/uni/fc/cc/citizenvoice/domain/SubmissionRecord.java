package pe.edu.uni.fc.cc.citizenvoice.domain;

public class SubmissionRecord {
    private final long proposalId;
    private final String archiveHashHex;
    private final String submittedAt;
    private final String channel;
    private final String destinationOffice;

    public SubmissionRecord(long proposalId, String archiveHashHex, String submittedAt, String channel, String destinationOffice) {
        this.proposalId = proposalId;
        this.archiveHashHex = archiveHashHex;
        this.submittedAt = submittedAt;
        this.channel = channel;
        this.destinationOffice = destinationOffice;
    }

    public long proposalId() { return proposalId; }
    public String archiveHashHex() { return archiveHashHex; }
    public String submittedAt() { return submittedAt; }
    public String channel() { return channel; }
    public String destinationOffice() { return destinationOffice; }
}
