package rs.raf.ispiti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.raf.ispiti.common.ApiException;
import rs.raf.ispiti.model.Korisnik;
import rs.raf.ispiti.model.Uloga;
import rs.raf.ispiti.repository.KorisnikRepository;
import rs.raf.ispiti.web.dto.KorisnikDto;
import rs.raf.ispiti.web.dto.KorisnikZahtev;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KorisnikService {
    private final KorisnikRepository korisnikRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public KorisnikDto kreiraj(KorisnikZahtev z) {
        if (korisnikRepository.existsByEmailIgnoreCase(z.email())) {
            throw ApiException.konflikt("Email je zauzet: " + z.email());
        }
        boolean imaIndeks = z.brojIndeksa() != null && !z.brojIndeksa().isBlank();
        if (z.uloga() == Uloga.STUDENT && !imaIndeks) {
            throw ApiException.losZahtev("Student mora imati broj indeksa!");
        }
        Korisnik k = new Korisnik();
        k.setIme(z.ime());
        k.setPrezime(z.prezime());
        k.setEmail(z.email().trim());
        k.setLozinka(passwordEncoder.encode(z.lozinka()));
        k.setUloga(z.uloga());
        k.setBrojIndeksa(imaIndeks ? z.brojIndeksa().trim() : null);
        k.setNalog(z.nalog() == null || z.nalog().isBlank() ? null : z.nalog().trim());
        return KorisnikDto.od(korisnikRepository.save(k));
    }

    public List<KorisnikDto> lista(Uloga uloga) {
        List<Korisnik> korisnici = uloga == null ? korisnikRepository.findAll() : korisnikRepository.findAllByUlogaOrderByPrezimeAscImeAsc(uloga);
        return korisnici.stream().map(KorisnikDto::od).toList();
    }
}
