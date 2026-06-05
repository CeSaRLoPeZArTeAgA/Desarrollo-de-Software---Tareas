package pe.edu.uni.fc.cc.citizenvoice.patterns.structural;

import pe.edu.uni.fc.cc.citizenvoice.util.HashUtil;

/** Decorator: agrega congelamiento criptografico SHA-256 al expediente. */
public class HashingArchiveDecorator extends ArchiveDecorator {
    private String hash;

    public HashingArchiveDecorator(ArchiveComponent delegate) { super(delegate); }

    public String hashHex() {
        if (hash == null) hash = HashUtil.sha256Hex(delegate.render());
        return hash;
    }

    @Override public String render() {
        String body = delegate.render();
        return body + "\n[CONGELAMIENTO CRIPTOGRAFICO]\nSHA-256: " + hashHex() + "\n";
    }
}
