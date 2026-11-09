package rs.raf.ispiti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.raf.ispiti.model.Termin;

import java.util.List;

public interface TerminRepository extends JpaRepository<Termin, Long> {
    @Query("""
            select t from Termin t
            where (:predmetId is null or t.predmet.id = :predmetId)
              and (:rokId is null or t.rok.id = :rokId)
            order by t.pocetak
            """)
    List<Termin> pretraga(Long predmetId, Long rokId);
}
