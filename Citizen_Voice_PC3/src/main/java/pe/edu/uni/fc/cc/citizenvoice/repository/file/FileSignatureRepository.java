package pe.edu.uni.fc.cc.citizenvoice.repository.file;

import pe.edu.uni.fc.cc.citizenvoice.domain.SignatureRecord;
import pe.edu.uni.fc.cc.citizenvoice.repository.SignatureRepository;
import pe.edu.uni.fc.cc.citizenvoice.util.Codec;
import pe.edu.uni.fc.cc.citizenvoice.util.FileUtil;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileSignatureRepository implements SignatureRepository {
    private final Path path;

    public FileSignatureRepository(Path path) { this.path = path; }

    @Override public synchronized void save(SignatureRecord r) {
        if (exists(r.proposalId(), r.dni())) return;
        FileUtil.appendLine(path, r.proposalId() + "|" + r.dni() + "|" + Codec.b64(r.fullName()) + "|" + r.signatureBase64() + "|" + r.payloadHashHex() + "|" + r.signedAt());
    }

    @Override public synchronized boolean exists(long proposalId, String dni) {
        return findByProposal(proposalId).stream().anyMatch(s -> s.dni().equals(dni));
    }

    @Override public synchronized long countByProposal(long proposalId) { return findByProposal(proposalId).size(); }

    @Override public synchronized List<SignatureRecord> findByProposal(long proposalId) {
        List<SignatureRecord> list = new ArrayList<>();
        if (!Files.exists(path)) return list;
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                String[] p = line.split("\\|", -1);
                if (p.length >= 6 && Long.parseLong(p[0]) == proposalId) {
                    list.add(new SignatureRecord(Long.parseLong(p[0]), p[1], Codec.fromB64(p[2]), p[3], p[4], p[5]));
                }
            }
            return list;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo leer firmas", e);
        }
    }

    @Override public synchronized void clear() {
        try { Files.deleteIfExists(path); }
        catch (Exception e) { throw new IllegalStateException("No se pudo limpiar firmas", e); }
    }
}
