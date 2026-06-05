package pe.edu.uni.fc.cc.citizenvoice.patterns.creational;

import pe.edu.uni.fc.cc.citizenvoice.repository.*;

public interface RepositoryAbstractFactory {
    CitizenPrivateRepository privateCitizenRepository();
    CitizenPublicRepository publicCitizenRepository();
    ProposalRepository proposalRepository();
    SignatureRepository signatureRepository();
    ResourceRepository resourceRepository();
    SubmissionRepository submissionRepository();
}

