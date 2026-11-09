package rs.raf.ispiti.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import rs.raf.ispiti.model.Uloga;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/nastavnik/**").hasRole(Uloga.NASTAVNIK.name())
                        .requestMatchers("/api/student/**").hasRole(Uloga.STUDENT.name())
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o.jwt(jwt -> jwt.jwtAuthenticationConverter(ulogaIzTokena())));
        return http.build();
    }

    private JwtAuthenticationConverter ulogaIzTokena() {
        JwtGrantedAuthoritiesConverter uloge = new JwtGrantedAuthoritiesConverter();
        uloge.setAuthoritiesClaimName(TokenService.CLAIM_ULOGA);
        uloge.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(uloge);
        return converter;
    }

    @Bean
    public SecretKey jwtKljuc(@Value("${app.jwt.secret}") String tajna) {
        byte[] bajtovi = tajna.getBytes(StandardCharsets.UTF_8);
        if (bajtovi.length < 32) {
            throw new IllegalStateException("app.jwt.secret mora imati najmanje 32 bajta!");
        }
        return new SecretKeySpec(bajtovi, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtKljuc) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtKljuc));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtKljuc) {
        return NimbusJwtDecoder.withSecretKey(jwtKljuc).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origins}") List<String> origins) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOriginPatterns(origins);
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setExposedHeaders(List.of("Content-Disposition"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cors);
        return source;
    }
}
