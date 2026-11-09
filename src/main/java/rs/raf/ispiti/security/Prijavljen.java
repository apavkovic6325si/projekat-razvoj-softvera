package rs.raf.ispiti.security;

import org.springframework.security.oauth2.jwt.Jwt;

public final class Prijavljen {
    private Prijavljen() {
    }

    public static Long id(Jwt jwt) {
        Object id = jwt.getClaim(TokenService.CLAIM_KORISNIK_ID);
        return ((Number)id).longValue();
    }
}
