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
public class Zadatak {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(unique = true)
    private Grupa grupa;

    @Column(nullable = false)
    private String naziv;

    @Column(length = 2000)
    private String opis;

    @ManyToOne(optional = false)
    private Korisnik nastavnik;

    @Column(nullable = false)
    private String nazivFajla;

    @Column(nullable = false)
    private String putanjaFajla;

    @Column(nullable = false)
    private LocalDateTime postavljen;
}
