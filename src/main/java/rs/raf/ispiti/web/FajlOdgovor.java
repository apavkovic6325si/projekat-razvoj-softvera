package rs.raf.ispiti.web;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

public final class FajlOdgovor {
    private FajlOdgovor() {
    }

    public static ResponseEntity<Resource> zip(Resource fajl, String naziv) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/zip")).header(HttpHeaders.CONTENT_DISPOSITION, dispozicija(naziv)).body(fajl);
    }

    public static String dispozicija(String naziv) {
        return ContentDisposition.attachment().filename(naziv, StandardCharsets.UTF_8).build().toString();
    }
}
