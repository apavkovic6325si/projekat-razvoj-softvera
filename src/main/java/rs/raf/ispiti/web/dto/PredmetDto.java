package rs.raf.ispiti.web.dto;

import rs.raf.ispiti.model.Predmet;

public record PredmetDto(Long id, String sifra, String naziv) {
    public static PredmetDto od(Predmet p) {
        return new PredmetDto(p.getId(), p.getSifra(), p.getNaziv());
    }
}
