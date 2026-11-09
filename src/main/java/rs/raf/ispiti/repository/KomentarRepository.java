package rs.raf.ispiti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.raf.ispiti.model.Komentar;

import java.util.List;

public interface KomentarRepository extends JpaRepository<Komentar, Long> {

    List<Komentar> findAllByResenjeIdOrderByFajlAscLinijaAsc(Long resenjeId);
}
