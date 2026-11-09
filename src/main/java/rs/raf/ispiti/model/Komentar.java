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
public class Komentar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Resenje resenje;

    @ManyToOne(optional = false)
    private Korisnik nastavnik;

    @Column(nullable = false)
    private String fajl;

    @Column(nullable = false)
    private int linija;

    @Column(nullable = false, length = 2000)
    private String tekst;

    @Column(nullable = false)
    private LocalDateTime kreiran;
}
