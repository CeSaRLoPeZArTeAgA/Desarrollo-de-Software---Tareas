package pe.edu.uni.fc.cc.citizenvoice.repository.file;

import pe.edu.uni.fc.cc.citizenvoice.domain.CitizenPrivate;
import pe.edu.uni.fc.cc.citizenvoice.repository.CitizenPrivateRepository;
import pe.edu.uni.fc.cc.citizenvoice.util.Codec;
import pe.edu.uni.fc.cc.citizenvoice.util.FileUtil;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;

public class FileCitizenPrivateRepository implements CitizenPrivateRepository {
    private final Path path;
    private volatile Map<String, CitizenPrivate> index;

    public FileCitizenPrivateRepository(Path path) { this.path = path; }

    @Override public synchronized void save(CitizenPrivate c) {
        FileUtil.appendLine(path, c.dni() + "|" + Codec.b64(c.fullName()) + "|" + c.privateKeyBase64());
        if (index != null) index.put(c.dni(), c);
    }

    @Override public Optional<CitizenPrivate> findByDni(String dni) { return Optional.ofNullable(loadIndex().get(dni)); }

    @Override public List<CitizenPrivate> sampleRandom(int sampleSize) {
        List<CitizenPrivate> citizens = new ArrayList<>(loadIndex().values());
        if (sampleSize > citizens.size()) throw new IllegalArgumentException("sampleSize supera la cantidad de ciudadanos registrados");
        Collections.shuffle(citizens, new SecureRandom());
        return new ArrayList<>(citizens.subList(0, sampleSize));
    }

    @Override public long count() { return loadIndex().size(); }

    @Override public synchronized void clear() {
        try { Files.deleteIfExists(path); }
        catch (Exception e) { throw new IllegalStateException("No se pudo limpiar " + path, e); }
        index = null;
    }

    private Map<String, CitizenPrivate> loadIndex() {
        Map<String, CitizenPrivate> local = index;
        if (local == null) {
            synchronized (this) {
                local = index;
                if (local == null) index = local = readAll();
            }
        }
        return local;
    }

    private Map<String, CitizenPrivate> readAll() {
        Map<String, CitizenPrivate> map = new LinkedHashMap<>();
        if (!Files.exists(path)) return map;
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                String[] p = line.split("\\|", -1);
                if (p.length >= 3) map.put(p[0], new CitizenPrivate(p[0], Codec.fromB64(p[1]), p[2]));
            }
            return map;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo leer base privada " + path, e);
        }
    }
}
