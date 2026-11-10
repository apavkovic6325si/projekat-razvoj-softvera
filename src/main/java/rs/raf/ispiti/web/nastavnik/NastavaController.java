package rs.raf.ispiti.web.nastavnik;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import rs.raf.ispiti.service.NastavaService;
import rs.raf.ispiti.web.dto.*;

import java.util.List;

@RestController
@RequestMapping("/api/nastavnik")
@RequiredArgsConstructor
public class NastavaController {
    private final NastavaService nastavaService;

    @PostMapping("/predmeti")
    @ResponseStatus(HttpStatus.CREATED)
    public PredmetDto kreirajPredmet(@RequestBody @Valid PredmetZahtev zahtev) {
        return nastavaService.kreirajPredmet(zahtev);
    }

    @GetMapping("/predmeti")
    public List<PredmetDto> predmeti() {
        return nastavaService.predmeti();
    }

    @PostMapping("/rokovi")
    @ResponseStatus(HttpStatus.CREATED)
    public RokDto kreirajRok(@RequestBody @Valid RokZahtev zahtev) {
        return nastavaService.kreirajRok(zahtev);
    }

    @GetMapping("/rokovi")
    public List<RokDto> rokovi() {
        return nastavaService.rokovi();
    }

    @PostMapping("/termini")
    @ResponseStatus(HttpStatus.CREATED)
    public TerminDto kreirajTermin(@RequestBody @Valid TerminZahtev zahtev) {
        return nastavaService.kreirajTermin(zahtev);
    }

    @GetMapping("/termini")
    public List<TerminDto> termini(@RequestParam(required = false) Long predmetId, @RequestParam(required = false) Long rokId) {
        return nastavaService.termini(predmetId, rokId);
    }

    @GetMapping("/termini/{terminId}")
    public TerminDto termin(@PathVariable Long terminId) {
        return TerminDto.od(nastavaService.termin(terminId));
    }

    @PostMapping("/termini/{terminId}/grupe")
    @ResponseStatus(HttpStatus.CREATED)
    public GrupaDto kreirajGrupu(@PathVariable Long terminId, @RequestBody @Valid GrupaZahtev zahtev) {
        return nastavaService.kreirajGrupu(terminId, zahtev);
    }

    @GetMapping("/termini/{terminId}/grupe")
    public List<GrupaDto> grupe(@PathVariable Long terminId) {
        return nastavaService.grupe(terminId);
    }
}
