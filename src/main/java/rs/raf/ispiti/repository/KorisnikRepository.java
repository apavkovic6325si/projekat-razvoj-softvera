package rs.raf.ispiti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.raf.ispiti.model.Korisnik;
import rs.raf.ispiti.model.Uloga;

import java.util.List;
import java.util.Optional;

public interface KorisnikRepository extends JpaRepository<Korisnik, Long> {
    Optional<Korisnik> findByEmailIgnoreCase(String email);
    Optional<Korisnik> findByBrojIndeksa(String brojIndeksa);
    boolean existsByEmailIgnoreCase(String email);
    List<Korisnik> findAllByUlogaOrderByPrezimeAscImeAsc(Uloga uloga);
}
