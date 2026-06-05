package pe.edu.uni.fc.cc.citizenvoice.config;

/**
 * Constantes criptograficas tomadas del enfoque del repositorio de Seguridad:
 * RSA para el par de llaves y SHA-256 para el resumen de mensajes.
 */
public final class Constants {
    private Constants() {}

    public static final String RSA_ALGORITHM = "RSA";
    public static final String SHA_256_ALGORITHM = "SHA-256";
    public static final int RSA_KEY_SIZE_2048 = 2048;
    public static final String SIGNATURE_PROTOCOL_NAME = "SHA-256 + RSA modular signature";
    public static final int DEFAULT_SIGNATURE_THRESHOLD = 25_000;
    public static final int DEFAULT_GENERAL_REGISTRY_SIZE = 100_000;
}

