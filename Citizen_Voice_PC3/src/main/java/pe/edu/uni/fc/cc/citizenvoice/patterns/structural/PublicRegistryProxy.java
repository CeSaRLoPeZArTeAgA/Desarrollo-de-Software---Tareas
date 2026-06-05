package pe.edu.uni.fc.cc.citizenvoice.patterns.structural;

import pe.edu.uni.fc.cc.citizenvoice.domain.CitizenPublic;
import pe.edu.uni.fc.cc.citizenvoice.repository.CitizenPublicRepository;

import java.security.PublicKey;
import java.util.Optional;

/** Proxy: controla acceso a la base publica y cachea llaves publicas. */
public class PublicRegistryProxy {
    private final CitizenPublicRepository repository;
    private final PublicKeyFlyweightFactory flyweightFactory;

    public PublicRegistryProxy(CitizenPublicRepository repository, PublicKeyFlyweightFactory flyweightFactory) {
        this.repository = repository;
        this.flyweightFactory = flyweightFactory;
    }

    public Optional<CitizenPublic> findCitizen(String dni) {
        if (dni == null || !dni.matches("\\d{8}")) return Optional.empty();
        return repository.findByDni(dni);
    }

    public Optional<PublicKey> findPublicKey(String dni) {
        return findCitizen(dni).map(c -> flyweightFactory.get(c.publicKeyBase64()));
    }
}

