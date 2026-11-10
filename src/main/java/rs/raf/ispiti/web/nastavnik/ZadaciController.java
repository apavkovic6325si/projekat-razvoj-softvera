package rs.raf.ispiti.web.nastavnik;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.raf.ispiti.common.FileStorage;
import rs.raf.ispiti.model.Zadatak;
import rs.raf.ispiti.security.Prijavljen;
import rs.raf.ispiti.service.ZadatakService;
import rs.raf.ispiti.web.FajlOdgovor;
import rs.raf.ispiti.web.dto.ZadatakDto;

import java.util.List;

@RestController
@RequestMapping("/api/nastavnik")
@RequiredArgsConstructor
public class ZadaciController {
    private final ZadatakService zadatakService;
    private final FileStorage fileStorage;

    @PostMapping(path = "/grupe/{grupaId}/zadatak", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ZadatakDto postavi(@PathVariable Long grupaId,
                              @RequestParam String naziv,
                              @RequestParam(required = false) String opis,
                              @RequestParam MultipartFile fajl,
                              @AuthenticationPrincipal Jwt jwt) {
        return zadatakService.postavi(grupaId, naziv, opis, fajl, Prijavljen.id(jwt));
    }

    @GetMapping("/termini/{terminId}/zadaci")
    public List<ZadatakDto> zaTermin(@PathVariable Long terminId) {
        return zadatakService.zaTermin(terminId);
    }

    @GetMapping("/zadaci/{zadatakId}/fajl")
    public ResponseEntity<Resource> fajl(@PathVariable Long zadatakId) {
        Zadatak z = zadatakService.zadatak(zadatakId);
        return FajlOdgovor.zip(fileStorage.ucitaj(z.getPutanjaFajla()), z.getNazivFajla());
    }
}
