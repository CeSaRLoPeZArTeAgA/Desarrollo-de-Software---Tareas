package pe.edu.uni.fc.cc.citizenvoice.repository;

import pe.edu.uni.fc.cc.citizenvoice.domain.CitizenPublic;
import java.util.Optional;

public interface CitizenPublicRepository {
    void save(CitizenPublic citizen);
    Optional<CitizenPublic> findByDni(String dni);
    long count();
    void clear();
}

