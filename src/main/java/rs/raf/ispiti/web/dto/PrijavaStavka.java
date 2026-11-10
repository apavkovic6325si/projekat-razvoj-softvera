package rs.raf.ispiti.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PrijavaStavka(@NotBlank String brojIndeksa, @NotNull Long grupaId) {
}
