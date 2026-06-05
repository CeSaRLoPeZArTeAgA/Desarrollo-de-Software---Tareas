package pe.edu.uni.fc.cc.citizenvoice.repository;

import pe.edu.uni.fc.cc.citizenvoice.domain.CitizenPrivate;
import java.util.List;
import java.util.Optional;

public interface CitizenPrivateRepository {
    void save(CitizenPrivate citizen);
    Optional<CitizenPrivate> findByDni(String dni);
    List<CitizenPrivate> sampleRandom(int sampleSize);
    long count();
    void clear();
}

