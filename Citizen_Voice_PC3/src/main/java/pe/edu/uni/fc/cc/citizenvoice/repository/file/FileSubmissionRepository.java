package pe.edu.uni.fc.cc.citizenvoice.repository.file;

import pe.edu.uni.fc.cc.citizenvoice.domain.SubmissionRecord;
import pe.edu.uni.fc.cc.citizenvoice.repository.SubmissionRepository;
import pe.edu.uni.fc.cc.citizenvoice.util.Codec;
import pe.edu.uni.fc.cc.citizenvoice.util.FileUtil;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileSubmissionRepository implements SubmissionRepository {
    private final Path path;
    public FileSubmissionRepository(Path path) { this.path = path; }

    @Override public synchronized void save(SubmissionRecord r) {
        FileUtil.appendLine(path, r.proposalId() + "|" + r.archiveHashHex() + "|" + r.submittedAt() + "|" + Codec.b64(r.channel()) + "|" + Codec.b64(r.destinationOffice()));
    }

    @Override public synchronized List<SubmissionRecord> findByProposal(long proposalId) {
        List<SubmissionRecord> list = new ArrayList<>();
        if (!Files.exists(path)) return list;
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                String[] p = line.split("\\|", -1);
                if (p.length >= 5 && Long.parseLong(p[0]) == proposalId) {
                    list.add(new SubmissionRecord(Long.parseLong(p[0]), p[1], p[2], Codec.fromB64(p[3]), Codec.fromB64(p[4])));
                }
            }
            return list;
        } catch (Exception e) { throw new IllegalStateException("No se pudo leer envios", e); }
    }

    @Override public synchronized void clear() {
        try { Files.deleteIfExists(path); }
        catch (Exception e) { throw new IllegalStateException("No se pudo limpiar envios", e); }
    }
}
