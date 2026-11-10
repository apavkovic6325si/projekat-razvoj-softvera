package rs.raf.ispiti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.raf.ispiti.common.ApiException;
import rs.raf.ispiti.model.*;
import rs.raf.ispiti.repository.GrupaRepository;
import rs.raf.ispiti.repository.IspitniRokRepository;
import rs.raf.ispiti.repository.PredmetRepository;
import rs.raf.ispiti.repository.TerminRepository;
import rs.raf.ispiti.web.dto.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NastavaService {
    private final PredmetRepository predmetRepository;
    private final IspitniRokRepository rokRepository;
    private final TerminRepository terminRepository;
    private final GrupaRepository grupaRepository;

    @Transactional
    public PredmetDto kreirajPredmet(PredmetZahtev z) {
        Predmet p = new Predmet();
        p.setSifra(z.sifra().trim());
        p.setNaziv(z.naziv().trim());
        return PredmetDto.od(predmetRepository.save(p));
    }

    public List<PredmetDto> predmeti() {
        return predmetRepository.findAll().stream().map(PredmetDto::od).toList();
    }

    @Transactional
    public RokDto kreirajRok(RokZahtev z) {
        IspitniRok r = new IspitniRok();
        r.setNaziv(z.naziv().trim());
        r.setSkolskaGodina(z.skolskaGodina().trim());
        return RokDto.od(rokRepository.save(r));
    }

    public List<RokDto> rokovi() {
        return rokRepository.findAll().stream().map(RokDto::od).toList();
    }

    @Transactional
    public TerminDto kreirajTermin(TerminZahtev z) {
        if (!z.kraj().isAfter(z.pocetak())) {
            throw ApiException.losZahtev("Kraj termina mora biti nakon početka!");
        }
        Termin t = new Termin();
        t.setPredmet(predmetRepository.findById(z.predmetId())
                .orElseThrow(() -> ApiException.nijePronadjeno("Ne postoji predmet " + z.predmetId())));
        t.setRok(rokRepository.findById(z.rokId())
                .orElseThrow(() -> ApiException.nijePronadjeno("Ne postoji ispitni rok " + z.rokId())));
        t.setTip(z.tip());
        t.setUcionica(z.ucionica().trim());
        t.setPocetak(z.pocetak());
        t.setKraj(z.kraj());
        return TerminDto.od(terminRepository.save(t));
    }

    public List<TerminDto> termini(Long predmetId, Long rokId) {
        return terminRepository.pretraga(predmetId, rokId).stream().map(TerminDto::od).toList();
    }

    public Termin termin(Long id) {
        return terminRepository.findById(id)
                .orElseThrow(() -> ApiException.nijePronadjeno("Ne postoji termin " + id));
    }

    @Transactional
    public GrupaDto kreirajGrupu(Long terminId, GrupaZahtev z) {
        Grupa g = new Grupa();
        g.setTermin(termin(terminId));
        g.setNaziv(z.naziv().trim());
        return GrupaDto.od(grupaRepository.save(g));
    }

    public List<GrupaDto> grupe(Long terminId) {
        termin(terminId);
        return grupaRepository.findAllByTerminIdOrderByNaziv(terminId).stream().map(GrupaDto::od).toList();
    }

    public Grupa grupa(Long id) {
        return grupaRepository.findById(id)
                .orElseThrow(() -> ApiException.nijePronadjeno("Ne postoji grupa " + id));
    }
}
