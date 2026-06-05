package pe.edu.uni.fc.cc.citizenvoice.domain;

public class CitizenPublic {
    private final String dni;
    private final String fullName;
    private final String publicKeyBase64;

    public CitizenPublic(String dni, String fullName, String publicKeyBase64) {
        this.dni = dni;
        this.fullName = fullName;
        this.publicKeyBase64 = publicKeyBase64;
    }

    public String dni() { return dni; }
    public String fullName() { return fullName; }
    public String publicKeyBase64() { return publicKeyBase64; }
}

