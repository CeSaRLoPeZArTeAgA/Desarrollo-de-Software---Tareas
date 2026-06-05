package pe.edu.uni.fc.cc.citizenvoice.patterns.creational;

import pe.edu.uni.fc.cc.citizenvoice.config.Constants;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

/** Factory Method: encapsula la creacion de pares de llaves RSA. */
public class RsaKeyPairFactory implements KeyPairFactory {
    @Override
    public KeyPair generate(int keySize) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(Constants.RSA_ALGORITHM);
            kpg.initialize(keySize);
            return kpg.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar par de llaves RSA", e);
        }
    }
}
