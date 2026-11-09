package rs.raf.ispiti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.raf.ispiti.model.Prijava;

import java.util.List;
import java.util.Optional;

public interface PrijavaRepository extends JpaRepository<Prijava, Long> {
    Optional<Prijava> findByStudentIdAndTerminId(Long studentId, Long terminId);
    List<Prijava> findAllByTerminIdOrderByGrupaNazivAscStudentPrezimeAsc(Long terminId);
    List<Prijava> findAllByStudentIdOrderByTerminPocetakDesc(Long studentId);
}
