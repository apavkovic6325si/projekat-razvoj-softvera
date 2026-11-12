package rs.raf.ispiti.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.zip.ZipInputStream;

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
            if (!jeIspravanZip(bajtovi)) {
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

    public void zameniPosleTransakcije(String nova, String stara) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            obrisi(stara);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                obrisi(status == STATUS_COMMITTED ? stara : nova);
            }
        });
    }

    private static boolean jeIspravanZip(byte[] bajtovi) {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bajtovi))) {
            int fajlova = 0;
            while (zip.getNextEntry() != null) {
                zip.transferTo(OutputStream.nullOutputStream());
                fajlova++;
            }
            return fajlova > 0;
        } catch (IOException e) {
            return false;
        }
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
