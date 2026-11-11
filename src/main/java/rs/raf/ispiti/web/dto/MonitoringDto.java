package rs.raf.ispiti.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MonitoringDto(
        TerminDto termin,
        LocalDateTime sada,
        int ukupnoPrijavljenih,
        int preuzeto,
        int predato,
        int uIzradi,
        Double prosecnoVremeIzradeMinuta,
        List<StudentStatus> studenti) {

    public enum Status {
        NIJE_PREUZEO,
        U_IZRADI,
        PREDAO
    }

    public record StudentStatus(Long studentId, String ime, String prezime, String brojIndeksa, Long grupaId, String nazivGrupe, Status status, LocalDateTime preuzeto, LocalDateTime predato, Long trajanjeMinuta) {
    }
}
