package pe.edu.uni.fc.cc.citizenvoice.repository;

import pe.edu.uni.fc.cc.citizenvoice.domain.Proposal;
import java.util.List;
import java.util.Optional;

public interface ProposalRepository {
    Proposal create(String title, String description, String collective);
    Optional<Proposal> findById(long id);
    List<Proposal> findAll();
    void update(Proposal proposal);
    void clear();
}

