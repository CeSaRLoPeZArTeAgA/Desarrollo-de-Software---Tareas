package pe.edu.uni.fc.cc.citizenvoice.repository;

import pe.edu.uni.fc.cc.citizenvoice.domain.SignatureRecord;
import java.util.List;

public interface SignatureRepository {
    void save(SignatureRecord record);
    boolean exists(long proposalId, String dni);
    long countByProposal(long proposalId);
    List<SignatureRecord> findByProposal(long proposalId);
    void clear();
}

