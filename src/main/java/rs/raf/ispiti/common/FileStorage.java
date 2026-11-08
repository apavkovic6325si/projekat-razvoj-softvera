package rs.raf.ispiti.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FileStorage {

    private final Path koren;

    public FileStorage(@Value("${app.storage.dir}") String folder) {
        this.koren = Path.of(folder).toAbsolutePath().normalize();
    }

    public String sacuvaj(MultipartFile fajl, String podfolder) {
        if (fajl == null || fajl.isEmpty()) {
            throw ApiException.losZahtev("Fajl nije poslat ili je prazan");
        }
        String naziv = fajl.getOriginalFilename();
        if (naziv == null || !naziv.toLowerCase().endsWith(".zip")) {
            throw ApiException.losZahtev("Projekat mora biti poslat kao .zip arhiva");
        }
        try (InputStream in = fajl.getInputStream()) {
            return sacuvaj(in, podfolder);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String sacuvaj(InputStream sadrzaj, String podfolder) {
        try {
            byte[] bajtovi = sadrzaj.readAllBytes();
            if (bajtovi.length < 4 || bajtovi[0] != 'P' || bajtovi[1] != 'K') {
                throw ApiException.losZahtev("Fajl nije ispravna .zip arhiva");
            }
            String relativna = podfolder + "/" + UUID.randomUUID() + ".zip";
            Path cilj = koren.resolve(relativna);
            Files.createDirectories(cilj.getParent());
            Files.write(cilj, bajtovi);
            return relativna;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Resource ucitaj(String relativna) {
        Path putanja = koren.resolve(relativna).normalize();
        if (!putanja.startsWith(koren) || !Files.exists(putanja)) {
            throw ApiException.nijePronadjeno("Fajl ne postoji na serveru");
        }
        return new FileSystemResource(putanja);
    }

    public void obrisi(String relativna) {
        if (relativna == null) return;
        try {
            Path putanja = koren.resolve(relativna).normalize();
            if (putanja.startsWith(koren)) Files.deleteIfExists(putanja);
        } catch (IOException ignored) {
        }
    }
}
