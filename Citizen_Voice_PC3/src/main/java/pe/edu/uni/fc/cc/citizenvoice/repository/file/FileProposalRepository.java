package pe.edu.uni.fc.cc.citizenvoice.repository.file;

import pe.edu.uni.fc.cc.citizenvoice.domain.Proposal;
import pe.edu.uni.fc.cc.citizenvoice.repository.ProposalRepository;
import pe.edu.uni.fc.cc.citizenvoice.util.Codec;
import pe.edu.uni.fc.cc.citizenvoice.util.FileUtil;
import pe.edu.uni.fc.cc.citizenvoice.util.TimeUtil;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileProposalRepository implements ProposalRepository {
    private final Path path;

    public FileProposalRepository(Path path) { this.path = path; }

    @Override public synchronized Proposal create(String title, String description, String collective) {
        long id = nextId();
        Proposal p = new Proposal(id, title, description, collective, TimeUtil.nowIso(), "ACTIVE", "", "");
        append(p);
        return p;
    }

    @Override public synchronized Optional<Proposal> findById(long id) {
        return findAll().stream().filter(p -> p.id() == id).findFirst();
    }

    @Override public synchronized List<Proposal> findAll() {
        List<Proposal> list = new ArrayList<>();
        if (!Files.exists(path)) return list;
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                String[] p = line.split("\\|", -1);
                if (p.length >= 8) {
                    list.add(new Proposal(Long.parseLong(p[0]), Codec.fromB64(p[1]), Codec.fromB64(p[2]), Codec.fromB64(p[3]), p[4], p[5], p[6], p[7]));
                }
            }
            return list;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo leer propuestas", e);
        }
    }

    @Override public synchronized void update(Proposal proposal) {
        List<Proposal> all = findAll();
        boolean found = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).id() == proposal.id()) {
                all.set(i, proposal);
                found = true;
                break;
            }
        }
        if (!found) all.add(proposal);
        StringBuilder sb = new StringBuilder();
        for (Proposal p : all) sb.append(line(p)).append(System.lineSeparator());
        FileUtil.writeString(path, sb.toString());
    }

    @Override public synchronized void clear() {
        try { Files.deleteIfExists(path); }
        catch (Exception e) { throw new IllegalStateException("No se pudo limpiar propuestas", e); }
    }

    private long nextId() {
        return findAll().stream().mapToLong(Proposal::id).max().orElse(0) + 1;
    }

    private void append(Proposal p) { FileUtil.appendLine(path, line(p)); }

    private String line(Proposal p) {
        return p.id() + "|" + Codec.b64(p.title()) + "|" + Codec.b64(p.description()) + "|" + Codec.b64(p.collective()) + "|" + p.createdAt() + "|" + p.status() + "|" + p.frozenHash() + "|" + p.submittedAt();
    }
}
