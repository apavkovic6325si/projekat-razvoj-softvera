package rs.raf.ispiti.web.nastavnik;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import rs.raf.ispiti.model.Uloga;
import rs.raf.ispiti.service.KorisnikService;
import rs.raf.ispiti.web.dto.KorisnikDto;
import rs.raf.ispiti.web.dto.KorisnikZahtev;

import java.util.List;

@RestController
@RequestMapping("/api/nastavnik/korisnici")
@RequiredArgsConstructor
public class KorisniciController {
    private final KorisnikService korisnikService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public KorisnikDto kreiraj(@RequestBody @Valid KorisnikZahtev zahtev) {
        return korisnikService.kreiraj(zahtev);
    }

    @GetMapping
    public List<KorisnikDto> lista(@RequestParam(required = false) Uloga uloga) {
        return korisnikService.lista(uloga);
    }
}
