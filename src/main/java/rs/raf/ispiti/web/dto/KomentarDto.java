package rs.raf.ispiti.web.dto;

import rs.raf.ispiti.model.Komentar;

import java.time.LocalDateTime;

public record KomentarDto(Long id, Long resenjeId, String fajl, int linija, String tekst,
                          String nastavnik, LocalDateTime kreiran) {

    public static KomentarDto od(Komentar k) {
        return new KomentarDto(k.getId(), k.getResenje().getId(), k.getFajl(), k.getLinija(), k.getTekst(),
                k.getNastavnik().punoIme(), k.getKreiran());
    }
}
