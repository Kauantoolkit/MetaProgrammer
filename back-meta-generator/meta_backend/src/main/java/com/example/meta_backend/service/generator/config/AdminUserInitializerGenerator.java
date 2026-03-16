package com.example.meta_backend.service.generator.config;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class AdminUserInitializerGenerator {

    public void generateAdminUserInitializer(String baseDir) throws IOException {
        Path configPath = Paths.get(baseDir + "config");
        Files.createDirectories(configPath);

        String initializer = """
            package com.metagen.backend.generated.config;

            import com.metagen.backend.generated.entity.Users;
            import com.metagen.backend.generated.repository.UsersRepository;
            import org.springframework.beans.factory.annotation.Autowired;
            import org.springframework.beans.factory.annotation.Value;
            import org.springframework.boot.context.event.ApplicationReadyEvent;
            import org.springframework.context.event.EventListener;
            import org.springframework.security.crypto.password.PasswordEncoder;
            import org.springframework.stereotype.Component;
            import org.springframework.transaction.annotation.Transactional;

            import java.util.Optional;

            @Component
            public class AdminUserInitializer {

                @Autowired
                private UsersRepository usersRepository;

                @Autowired
                private PasswordEncoder passwordEncoder;

                @Value("${admin.password}")
                private String adminPassword;

                @EventListener(ApplicationReadyEvent.class)
                @Transactional
                public void createAdminUser() {
                    Optional<Users> existingAdmin = usersRepository.findByUsername("admin");
                    
                    if (existingAdmin.isEmpty()) {
                        Users admin = new Users();
                        admin.setUsername("admin");
                        admin.setPassword(passwordEncoder.encode(adminPassword));
                        admin.setRoles("ADMIN");
                        
                        usersRepository.save(admin);
                        
                        System.out.println("========================================");
                        System.out.println("Admin user created successfully!");
                        System.out.println("Username: admin");
                        System.out.println("Password: " + adminPassword);
                        System.out.println("========================================");
                    } else {
                        System.out.println("Admin user already exists, skipping creation.");
                    }
                }
            }
            """;

        Files.writeString(configPath.resolve("AdminUserInitializer.java"), initializer);
    }
}
