package rs.raf.ispiti.web.nastavnik;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import rs.raf.ispiti.service.PrijavaService;
import rs.raf.ispiti.web.dto.PrijavaDto;
import rs.raf.ispiti.web.dto.UnosSpiska;
import rs.raf.ispiti.web.dto.UnosSpiskaOdgovor;

import java.util.List;

@RestController
@RequestMapping("/api/nastavnik")
@RequiredArgsConstructor
public class PrijaveController {
    private final PrijavaService prijavaService;

    @PostMapping("/termini/{terminId}/prijave")
    public UnosSpiskaOdgovor unesi(@PathVariable Long terminId, @RequestBody @Valid UnosSpiska spisak) {
        return prijavaService.unesiSpisak(terminId, spisak.stavke());
    }

    @GetMapping("/termini/{terminId}/prijave")
    public List<PrijavaDto> spisak(@PathVariable Long terminId) {
        return prijavaService.spisak(terminId);
    }

    @DeleteMapping("/prijave/{prijavaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void obrisi(@PathVariable Long prijavaId) {
        prijavaService.obrisi(prijavaId);
    }
}
