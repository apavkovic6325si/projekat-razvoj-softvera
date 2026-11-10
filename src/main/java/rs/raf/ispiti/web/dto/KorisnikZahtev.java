package rs.raf.ispiti.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import rs.raf.ispiti.model.Uloga;

public record KorisnikZahtev(
        @NotBlank String ime,
        @NotBlank String prezime,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String lozinka,
        @NotNull Uloga uloga,
        String brojIndeksa,
        String nalog) {
}
