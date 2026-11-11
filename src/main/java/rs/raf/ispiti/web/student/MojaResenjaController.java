package rs.raf.ispiti.web.student;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import rs.raf.ispiti.common.ApiException;
import rs.raf.ispiti.common.FileStorage;
import rs.raf.ispiti.model.Resenje;
import rs.raf.ispiti.security.Prijavljen;
import rs.raf.ispiti.service.OcenjivanjeService;
import rs.raf.ispiti.web.FajlOdgovor;
import rs.raf.ispiti.web.dto.KomentarDto;
import rs.raf.ispiti.web.dto.ResenjeDto;

import java.util.List;

@RestController
@RequestMapping("/api/student/resenja")
@RequiredArgsConstructor
public class MojaResenjaController {

    private final OcenjivanjeService ocenjivanjeService;
    private final FileStorage fileStorage;

    @GetMapping
    public List<ResenjeDto> mojaResenja(@AuthenticationPrincipal Jwt jwt) {
        return ocenjivanjeService.mojaResenja(Prijavljen.id(jwt));
    }

    @GetMapping("/{resenjeId}/komentari")
    public List<KomentarDto> komentari(@PathVariable Long resenjeId, @AuthenticationPrincipal Jwt jwt) {
        return ocenjivanjeService.mojiKomentari(Prijavljen.id(jwt), resenjeId);
    }

    @GetMapping("/{resenjeId}/fajl")
    public ResponseEntity<Resource> fajl(@PathVariable Long resenjeId, @AuthenticationPrincipal Jwt jwt) {
        Resenje r = ocenjivanjeService.mojeResenje(Prijavljen.id(jwt), resenjeId);
        if (!r.jePredato()) {
            throw ApiException.losZahtev("Resenje jos nije predato");
        }
        return FajlOdgovor.zip(fileStorage.ucitaj(r.getPutanjaFajla()), r.getNazivFajla());
    }
}
