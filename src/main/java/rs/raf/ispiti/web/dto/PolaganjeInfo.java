package rs.raf.ispiti.web.dto;

import java.time.LocalDateTime;

public record PolaganjeInfo(
        TerminDto termin,
        GrupaDto grupa,
        Long zadatakId,
        String nazivZadatka,
        String opisZadatka,
        Stanje stanje,
        LocalDateTime sada,
        long preostaloSekundi,
        LocalDateTime preuzeto,
        LocalDateTime predato) {

    public enum Stanje {
        NIJE_POCEO,
        U_TOKU,
        ZAVRSEN
    }
}
