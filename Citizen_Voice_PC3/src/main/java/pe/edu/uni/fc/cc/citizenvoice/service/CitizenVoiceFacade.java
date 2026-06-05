package pe.edu.uni.fc.cc.citizenvoice.service;

import pe.edu.uni.fc.cc.citizenvoice.config.ApplicationConfig;
import pe.edu.uni.fc.cc.citizenvoice.crypto.*;
import pe.edu.uni.fc.cc.citizenvoice.domain.*;
import pe.edu.uni.fc.cc.citizenvoice.patterns.creational.LegislativeArchiveBuilder;
import pe.edu.uni.fc.cc.citizenvoice.patterns.creational.ProposalDraftPrototype;
import pe.edu.uni.fc.cc.citizenvoice.patterns.creational.ProposalTemplateRegistry;
import pe.edu.uni.fc.cc.citizenvoice.patterns.structural.*;
import pe.edu.uni.fc.cc.citizenvoice.repository.*;
import pe.edu.uni.fc.cc.citizenvoice.util.FileUtil;
import pe.edu.uni.fc.cc.citizenvoice.util.TimeUtil;

import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Facade estructural: concentra los casos de uso exigidos por la PC3. */
public class CitizenVoiceFacade {
    private final ApplicationConfig config;
    private final CitizenPrivateRepository privateRepository;
    private final PublicRegistryProxy publicRegistryProxy;
    private final ProposalRepository proposalRepository;
    private final SignatureRepository signatureRepository;
    private final ResourceRepository resourceRepository;
    private final SubmissionRepository submissionRepository;
    private final DigitalSignatureAdapter signatureAdapter;
    private final SubmissionServiceBridge submissionBridge;

    public CitizenVoiceFacade(ApplicationConfig config,
                              CitizenPrivateRepository privateRepository,
                              PublicRegistryProxy publicRegistryProxy,
                              ProposalRepository proposalRepository,
                              SignatureRepository signatureRepository,
                              ResourceRepository resourceRepository,
                              SubmissionRepository submissionRepository,
                              DigitalSignatureAdapter signatureAdapter,
                              SubmissionServiceBridge submissionBridge) {
        this.config = config;
        this.privateRepository = privateRepository;
        this.publicRegistryProxy = publicRegistryProxy;
        this.proposalRepository = proposalRepository;
        this.signatureRepository = signatureRepository;
        this.resourceRepository = resourceRepository;
        this.submissionRepository = submissionRepository;
        this.signatureAdapter = signatureAdapter;
        this.submissionBridge = submissionBridge;
    }

    public Proposal createProposal(String title, String description, String collective) {
        requireText(title, "titulo");
        requireText(description, "descripcion");
        requireText(collective, "colectivo");
        return proposalRepository.create(title.trim(), description.trim(), collective.trim());
    }

    public Proposal createProposalFromTemplate(String templateKey, String title, String collective) {
        ProposalDraftPrototype draft = ProposalTemplateRegistry.getInstance().cloneTemplate(templateKey);
        if (title != null && !title.isBlank()) draft.withTitle(title);
        if (collective != null && !collective.isBlank()) draft.withCollective(collective);
        return createProposal(draft.title(), draft.description(), draft.collective());
    }

    public void addResource(long proposalId, String type, String content) {
        proposalRepository.findById(proposalId).orElseThrow(() -> new IllegalArgumentException("Propuesta no encontrada"));
        resourceRepository.save(new ProposalResource(proposalId, type, content, TimeUtil.nowIso()));
    }

    public OperationResult signProposal(long proposalId, String dni) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Propuesta no encontrada: " + proposalId));
        if (!"ACTIVE".equals(proposal.status())) {
            return OperationResult.fail("La propuesta ya esta congelada o enviada", proposalId, signatureRepository.countByProposal(proposalId));
        }
        if (signatureRepository.exists(proposalId, dni)) {
            return OperationResult.fail("El DNI ya firmo esta propuesta", proposalId, signatureRepository.countByProposal(proposalId));
        }

        CitizenPrivate privateCitizen = privateRepository.findByDni(dni)
                .orElseThrow(() -> new IllegalArgumentException("DNI no existe en base privada: " + dni));
        CitizenPublic publicCitizen = publicRegistryProxy.findCitizen(dni)
                .orElseThrow(() -> new IllegalArgumentException("DNI no existe en base publica: " + dni));
        if (!privateCitizen.fullName().equals(publicCitizen.fullName())) {
            throw new IllegalStateException("Inconsistencia entre base privada y publica para DNI " + dni);
        }

