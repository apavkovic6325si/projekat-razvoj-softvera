package rs.raf.ispiti.web.student;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.raf.ispiti.common.FileStorage;
import rs.raf.ispiti.model.Zadatak;
import rs.raf.ispiti.security.Prijavljen;
import rs.raf.ispiti.service.PolaganjeService;
import rs.raf.ispiti.web.FajlOdgovor;
import rs.raf.ispiti.web.dto.PolaganjeInfo;
import rs.raf.ispiti.web.dto.PolaganjeZahtev;
import rs.raf.ispiti.web.dto.PrijavaDto;
import rs.raf.ispiti.web.dto.ResenjeDto;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class PolaganjeController {

    private final PolaganjeService polaganjeService;
    private final FileStorage fileStorage;

    @GetMapping("/prijave")
    public List<PrijavaDto> mojePrijave(@AuthenticationPrincipal Jwt jwt) {
        return polaganjeService.mojePrijave(Prijavljen.id(jwt));
    }

    @PostMapping("/polaganje/provera")
    public PolaganjeInfo provera(@RequestBody @Valid PolaganjeZahtev zahtev, @AuthenticationPrincipal Jwt jwt) {
        return polaganjeService.info(Prijavljen.id(jwt), zahtev);
    }

    @PostMapping("/polaganje/preuzimanje")
    public ResponseEntity<Resource> preuzmi(@RequestBody @Valid PolaganjeZahtev zahtev, @AuthenticationPrincipal Jwt jwt) {
        Zadatak z = polaganjeService.preuzmi(Prijavljen.id(jwt), zahtev);
        return FajlOdgovor.zip(fileStorage.ucitaj(z.getPutanjaFajla()), z.getNazivFajla());
    }

    @PostMapping(path = "/polaganje/predaja", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResenjeDto predaj(@RequestParam Long zadatakId, @RequestParam MultipartFile fajl,
                             @AuthenticationPrincipal Jwt jwt) {
        return polaganjeService.predaj(Prijavljen.id(jwt), zadatakId, fajl);
    }
}
