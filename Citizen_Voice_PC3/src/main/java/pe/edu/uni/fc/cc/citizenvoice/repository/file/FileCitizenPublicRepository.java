package pe.edu.uni.fc.cc.citizenvoice.repository.file;

import pe.edu.uni.fc.cc.citizenvoice.domain.CitizenPublic;
import pe.edu.uni.fc.cc.citizenvoice.repository.CitizenPublicRepository;
import pe.edu.uni.fc.cc.citizenvoice.util.Codec;
import pe.edu.uni.fc.cc.citizenvoice.util.FileUtil;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileCitizenPublicRepository implements CitizenPublicRepository {
    private final Path path;
    private volatile Map<String, CitizenPublic> index;

    public FileCitizenPublicRepository(Path path) { this.path = path; }

    @Override public synchronized void save(CitizenPublic c) {
        FileUtil.appendLine(path, c.dni() + "|" + Codec.b64(c.fullName()) + "|" + c.publicKeyBase64());
        if (index != null) index.put(c.dni(), c);
    }

    @Override public Optional<CitizenPublic> findByDni(String dni) { return Optional.ofNullable(loadIndex().get(dni)); }

    @Override public long count() { return loadIndex().size(); }

    @Override public synchronized void clear() {
        try { Files.deleteIfExists(path); }
        catch (Exception e) { throw new IllegalStateException("No se pudo limpiar " + path, e); }
        index = null;
    }

    private Map<String, CitizenPublic> loadIndex() {
        Map<String, CitizenPublic> local = index;
        if (local == null) {
            synchronized (this) {
                local = index;
                if (local == null) index = local = readAll();
            }
        }
        return local;
    }

    private Map<String, CitizenPublic> readAll() {
        Map<String, CitizenPublic> map = new LinkedHashMap<>();
        if (!Files.exists(path)) return map;
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                String[] p = line.split("\\|", -1);
                if (p.length >= 3) map.put(p[0], new CitizenPublic(p[0], Codec.fromB64(p[1]), p[2]));
            }
            return map;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo leer base publica " + path, e);
        }
    }
}
