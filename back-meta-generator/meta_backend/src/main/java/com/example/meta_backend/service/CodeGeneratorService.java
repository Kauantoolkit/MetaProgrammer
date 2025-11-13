package com.example.meta_backend.service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class CodeGeneratorService {

    private static final String BASE_DIR = "generated/";

    /**
     * Método principal que gera o backend completo.
     */
    public void generateEntities(List<Map<String, Object>> entities) {
        try {
            // Cria estrutura de pastas
            createBaseStructure();

            // Gera arquivos de bootstrapping
            generatePomFile("generated_app");
            generateMainClass("generated_app");
            generateGitignore();
            generateApplicationProperties("generated_app");

            // Gera arquivos por entidade
            for (Map<String, Object> entity : entities) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> attributes = (List<Map<String, Object>>) entity.get("attributes");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> relations = (List<Map<String, Object>>) entity.get("relations");
                String name = (String) entity.get("name");

                generateEntity(name, attributes == null ? Collections.emptyList() : attributes,
                                     relations == null ? Collections.emptyList() : relations);
                generateRepository(name);
                generateDTOs(name, attributes == null ? Collections.emptyList() : attributes);
                generateService(name);
                generateController(name, attributes == null ? Collections.emptyList() : attributes);
            }

            // Gera classes de segurança básicas
            generateSecurityClasses();
            generateGlobalExceptionHandler();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar arquivos: " + e.getMessage(), e);
        }
    }


    


    // ================= BASE / AUX =================
    private void createBaseStructure() throws IOException {
        for (String sub : List.of("entity", "dto", "repository", "service", "controller", "security", "exception", "mapper")) {
            Files.createDirectories(Paths.get(BASE_DIR + sub));
        }
        Files.createDirectories(Paths.get("src/main/resources"));
    }

    // ================= POM / MAIN / GIT =================
    private void generatePomFile(String projectName) throws IOException {
        String content = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                     https://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.metagen.backend</groupId>
                <artifactId>%s</artifactId>
                <version>0.0.1-SNAPSHOT</version>
                <name>%s</name>

                <properties>
                    <java.version>17</java.version>
                    <spring-boot.version>3.2.5</spring-boot.version>
                    <mapstruct.version>1.5.5.Final</mapstruct.version>
                </properties>

                <dependencies>
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-web</artifactId>
                    </dependency>
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-data-jpa</artifactId>
                    </dependency>
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-validation</artifactId>
                    </dependency>
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-security</artifactId>
                    </dependency>
                    <dependency>
                        <groupId>io.jsonwebtoken</groupId>
                        <artifactId>jjwt-api</artifactId>
                        <version>0.11.5</version>
                    </dependency>
                    <dependency>
                        <groupId>io.jsonwebtoken</groupId>
                        <artifactId>jjwt-impl</artifactId>
                        <version>0.11.5</version>
                        <scope>runtime</scope>
                    </dependency>
                    <dependency>
                        <groupId>io.jsonwebtoken</groupId>
                        <artifactId>jjwt-jackson</artifactId>
                        <version>0.11.5</version>
                        <scope>runtime</scope>
                    </dependency>
                    <dependency>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct</artifactId>
                        <version>${mapstruct.version}</version>
                    </dependency>
                    <dependency>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <optional>true</optional>
                    </dependency>
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-test</artifactId>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>org.postgresql</groupId>
                        <artifactId>postgresql</artifactId>
                        <scope>runtime</scope>
                    </dependency>
                </dependencies>

                <build>
                    <plugins>
                        <plugin>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-maven-plugin</artifactId>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """.formatted(projectName, projectName);

        Files.writeString(Paths.get("pom.xml"), content);
    }

    private void generateMainClass(String projectName) throws IOException {
        String packagePath = "/generated";
        Files.createDirectories(Paths.get(packagePath));

        String className = capitalize(projectName) + "Application";
        String content = """
            package com.metagen.backend.generated;

            import org.springframework.boot.SpringApplication;
            import org.springframework.boot.autoconfigure.SpringBootApplication;

            @SpringBootApplication
            public class %s {
                public static void main(String[] args) {
                    SpringApplication.run(%s.class, args);
                }
            }
            """.formatted(className, className);

        Files.writeString(Paths.get(packagePath + "/" + className + ".java"), content);
    }

    private void generateGitignore() throws IOException {
        String content = """
            /target/
            /.idea/
            /.vscode/
            *.iml
            *.log
            .DS_Store
            /bin/
            /build/
            """;
        Files.writeString(Paths.get(".gitignore"), content);
    }

    // ================= APPLICATION.PROPERTIES =================
    private void generateApplicationProperties(String projectName) throws IOException {
        Path resourcesPath = Paths.get("src/main/resources");
        Files.createDirectories(resourcesPath);

        String content = """
            spring.application.name=%s

            spring.datasource.url=jdbc:postgresql://localhost:5432/%s
            spring.datasource.username=postgres
            spring.datasource.password=postgres
            spring.datasource.driver-class-name=org.postgresql.Driver

            spring.jpa.hibernate.ddl-auto=update
            spring.jpa.show-sql=true
            spring.jpa.properties.hibernate.format_sql=true

            server.port=8080
            """.formatted(projectName, projectName.toLowerCase());

        Files.writeString(resourcesPath.resolve("application.properties"), content);
    }

    // ================= ENTITY =================
    private void generateEntity(String name, List<Map<String, Object>> attrs, List<Map<String, Object>> rels) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("package com.metagen.backend.generated.entity;\n\n")
          .append("import jakarta.persistence.*;\n")
          .append("import jakarta.validation.constraints.*;\n")
          .append("import java.util.*;\n\n")
          .append("@Entity\n@Table(name = \"" + name.toLowerCase() + "\")\n")
          .append("public class " + name + " {\n\n");

        if (attrs != null) {
            for (Map<String, Object> attr : attrs) {
                String type = mapType((String) attr.get("type"));
                String attrName = (String) attr.get("name");

                if (attrName.equalsIgnoreCase("id")) {
                    sb.append("    @Id\n    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
                } else {
                    sb.append("    @NotNull\n"); // Exemplo de validação básica
                }
                sb.append("    private " + type + " " + attrName + ";\n\n");
            }
        }

        if (rels != null) {
            for (Map<String, Object> rel : rels) {
                String target = (String) rel.get("target");
                String type = (String) rel.get("type");
                switch (type) {
                    case "1:N" -> sb.append("    @OneToMany(mappedBy = \"" + name.toLowerCase() + "\")\n" +
                                            "    private List<" + target + "> " + target.toLowerCase() + "s;\n\n");
                    case "N:1" -> sb.append("    @ManyToOne\n    @JoinColumn(name = \"" + target.toLowerCase() + "_id\")\n" +
                                            "    private " + target + " " + target.toLowerCase() + ";\n\n");
                    case "1:1" -> sb.append("    @OneToOne\n    @JoinColumn(name = \"" + target.toLowerCase() + "_id\")\n" +
                                            "    private " + target + " " + target.toLowerCase() + ";\n\n");
                    case "N:N" -> sb.append("    @ManyToMany\n    @JoinTable(name = \"" + name.toLowerCase() + "_" + target.toLowerCase() + "\", " +
                                            "joinColumns = @JoinColumn(name = \"" + name.toLowerCase() + "_id\"), " +
                                            "inverseJoinColumns = @JoinColumn(name = \"" + target.toLowerCase() + "_id\"))\n" +
                                            "    private List<" + target + "> " + target.toLowerCase() + "s;\n\n");
                }
            }
        }

        // Getters e Setters (simples)
        if (attrs != null) {
            sb.append("    // Getters e Setters\n");
            for (Map<String, Object> attr : attrs) {
                String type = mapType((String) attr.get("type"));
                String attrName = (String) attr.get("name");
                String cap = capitalize(attrName);
                sb.append("    public " + type + " get" + cap + "() { return " + attrName + "; }\n");
                sb.append("    public void set" + cap + "(" + type + " " + attrName + ") { this." + attrName + " = " + attrName + "; }\n\n");
            }
        }

        sb.append("}\n");
        Files.writeString(Paths.get(BASE_DIR + "entity/" + name + ".java"), sb.toString());
    }

    // ================= REPOSITORY =================
    private void generateRepository(String name) throws IOException {
        String code = """
            package com.metagen.backend.generated.repository;

            import org.springframework.data.jpa.repository.JpaRepository;
            import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
            import com.metagen.backend.generated.entity.%s;

            public interface %sRepository extends JpaRepository<%s, Long>, JpaSpecificationExecutor<%s> {}
            """.formatted(name, name, name, name);

        Files.writeString(Paths.get(BASE_DIR + "repository/" + name + "Repository.java"), code);
    }

    // ================= DTOs =================
    private void generateDTOs(String name, List<Map<String, Object>> attrs) throws IOException {
        StringBuilder req = new StringBuilder("package com.metagen.backend.generated.dto;\n\nimport jakarta.validation.constraints.*;\n\npublic class " + name + "Request {\n");
        StringBuilder res = new StringBuilder("package com.metagen.backend.generated.dto;\n\npublic class " + name + "Response {\n");

        if (attrs != null) {
            for (Map<String, Object> attr : attrs) {
                String field = (String) attr.get("name");
                String type = mapType((String) attr.get("type"));

                if (!field.equalsIgnoreCase("id"))
                    req.append("    @NotNull\n    private " + type + " " + field + ";\n");
                res.append("    private " + type + " " + field + ";\n");
            }
        }

        req.append("\n    // Getters e Setters\n");
        res.append("\n    // Getters e Setters\n");

        if (attrs != null) {
            for (Map<String, Object> attr : attrs) {
                String field = (String) attr.get("name");
                String type = mapType((String) attr.get("type"));
                String cap = capitalize(field);

                if (!field.equalsIgnoreCase("id")) {
                    req.append("    public " + type + " get" + cap + "() { return " + field + "; }\n");
                    req.append("    public void set" + cap + "(" + type + " " + field + ") { this." + field + " = " + field + "; }\n\n");
                }

                res.append("    public " + type + " get" + cap + "() { return " + field + "; }\n");
                res.append("    public void set" + cap + "(" + type + " " + field + ") { this." + field + " = " + field + "; }\n\n");
            }
        }

        req.append("}\n");
        res.append("}\n");

        Files.writeString(Paths.get(BASE_DIR + "dto/" + name + "Request.java"), req.toString());
        Files.writeString(Paths.get(BASE_DIR + "dto/" + name + "Response.java"), res.toString());
    }

    // ================= SERVICE (per-entity) =================
    private void generateService(String name) throws IOException {
        String code = """
            package com.metagen.backend.generated.service;

            import org.springframework.stereotype.Service;
            import org.springframework.data.domain.Page;
            import org.springframework.data.domain.Pageable;
            import java.util.*;
            import com.metagen.backend.generated.entity.%s;
            import com.metagen.backend.generated.repository.%sRepository;

            @Service
            public class %sService {

                private final %sRepository repository;

                public %sService(%sRepository repository) {
                    this.repository = repository;
                }

                public List<%s> findAll() { return repository.findAll(); }

                public Page<%s> findAll(Pageable pageable) { return repository.findAll(pageable); }

                public Optional<%s> findById(Long id) { return repository.findById(id); }

                public %s save(%s entity) { return repository.save(entity); }

                public void delete(Long id) { repository.deleteById(id); }
            }
            """.formatted(name, name, name, name, name, name, name, name, name, name, name, name);

        Files.writeString(Paths.get(BASE_DIR + "service/" + name + "Service.java"), code);
    }

    // ================= CONTROLLER =================
    private void generateController(String name, List<Map<String, Object>> attrs) throws IOException {
        String lname = name.toLowerCase();

        // Map DTO <-> Entity
        StringBuilder mapToEntity = new StringBuilder();
        StringBuilder mapToResponse = new StringBuilder();

        if (attrs != null) {
            for (Map<String, Object> attr : attrs) {
                String field = (String) attr.get("name");
                String cap = capitalize(field);

                if (!field.equalsIgnoreCase("id")) mapToEntity.append("        entity.set" + cap + "(dto.get" + cap + "());\n");
                mapToResponse.append("        dto.set" + cap + "(entity.get" + cap + "());\n");
            }
        }

        String code = """
            package com.metagen.backend.generated.controller;

            import org.springframework.http.ResponseEntity;
            import org.springframework.web.bind.annotation.*;
            import jakarta.validation.Valid;
            import java.util.*;
            import com.metagen.backend.generated.dto.%sRequest;
            import com.metagen.backend.generated.dto.%sResponse;
            import com.metagen.backend.generated.entity.%s;
            import com.metagen.backend.generated.service.%sService;

            @RestController
            @RequestMapping("/api/%s")
            public class %sController {

                private final %sService service;

                public %sController(%sService service) {
                    this.service = service;
                }

                @GetMapping
                public ResponseEntity<List<%sResponse>> getAll() {
                    List<%sResponse> list = service.findAll().stream().map(this::toResponse).toList();
                    return ResponseEntity.ok(list);
                }

                @GetMapping("/{id}")
                public ResponseEntity<%sResponse> getById(@PathVariable Long id) {
                    return service.findById(id)
                            .map(this::toResponse)
                            .map(ResponseEntity::ok)
                            .orElse(ResponseEntity.notFound().build());
                }

                @PostMapping
                public ResponseEntity<%sResponse> create(@Valid @RequestBody %sRequest dto) {
                    %s entity = toEntity(dto);
                    return ResponseEntity.ok(toResponse(service.save(entity)));
                }

                @PutMapping("/{id}")
                public ResponseEntity<%sResponse> update(@PathVariable Long id, @Valid @RequestBody %sRequest dto) {
                    %s entity = toEntity(dto);
                    entity.setId(id);
                    return ResponseEntity.ok(toResponse(service.save(entity)));
                }

                @DeleteMapping("/{id}")
                public ResponseEntity<Void> delete(@PathVariable Long id) {
                    service.delete(id);
                    return ResponseEntity.noContent().build();
                }

                private %sResponse toResponse(%s entity) {
                    %sResponse dto = new %sResponse();
            %s
                    return dto;
                }

                private %s toEntity(%sRequest dto) {
                    %s entity = new %s();
            %s
                    return entity;
                }
            }
            """.formatted(
                name, name, name, name, lname,
                name, name, name, name,
                name, name, name, name, name, name, name,
                name, name, name, name, name, name, name, name,
                mapToResponse, name, name, name, name, mapToEntity
        );

        Files.writeString(Paths.get(BASE_DIR + "controller/" + name + "Controller.java"), code);
    }

    // ================= SECURITY & EXCEPTIONS =================
    private void generateSecurityClasses() throws IOException {
    // Paths
    Path secPath = Paths.get(BASE_DIR + "security/");
    Path entityPath = Paths.get(BASE_DIR + "entity/");
    Path repoPath = Paths.get(BASE_DIR + "repository/");
    Path servicePath = Paths.get(BASE_DIR + "service/");
    Path controllerPath = Paths.get(BASE_DIR + "controller/");
    Path dtoPath = Paths.get(BASE_DIR + "dto/");

    Files.createDirectories(secPath);
    Files.createDirectories(entityPath);
    Files.createDirectories(repoPath);
    Files.createDirectories(servicePath);
    Files.createDirectories(controllerPath);
    Files.createDirectories(dtoPath);

    // ---------- 1) Role entity ----------
    String roleEntity = """
        package com.metagen.backend.generated.entity;

        import jakarta.persistence.*;
        import java.util.Set;

        @Entity
        @Table(name = "roles")
        public class Role {

            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            private Long id;

            @Column(unique = true, nullable = false)
            private String name;

            // getters / setters
            public Long getId() { return id; }
            public void setId(Long id) { this.id = id; }
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
        }
        """;
    Files.writeString(entityPath.resolve("Role.java"), roleEntity);

    // ---------- 2) User entity ----------
    String userEntity = """
        package com.metagen.backend.generated.entity;

        import jakarta.persistence.*;
        import jakarta.validation.constraints.*;
        import java.util.Set;
        import java.util.HashSet;

        @Entity
        @Table(name = "users")
        public class User {

            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            private Long id;

            @Column(unique = true, nullable = false)
            private String username;

            @Column(unique = true, nullable = false)
            private String email;

            @Column(nullable = false)
            private String password;

            @ManyToMany(fetch = FetchType.EAGER)
            @JoinTable(
                name = "user_roles",
                joinColumns = @JoinColumn(name = "user_id"),
                inverseJoinColumns = @JoinColumn(name = "role_id")
            )
            private Set<Role> roles = new HashSet<>();

            // getters / setters
            public Long getId() { return id; }
            public void setId(Long id) { this.id = id; }
            public String getUsername() { return username; }
            public void setUsername(String username) { this.username = username; }
            public String getEmail() { return email; }
            public void setEmail(String email) { this.email = email; }
            public String getPassword() { return password; }
            public void setPassword(String password) { this.password = password; }
            public Set<Role> getRoles() { return roles; }
            public void setRoles(Set<Role> roles) { this.roles = roles; }
        }
        """;
    Files.writeString(entityPath.resolve("User.java"), userEntity);

    // ---------- 3) RoleRepository ----------
    String roleRepo = """
        package com.metagen.backend.generated.repository;

        import org.springframework.data.jpa.repository.JpaRepository;
        import com.metagen.backend.generated.entity.Role;
        import java.util.Optional;

        public interface RoleRepository extends JpaRepository<Role, Long> {
            Optional<Role> findByName(String name);
        }
        """;
    Files.writeString(repoPath.resolve("RoleRepository.java"), roleRepo);

    // ---------- 4) UserRepository ----------
    String userRepo = """
        package com.metagen.backend.generated.repository;

        import org.springframework.data.jpa.repository.JpaRepository;
        import com.metagen.backend.generated.entity.User;
        import java.util.Optional;

        public interface UserRepository extends JpaRepository<User, Long> {
            Optional<User> findByUsername(String username);
            Optional<User> findByEmail(String email);
            boolean existsByUsername(String username);
            boolean existsByEmail(String email);
        }
        """;
    Files.writeString(repoPath.resolve("UserRepository.java"), userRepo);

    // ---------- 5) Auth DTOs ----------
    String loginReq = """
        package com.metagen.backend.generated.dto;

        public class LoginRequest {
            private String username;
            private String password;

            public String getUsername() { return username; }
            public void setUsername(String username) { this.username = username; }
            public String getPassword() { return password; }
            public void setPassword(String password) { this.password = password; }
        }
        """;
    Files.writeString(dtoPath.resolve("LoginRequest.java"), loginReq);

    String authResp = """
        package com.metagen.backend.generated.dto;

        public class AuthResponse {
            private String token;
            private String tokenType = "Bearer";

            public AuthResponse() {}
            public AuthResponse(String token) { this.token = token; }

            public String getToken() { return token; }
            public void setToken(String token) { this.token = token; }
            public String getTokenType() { return tokenType; }
            public void setTokenType(String tokenType) { this.tokenType = tokenType; }
        }
        """;
    Files.writeString(dtoPath.resolve("AuthResponse.java"), authResp);

    String registerReq = """
        package com.metagen.backend.generated.dto;

        public class RegisterRequest {
            private String username;
            private String email;
            private String password;

            public String getUsername() { return username; }
            public void setUsername(String username) { this.username = username; }
            public String getEmail() { return email; }
            public void setEmail(String email) { this.email = email; }
            public String getPassword() { return password; }
            public void setPassword(String password) { this.password = password; }
        }
        """;
    Files.writeString(dtoPath.resolve("RegisterRequest.java"), registerReq);

    // ---------- 6) JwtTokenProvider ----------
    String jwtProvider = """
        package com.metagen.backend.generated.security;

        import io.jsonwebtoken.*;
        import io.jsonwebtoken.security.Keys;
        import org.springframework.beans.factory.annotation.Value;
        import org.springframework.stereotype.Component;

        import java.security.Key;
        import java.util.Date;

        @Component
        public class JwtTokenProvider {

            @Value("${app.jwtSecret:ChangeThisSecretKeyToSomethingSecure}")
            private String jwtSecret;

            @Value("${app.jwtExpirationMs:3600000}")
            private long jwtExpirationMs;

            private Key getSigningKey() {
                return Keys.hmacShaKeyFor(jwtSecret.getBytes());
            }

            public String generateToken(String username) {
                Date now = new Date();
                Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
                return Jwts.builder()
                        .setSubject(username)
                        .setIssuedAt(now)
                        .setExpiration(expiryDate)
                        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                        .compact();
            }

            public String getUsernameFromJWT(String token) {
                return Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
                        .parseClaimsJws(token).getBody().getSubject();
            }

            public boolean validateToken(String token) {
                try {
                    Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
                    return true;
                } catch (JwtException ex) {
                    return false;
                }
            }
        }
        """;
    Files.writeString(secPath.resolve("JwtTokenProvider.java"), jwtProvider);

    // ---------- 7) JwtAuthenticationFilter ----------
    String jwtFilter = """
        package com.metagen.backend.generated.security;

        import jakarta.servlet.FilterChain;
        import jakarta.servlet.ServletException;
        import jakarta.servlet.http.HttpServletRequest;
        import jakarta.servlet.http.HttpServletResponse;
        import org.springframework.security.core.context.SecurityContextHolder;
        import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
        import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
        import org.springframework.util.StringUtils;
        import org.springframework.web.filter.OncePerRequestFilter;
        import com.metagen.backend.generated.entity.User;
        import com.metagen.backend.generated.service.SecurityUserDetailsService;

        import java.io.IOException;

        public class JwtAuthenticationFilter extends OncePerRequestFilter {

            private final JwtTokenProvider tokenProvider;
            private final SecurityUserDetailsService userDetailsService;

            public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, SecurityUserDetailsService userDetailsService) {
                this.tokenProvider = tokenProvider;
                this.userDetailsService = userDetailsService;
            }

            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                try {
                    String jwt = getJwtFromRequest(request);
                    if (jwt != null && tokenProvider.validateToken(jwt)) {
                        String username = tokenProvider.getUsernameFromJWT(jwt);
                        var userDetails = userDetailsService.loadUserByUsername(username);
                        var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                } catch (Exception ex) {
                    // ignore auth errors here; will be handled downstream where necessary
                }
                filterChain.doFilter(request, response);
            }

            private String getJwtFromRequest(HttpServletRequest request) {
                String bearerToken = request.getHeader("Authorization");
                if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                    return bearerToken.substring(7);
                }
                return null;
            }
        }
        """;
    Files.writeString(secPath.resolve("JwtAuthenticationFilter.java"), jwtFilter);

    // ---------- 8) SecurityUserDetailsService ----------
    String userDetailsService = """
        package com.metagen.backend.generated.service;

        import org.springframework.security.core.userdetails.UserDetailsService;
        import org.springframework.security.core.userdetails.UsernameNotFoundException;
        import org.springframework.security.core.userdetails.UserDetails;
        import org.springframework.stereotype.Service;
        import org.springframework.beans.factory.annotation.Autowired;
        import com.metagen.backend.generated.repository.UserRepository;
        import com.metagen.backend.generated.entity.User;
        import org.springframework.security.core.authority.SimpleGrantedAuthority;
        import java.util.stream.Collectors;

        @Service
        public class SecurityUserDetailsService implements UserDetailsService {

            @Autowired
            private UserRepository userRepository;

            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
                var authorities = user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.getName()))
                        .collect(Collectors.toList());
                return org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .authorities(authorities)
                        .build();
            }
        }
        """;
    Files.writeString(servicePath.resolve("SecurityUserDetailsService.java"), userDetailsService);

    // ---------- 9) AuthService ----------
    String authService = """
        package com.metagen.backend.generated.service;

        import org.springframework.stereotype.Service;
        import org.springframework.beans.factory.annotation.Autowired;
        import com.metagen.backend.generated.repository.UserRepository;
        import com.metagen.backend.generated.repository.RoleRepository;
        import com.metagen.backend.generated.entity.User;
        import com.metagen.backend.generated.entity.Role;
        import org.springframework.security.crypto.password.PasswordEncoder;
        import java.util.HashSet;
        import java.util.Optional;

        @Service
        public class AuthService {

            @Autowired
            private UserRepository userRepository;

            @Autowired
            private RoleRepository roleRepository;

            @Autowired
            private PasswordEncoder passwordEncoder;

            public User register(String username, String email, String password) {
                if (userRepository.existsByUsername(username)) throw new IllegalArgumentException("Username already taken");
                if (userRepository.existsByEmail(email)) throw new IllegalArgumentException("Email already taken");

                User user = new User();
                user.setUsername(username);
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode(password));
                // default role USER
                Role role = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ROLE_USER");
                    return roleRepository.save(r);
                });
                var roles = new HashSet<Role>();
                roles.add(role);
                user.setRoles(roles);
                return userRepository.save(user);
            }
        }
        """;
    Files.writeString(servicePath.resolve("AuthService.java"), authService);

    // ---------- 10) AuthController ----------
    String authController = """
        package com.metagen.backend.generated.controller;

        import org.springframework.web.bind.annotation.*;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.http.ResponseEntity;
        import com.metagen.backend.generated.dto.LoginRequest;
        import com.metagen.backend.generated.dto.RegisterRequest;
        import com.metagen.backend.generated.dto.AuthResponse;
        import com.metagen.backend.generated.security.JwtTokenProvider;
        import com.metagen.backend.generated.service.AuthService;
        import org.springframework.security.authentication.AuthenticationManager;
        import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
        import org.springframework.security.core.Authentication;
        import org.springframework.security.core.context.SecurityContextHolder;

        @RestController
        @RequestMapping("/auth")
        public class AuthController {

            @Autowired
            private AuthenticationManager authenticationManager;

            @Autowired
            private JwtTokenProvider tokenProvider;

            @Autowired
            private AuthService authService;

            @PostMapping("/login")
            public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                String token = tokenProvider.generateToken(authentication.getName());
                return ResponseEntity.ok(new AuthResponse(token));
            }

            @PostMapping("/register")
            public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
                try {
                    authService.register(request.getUsername(), request.getEmail(), request.getPassword());
                    return ResponseEntity.ok().build();
                } catch (IllegalArgumentException ex) {
                    return ResponseEntity.badRequest().body(ex.getMessage());
                }
            }
        }
        """;
    Files.writeString(controllerPath.resolve("AuthController.java"), authController);

    // ---------- 11) SecurityConfig ----------
    String securityConfig = """
        package com.metagen.backend.generated.security;

        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.security.config.annotation.web.builders.HttpSecurity;
        import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
        import org.springframework.security.web.SecurityFilterChain;
        import org.springframework.security.authentication.AuthenticationManager;
        import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
        import org.springframework.security.config.http.SessionCreationPolicy;
        import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
        import org.springframework.beans.factory.annotation.Autowired;
        import com.metagen.backend.generated.service.SecurityUserDetailsService;
        import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
        import org.springframework.security.crypto.password.PasswordEncoder;

        @Configuration
        @EnableWebSecurity
        public class SecurityConfig {

            @Autowired
            private JwtTokenProvider tokenProvider;

            @Autowired
            private SecurityUserDetailsService userDetailsService;

            @Bean
            public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(tokenProvider, userDetailsService);

                http
                    .cors().and()
                    .csrf().disable()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                    .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated()
                    )
                    .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
            }

            @Bean
            public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
            }

            @Bean
            public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
            }
        }
        """;
    Files.writeString(secPath.resolve("SecurityConfig.java"), securityConfig);

    // ---------- 12) PasswordConfig (optional standalone bean file) ----------
    String pwConfig = """
        package com.metagen.backend.generated.security;

        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
        import org.springframework.security.crypto.password.PasswordEncoder;

        @Configuration
        public class PasswordConfig {
            @Bean
            public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
            }
        }
        """;
    Files.writeString(secPath.resolve("PasswordConfig.java"), pwConfig);

    // ---------- 13) Add basic DataLoader to seed a default admin user ----------
    String dataLoader = """
        package com.metagen.backend.generated;

        import org.springframework.boot.CommandLineRunner;
        import org.springframework.stereotype.Component;
        import org.springframework.beans.factory.annotation.Autowired;
        import com.metagen.backend.generated.repository.UserRepository;
        import com.metagen.backend.generated.repository.RoleRepository;
        import com.metagen.backend.generated.entity.User;
        import com.metagen.backend.generated.entity.Role;
        import org.springframework.security.crypto.password.PasswordEncoder;
        import java.util.Set;
        import java.util.HashSet;

        @Component
        public class DataLoader implements CommandLineRunner {

            @Autowired
            private UserRepository userRepository;

            @Autowired
            private RoleRepository roleRepository;

            @Autowired
            private PasswordEncoder passwordEncoder;

            @Override
            public void run(String... args) throws Exception {
                if (userRepository.count() == 0) {
                    Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
                        Role r = new Role();
                        r.setName("ROLE_ADMIN");
                        return roleRepository.save(r);
                    });

                    Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
                        Role r = new Role();
                        r.setName("ROLE_USER");
                        return roleRepository.save(r);
                    });

                    User admin = new User();
                    admin.setUsername("admin");
                    admin.setEmail("admin@example.com");
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setRoles(new HashSet<>(Set.of(adminRole, userRole)));
                    userRepository.save(admin);
                }
            }
        }
        """;
    Files.writeString(Paths.get("src/main/java/com/metagen/backend/generated/DataLoader.java"), dataLoader);

    // Done
}


    private void generateGlobalExceptionHandler() throws IOException {
        // Gera classe @ControllerAdvice para capturar todas as exceções
    }

    // ================= HELPERS =================
    private String mapType(String t) {
        return switch (t == null ? "" : t) {
            case "string" -> "String";
            case "number" -> "Long";
            case "boolean" -> "Boolean";
            default -> "String";
        };
    }

    private String capitalize(String str) {
        return str == null || str.isEmpty() ? str : str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
