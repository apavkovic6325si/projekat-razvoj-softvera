package rs.raf.ispiti.web.student;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import rs.raf.ispiti.common.FileStorage;
import rs.raf.ispiti.model.Zadatak;
import rs.raf.ispiti.security.Prijavljen;
import rs.raf.ispiti.service.PolaganjeService;
import rs.raf.ispiti.web.FajlOdgovor;
import rs.raf.ispiti.web.dto.PolaganjeInfo;
import rs.raf.ispiti.web.dto.PolaganjeZahtev;
import rs.raf.ispiti.web.dto.PrijavaDto;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class PolaganjeController {

    private final PolaganjeService polaganjeService;
    private final FileStorage fileStorage;

    /** termini i grupe za koje je student na spisku (za izbor u editoru) */
    @GetMapping("/prijave")
    public List<PrijavaDto> mojePrijave(@AuthenticationPrincipal Jwt jwt) {
        return polaganjeService.mojePrijave(Prijavljen.id(jwt));
    }

    /** provera da li je student na spisku; 403 sa jasnom porukom ako nije */
    @PostMapping("/polaganje/provera")
    public PolaganjeInfo provera(@RequestBody @Valid PolaganjeZahtev zahtev, @AuthenticationPrincipal Jwt jwt) {
        return polaganjeService.info(Prijavljen.id(jwt), zahtev);
    }

    /** vraca .zip projekta; radi samo u toku termina */
    @PostMapping("/polaganje/preuzimanje")
    public ResponseEntity<Resource> preuzmi(@RequestBody @Valid PolaganjeZahtev zahtev, @AuthenticationPrincipal Jwt jwt) {
        Zadatak z = polaganjeService.preuzmi(Prijavljen.id(jwt), zahtev);
        return FajlOdgovor.zip(fileStorage.ucitaj(z.getPutanjaFajla()), z.getNazivFajla());
    }
}
