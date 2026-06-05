package pe.edu.uni.fc.cc.citizenvoice.patterns.structural;

import pe.edu.uni.fc.cc.citizenvoice.domain.Proposal;

public class ProposalTextComponent implements ArchiveComponent {
    private final Proposal proposal;
    public ProposalTextComponent(Proposal proposal) { this.proposal = proposal; }

    @Override public String render() {
        return "[PROPUESTA]\n" +
                "ID: " + proposal.id() + "\n" +
                "Titulo: " + proposal.title() + "\n" +
                "Colectivo: " + proposal.collective() + "\n" +
                "Creacion: " + proposal.createdAt() + "\n" +
                "Descripcion:\n" + proposal.description() + "\n";
    }
}
