package rs.raf.ispiti.web.dto;

import rs.raf.ispiti.model.Korisnik;
import rs.raf.ispiti.model.Uloga;

public record KorisnikDto(Long id, String ime, String prezime, String email, Uloga uloga, String brojIndeksa, String nalog) {
    public static KorisnikDto od(Korisnik k) {
        return new KorisnikDto(k.getId(), k.getIme(), k.getPrezime(), k.getEmail(), k.getUloga(), k.getBrojIndeksa(), k.getNalog());
    }
}
