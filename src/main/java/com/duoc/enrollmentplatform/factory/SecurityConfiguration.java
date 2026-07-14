package com.duoc.enrollmentplatform.factory;

import com.duoc.enrollmentplatform.shared.domain.valueobjects.Email;
import com.duoc.enrollmentplatform.users.domain.entities.User;
import com.duoc.enrollmentplatform.users.domain.repositories.UserRepository;
import com.duoc.enrollmentplatform.users.domain.valueobjects.UserStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    @ConditionalOnProperty(name = "enrollment.security.jwt.enabled", havingValue = "true")
    @ConditionalOnMissingBean(JwtDecoder.class)
    JwtDecoder jwtDecoder(
            @Value("${AZURE_B2C_JWK_SET_URI:}") String jwkSetUri,
            @Value("${AZURE_B2C_AUDIENCE:}") String audience) {
        validateJwtProperties(jwkSetUri, audience);
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<List<String>>(
                JwtClaimNames.AUD,
                aud -> aud != null && aud.contains(audience));
        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(),
                audienceValidator));
        return jwtDecoder;
    }

    @Bean
    @ConditionalOnProperty(name = "enrollment.security.jwt.enabled", havingValue = "true")
    JwtAuthenticationConverter jwtAuthenticationConverter(UserRepository userRepository) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String emailClaim = jwt.getClaimAsString("email");
            if (emailClaim == null || emailClaim.isBlank()) {
                emailClaim = jwt.getClaimAsString("preferred_username");
            }
            if (emailClaim == null || emailClaim.isBlank()) {
                return List.of();
            }
            try {
                Optional<User> user = userRepository.findByEmail(Email.create(emailClaim));
                if (user.isEmpty() || user.get().getStatus() != UserStatus.ACTIVE) {
                    return List.of();
                }
                return List.of(new SimpleGrantedAuthority("ROLE_" + user.get().getRole().name()));
            } catch (RuntimeException ignored) {
                return List.of();
            }
        });
        return converter;
    }

    @Bean
    @ConditionalOnProperty(
            name = {"enrollment.security.jwt.enabled", "spring.h2.console.enabled"},
            havingValue = "true")
    WebSecurityCustomizer h2ConsoleWebSecurityCustomizer() {
        // H2 console is local-only; bypass JWT resource-server filters completely.
        return web -> web.ignoring().requestMatchers("/h2-console", "/h2-console/**");
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${enrollment.security.jwt.enabled:false}") boolean jwtEnabled,
            @Value("${AZURE_B2C_JWK_SET_URI:}") String jwkSetUri,
            @Value("${AZURE_B2C_AUDIENCE:}") String audience,
            Optional<JwtAuthenticationConverter> jwtAuthenticationConverter) throws Exception {
        http.csrf(csrf -> csrf.disable());
        if (jwtEnabled) {
            validateJwtProperties(jwkSetUri, audience);
            http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
            http.authorizeHttpRequests(auth -> auth
                            .requestMatchers("/actuator/health").permitAll()
                            .requestMatchers(HttpMethod.POST, "/users/registrations", "/users/login").permitAll()
                            .requestMatchers(HttpMethod.POST, "/users/pre-registrations").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.GET, "/users", "/users/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/users/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.GET, "/courses").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                            .requestMatchers(HttpMethod.POST, "/courses").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.POST, "/enrollments/*/summary").hasAnyRole("STUDENT", "ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/enrollments/*/summary").hasAnyRole("STUDENT", "ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/enrollments/*/summary").hasAnyRole("STUDENT", "ADMIN")
                            .requestMatchers(HttpMethod.GET, "/enrollments", "/enrollments/**")
                                    .hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                            .requestMatchers(HttpMethod.POST, "/enrollments").hasAnyRole("STUDENT", "ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/enrollments/**").hasAnyRole("STUDENT", "ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/enrollments/**").hasAnyRole("STUDENT", "ADMIN")
                            .anyRequest().denyAll())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
                        jwtAuthenticationConverter.ifPresent(jwt::jwtAuthenticationConverter);
                    }));
        } else {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        }
        return http.build();
    }

    private void validateJwtProperties(String jwkSetUri, String audience) {
        if (jwkSetUri == null || jwkSetUri.isBlank()) {
            throw new IllegalStateException(
                    "ENROLLMENT_SECURITY_JWT_ENABLED=true requires AZURE_B2C_JWK_SET_URI to be set");
        }
        if (audience == null || audience.isBlank()) {
            throw new IllegalStateException(
                    "ENROLLMENT_SECURITY_JWT_ENABLED=true requires AZURE_B2C_AUDIENCE to be set");
        }
    }
}
