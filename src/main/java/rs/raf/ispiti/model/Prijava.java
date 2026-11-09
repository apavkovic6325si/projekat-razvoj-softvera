package rs.raf.ispiti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "termin_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Prijava {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Korisnik student;

    @ManyToOne(optional = false)
    private Termin termin;

    @ManyToOne(optional = false)
    private Grupa grupa;
}
