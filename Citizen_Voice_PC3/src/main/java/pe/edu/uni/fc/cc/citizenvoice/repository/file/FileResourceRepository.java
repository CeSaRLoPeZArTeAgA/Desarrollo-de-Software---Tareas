package pe.edu.uni.fc.cc.citizenvoice.repository.file;

import pe.edu.uni.fc.cc.citizenvoice.domain.ProposalResource;
import pe.edu.uni.fc.cc.citizenvoice.repository.ResourceRepository;
import pe.edu.uni.fc.cc.citizenvoice.util.Codec;
import pe.edu.uni.fc.cc.citizenvoice.util.FileUtil;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileResourceRepository implements ResourceRepository {
    private final Path path;
    public FileResourceRepository(Path path) { this.path = path; }

    @Override public synchronized void save(ProposalResource r) {
        FileUtil.appendLine(path, r.proposalId() + "|" + Codec.b64(r.type()) + "|" + Codec.b64(r.content()) + "|" + r.createdAt());
    }

    @Override public synchronized List<ProposalResource> findByProposal(long proposalId) {
        List<ProposalResource> list = new ArrayList<>();
        if (!Files.exists(path)) return list;
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                String[] p = line.split("\\|", -1);
                if (p.length >= 4 && Long.parseLong(p[0]) == proposalId) {
                    list.add(new ProposalResource(Long.parseLong(p[0]), Codec.fromB64(p[1]), Codec.fromB64(p[2]), p[3]));
                }
            }
            return list;
        } catch (Exception e) { throw new IllegalStateException("No se pudo leer recursos", e); }
    }

    @Override public synchronized void clear() {
        try { Files.deleteIfExists(path); }
        catch (Exception e) { throw new IllegalStateException("No se pudo limpiar recursos", e); }
    }
}
