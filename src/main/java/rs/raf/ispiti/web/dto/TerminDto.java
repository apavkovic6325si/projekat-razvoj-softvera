package rs.raf.ispiti.web.dto;

import rs.raf.ispiti.model.Termin;
import rs.raf.ispiti.model.TipProvere;

import java.time.LocalDateTime;

public record TerminDto(Long id, PredmetDto predmet, RokDto rok, TipProvere tip, String ucionica, LocalDateTime pocetak, LocalDateTime kraj) {
    public static TerminDto od(Termin t) {
        return new TerminDto(t.getId(), PredmetDto.od(t.getPredmet()), RokDto.od(t.getRok()), t.getTip(), t.getUcionica(), t.getPocetak(), t.getKraj());
    }
}
