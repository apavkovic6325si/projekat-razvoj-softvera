package rs.raf.ispiti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rs.raf.ispiti.common.ApiException;
import rs.raf.ispiti.common.FileStorage;
import rs.raf.ispiti.model.*;
import rs.raf.ispiti.repository.KomentarRepository;
import rs.raf.ispiti.repository.KorisnikRepository;
import rs.raf.ispiti.repository.PrijavaRepository;
import rs.raf.ispiti.repository.ResenjeRepository;
import rs.raf.ispiti.repository.ZadatakRepository;
import rs.raf.ispiti.web.dto.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PolaganjeService {

    private final KorisnikRepository korisnikRepository;
    private final PrijavaRepository prijavaRepository;
    private final ZadatakRepository zadatakRepository;
    private final ResenjeRepository resenjeRepository;
    private final KomentarRepository komentarRepository;
    private final FileStorage fileStorage;

    public List<PrijavaDto> mojePrijave(Long studentId) {
        return prijavaRepository.findAllByStudentIdOrderByTerminPocetakDesc(studentId).stream()
                .map(PrijavaDto::od).toList();
    }

    public Prijava proveriPravo(Long studentId, PolaganjeZahtev z) {
        Korisnik student = korisnikRepository.findById(studentId)
                .orElseThrow(() -> ApiException.nijePronadjeno("Korisnik vise ne postoji"));
        if (student.getBrojIndeksa() == null || !normalizuj(student.getBrojIndeksa()).equals(normalizuj(z.brojIndeksa()))) {
            throw ApiException.zabranjeno("Uneti broj indeksa se ne poklapa sa prijavljenim nalogom");
        }
        Prijava prijava = prijavaRepository.findByStudentIdAndTerminId(studentId, z.terminId())
                .orElseThrow(() -> ApiException.zabranjeno("Niste na spisku za polaganje u ovom terminu"));
        if (!prijava.getGrupa().getId().equals(z.grupaId())) {
            throw ApiException.zabranjeno("Na spisku ste za grupu \"" + prijava.getGrupa().getNaziv()
                    + "\", a ne za izabranu grupu");
        }
        return prijava;
    }

    public PolaganjeInfo info(Long studentId, PolaganjeZahtev z) {
        Prijava prijava = proveriPravo(studentId, z);
        Termin termin = prijava.getTermin();
        Zadatak zadatak = zadatakRepository.findByGrupaId(prijava.getGrupa().getId()).orElse(null);
        Resenje resenje = zadatak == null ? null
                : resenjeRepository.findByStudentIdAndZadatakId(studentId, zadatak.getId()).orElse(null);

        LocalDateTime sada = LocalDateTime.now();
        PolaganjeInfo.Stanje stanje = sada.isBefore(termin.getPocetak()) ? PolaganjeInfo.Stanje.NIJE_POCEO
                : sada.isAfter(termin.getKraj()) ? PolaganjeInfo.Stanje.ZAVRSEN : PolaganjeInfo.Stanje.U_TOKU;
        long preostalo = Math.max(0, Duration.between(sada, termin.getKraj()).toSeconds());
        boolean prikaziZadatak = zadatak != null && stanje != PolaganjeInfo.Stanje.NIJE_POCEO;

        return new PolaganjeInfo(TerminDto.od(termin), GrupaDto.od(prijava.getGrupa()),
                zadatak == null ? null : zadatak.getId(),
                prikaziZadatak ? zadatak.getNaziv() : null,
                prikaziZadatak ? zadatak.getOpis() : null,
                stanje, sada, preostalo,
                resenje == null ? null : resenje.getPreuzeto(),
                resenje == null ? null : resenje.getPredato());
    }

    @Transactional
    public Zadatak preuzmi(Long studentId, PolaganjeZahtev z) {
        Prijava prijava = proveriPravo(studentId, z);
        Termin termin = prijava.getTermin();
        LocalDateTime sada = LocalDateTime.now();
        if (!termin.jeUToku(sada)) {
            throw ApiException.zabranjeno("Zadatak moze da se preuzme samo u toku termina (od "
                    + termin.getPocetak() + " do " + termin.getKraj() + ")");
        }
        Zadatak zadatak = zadatakRepository.findByGrupaId(prijava.getGrupa().getId())
                .orElseThrow(() -> ApiException.nijePronadjeno("Za vasu grupu jos nije postavljen zadatak"));

        if (resenjeRepository.findByStudentIdAndZadatakId(studentId, zadatak.getId()).isEmpty()) {
            Resenje r = new Resenje();
            r.setStudent(prijava.getStudent());
            r.setZadatak(zadatak);
            r.setPreuzeto(sada);
            resenjeRepository.save(r);
        }
        return zadatak;
    }

    @Transactional
    public ResenjeDto predaj(Long studentId, Long zadatakId, MultipartFile fajl) {
        Resenje resenje = resenjeRepository.findByStudentIdAndZadatakId(studentId, zadatakId)
                .orElseThrow(() -> ApiException.losZahtev("Prvo preuzmite zadatak, pa ga onda predajte"));
        Termin termin = resenje.getZadatak().getGrupa().getTermin();
        LocalDateTime sada = LocalDateTime.now();
        if (sada.isAfter(termin.getKraj())) {
            throw ApiException.zabranjeno("Termin je istekao u " + termin.getKraj() + ", predaja vise nije moguca");
        }

        String staraPutanja = resenje.getPutanjaFajla();
        resenje.setPutanjaFajla(fileStorage.sacuvaj(fajl, "resenja"));
        resenje.setNazivFajla(fajl.getOriginalFilename());
        resenje.setPredato(sada);
        resenje.setPoeni(null);
        komentarRepository.deleteAllByResenjeId(resenje.getId());
        Resenje sacuvano = resenjeRepository.save(resenje);
        fileStorage.obrisi(staraPutanja);
        return ResenjeDto.od(sacuvano);
    }

    private static String normalizuj(String indeks) {
        return indeks.replaceAll("\\s+", "").toUpperCase();
    }
}
