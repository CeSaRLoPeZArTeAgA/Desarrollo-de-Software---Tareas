package pe.edu.uni.fc.cc.citizenvoice.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;

public final class FileUtil {
    private FileUtil() {}

    public static void ensureParent(Path path) {
        try {
            Path parent = path.getParent();
            if (parent != null) Files.createDirectories(parent);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear el directorio padre de " + path, e);
        }
    }

    public static void ensureDirectory(Path path) {
        try { Files.createDirectories(path); }
        catch (IOException e) { throw new IllegalStateException("No se pudo crear directorio " + path, e); }
    }

    public static synchronized void appendLine(Path path, String line) {
        ensureParent(path);
        try {
            Files.writeString(path, line + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo escribir en " + path, e);
        }
    }

    public static synchronized void writeString(Path path, String body) {
        ensureParent(path);
        try {
            Files.writeString(path, body, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo escribir archivo " + path, e);
        }
    }

    public static void deleteDirectory(Path path) {
        if (path == null || !Files.exists(path)) return;
        try {
            Files.walk(path).sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p); }
                catch (IOException e) { throw new RuntimeException(e); }
            });
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo eliminar directorio " + path, e);
        }
    }
}
