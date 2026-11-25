package com.example.meta_backend.service.generator.security;

import com.example.meta_backend.service.generator.ProjectWriter;
import org.springframework.stereotype.Component;

/**
 * SecurityConfig do estudio-app: JWT do basilisk-security + TenantFilter, tudo autenticado menos /auth. Antes o
 * gerado tinha login por formulário e `anyRequest().permitAll()`, ou seja, a API inteira aberta.
 * A checagem fina é o @RequiresPermission de cada endpoint (ControllerGenerator).
 */
@Component
public class SecurityGenerator {

    public void generate(ProjectWriter out) {
        String config = """
            package @PKG@.config;

            import com.basilisk.security.jwt.JwtAuthFilter;
            import com.basilisk.tenant.filter.TenantFilter;
            import lombok.RequiredArgsConstructor;
            import org.springframework.context.annotation.Bean;
            import org.springframework.context.annotation.Configuration;
            import org.springframework.http.HttpStatus;
            import org.springframework.security.config.annotation.web.builders.HttpSecurity;
            import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
            import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
            import org.springframework.security.config.http.SessionCreationPolicy;
            import org.springframework.security.web.SecurityFilterChain;
            import org.springframework.security.web.authentication.HttpStatusEntryPoint;
            import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

            @Configuration
            @EnableWebSecurity
            @RequiredArgsConstructor
            public class SecurityConfig {

                private final JwtAuthFilter jwtAuthFilter;
                private final TenantFilter tenantFilter;

                @Bean
                public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                    http
                        .csrf(AbstractHttpConfigurer::disable)
                        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                        .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/v1/auth/**").permitAll()
                            .anyRequest().authenticated()
                        )
                        // Sem login (token ausente, vencido ou adulterado) responde 401, não o 403 padrão: o front
                        // só derruba a sessão e volta ao login no 401
                        .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                        .addFilterAfter(tenantFilter, JwtAuthFilter.class);

                    return http.build();
                }
            }
            """.replace("@PKG@", ProjectWriter.PACKAGE);
        out.java("config", "SecurityConfig", config);
    }
}
