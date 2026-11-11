package rs.raf.ispiti;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import rs.raf.ispiti.model.*;
import rs.raf.ispiti.repository.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:raf_test;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.seed.enabled=false",
        "app.storage.dir=target/test-fajlovi"
})

@AutoConfigureMockMvc
class TokPolaganjaTest {
    @Autowired MockMvc mvc;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired KorisnikRepository korisnici;
    @Autowired PredmetRepository predmeti;
    @Autowired IspitniRokRepository rokovi;
    @Autowired TerminRepository termini;
    @Autowired GrupaRepository grupe;
    @Autowired ZadatakRepository zadaci;
    @Autowired PrijavaRepository prijave;
    @Autowired ResenjeRepository resenja;
    @Autowired KomentarRepository komentari;

    Korisnik nastavnik;
    Korisnik ana;
    Korisnik marko;
    Termin termin;
    Grupa grupa1;
    Grupa grupa2;

    @BeforeEach
    void pripremi() {
        komentari.deleteAll();
        resenja.deleteAll();
        prijave.deleteAll();
        zadaci.deleteAll();
        grupe.deleteAll();
        termini.deleteAll();
        rokovi.deleteAll();
        predmeti.deleteAll();
        korisnici.deleteAll();

        nastavnik = korisnik("nastavnik@raf.rs", Uloga.NASTAVNIK, null);
        ana = korisnik("ana@raf.rs", Uloga.STUDENT, "RN 1/2024");
        marko = korisnik("marko@raf.rs", Uloga.STUDENT, "RN 2/2024");

        termin = termin(LocalDateTime.now().minusMinutes(30), LocalDateTime.now().plusHours(2));
        grupa1 = grupa(termin, "Grupa 1");
        grupa2 = grupa(termin, "Grupa 2");
        prijava(ana, termin, grupa1);
    }

