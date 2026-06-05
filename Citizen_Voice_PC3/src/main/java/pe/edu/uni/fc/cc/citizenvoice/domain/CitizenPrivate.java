package pe.edu.uni.fc.cc.citizenvoice.domain;

public class CitizenPrivate {
    private final String dni;
    private final String fullName;
    private final String privateKeyBase64;

    public CitizenPrivate(String dni, String fullName, String privateKeyBase64) {
        this.dni = dni;
        this.fullName = fullName;
        this.privateKeyBase64 = privateKeyBase64;
    }

    public String dni() { return dni; }
    public String fullName() { return fullName; }
    public String privateKeyBase64() { return privateKeyBase64; }
}
