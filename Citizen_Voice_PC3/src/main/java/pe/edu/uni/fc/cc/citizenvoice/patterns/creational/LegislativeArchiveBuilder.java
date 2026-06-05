package pe.edu.uni.fc.cc.citizenvoice.patterns.creational;

import pe.edu.uni.fc.cc.citizenvoice.domain.Proposal;
import pe.edu.uni.fc.cc.citizenvoice.domain.ProposalResource;
import pe.edu.uni.fc.cc.citizenvoice.domain.SignatureRecord;
import pe.edu.uni.fc.cc.citizenvoice.patterns.structural.*;

import java.util.ArrayList;
import java.util.List;

/** Builder: construye paso a paso el expediente legislativo congelable. */
public class LegislativeArchiveBuilder {
    private Proposal proposal;
    private List<ProposalResource> resources = new ArrayList<>();
    private List<SignatureRecord> signatures = new ArrayList<>();

    public LegislativeArchiveBuilder proposal(Proposal proposal) { this.proposal = proposal; return this; }
    public LegislativeArchiveBuilder resources(List<ProposalResource> resources) { this.resources = resources; return this; }
    public LegislativeArchiveBuilder signatures(List<SignatureRecord> signatures) { this.signatures = signatures; return this; }

    public ArchivePackage build() {
        if (proposal == null) throw new IllegalStateException("No se puede construir expediente sin propuesta");
        return new ArchivePackage()
                .add(new ProposalTextComponent(proposal))
                .add(new ResourcesComponent(resources))
                .add(new SignatureManifestComponent(signatures));
    }
}

