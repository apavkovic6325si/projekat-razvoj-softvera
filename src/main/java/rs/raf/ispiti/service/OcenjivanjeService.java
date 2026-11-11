package rs.raf.ispiti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.raf.ispiti.common.ApiException;
import rs.raf.ispiti.common.FileStorage;
import rs.raf.ispiti.model.Komentar;
import rs.raf.ispiti.model.Resenje;
import rs.raf.ispiti.model.Termin;
import rs.raf.ispiti.repository.KomentarRepository;
import rs.raf.ispiti.repository.KorisnikRepository;
import rs.raf.ispiti.repository.ResenjeRepository;
import rs.raf.ispiti.web.dto.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class OcenjivanjeService {

    private final ResenjeRepository resenjeRepository;
    private final KomentarRepository komentarRepository;
    private final KorisnikRepository korisnikRepository;
    private final FileStorage fileStorage;

    public List<Resenje> pretraga(ResenjeFilter f) {
        String ucionica = prazanUNull(f.ucionica());
        String indeks = prazanUNull(f.brojIndeksa());
        return resenjeRepository.pretraga(f.predmetId(), f.rokId(), f.terminId(), f.grupaId(), ucionica, indeks);
    }

    public List<Resenje> zaArhivu(ResenjeFilter f) {
        List<Resenje> predata = pretraga(f).stream().filter(Resenje::jePredato).toList();
        if (predata.isEmpty()) {
            throw ApiException.nijePronadjeno("Nema predatih resenja za zadate kriterijume");
        }
        return predata;
    }

    public void zapisiArhivu(List<Resenje> resenja, OutputStream izlaz) throws IOException {
        ZipOutputStream zip = new ZipOutputStream(izlaz);
        for (Resenje r : resenja) {
            Termin t = r.getZadatak().getGrupa().getTermin();
            String folder = bezbedno(t.getPredmet().getSifra() + "_termin-" + t.getId()) + "/"
                    + bezbedno(r.getZadatak().getGrupa().getNaziv()) + "/";
            String naziv = bezbedno(r.getStudent().getBrojIndeksa() + "_" + r.getStudent().getPrezime()
                    + "_" + r.getStudent().getIme()) + ".zip";
            zip.putNextEntry(new ZipEntry(folder + naziv));
            try (InputStream in = fileStorage.ucitaj(r.getPutanjaFajla()).getInputStream()) {
                in.transferTo(zip);
            }
            zip.closeEntry();
        }
        zip.finish();
    }

    public Resenje resenje(Long id) {
        return resenjeRepository.findById(id)
                .orElseThrow(() -> ApiException.nijePronadjeno("Ne postoji resenje " + id));
    }

    public Resenje predatoResenje(Long id) {
        Resenje r = resenje(id);
        if (!r.jePredato()) {
            throw ApiException.losZahtev("Student jos nije predao ovo resenje");
        }
        return r;
    }

    @Transactional
    public ResenjeDto dodeliPoene(Long resenjeId, int poeni) {
        Resenje r = predatoResenje(resenjeId);
        r.setPoeni(poeni);
        return ResenjeDto.od(resenjeRepository.save(r));
    }

    @Transactional
    public KomentarDto dodajKomentar(Long resenjeId, Long nastavnikId, KomentarZahtev z) {
        Komentar k = new Komentar();
        k.setResenje(predatoResenje(resenjeId));
        k.setNastavnik(korisnikRepository.getReferenceById(nastavnikId));
        k.setFajl(z.fajl().trim());
        k.setLinija(z.linija());
        k.setTekst(z.tekst());
        k.setKreiran(LocalDateTime.now());
        return KomentarDto.od(komentarRepository.save(k));
    }

    public List<KomentarDto> komentari(Long resenjeId) {
        resenje(resenjeId);
        return komentarRepository.findAllByResenjeIdOrderByFajlAscLinijaAsc(resenjeId).stream()
                .map(KomentarDto::od).toList();
    }

    @Transactional
    public void obrisiKomentar(Long komentarId) {
        if (!komentarRepository.existsById(komentarId)) {
            throw ApiException.nijePronadjeno("Ne postoji komentar " + komentarId);
        }
        komentarRepository.deleteById(komentarId);
    }

    public List<ResenjeDto> mojaResenja(Long studentId) {
        return resenjeRepository.findAllByStudentIdOrderByPreuzetoDesc(studentId).stream()
                .map(ResenjeDto::od).toList();
    }

    public Resenje mojeResenje(Long studentId, Long resenjeId) {
        Resenje r = resenje(resenjeId);
        if (!r.getStudent().getId().equals(studentId)) {
            throw ApiException.nijePronadjeno("Ne postoji resenje " + resenjeId);
        }
        return r;
    }

    public List<KomentarDto> mojiKomentari(Long studentId, Long resenjeId) {
        mojeResenje(studentId, resenjeId);
        return komentari(resenjeId);
    }

    private static String prazanUNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static String bezbedno(String s) {
        return s.replaceAll("[^A-Za-z0-9._-]+", "_");
    }
}
