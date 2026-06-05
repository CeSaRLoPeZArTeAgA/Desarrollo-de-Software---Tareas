package pe.edu.uni.fc.cc.citizenvoice.patterns.creational;

/** Prototype: plantilla clonable de propuesta legislativa. */
public class ProposalDraftPrototype implements Cloneable {
    private String title;
    private String description;
    private String collective;

    public ProposalDraftPrototype(String title, String description, String collective) {
        this.title = title;
        this.description = description;
        this.collective = collective;
    }

    public String title() { return title; }
    public String description() { return description; }
    public String collective() { return collective; }

    public ProposalDraftPrototype withTitle(String title) { this.title = title; return this; }
    public ProposalDraftPrototype withDescription(String description) { this.description = description; return this; }
    public ProposalDraftPrototype withCollective(String collective) { this.collective = collective; return this; }

    @Override public ProposalDraftPrototype clone() {
        try { return (ProposalDraftPrototype) super.clone(); }
        catch (CloneNotSupportedException e) { throw new AssertionError(e); }
    }
}
