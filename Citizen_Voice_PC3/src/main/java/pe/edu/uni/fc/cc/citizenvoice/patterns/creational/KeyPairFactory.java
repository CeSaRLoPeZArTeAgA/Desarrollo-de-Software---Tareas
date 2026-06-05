package pe.edu.uni.fc.cc.citizenvoice.patterns.creational;

import java.security.KeyPair;

public interface KeyPairFactory {
    KeyPair generate(int keySize);
}

