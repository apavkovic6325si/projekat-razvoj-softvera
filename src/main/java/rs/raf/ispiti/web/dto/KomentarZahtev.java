package rs.raf.ispiti.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record KomentarZahtev(@NotBlank String fajl, @NotNull @Min(1) Integer linija,
                             @NotBlank @Size(max = 2000) String tekst) {
}
