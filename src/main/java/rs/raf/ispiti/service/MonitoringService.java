package rs.raf.ispiti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.raf.ispiti.model.Korisnik;
import rs.raf.ispiti.model.Prijava;
import rs.raf.ispiti.model.Resenje;
import rs.raf.ispiti.model.Termin;
import rs.raf.ispiti.repository.PrijavaRepository;
import rs.raf.ispiti.repository.ResenjeRepository;
import rs.raf.ispiti.web.dto.MonitoringDto;
import rs.raf.ispiti.web.dto.MonitoringDto.Status;
import rs.raf.ispiti.web.dto.MonitoringDto.StudentStatus;
import rs.raf.ispiti.web.dto.TerminDto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonitoringService {
    private final NastavaService nastavaService;
    private final PrijavaRepository prijavaRepository;
    private final ResenjeRepository resenjeRepository;

    public MonitoringDto zaTermin(Long terminId, Long grupaId) {
        Termin termin = nastavaService.termin(terminId);
        List<Prijava> prijave = prijavaRepository.findAllByTerminIdOrderByGrupaNazivAscStudentPrezimeAsc(terminId)
                .stream()
                .filter(p -> grupaId == null || p.getGrupa().getId().equals(grupaId))
                .toList();
        Map<Long, Resenje> resenjePoStudentu = resenjeRepository.findAllByZadatakGrupaTerminId(terminId).stream()
                .collect(Collectors.toMap(r -> r.getStudent().getId(), Function.identity(), (a, b) -> a));

        List<StudentStatus> studenti = prijave.stream().map(p -> {
            Korisnik s = p.getStudent();
            Resenje r = resenjePoStudentu.get(s.getId());
            Status status = r == null ? Status.NIJE_PREUZEO : r.jePredato() ? Status.PREDAO : Status.U_IZRADI;
            Long trajanje = r != null && r.jePredato() ? Duration.between(r.getPreuzeto(), r.getPredato()).toMinutes() : null;
            return new StudentStatus(s.getId(), s.getIme(), s.getPrezime(), s.getBrojIndeksa(),
                    p.getGrupa().getId(), p.getGrupa().getNaziv(), status,
                    r == null ? null : r.getPreuzeto(), r == null ? null : r.getPredato(), trajanje);
        }).toList();

        int predato = (int) studenti.stream().filter(s -> s.status() == Status.PREDAO).count();
        int uIzradi = (int) studenti.stream().filter(s -> s.status() == Status.U_IZRADI).count();
        Double prosek = studenti.stream()
                .filter(s -> s.status() == Status.PREDAO)
                .mapToDouble(s -> Duration.between(s.preuzeto(), s.predato()).toSeconds() / 60.0)
                .average()
                .stream().boxed().findFirst().orElse(null);
        Double zaokruzen = prosek == null ? null : Math.round(prosek * 10) / 10.0;

        return new MonitoringDto(TerminDto.od(termin), LocalDateTime.now(), studenti.size(),
                predato + uIzradi, predato, uIzradi, zaokruzen, studenti);
    }
}
