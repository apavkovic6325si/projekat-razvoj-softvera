package rs.raf.ispiti.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginZahtev(@NotBlank String email, @NotBlank String lozinka) {
}
