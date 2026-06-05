package pe.edu.uni.fc.cc.citizenvoice.patterns.creational;

import pe.edu.uni.fc.cc.citizenvoice.crypto.DigitalSignatureAdapter;

public abstract class CryptoFactory {
    public abstract DigitalSignatureAdapter createSignatureAdapter();
    public abstract KeyPairFactory createKeyPairFactory();
}


