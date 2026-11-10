package rs.raf.ispiti.web.dto;

import rs.raf.ispiti.model.Resenje;
import rs.raf.ispiti.model.Zadatak;

import java.time.Duration;
import java.time.LocalDateTime;

public record ResenjeDto(
        Long id,
        KorisnikDto student,
        Long zadatakId,
        String nazivZadatka,
        Long grupaId,
        String nazivGrupe,
        TerminDto termin,
        LocalDateTime preuzeto,
        LocalDateTime predato,
        Long trajanjeMinuta,
        String nazivFajla,
        Integer poeni) {

    public static ResenjeDto od(Resenje r) {
        Zadatak z = r.getZadatak();
        Long trajanje = r.jePredato() ? Duration.between(r.getPreuzeto(), r.getPredato()).toMinutes() : null;
        return new ResenjeDto(r.getId(), KorisnikDto.od(r.getStudent()), z.getId(), z.getNaziv(),
                z.getGrupa().getId(), z.getGrupa().getNaziv(), TerminDto.od(z.getGrupa().getTermin()),
                r.getPreuzeto(), r.getPredato(), trajanje, r.getNazivFajla(), r.getPoeni());
    }
}
