package pe.edu.uni.fc.cc.citizenvoice.repository;

import pe.edu.uni.fc.cc.citizenvoice.domain.SubmissionRecord;
import java.util.List;

public interface SubmissionRepository {
    void save(SubmissionRecord record);
    List<SubmissionRecord> findByProposal(long proposalId);
    void clear();
}
