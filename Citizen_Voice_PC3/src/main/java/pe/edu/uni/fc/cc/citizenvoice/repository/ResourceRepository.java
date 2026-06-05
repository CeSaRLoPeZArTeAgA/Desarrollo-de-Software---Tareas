package pe.edu.uni.fc.cc.citizenvoice.repository;

import pe.edu.uni.fc.cc.citizenvoice.domain.ProposalResource;
import java.util.List;

public interface ResourceRepository {
    void save(ProposalResource resource);
    List<ProposalResource> findByProposal(long proposalId);
    void clear();
}