        byte[] payload = SignaturePayloadBuilder.payload(proposal, dni);
        PrivateKey privateKey = KeyCodec.decodePrivate(privateCitizen.privateKeyBase64());
        PublicKey publicKey = publicRegistryProxy.findPublicKey(dni).orElseThrow();
        String signature = signatureAdapter.sign(payload, privateKey);
        boolean valid = signatureAdapter.verify(payload, signature, publicKey);
        if (!valid) throw new IllegalStateException("La firma no pudo verificarse con la llave publica");

        String payloadHash = signatureAdapter.hashHex(payload);
        signatureRepository.save(new SignatureRecord(proposalId, dni, privateCitizen.fullName(), signature, payloadHash, TimeUtil.nowIso()));
        long count = signatureRepository.countByProposal(proposalId);
        if (count >= config.signatureThreshold()) {
            OperationResult frozen = freezeAndSubmit(proposalId);
            return OperationResult.ok("Firma registrada. Umbral alcanzado; expediente congelado y enviado.", proposalId, count, frozen.hash());
        }
        return OperationResult.ok("Firma registrada y verificada", proposalId, count, "");
    }

    public OperationResult runPc3Demo(long proposalId, int sampleSize, boolean freezeAtEnd) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Propuesta no encontrada: " + proposalId));
        if (!"ACTIVE".equals(proposal.status())) {
            return OperationResult.fail("La propuesta no esta activa", proposalId, signatureRepository.countByProposal(proposalId));
        }
        List<CitizenPrivate> sample = privateRepository.sampleRandom(sampleSize);
        int processed = 0;
        for (CitizenPrivate c : sample) {
            OperationResult result = signProposal(proposalId, c.dni());
            processed++;
            if (processed % 1000 == 0 || processed == sample.size()) {
                System.out.println("Firmas procesadas: " + processed + " / " + sample.size());
            }
            if (result.hash() != null && !result.hash().isBlank()) return result;
        }
        if (freezeAtEnd) return freezeAndSubmit(proposalId);
        return OperationResult.ok("Demo PC3 ejecutada", proposalId, signatureRepository.countByProposal(proposalId), "");
    }

    public OperationResult freezeAndSubmit(long proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Propuesta no encontrada: " + proposalId));
        long count = signatureRepository.countByProposal(proposalId);
        if (count < config.signatureThreshold()) {
            return OperationResult.fail("No se puede congelar: faltan firmas. Requeridas=" + config.signatureThreshold() + ", actuales=" + count,
                    proposalId, count);
        }
        if (!"ACTIVE".equals(proposal.status()) && proposal.frozenHash() != null && !proposal.frozenHash().isBlank()) {
            return OperationResult.ok("La propuesta ya estaba congelada", proposalId, count, proposal.frozenHash());
        }

        List<ProposalResource> resources = resourceRepository.findByProposal(proposalId);
        List<SignatureRecord> signatures = signatureRepository.findByProposal(proposalId);
        ArchivePackage baseArchive = new LegislativeArchiveBuilder()
                .proposal(proposal)
                .resources(resources)
                .signatures(signatures)
                .build();
        HashingArchiveDecorator frozenArchive = new HashingArchiveDecorator(baseArchive);
        String archiveBody = frozenArchive.render();
        String hash = frozenArchive.hashHex();
        Path archiveFile = config.archiveDirectory().resolve("proposal-" + proposalId + "-frozen.txt");
        FileUtil.writeString(archiveFile, archiveBody);

        String submittedAt = TimeUtil.nowIso();
        SubmissionRecord record = new SubmissionRecord(proposalId, hash, submittedAt, submissionBridge.channelName(), "Oficina del Congreso");
        submissionBridge.send(record, archiveBody);
        submissionRepository.save(record);
        proposal.freeze(hash, submittedAt);
        proposalRepository.update(proposal);
        return OperationResult.ok("Expediente congelado y enviado a la Oficina del Congreso", proposalId, count, hash);
    }

    public boolean verifyStoredSignature(long proposalId, String dni) {
        Proposal proposal = proposalRepository.findById(proposalId).orElseThrow();
        Optional<SignatureRecord> record = signatureRepository.findByProposal(proposalId).stream().filter(s -> s.dni().equals(dni)).findFirst();
        if (record.isEmpty()) return false;
        PublicKey publicKey = publicRegistryProxy.findPublicKey(dni).orElseThrow();
        return signatureAdapter.verify(SignaturePayloadBuilder.payload(proposal, dni), record.get().signatureBase64(), publicKey);
    }

    public List<Proposal> listProposals() { return proposalRepository.findAll(); }
    public Optional<Proposal> findProposal(long id) { return proposalRepository.findById(id); }
    public long signatureCount(long proposalId) { return signatureRepository.countByProposal(proposalId); }
    public Map<String, String> patterns() { return PatternCatalog.catalog(); }

    private static void requireText(String text, String field) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("Campo obligatorio: " + field);
    }
}
