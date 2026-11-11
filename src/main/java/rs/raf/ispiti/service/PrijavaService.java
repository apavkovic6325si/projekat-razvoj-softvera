package rs.raf.ispiti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.raf.ispiti.common.ApiException;
import rs.raf.ispiti.model.*;
import rs.raf.ispiti.repository.KorisnikRepository;
import rs.raf.ispiti.repository.PrijavaRepository;
import rs.raf.ispiti.repository.ResenjeRepository;
import rs.raf.ispiti.web.dto.PrijavaDto;
import rs.raf.ispiti.web.dto.PrijavaStavka;
import rs.raf.ispiti.web.dto.UnosSpiskaOdgovor;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrijavaService {

    private final PrijavaRepository prijavaRepository;
    private final KorisnikRepository korisnikRepository;
    private final ResenjeRepository resenjeRepository;
    private final NastavaService nastavaService;

    @Transactional
    public UnosSpiskaOdgovor unesiSpisak(Long terminId, List<PrijavaStavka> stavke) {
        Termin termin = nastavaService.termin(terminId);

        List<String> greske = new ArrayList<>();
        List<Korisnik> studenti = new ArrayList<>();
        List<Grupa> grupe = new ArrayList<>();
        for (PrijavaStavka s : stavke) {
            Korisnik student = korisnikRepository.findByBrojIndeksa(s.brojIndeksa().trim()).orElse(null);
            Grupa grupa = nastavaService.grupa(s.grupaId());
            if (student == null || student.getUloga() != Uloga.STUDENT) {
                greske.add("ne postoji student sa indeksom " + s.brojIndeksa());
            }
            if (!grupa.getTermin().getId().equals(terminId)) {
                greske.add("grupa " + s.grupaId() + " ne pripada terminu " + terminId);
            } else if (student != null && premestaSePoslePreuzimanja(student, terminId, grupa)) {
                greske.add(s.brojIndeksa() + " je vec preuzeo zadatak i ne moze da se premesti u drugu grupu");
            }
            studenti.add(student);
            grupe.add(grupa);
        }
        if (!greske.isEmpty()) {
            throw ApiException.losZahtev("Spisak nije unet: " + String.join("; ", greske));
        }

        int dodato = 0, premesteno = 0, bezPromene = 0;
        for (int i = 0; i < stavke.size(); i++) {
            Korisnik student = studenti.get(i);
            Grupa grupa = grupe.get(i);
            Prijava p = prijavaRepository.findByStudentIdAndTerminId(student.getId(), terminId).orElse(null);
            if (p == null) {
                p = new Prijava();
                p.setStudent(student);
                p.setTermin(termin);
                dodato++;
            } else if (p.getGrupa().getId().equals(grupa.getId())) {
                bezPromene++;
                continue;
            } else {
                premesteno++;
            }
            p.setGrupa(grupa);
            prijavaRepository.save(p);
        }
        return new UnosSpiskaOdgovor(dodato, premesteno, bezPromene);
    }

    public List<PrijavaDto> spisak(Long terminId) {
        nastavaService.termin(terminId);
        return prijavaRepository.findAllByTerminIdOrderByGrupaNazivAscStudentPrezimeAsc(terminId).stream()
                .map(PrijavaDto::od).toList();
    }

    @Transactional
    public void obrisi(Long prijavaId) {
        Prijava p = prijavaRepository.findById(prijavaId)
                .orElseThrow(() -> ApiException.nijePronadjeno("Ne postoji prijava " + prijavaId));
        if (resenjeRepository.existsByStudentIdAndZadatakGrupaTerminId(p.getStudent().getId(), p.getTermin().getId())) {
            throw ApiException.konflikt("Student je vec preuzeo zadatak, prijava ne moze da se obrise");
        }
        prijavaRepository.delete(p);
    }

    private boolean premestaSePoslePreuzimanja(Korisnik student, Long terminId, Grupa novaGrupa) {
        return prijavaRepository.findByStudentIdAndTerminId(student.getId(), terminId)
                .filter(p -> !p.getGrupa().getId().equals(novaGrupa.getId()))
                .isPresent()
                && resenjeRepository.existsByStudentIdAndZadatakGrupaTerminId(student.getId(), terminId);
    }
}
