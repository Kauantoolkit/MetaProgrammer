package com.example.meta_backend.service.generator.security;

import java.io.IOException;
import java.nio.file.*;

import org.springframework.stereotype.Component;

@Component
public class SecurityGenerator {

    public void generateSecurityClasses(String baseDir) throws IOException {
        Path secPath = Paths.get(baseDir + "security");
        Files.createDirectories(secPath);

        String config = """
            package com.metagen.backend.generated.security;

            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;
            import org.springframework.context.annotation.*;
            import org.springframework.security.config.annotation.web.builders.HttpSecurity;
            import org.springframework.security.web.SecurityFilterChain;
            import org.springframework.security.web.authentication.AuthenticationFailureHandler;
            import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
            import org.springframework.security.core.AuthenticationException;
            import org.springframework.security.core.userdetails.*;
            import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
            import org.springframework.security.crypto.password.PasswordEncoder;
            import org.springframework.beans.factory.annotation.Autowired;
            import com.metagen.backend.generated.repository.UsersRepository;
            import com.metagen.backend.generated.entity.Users;
            import jakarta.servlet.ServletException;
            import jakarta.servlet.http.HttpServletRequest;
            import jakarta.servlet.http.HttpServletResponse;
            import java.io.IOException;
            import java.util.Optional;

            @Configuration
            public class SecurityConfig {

                private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

                @Autowired
                private UsersRepository usersRepository;

                @Bean
                public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                    http.csrf(csrf -> csrf.disable())
                        .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/login").permitAll()
                            .requestMatchers("/backoffice/**").authenticated()
                            .anyRequest().permitAll()
                        )
                        .formLogin(form -> form
                            .loginPage("/login")
                            .defaultSuccessUrl("/backoffice", true)
                            .failureHandler(authenticationFailureHandler())
                            .permitAll()
                        )
                        .logout(logout -> logout
                            .logoutSuccessUrl("/")
                            .permitAll()
                        );
                    return http.build();
                }

                @Bean
                public UserDetailsService userDetailsService() {
                    return username -> {
                        Optional<Users> usersOpt = usersRepository.findByUsername(username);
                        if (usersOpt.isPresent()) {
                            Users users = usersOpt.get();
                            return org.springframework.security.core.userdetails.User.builder()
                                .username(users.getUsername())
                                .password(users.getPassword())
                                .roles(users.getRoles().split(","))
                                .build();
                        }
                        throw new UsernameNotFoundException("User not found: " + username);
                    };
                }

                @Bean
                public PasswordEncoder passwordEncoder() {
                    return new BCryptPasswordEncoder();
                }

                @Bean
                public AuthenticationFailureHandler authenticationFailureHandler() {
                    return new SimpleUrlAuthenticationFailureHandler() {
                        @Override
                        public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                AuthenticationException exception) throws IOException, ServletException {
                            log.warn("Authentication failure for user '{}': {}",
                                     request.getParameter("username"), exception.getMessage());
                            super.onAuthenticationFailure(request, response, exception);
                        }
                    };
                }
            }
            """;

        Files.writeString(secPath.resolve("SecurityConfig.java"), config);
    }
}
