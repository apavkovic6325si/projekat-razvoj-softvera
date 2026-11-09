package rs.raf.ispiti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Termin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Predmet predmet;

    @ManyToOne(optional = false)
    private IspitniRok rok;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipProvere tip;

    @Column(nullable = false)
    private String ucionica;

    @Column(nullable = false)
    private LocalDateTime pocetak;

    @Column(nullable = false)
    private LocalDateTime kraj;

    public boolean jeUToku(LocalDateTime trenutak) {
        return !trenutak.isBefore(pocetak) && !trenutak.isAfter(kraj);
    }
}
