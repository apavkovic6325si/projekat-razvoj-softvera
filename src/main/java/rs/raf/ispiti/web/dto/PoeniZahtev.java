package rs.raf.ispiti.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PoeniZahtev(@NotNull @Min(0) @Max(100) Integer poeni) {
}
