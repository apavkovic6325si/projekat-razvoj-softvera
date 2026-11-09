package rs.raf.ispiti.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import rs.raf.ispiti.security.Prijavljen;
import rs.raf.ispiti.service.AuthService;
import rs.raf.ispiti.web.dto.KorisnikDto;
import rs.raf.ispiti.web.dto.LoginOdgovor;
import rs.raf.ispiti.web.dto.LoginZahtev;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public LoginOdgovor login(@RequestBody @Valid LoginZahtev zahtev) {
        return authService.login(zahtev.email(), zahtev.lozinka());
    }

    @GetMapping("/ja")
    public KorisnikDto ja(@AuthenticationPrincipal Jwt jwt) {
        return authService.ja(Prijavljen.id(jwt));
    }
}
