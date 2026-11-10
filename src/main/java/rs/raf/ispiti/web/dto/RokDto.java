package rs.raf.ispiti.web.dto;

import rs.raf.ispiti.model.IspitniRok;

public record RokDto(Long id, String naziv, String skolskaGodina) {
    public static RokDto od(IspitniRok r) {
        return new RokDto(r.getId(), r.getNaziv(), r.getSkolskaGodina());
    }
}
