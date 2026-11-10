package rs.raf.ispiti.web.dto;

import rs.raf.ispiti.model.Grupa;

public record GrupaDto(Long id, Long terminId, String naziv) {
    public static GrupaDto od(Grupa g) {
        return new GrupaDto(g.getId(), g.getTermin().getId(), g.getNaziv());
    }
}
