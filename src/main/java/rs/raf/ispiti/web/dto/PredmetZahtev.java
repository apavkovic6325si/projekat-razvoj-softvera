package rs.raf.ispiti.web.dto;

import jakarta.validation.constraints.NotBlank;

public record PredmetZahtev(@NotBlank String sifra, @NotBlank String naziv) {
}
