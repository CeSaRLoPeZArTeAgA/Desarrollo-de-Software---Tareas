package pe.edu.uni.fc.cc.citizenvoice.patterns.structural;

import pe.edu.uni.fc.cc.citizenvoice.crypto.KeyCodec;

import java.security.PublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Flyweight: reutiliza objetos PublicKey decodificados desde Base64. */
public class PublicKeyFlyweightFactory {
    private final Map<String, PublicKey> cache = new ConcurrentHashMap<>();

    public PublicKey get(String publicKeyBase64) {
        return cache.computeIfAbsent(publicKeyBase64, KeyCodec::decodePublic);
    }

    public int size() { return cache.size(); }
}
