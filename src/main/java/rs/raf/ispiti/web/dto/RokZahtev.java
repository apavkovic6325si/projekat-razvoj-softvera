package rs.raf.ispiti.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RokZahtev(@NotBlank String naziv, @NotBlank String skolskaGodina) {
}
