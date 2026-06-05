package pe.edu.uni.fc.cc.citizenvoice.patterns.creational;

import pe.edu.uni.fc.cc.citizenvoice.crypto.DigitalSignatureAdapter;
import pe.edu.uni.fc.cc.citizenvoice.crypto.ProtocolRsaSignatureAdapter;

/** Factory Method concreto para la familia criptografica RSA. */
public class RsaCryptoFactory extends CryptoFactory {
    @Override public DigitalSignatureAdapter createSignatureAdapter() { return new ProtocolRsaSignatureAdapter(); }
    @Override public KeyPairFactory createKeyPairFactory() { return new RsaKeyPairFactory(); }
}
