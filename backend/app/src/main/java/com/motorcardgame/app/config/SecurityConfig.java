package com.motorcardgame.app.config;

import com.motorcardgame.app.auth.application.JwtService;
import com.motorcardgame.app.auth.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * API sin estado (JWT Bearer, sin cookies de sesión) — CSRF desactivado porque solo tiene sentido
 * contra ataques que se apoyan en cookies. {@code /api/auth/**} queda abierto (es como se
 * consiguen los tokens); el resto de {@code /api/**} exige un JWT válido. El handshake de
 * {@code /ws} queda fuera de este filtro: se autentica aparte en
 * {@code PlayerHandshakeHandler} porque un WebSocket nativo del navegador no puede mandar la
 * cabecera Authorization.
 */
@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(
                        exceptions -> exceptions.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Un 403/500 hace sendError(), que Tomcat reenvía internamente a /error — ese
                        // segundo paso por la cadena de filtros llega sin la Authorization original
                        // (SecurityContext queda anónimo) y, sin este permitAll, .anyRequest().authenticated()
                        // lo rechaza otra vez y el authenticationEntryPoint pisa el código real con 401. Sin
                        // esto un 403 de verdad (p.ej. /api/admin/** sin ser admin) le llega al cliente como 401.
                        .requestMatchers("/error").permitAll()
                        // Unirse a una sala pasa un nombre, no una cuenta (ver RoomController) — todavía no
                        // hay token en ese punto, así que estas dos rutas quedan fuera del resto de /api/**.
                        .requestMatchers(HttpMethod.GET, "/api/rooms/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/rooms/*/join").permitAll()
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