    @Test
    void ceoTokPolaganja() throws Exception {
        String tNastavnik = login("nastavnik@raf.rs");
        String tAna = login("ana@raf.rs");

        String zadatakJson = mvc.perform(multipart("/api/nastavnik/grupe/{id}/zadatak", grupa1.getId())
                        .file(zip("fajl", "zadatak.zip"))
                        .param("naziv", "Biblioteka")
                        .param("opis", "Napraviti klasu Knjiga")
                        .header("Authorization", tNastavnik))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer zadatakId = JsonPath.read(zadatakJson, "$.id");

        String polaganje = """
                {"terminId": %d, "grupaId": %d, "brojIndeksa": "rn 1/2024"}
                """.formatted(termin.getId(), grupa1.getId());
        mvc.perform(post("/api/student/polaganje/provera").header("Authorization", tAna)
                        .contentType(MediaType.APPLICATION_JSON).content(polaganje))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stanje").value("U_TOKU"))
                .andExpect(jsonPath("$.zadatakId").value(zadatakId));

        byte[] preuzet = mvc.perform(post("/api/student/polaganje/preuzimanje").header("Authorization", tAna)
                        .contentType(MediaType.APPLICATION_JSON).content(polaganje))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"))
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(new String(preuzet, 0, 2)).isEqualTo("PK");

        mvc.perform(multipart("/api/student/polaganje/predaja")
                        .file(zip("fajl", "resenje.zip"))
                        .param("zadatakId", zadatakId.toString())
                        .header("Authorization", tAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.predato", notNullValue()));

        mvc.perform(get("/api/nastavnik/termini/{id}/monitoring", termin.getId()).header("Authorization", tNastavnik))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ukupnoPrijavljenih").value(1))
                .andExpect(jsonPath("$.preuzeto").value(1))
                .andExpect(jsonPath("$.predato").value(1))
                .andExpect(jsonPath("$.studenti[0].status").value("PREDAO"));

        String lista = mvc.perform(get("/api/nastavnik/resenja")
                        .param("terminId", termin.getId().toString())
                        .param("ucionica", "RAF1")
                        .header("Authorization", tNastavnik))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andReturn().getResponse().getContentAsString();
        Integer resenjeId = JsonPath.read(lista, "$[0].id");

        mvc.perform(get("/api/nastavnik/resenja").param("ucionica", "NEMA").header("Authorization", tNastavnik))
                .andExpect(jsonPath("$.length()").value(0));

        byte[] arhiva = mvc.perform(get("/api/nastavnik/resenja/arhiva")
                        .param("predmetId", termin.getPredmet().getId().toString())
                        .header("Authorization", tNastavnik))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(new String(arhiva, 0, 2)).isEqualTo("PK");

        mvc.perform(put("/api/nastavnik/resenja/{id}/poeni", resenjeId).header("Authorization", tNastavnik)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"poeni\": 85}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/nastavnik/resenja/{id}/komentari", resenjeId).header("Authorization", tNastavnik)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fajl\": \"src/Main.java\", \"linija\": 3, \"tekst\": \"Nedostaje provera null\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/student/resenja").header("Authorization", tAna))
                .andExpect(jsonPath("$[0].poeni").value(85));
        mvc.perform(get("/api/student/resenja/{id}/komentari", resenjeId).header("Authorization", tAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].linija").value(3))
                .andExpect(jsonPath("$[0].fajl").value("src/Main.java"));
    }

    @Test
    void studentKojiNijeNaSpiskuNeMozeDaPreuzme() throws Exception {
        zadatak(grupa1);
        String tMarko = login("marko@raf.rs");
        String polaganje = """
                {"terminId": %d, "grupaId": %d, "brojIndeksa": "RN 2/2024"}
                """.formatted(termin.getId(), grupa1.getId());

        mvc.perform(post("/api/student/polaganje/provera").header("Authorization", tMarko)
                        .contentType(MediaType.APPLICATION_JSON).content(polaganje))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Niste na spisku za polaganje u ovom terminu"));
        mvc.perform(post("/api/student/polaganje/preuzimanje").header("Authorization", tMarko)
                        .contentType(MediaType.APPLICATION_JSON).content(polaganje))
                .andExpect(status().isForbidden());
    }

    @Test
    void pogresnaGrupaIliTudjiIndeksSuOdbijeni() throws Exception {
        String tAna = login("ana@raf.rs");
        mvc.perform(post("/api/student/polaganje/provera").header("Authorization", tAna)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"terminId\": %d, \"grupaId\": %d, \"brojIndeksa\": \"RN 1/2024\"}"
                                .formatted(termin.getId(), grupa2.getId())))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/student/polaganje/provera").header("Authorization", tAna)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"terminId\": %d, \"grupaId\": %d, \"brojIndeksa\": \"RN 2/2024\"}"
                                .formatted(termin.getId(), grupa1.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void vanTerminaNemaPreuzimanjaNiPredaje() throws Exception {
        Termin prosli = termin(LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(1));
        Grupa g = grupa(prosli, "Grupa 1");
        Zadatak z = zadatak(g);
        prijava(ana, prosli, g);
        Resenje r = new Resenje();
        r.setStudent(ana);
        r.setZadatak(z);
        r.setPreuzeto(LocalDateTime.now().minusHours(2));
        resenja.save(r);

        String tAna = login("ana@raf.rs");
        mvc.perform(post("/api/student/polaganje/preuzimanje").header("Authorization", tAna)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"terminId\": %d, \"grupaId\": %d, \"brojIndeksa\": \"RN 1/2024\"}"
                                .formatted(prosli.getId(), g.getId())))
                .andExpect(status().isForbidden());
        mvc.perform(multipart("/api/student/polaganje/predaja")
                        .file(zip("fajl", "resenje.zip"))
                        .param("zadatakId", z.getId().toString())
                        .header("Authorization", tAna))
                .andExpect(status().isForbidden());
    }

    @Test
    void svakaUlogaVidiSamoSvojeEndpointe() throws Exception {
        String tAna = login("ana@raf.rs");
        String tNastavnik = login("nastavnik@raf.rs");

        mvc.perform(get("/api/nastavnik/predmeti").header("Authorization", tAna)).andExpect(status().isForbidden());
        mvc.perform(get("/api/student/resenja").header("Authorization", tNastavnik)).andExpect(status().isForbidden());
        mvc.perform(get("/api/nastavnik/predmeti")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"ana@raf.rs\", \"lozinka\": \"pogresna\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/ja").header("Authorization", tAna))
                .andExpect(jsonPath("$.brojIndeksa").value("RN 1/2024"))
                .andExpect(jsonPath("$.lozinka").doesNotExist());
    }

