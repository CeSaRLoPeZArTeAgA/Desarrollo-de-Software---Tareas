package pe.edu.uni.fc.cc.citizenvoice.domain;

public class ProposalResource {
    private final long proposalId;
    private final String type;
    private final String content;
    private final String createdAt;

    public ProposalResource(long proposalId, String type, String content, String createdAt) {
        this.proposalId = proposalId;
        this.type = type;
        this.content = content;
        this.createdAt = createdAt;
    }

    public long proposalId() { return proposalId; }
    public String type() { return type; }
    public String content() { return content; }
    public String createdAt() { return createdAt; }
}
