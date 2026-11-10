package rs.raf.ispiti.web.dto;

import rs.raf.ispiti.model.Prijava;

public record PrijavaDto(Long id, KorisnikDto student, TerminDto termin, GrupaDto grupa) {
    public static PrijavaDto od(Prijava p) {
        return new PrijavaDto(p.getId(), KorisnikDto.od(p.getStudent()), TerminDto.od(p.getTermin()), GrupaDto.od(p.getGrupa()));
    }
}
