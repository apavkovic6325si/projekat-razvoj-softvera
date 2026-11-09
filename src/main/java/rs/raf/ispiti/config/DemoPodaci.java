package rs.raf.ispiti.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rs.raf.ispiti.common.FileStorage;
import rs.raf.ispiti.model.*;
import rs.raf.ispiti.repository.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DemoPodaci implements CommandLineRunner {

    private final KorisnikRepository korisnikRepository;
    private final PredmetRepository predmetRepository;
    private final IspitniRokRepository rokRepository;
    private final TerminRepository terminRepository;
    private final GrupaRepository grupaRepository;
    private final ZadatakRepository zadatakRepository;
    private final PrijavaRepository prijavaRepository;
    private final FileStorage fileStorage;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (korisnikRepository.count() > 0) return;

        Korisnik nastavnik = korisnik("Petar", "Petrovic", "nastavnik@raf.rs", "nastavnik123", Uloga.NASTAVNIK, null, null);
        Korisnik s1 = korisnik("Ana", "Anic", "student1@raf.rs", "student123", Uloga.STUDENT, "RN 1/2024", "aanic");
        Korisnik s2 = korisnik("Marko", "Markovic", "student2@raf.rs", "student123", Uloga.STUDENT, "RN 2/2024", "mmarkovic");
        korisnik("Jovana", "Jovic", "student3@raf.rs", "student123", Uloga.STUDENT, "RN 3/2024", "jjovic");

        Predmet predmet = new Predmet();
        predmet.setSifra("OOP");
        predmet.setNaziv("Objektno orijentisano programiranje");
        predmetRepository.save(predmet);

        IspitniRok rok = new IspitniRok();
        rok.setNaziv("Kolokvijumska nedelja 1");
        rok.setSkolskaGodina("2026/2027");
        rokRepository.save(rok);

        Termin termin = new Termin();
        termin.setPredmet(predmet);
        termin.setRok(rok);
        termin.setTip(TipProvere.KOLOKVIJUM);
        termin.setUcionica("RAF1");
        termin.setPocetak(LocalDateTime.now().minusMinutes(15));
        termin.setKraj(LocalDateTime.now().plusHours(3));
        terminRepository.save(termin);

        Grupa g1 = grupa(termin, "Grupa 1");
        Grupa g2 = grupa(termin, "Grupa 2");
        zadatak(g1, nastavnik, "Biblioteka", "Implementirati klasu Knjiga i pretragu po autoru.");
        zadatak(g2, nastavnik, "Prodavnica", "Implementirati korpu i racunanje ukupne cene.");

        prijava(s1, termin, g1);
        prijava(s2, termin, g2);

        log.info("Demo podaci ubaceni. Nalozi: nastavnik@raf.rs/nastavnik123, student1@raf.rs/student123 (Grupa 1), "
                + "student2@raf.rs/student123 (Grupa 2), student3@raf.rs/student123 (nije na spisku). Termin id={}", termin.getId());
    }

    private Korisnik korisnik(String ime, String prezime, String email, String lozinka, Uloga uloga,
                              String indeks, String nalog) {
        Korisnik k = new Korisnik();
        k.setIme(ime);
        k.setPrezime(prezime);
        k.setEmail(email);
        k.setLozinka(passwordEncoder.encode(lozinka));
        k.setUloga(uloga);
        k.setBrojIndeksa(indeks);
        k.setNalog(nalog);
        return korisnikRepository.save(k);
    }

    private Grupa grupa(Termin termin, String naziv) {
        Grupa g = new Grupa();
        g.setTermin(termin);
        g.setNaziv(naziv);
        return grupaRepository.save(g);
    }

    private void zadatak(Grupa grupa, Korisnik nastavnik, String naziv, String opis) {
        Zadatak z = new Zadatak();
        z.setGrupa(grupa);
        z.setNastavnik(nastavnik);
        z.setNaziv(naziv);
        z.setOpis(opis);
        z.setNazivFajla(naziv.toLowerCase() + ".zip");
        z.setPutanjaFajla(fileStorage.sacuvaj(new ByteArrayInputStream(primerProjekta(naziv, opis)), "zadaci"));
        z.setPostavljen(LocalDateTime.now());
        zadatakRepository.save(z);
    }

    private void prijava(Korisnik student, Termin termin, Grupa grupa) {
        Prijava p = new Prijava();
        p.setStudent(student);
        p.setTermin(termin);
        p.setGrupa(grupa);
        prijavaRepository.save(p);
    }

    private static byte[] primerProjekta(String naziv, String opis) {
        String main = """
                public class Main {
                    public static void main(String[] args) {
                        // TODO: %s
                        System.out.println("%s");
                    }
                }
                """.formatted(opis, naziv);
        try (ByteArrayOutputStream bajtovi = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(bajtovi)) {
            zip.putNextEntry(new ZipEntry("README.md"));
            zip.write(("# " + naziv + "\n\n" + opis + "\n").getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("src/Main.java"));
            zip.write(main.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.finish();
            return bajtovi.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
