package pe.edu.uni.fc.cc.citizenvoice.crypto;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface DigitalSignatureAdapter {
    String sign(byte[] message, PrivateKey privateKey);
    boolean verify(byte[] message, String signatureBase64, PublicKey publicKey);
    String hashHex(byte[] message);
}
