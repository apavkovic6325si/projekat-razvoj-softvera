package rs.raf.ispiti.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rs.raf.ispiti.model.TipProvere;

import java.time.LocalDateTime;

public record TerminZahtev(
        @NotNull Long predmetId,
        @NotNull Long rokId,
        @NotNull TipProvere tip,
        @NotBlank String ucionica,
        @NotNull LocalDateTime pocetak,
        @NotNull LocalDateTime kraj) {
}
