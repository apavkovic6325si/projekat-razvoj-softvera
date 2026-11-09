package rs.raf.ispiti.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"termin_id", "naziv"}))
@Getter
@Setter
@NoArgsConstructor
public class Grupa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Termin termin;

    @Column(nullable = false)
    private String naziv;
}
