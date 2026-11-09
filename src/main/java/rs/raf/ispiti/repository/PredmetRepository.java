package rs.raf.ispiti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.raf.ispiti.model.Predmet;

public interface PredmetRepository extends JpaRepository<Predmet, Long> {
}
