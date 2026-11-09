package rs.raf.ispiti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.raf.ispiti.model.Grupa;

import java.util.List;

public interface GrupaRepository extends JpaRepository<Grupa, Long> {
    List<Grupa> findAllByTerminIdOrderByNaziv(Long terminId);
}
