package rs.raf.ispiti.web.nastavnik;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rs.raf.ispiti.service.MonitoringService;
import rs.raf.ispiti.web.dto.MonitoringDto;

@RestController
@RequestMapping("/api/nastavnik")
@RequiredArgsConstructor
public class MonitoringController {
    private final MonitoringService monitoringService;

    @GetMapping("/termini/{terminId}/monitoring")
    public MonitoringDto monitoring(@PathVariable Long terminId, @RequestParam(required = false) Long grupaId) {
        return monitoringService.zaTermin(terminId, grupaId);
    }
}
