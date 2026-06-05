package pe.edu.uni.fc.cc.citizenvoice.patterns.creational;

import pe.edu.uni.fc.cc.citizenvoice.config.ApplicationConfig;
import pe.edu.uni.fc.cc.citizenvoice.repository.*;
import pe.edu.uni.fc.cc.citizenvoice.repository.file.*;

/** Abstract Factory: crea la familia completa de repositorios basados en archivos .db. */
public class FileRepositoryFactory implements RepositoryAbstractFactory {
    private final ApplicationConfig config;

    public FileRepositoryFactory(ApplicationConfig config) { this.config = config; }

    @Override public CitizenPrivateRepository privateCitizenRepository() { return new FileCitizenPrivateRepository(config.privateRegistryPath()); }
    @Override public CitizenPublicRepository publicCitizenRepository() { return new FileCitizenPublicRepository(config.publicRegistryPath()); }
    @Override public ProposalRepository proposalRepository() { return new FileProposalRepository(config.proposalPath()); }
    @Override public SignatureRepository signatureRepository() { return new FileSignatureRepository(config.signaturePath()); }
    @Override public ResourceRepository resourceRepository() { return new FileResourceRepository(config.resourcePath()); }
    @Override public SubmissionRepository submissionRepository() { return new FileSubmissionRepository(config.submissionPath()); }
}

