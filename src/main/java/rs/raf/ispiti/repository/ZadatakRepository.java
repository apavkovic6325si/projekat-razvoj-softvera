package rs.raf.ispiti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.raf.ispiti.model.Zadatak;

import java.util.List;
import java.util.Optional;

public interface ZadatakRepository extends JpaRepository<Zadatak, Long> {
    Optional<Zadatak> findByGrupaId(Long grupaId);
    List<Zadatak> findAllByGrupaTerminIdOrderByGrupaNaziv(Long terminId);
}
