package pe.edu.uni.fc.cc.citizenvoice.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Singleton: configuracion central del sistema Citizen Voice.
 */
public final class ApplicationConfig {
    private static volatile ApplicationConfig instance;

    private Path dataDirectory;
    private int signatureThreshold;
    private int serverPort;
    private int defaultKeySize;

    private ApplicationConfig() {
        this.dataDirectory = Paths.get(readEnv("CITIZEN_VOICE_DATA_DIR", "data"));
        this.signatureThreshold = parseInt(readEnv("SIGNATURE_THRESHOLD", String.valueOf(Constants.DEFAULT_SIGNATURE_THRESHOLD)), Constants.DEFAULT_SIGNATURE_THRESHOLD);
        this.serverPort = parseInt(readEnv("CITIZEN_VOICE_PORT", "8080"), 8080);
        this.defaultKeySize = parseInt(readEnv("RSA_KEY_SIZE", String.valueOf(Constants.RSA_KEY_SIZE_2048)), Constants.RSA_KEY_SIZE_2048);
    }

    public static ApplicationConfig getInstance() {
        ApplicationConfig local = instance;
        if (local == null) {
            synchronized (ApplicationConfig.class) {
                local = instance;
                if (local == null) {
                    instance = local = new ApplicationConfig();
                }
            }
        }
        return local;
    }

    public Path dataDirectory() { return dataDirectory; }
    public Path privateRegistryPath() { return dataDirectory.resolve("private_registry.db"); }
    public Path publicRegistryPath() { return dataDirectory.resolve("public_registry.db"); }
    public Path proposalPath() { return dataDirectory.resolve("proposals.db"); }
    public Path signaturePath() { return dataDirectory.resolve("signatures.db"); }
    public Path resourcePath() { return dataDirectory.resolve("resources.db"); }
    public Path archiveDirectory() { return dataDirectory.resolve("archives"); }
    public Path submissionDirectory() { return dataDirectory.resolve("submissions"); }
    public Path submissionPath() { return dataDirectory.resolve("submissions.db"); }

    public int signatureThreshold() { return signatureThreshold; }
    public int serverPort() { return serverPort; }
    public int defaultKeySize() { return defaultKeySize; }

    public void overrideForTests(Path dataDirectory, int signatureThreshold, int defaultKeySize) {
        this.dataDirectory = dataDirectory;
        this.signatureThreshold = signatureThreshold;
        this.defaultKeySize = defaultKeySize;
        this.serverPort = 8090;
    }

    public void setSignatureThreshold(int signatureThreshold) {
        this.signatureThreshold = signatureThreshold;
    }

    private static String readEnv(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value.trim();
    }

    private static int parseInt(String raw, int fallback) {
        try { return Integer.parseInt(raw); }
        catch (Exception ignored) { return fallback; }
    }
}

