package rs.raf.ispiti.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UnosSpiska(@NotEmpty List<@Valid PrijavaStavka> stavke) {
}
