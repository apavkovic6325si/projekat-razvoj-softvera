package rs.raf.ispiti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rs.raf.ispiti.common.ApiException;
import rs.raf.ispiti.common.FileStorage;
import rs.raf.ispiti.model.Grupa;
import rs.raf.ispiti.model.Zadatak;
import rs.raf.ispiti.repository.KorisnikRepository;
import rs.raf.ispiti.repository.ZadatakRepository;
import rs.raf.ispiti.web.dto.ZadatakDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ZadatakService {
    private final ZadatakRepository zadatakRepository;
    private final KorisnikRepository korisnikRepository;
    private final NastavaService nastavaService;
    private final FileStorage fileStorage;

    @Transactional
    public ZadatakDto postavi(Long grupaId, String naziv, String opis, MultipartFile fajl, Long nastavnikId) {
        if (naziv == null || naziv.isBlank()) {
            throw ApiException.losZahtev("Naziv zadatka je obavezan!");
        }
        Grupa grupa = nastavaService.grupa(grupaId);
        String putanja = fileStorage.sacuvaj(fajl, "zadaci");

        Zadatak z = zadatakRepository.findByGrupaId(grupaId).orElseGet(Zadatak::new);
        String staraPutanja = z.getPutanjaFajla();
        z.setGrupa(grupa);
        z.setNaziv(naziv.trim());
        z.setOpis(opis);
        z.setNastavnik(korisnikRepository.getReferenceById(nastavnikId));
        z.setNazivFajla(fajl.getOriginalFilename());
        z.setPutanjaFajla(putanja);
        z.setPostavljen(LocalDateTime.now());
        Zadatak sacuvan = zadatakRepository.save(z);
        fileStorage.obrisi(staraPutanja);
        return ZadatakDto.od(sacuvan);
    }

    public List<ZadatakDto> zaTermin(Long terminId) {
        return zadatakRepository.findAllByGrupaTerminIdOrderByGrupaNaziv(terminId).stream().map(ZadatakDto::od).toList();
    }

    public Zadatak zadatak(Long id) {
        return zadatakRepository.findById(id).orElseThrow(() -> ApiException.nijePronadjeno("Ne postoji zadatak " + id));
    }
}
