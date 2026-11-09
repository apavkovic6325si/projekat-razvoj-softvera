package rs.raf.ispiti.web.dto;

import java.time.Instant;

public record LoginOdgovor(String token, Instant istice, KorisnikDto korisnik) {
}
