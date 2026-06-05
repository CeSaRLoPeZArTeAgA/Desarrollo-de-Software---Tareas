package pe.edu.uni.fc.cc.citizenvoice.patterns.structural;

public abstract class ArchiveDecorator implements ArchiveComponent {
    protected final ArchiveComponent delegate;
    protected ArchiveDecorator(ArchiveComponent delegate) { this.delegate = delegate; }
}

