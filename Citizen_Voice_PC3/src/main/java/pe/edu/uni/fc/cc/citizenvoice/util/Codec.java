package pe.edu.uni.fc.cc.citizenvoice.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class Codec {
    private Codec() {}

    public static String b64(String text) {
        if (text == null) return "";
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    public static String fromB64(String encoded) {
        if (encoded == null || encoded.isEmpty()) return "";
        return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
    }
}
