package pe.edu.uni.fc.cc.citizenvoice.domain;

public class Proposal {
    private long id;
    private String title;
    private String description;
    private String collective;
    private String createdAt;
    private String status;
    private String frozenHash;
    private String submittedAt;

    public Proposal(long id, String title, String description, String collective, String createdAt, String status, String frozenHash, String submittedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.collective = collective;
        this.createdAt = createdAt;
        this.status = status;
        this.frozenHash = frozenHash == null ? "" : frozenHash;
        this.submittedAt = submittedAt == null ? "" : submittedAt;
    }

    public long id() { return id; }
    public String title() { return title; }
    public String description() { return description; }
    public String collective() { return collective; }
    public String createdAt() { return createdAt; }
    public String status() { return status; }
    public String frozenHash() { return frozenHash; }
    public String submittedAt() { return submittedAt; }

    public void freeze(String hash, String submittedAt) {
        this.status = "FROZEN_SUBMITTED";
        this.frozenHash = hash;
        this.submittedAt = submittedAt;
    }
}
