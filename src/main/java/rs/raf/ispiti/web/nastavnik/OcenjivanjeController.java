package rs.raf.ispiti.web.nastavnik;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import rs.raf.ispiti.common.FileStorage;
import rs.raf.ispiti.model.Resenje;
import rs.raf.ispiti.security.Prijavljen;
import rs.raf.ispiti.service.OcenjivanjeService;
import rs.raf.ispiti.web.FajlOdgovor;
import rs.raf.ispiti.web.dto.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/nastavnik")
@RequiredArgsConstructor
public class OcenjivanjeController {

    private final OcenjivanjeService ocenjivanjeService;
    private final FileStorage fileStorage;

    @GetMapping("/resenja")
    public List<ResenjeDto> pretraga(@ModelAttribute ResenjeFilter filter) {
        return ocenjivanjeService.pretraga(filter).stream().map(ResenjeDto::od).toList();
    }

    @GetMapping("/resenja/arhiva")
    public void arhiva(@ModelAttribute ResenjeFilter filter, HttpServletResponse odgovor) throws IOException {
        List<Resenje> resenja = ocenjivanjeService.zaArhivu(filter);
        odgovor.setContentType("application/zip");
        odgovor.setHeader(HttpHeaders.CONTENT_DISPOSITION, FajlOdgovor.dispozicija("resenja.zip"));
        ocenjivanjeService.zapisiArhivu(resenja, odgovor.getOutputStream());
    }

    @GetMapping("/resenja/{resenjeId}")
    public ResenjeDto resenje(@PathVariable Long resenjeId) {
        return ResenjeDto.od(ocenjivanjeService.resenje(resenjeId));
    }

    @GetMapping("/resenja/{resenjeId}/fajl")
    public ResponseEntity<Resource> fajl(@PathVariable Long resenjeId) {
        Resenje r = ocenjivanjeService.predatoResenje(resenjeId);
        return FajlOdgovor.zip(fileStorage.ucitaj(r.getPutanjaFajla()), r.getNazivFajla());
    }

    @PutMapping("/resenja/{resenjeId}/poeni")
    public ResenjeDto poeni(@PathVariable Long resenjeId, @RequestBody @Valid PoeniZahtev zahtev) {
        return ocenjivanjeService.dodeliPoene(resenjeId, zahtev.poeni());
    }

    @PostMapping("/resenja/{resenjeId}/komentari")
    @ResponseStatus(HttpStatus.CREATED)
    public KomentarDto dodajKomentar(@PathVariable Long resenjeId, @RequestBody @Valid KomentarZahtev zahtev,
                                     @AuthenticationPrincipal Jwt jwt) {
        return ocenjivanjeService.dodajKomentar(resenjeId, Prijavljen.id(jwt), zahtev);
    }

    @GetMapping("/resenja/{resenjeId}/komentari")
    public List<KomentarDto> komentari(@PathVariable Long resenjeId) {
        return ocenjivanjeService.komentari(resenjeId);
    }

    @DeleteMapping("/komentari/{komentarId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void obrisiKomentar(@PathVariable Long komentarId) {
        ocenjivanjeService.obrisiKomentar(komentarId);
    }
}