    @Test
    void studentNeVidiTudjeResenjeNiKomentare() throws Exception {
        Zadatak z = zadatak(grupa1);
        Resenje anino = new Resenje();
        anino.setStudent(ana);
        anino.setZadatak(z);
        anino.setPreuzeto(LocalDateTime.now());
        resenja.save(anino);

        String tMarko = login("marko@raf.rs");
        mvc.perform(get("/api/student/resenja/{id}/komentari", anino.getId()).header("Authorization", tMarko))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/student/resenja").header("Authorization", tMarko))
                .andExpect(jsonPath("$.length()").value(0));
    }

    private String login(String email) throws Exception {
        String odgovor = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"%s\", \"lozinka\": \"lozinka123\"}".formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(odgovor, "$.token");
    }

    private Korisnik korisnik(String email, Uloga uloga, String indeks) {
        Korisnik k = new Korisnik();
        k.setIme(email.substring(0, email.indexOf('@')));
        k.setPrezime("Test");
        k.setEmail(email);
        k.setLozinka(passwordEncoder.encode("lozinka123"));
        k.setUloga(uloga);
        k.setBrojIndeksa(indeks);
        return korisnici.save(k);
    }

    private Termin termin(LocalDateTime pocetak, LocalDateTime kraj) {
        Predmet p = new Predmet();
        p.setSifra("OOP" + System.nanoTime());
        p.setNaziv("Objektno orijentisano programiranje");
        predmeti.save(p);
        IspitniRok r = new IspitniRok();
        r.setNaziv("Januarski");
        r.setSkolskaGodina("2026/2027");
        rokovi.save(r);
        Termin t = new Termin();
        t.setPredmet(p);
        t.setRok(r);
        t.setTip(TipProvere.KOLOKVIJUM);
        t.setUcionica("RAF1");
        t.setPocetak(pocetak);
        t.setKraj(kraj);
        return termini.save(t);
    }

    private Grupa grupa(Termin t, String naziv) {
        Grupa g = new Grupa();
        g.setTermin(t);
        g.setNaziv(naziv);
        return grupe.save(g);
    }

    private void prijava(Korisnik s, Termin t, Grupa g) {
        Prijava p = new Prijava();
        p.setStudent(s);
        p.setTermin(t);
        p.setGrupa(g);
        prijave.save(p);
    }

    private Zadatak zadatak(Grupa g) {
        Zadatak z = new Zadatak();
        z.setGrupa(g);
        z.setNaziv("Zadatak");
        z.setNastavnik(nastavnik);
        z.setNazivFajla("zadatak.zip");
        z.setPutanjaFajla("zadaci/ne-postoji.zip");
        z.setPostavljen(LocalDateTime.now());
        return zadaci.save(z);
    }

    private static MockMultipartFile zip(String polje, String naziv) throws IOException {
        ByteArrayOutputStream bajtovi = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bajtovi)) {
            zip.putNextEntry(new ZipEntry("src/Main.java"));
            zip.write("public class Main {}".getBytes());
            zip.closeEntry();
        }
        return new MockMultipartFile(polje, naziv, "application/zip", bajtovi.toByteArray());
    }
}
