package pe.edu.uni.fc.cc.citizenvoice.crypto;

import pe.edu.uni.fc.cc.citizenvoice.util.HashUtil;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

/**
 * Adapter estructural.
 * Adapta el protocolo de firma digital visto en el repositorio Java de Seguridad:
 * 1) calcular SHA-256(mensaje),
 * 2) firmar el resumen con RSA: s = h^d mod n,
 * 3) verificar recuperando h' = s^e mod n y comparando con SHA-256(mensaje).
 */
public class ProtocolRsaSignatureAdapter implements DigitalSignatureAdapter {
    @Override
    public String sign(byte[] message, PrivateKey privateKey) {
        if (!(privateKey instanceof RSAPrivateKey rsaPrivateKey)) {
            throw new IllegalArgumentException("Se requiere RSAPrivateKey");
        }
        BigInteger hash = new BigInteger(1, HashUtil.sha256(message));
        BigInteger d = rsaPrivateKey.getPrivateExponent();
        BigInteger n = rsaPrivateKey.getModulus();
        BigInteger signature = hash.modPow(d, n);
        return Base64.getEncoder().encodeToString(signature.toByteArray());
    }

    @Override
    public boolean verify(byte[] message, String signatureBase64, PublicKey publicKey) {
        if (!(publicKey instanceof RSAPublicKey rsaPublicKey)) {
            throw new IllegalArgumentException("Se requiere RSAPublicKey");
        }
        BigInteger signature = new BigInteger(1, Base64.getDecoder().decode(signatureBase64));
        BigInteger e = rsaPublicKey.getPublicExponent();
        BigInteger n = rsaPublicKey.getModulus();
        BigInteger recoveredHash = signature.modPow(e, n);
        BigInteger calculatedHash = new BigInteger(1, HashUtil.sha256(message));
        return recoveredHash.equals(calculatedHash);
    }

    @Override
    public String hashHex(byte[] message) {
        return HashUtil.toHex(HashUtil.sha256(message));
    }
}

