package rs.raf.ispiti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.raf.ispiti.model.Resenje;

import java.util.List;
import java.util.Optional;

public interface ResenjeRepository extends JpaRepository<Resenje, Long> {

    Optional<Resenje> findByStudentIdAndZadatakId(Long studentId, Long zadatakId);

    List<Resenje> findAllByZadatakGrupaTerminId(Long terminId);

    List<Resenje> findAllByStudentIdOrderByPreuzetoDesc(Long studentId);

    boolean existsByStudentIdAndZadatakGrupaTerminId(Long studentId, Long terminId);

    @Query("""
            select r from Resenje r
            join r.zadatak z join z.grupa g join g.termin t
            where (:predmetId is null or t.predmet.id = :predmetId)
              and (:rokId is null or t.rok.id = :rokId)
              and (:terminId is null or t.id = :terminId)
              and (:grupaId is null or g.id = :grupaId)
              and (:ucionica is null or t.ucionica = :ucionica)
              and (:brojIndeksa is null or r.student.brojIndeksa = :brojIndeksa)
            order by t.pocetak, g.naziv, r.student.prezime, r.student.ime
            """)
    List<Resenje> pretraga(Long predmetId, Long rokId, Long terminId, Long grupaId,
                           String ucionica, String brojIndeksa);
}
