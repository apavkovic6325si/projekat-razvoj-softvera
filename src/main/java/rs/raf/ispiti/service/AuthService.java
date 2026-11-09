package rs.raf.ispiti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rs.raf.ispiti.common.ApiException;
import rs.raf.ispiti.model.Korisnik;
import rs.raf.ispiti.repository.KorisnikRepository;
import rs.raf.ispiti.security.TokenService;
import rs.raf.ispiti.web.dto.KorisnikDto;
import rs.raf.ispiti.web.dto.LoginOdgovor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final KorisnikRepository korisnikRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public LoginOdgovor login(String email, String lozinka) {
        Korisnik korisnik = korisnikRepository.findByEmailIgnoreCase(email)
                .filter(k -> passwordEncoder.matches(lozinka, k.getLozinka()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Pogrešan email ili lozinka!"));
        TokenService.Token token = tokenService.izdaj(korisnik);
        return new LoginOdgovor(token.vrednost(), token.istice(), KorisnikDto.od(korisnik));
    }

    public KorisnikDto ja(Long korisnikId) {
        return korisnikRepository.findById(korisnikId)
                .map(KorisnikDto::od)
                .orElseThrow(() -> ApiException.nijePronadjeno("Korisnik više ne postoji!"));
    }
}
