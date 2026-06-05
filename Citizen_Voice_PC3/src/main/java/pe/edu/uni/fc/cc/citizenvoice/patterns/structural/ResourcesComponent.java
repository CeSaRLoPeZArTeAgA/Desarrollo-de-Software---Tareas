package pe.edu.uni.fc.cc.citizenvoice.patterns.structural;

import pe.edu.uni.fc.cc.citizenvoice.domain.ProposalResource;
import java.util.List;

public class ResourcesComponent implements ArchiveComponent {
    private final List<ProposalResource> resources;
    public ResourcesComponent(List<ProposalResource> resources) { this.resources = resources; }

    @Override public String render() {
        StringBuilder sb = new StringBuilder("[RECURSOS Y SUSTENTO]\n");
        if (resources.isEmpty()) sb.append("Sin recursos adicionales registrados.\n");
        for (ProposalResource r : resources) {
            sb.append("- ").append(r.type()).append(" | ").append(r.createdAt()).append(" | ").append(r.content()).append('\n');
        }
        return sb.toString();
    }
}
