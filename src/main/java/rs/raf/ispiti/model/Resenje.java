package rs.raf.ispiti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "zadatak_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Resenje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Korisnik student;

    @ManyToOne(optional = false)
    private Zadatak zadatak;

    @Column(nullable = false)
    private LocalDateTime preuzeto;

    private LocalDateTime predato;

    private String nazivFajla;

    private String putanjaFajla;

    private Integer poeni;

    public boolean jePredato() {
        return predato != null;
    }
}
