package pe.edu.uni.fc.cc.citizenvoice.patterns.structural;

import java.util.ArrayList;
import java.util.List;

/** Composite: expediente formado por componentes hijos. */
public class ArchivePackage implements ArchiveComponent {
    private final List<ArchiveComponent> components = new ArrayList<>();

    public ArchivePackage add(ArchiveComponent component) {
        components.add(component);
        return this;
    }

    @Override public String render() {
        StringBuilder sb = new StringBuilder();
        for (ArchiveComponent c : components) sb.append(c.render()).append(System.lineSeparator());
        return sb.toString();
    }
}
