package rs.raf.ispiti.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PolaganjeZahtev(@NotNull Long terminId, @NotNull Long grupaId, @NotBlank String brojIndeksa) {
}
