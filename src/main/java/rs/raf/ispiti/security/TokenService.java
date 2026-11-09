package rs.raf.ispiti.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import rs.raf.ispiti.model.Korisnik;

import java.time.Duration;
import java.time.Instant;

@Service
public class TokenService {
    public static final String CLAIM_ULOGA = "uloga";
    public static final String CLAIM_KORISNIK_ID = "korisnikId";

    private final JwtEncoder encoder;
    private final Duration trajanje;

    public TokenService(JwtEncoder encoder, @Value("${app.jwt.trajanje}") Duration trajanje) {
        this.encoder = encoder;
        this.trajanje = trajanje;
    }

    public record Token(String vrednost, Instant istice) {
    }

    public Token izdaj(Korisnik korisnik) {
        Instant sada = Instant.now();
        Instant istice = sada.plus(trajanje);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("raf-ispiti")
                .subject(korisnik.getEmail())
                .issuedAt(sada)
                .expiresAt(istice)
                .claim(CLAIM_ULOGA, korisnik.getUloga().name())
                .claim(CLAIM_KORISNIK_ID, korisnik.getId())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return new Token(encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue(), istice);
    }
}
