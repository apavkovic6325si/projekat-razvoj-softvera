package rs.raf.ispiti.web.dto;

import rs.raf.ispiti.model.Zadatak;

import java.time.LocalDateTime;

public record ZadatakDto(Long id, Long terminId, Long grupaId, String nazivGrupe, String naziv, String opis, String nazivFajla, String nastavnik, LocalDateTime postavljen) {
    public static ZadatakDto od(Zadatak z) {
        return new ZadatakDto(z.getId(), z.getGrupa().getTermin().getId(), z.getGrupa().getId(), z.getGrupa().getNaziv(), z.getNaziv(), z.getOpis(), z.getNazivFajla(), z.getNastavnik().punoIme(), z.getPostavljen());
    }
}
