package pe.edu.uni.fc.cc.citizenvoice.util;

import pe.edu.uni.fc.cc.citizenvoice.config.Constants;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class HashUtil {
    private HashUtil() {}

    public static byte[] sha256(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance(Constants.SHA_256_ALGORITHM);
            return md.digest(input);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular SHA-256", e);
        }
    }

    public static String sha256Hex(String input) {
        return toHex(sha256(input.getBytes(StandardCharsets.UTF_8)));
    }

    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
