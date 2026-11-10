package rs.raf.ispiti.web.dto;

import jakarta.validation.constraints.NotBlank;

public record GrupaZahtev(@NotBlank String naziv) {
}
