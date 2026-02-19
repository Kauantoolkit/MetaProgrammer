package com.example.meta_backend.service.generator.base;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class ThymeleafFrontGenerator {

    /**
     * Gera as templates Thymeleaf + assets (css + static login) dentro do projeto alvo (appName).
     *
     * Observações importantes:
     * - Requer Java 17+ (text blocks & switch expressions).
     * - Este gerador **não** injeta estilos inline nas páginas — todos estilos ficam em static/css/style.css.
     * - Helpers safeMap/safeAttrList/safeStringList protegem contra entradas dinâmicas do meta-gerador.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> safeMap(Object obj) {
        return (obj instanceof Map) ? (Map<String, Object>) obj : Map.of();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> safeAttrList(Object obj) {
        return (obj instanceof List) ? (List<Map<String, Object>>) obj : List.of();
    }

    @SuppressWarnings("unchecked")
    private List<String> safeStringList(Object obj) {
        return (obj instanceof List) ? (List<String>) obj : List.of();
    }

    public void generateTemplates(List<Map<String, Object>> entities, String appName) throws IOException {
        // baseDir e staticDir referem-se ao projeto alvo (o backend que será gerado)
        String baseDir = appName + "/src/main/resources/templates/backoffice";
        String staticDir = appName + "/src/main/resources/static";

        // Cria diretórios base (layouts, fragments, backoffice e static/css)
        Files.createDirectories(Paths.get(baseDir, "layouts"));
        Files.createDirectories(Paths.get(baseDir, "fragments"));
        Files.createDirectories(Paths.get(staticDir));
        Files.createDirectories(Paths.get(staticDir, "css"));

        // Arquivos base
        Files.writeString(Paths.get(baseDir, "layouts", "base.html"), baseLayoutContent());
        Files.writeString(Paths.get(baseDir, "fragments", "sidebar.html"), sidebarContent(entities));
        Files.writeString(Paths.get(baseDir,  "index.html"), backofficeIndexContent(entities));
        Files.writeString(Paths.get(baseDir, "login.html"), loginContent());
        Files.writeString(Paths.get(baseDir, "error.html"), errorContent());

        // Static login html (arquivo estático em /static)
        Files.writeString(Paths.get(staticDir, "login.html"), staticLoginContent());

        // CSS centralizado
        Files.writeString(Paths.get(staticDir, "css", "style.css"), cssContent());

        // Templates por entidade (apenas se CRUD estiver habilitado)
        for (Map<String, Object> entity : entities) {
            Map<String, Object> api = safeMap(entity.get("api"));
            List<String> endpoints = safeStringList(api.get("endpoints"));

            if (endpoints.contains("crud")) {
                String name = (String) entity.get("name");
                List<Map<String, Object>> attrs = safeAttrList(entity.get("attributes"));

                Path entityDir = Paths.get(baseDir, name.toLowerCase());
                Files.createDirectories(entityDir);

                Files.writeString(entityDir.resolve("list.html"), generateListHtml(name, attrs));
                Files.writeString(entityDir.resolve("addEditDialog.html"), generateDialogHtml(name, attrs));
            }
        }
    }

    /* ----------------------
       Templates (text blocks)
       ---------------------- */

    private String baseLayoutContent() {
        return """
                <!DOCTYPE html>
                <html xmlns:th="http://www.thymeleaf.org" xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width,initial-scale=1"/>
                    <title>Generated App</title>
                    <link rel="stylesheet" href="/css/style.css"/>
                    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"/>
                </head>
                <body>
                    <div class="d-flex">
                        <div th:replace="fragments/sidebar :: sidebar"></div>
                        <div class="flex-grow-1 p-3">
                            <div class="d-flex justify-content-end mb-3">
                                <a th:href="@{/logout}" class="btn btn-outline-danger btn-sm">Logout</a>
                            </div>
                            <div layout:fragment="content"></div>
                        </div>
                    </div>

                    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
                    <script>
                        // openEditModal, deleteItem, submitForm — estes helpers são usados pelos templates gerados.
                        function openEditModal(entity, id) {
                            fetch('/api/' + entity + '/' + id)
                                .then(res => res.json())
                                .then(data => {
                                    const form = document.querySelector('#addEditModal-' + entity + ' form');
                                    if (!form) return;
                                    Object.keys(data).forEach(key => {
                                        if (form.elements[key]) {
                                            const el = form.elements[key];
                                            if (el.type === 'checkbox') {
                                                el.checked = !!data[key];
                                            } else {
                                                el.value = data[key] == null ? '' : String(data[key]);
                                            }
                                        }
                                    });
                                    new bootstrap.Modal(document.getElementById('addEditModal-' + entity)).show();
                                });
                        }

                        function deleteItem(entity, id) {
                            if (confirm('Deseja realmente deletar este registro?')) {
                                fetch('/api/' + entity + '/' + id, { method: 'DELETE' })
                                    .then(() => location.reload());
                            }
                        }

                        function submitForm(entity) {
                            const form = document.querySelector('#form-' + entity);
                            if (!form) return;
                            const formData = new FormData(form);
                            const data = {};
                            for (let [key, value] of formData.entries()) {
                                if (value === 'true') data[key] = true;
                                else if (value === 'false') data[key] = false;
                                else if (value !== '' && !isNaN(value) && value.trim() !== '') data[key] = Number(value);
                                else data[key] = value;
                            }
                            fetch('/api/' + entity, {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify(data)
                            }).then(() => location.reload());
                        }
                    </script>
                </body>
                </html>
                """;
    }

    private String sidebarContent(List<Map<String, Object>> entities) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                <div th:fragment="sidebar" class="sidebar">
                    <h5>Entidades</h5>
                    <ul class="nav flex-column">
                """);

        for (Map<String, Object> entity : entities) {
            Map<String, Object> api = safeMap(entity.get("api"));
            List<String> endpoints = safeStringList(api.get("endpoints"));

            if (endpoints.contains("crud")) {
                String name = (String) entity.get("name");
                String icon = switch (name.toLowerCase()) {
                    case "conversiontype" -> "📊";
                    case "conversionjob" -> "⚙️";
                    case "clientusage" -> "👥";
                    default -> "📄";
                };
                sb.append("<li class=\"nav-item mb-2\">")
                  .append(String.format("<a class=\"nav-link d-flex align-items-center\" th:href=\"@{/backoffice/%s}\">", name.toLowerCase()))
                  .append(String.format("<span class=\"me-2\">%s</span> %s", icon, name))
                  .append("</a>")
                  .append("</li>");
            }
        }

        sb.append("""
                    </ul>
                </div>
                """);
        return sb.toString();
    }

    private String backofficeIndexContent(List<Map<String, Object>> entities) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                <!DOCTYPE html>
                <html xmlns:th="http://www.thymeleaf.org" xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout" layout:decorate="~{layouts/base}">
                <head>
                    <meta charset="UTF-8"/>
                    <title>Backoffice</title>
                </head>
                <body>
                <div layout:fragment="content" class="dashboard-container">
                    <h2>Painel do Backoffice</h2>
                    <ul>
                """);

        for (Map<String, Object> entity : entities) {
            Map<String, Object> api = safeMap(entity.get("api"));
            List<String> endpoints = safeStringList(api.get("endpoints"));

            if (endpoints.contains("crud")) {
                String name = (String) entity.get("name");
                sb.append(String.format("<li><a th:href=\"@{/backoffice/%s}\">%s</a></li>", name.toLowerCase(), name));
            }
        }

        sb.append("""
                    </ul>
                </div>
                </body>
                </html>
                """);
        return sb.toString();
    }

    /**
     * Gera a list.html para cada entidade.
     * Importante: mantém referências Thymeleaf (th:each, th:text, th:href etc) — o backend alvo deve prover
     * o atributo "items" com a lista de registros.
     */
    private String generateListHtml(String entityName, List<Map<String, Object>> attrs) {
        String entityPath = entityName.toLowerCase();
        StringBuilder sb = new StringBuilder();

        sb.append("""
                <!DOCTYPE html>
                <html xmlns:th="http://www.thymeleaf.org">
                <head>
                    <meta charset="UTF-8"/>
                    <title>""").append(entityName).append(" List</title>")
          .append("""
                    <link rel="stylesheet" href="/css/style.css"/>
                    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"/>
                </head>
                <body>
                <div class="d-flex">
                <div th:replace="fragments/sidebar :: sidebar"></div>
                <div class="content-area">
                """)
          .append("<h2>Lista de ").append(entityName).append("</h2>\n")
          .append("<button class=\"btn btn-primary mb-3\" data-bs-toggle=\"modal\" data-bs-target=\"#addEditModal-")
          .append(entityPath).append("\">Adicionar</button>\n")
          .append("<table class=\"table\">\n<thead>\n<tr>\n");

        for (Map<String, Object> attr : attrs) {
            sb.append("<th>").append(attr.get("name")).append("</th>\n");
        }
        sb.append("<th>Ações</th>\n</tr>\n</thead>\n<tbody>\n<tr th:each=\"item : ${items}\">\n");

        for (Map<String, Object> attr : attrs) {
            sb.append("<td th:text=\"${item.").append(attr.get("name")).append("}\"></td>\n");
        }

        sb.append("<td>\n")
          .append("<button class=\"btn btn-sm btn-warning\" th:onclick=\"|openEditModal('").append(entityPath).append("','${item.id}')|\">Editar</button>\n")
          .append("<button class=\"btn btn-sm btn-danger\" th:onclick=\"|deleteItem('").append(entityPath).append("','${item.id}')|\">Deletar</button>\n")
          .append("</td>\n</tr>\n</tbody>\n</table>\n");

        // inclui o fragment do modal
        sb.append(String.format("<div th:insert=\"~{%s/addEditDialog :: modal}\"></div>\n", entityPath));

        sb.append("""
                </div>
                </div>
                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
                </body>
                </html>
                """);

        return sb.toString();
    }

    /**
     * Gera apenas o fragmento modal para add/edit da entidade.
     * Este template fica em templates/<entity>/addEditDialog.html e é incluído na list.html.
     */
    private String generateDialogHtml(String entityName, List<Map<String, Object>> attrs) {
        String entityPath = entityName.toLowerCase();
        StringBuilder sb = new StringBuilder();

        sb.append("<div th:fragment=\"modal\">\n")
          .append("<div class=\"modal fade\" id=\"addEditModal-").append(entityPath).append("\" tabindex=\"-1\">\n")
          .append("<div class=\"modal-dialog\">\n<div class=\"modal-content\">\n")
          .append("<div class=\"modal-header\"><h5 class=\"modal-title\">Adicionar/Editar ").append(entityName).append("</h5></div>\n")
          .append("<div class=\"modal-body\">\n")
          .append("<form id=\"form-").append(entityPath).append("\">\n");

        for (Map<String, Object> attr : attrs) {
            String attrName = (String) attr.get("name");
            String type = (String) attr.getOrDefault("type", "string");
            boolean isRelation = "relation".equalsIgnoreCase(type);

            sb.append("<div class=\"mb-3\">\n<label class=\"form-label\">").append(attrName).append("</label>\n");

            if (isRelation) {
                String relEntity = (String) attr.get("relationEntity");
                sb.append(String.format("<select class=\"form-select\" name=\"%s\">\n<option th:each=\"e : ${%sList}\" th:value=\"${e.id}\" th:text=\"${e}\"></option>\n</select>\n",
                        attrName, relEntity.toLowerCase()));
            } else if ("boolean".equalsIgnoreCase(type)) {
                sb.append(String.format("<select class=\"form-select\" name=\"%s\">\n<option value=\"true\">Sim</option>\n<option value=\"false\">Não</option>\n</select>\n", attrName));
            } else {
                String inputType = switch (type.toLowerCase()) {
                    case "number" -> "number";
                    case "date" -> "date";
                    default -> "text";
                };
                sb.append(String.format("<input class=\"form-control\" type=\"%s\" name=\"%s\" />\n", inputType, attrName));
            }

            sb.append("</div>\n");
        }

        sb.append("<button type=\"button\" class=\"btn btn-primary\" onclick=\"submitForm('").append(entityPath).append("')\">Salvar</button>\n")
          .append("</form>\n</div></div></div></div>\n</div>\n");

        return sb.toString();
    }

    private String loginContent() {
        return """
                <!DOCTYPE html>
                <html xmlns:th="http://www.thymeleaf.org">
                <head>
                    <meta charset="UTF-8"/>
                    <meta name="viewport" content="width=device-width,initial-scale=1"/>
                    <title>Admin Login</title>
                    <link rel="stylesheet" href="/css/style.css"/>
                </head>
                <body>
                <div class="login-container">
                    <h2 class="text-center mb-4">Admin Login</h2>
                    <form th:action="@{/login}" method="post">
                        <div class="mb-3"><label>Username</label><input type="text" name="username" class="form-control"/></div>
                        <div class="mb-3"><label>Password</label><input type="password" name="password" class="form-control"/></div>
                        <button class="btn btn-primary btn-login" type="submit">Login</button>
                    </form>
                </div>
                </body>
                </html>
                """;
    }

    private String staticLoginContent() {
        // arquivo estático simples (usado por /static/login.html)
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <meta name="viewport" content="width=device-width,initial-scale=1"/>
                    <title>Admin Login</title>
                    <link rel="stylesheet" href="/css/style.css"/>
                </head>
                <body>
                <div class="login-container">
                    <h2 class="text-center mb-4">Admin Login</h2>
                    <form action="/login" method="post">
                        <div class="mb-3"><label>Username</label><input type="text" name="username" class="form-control"/></div>
                        <div class="mb-3"><label>Password</label><input type="password" name="password" class="form-control"/></div>
                        <button class="btn btn-primary btn-login" type="submit">Login</button>
                    </form>
                </div>
                </body>
                </html>
                """;
    }

    private String errorContent() {
        return """
                <!DOCTYPE html>
                <html xmlns:th="http://www.thymeleaf.org">
                <head>
                    <meta charset="UTF-8"/>
                    <meta name="viewport" content="width=device-width,initial-scale=1"/>
                    <title>Error</title>
                    <link rel="stylesheet" href="/css/style.css"/>
                </head>
                <body>
                <div class="error-container">
                    <div class="error-code" th:text="${status}">Error</div>
                    <h2 th:text="${error}">Something went wrong</h2>
                    <div class="error-message" th:text="${message}">An unexpected error occurred.</div>
                    <a href="/" class="btn-home">Go to Home</a>
                </div>
                </body>
                </html>
                """;
    }

    /** CSS centralizado que o gerador salva em static/css/style.css do app alvo. */
    private String cssContent() {
        return """
                :root {
                  --primary-1: #667eea;
                  --primary-2: #764ba2;
                  --accent-1: #f093fb;
                  --accent-2: #f5576c;
                  --danger-1: #ff6b6b;
                  --danger-2: #ee5a24;
                  --surface: rgba(255,255,255,0.98);
                  --text: #222;
                }

                * { box-sizing: border-box; }
                html,body { height: 100%; margin:0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg,var(--primary-1),var(--primary-2)); color:var(--text); }

                .d-flex { display:flex; }
                .sidebar { width:260px; padding:18px; background:var(--surface); border-right:1px solid rgba(0,0,0,0.04); }
                .sidebar h5 { margin:0 0 .5rem 0; color:var(--primary-2); }
                .nav { list-style:none; padding-left:0; margin:0; }
                .nav .nav-item { margin-bottom:.5rem; }
                .nav .nav-link { display:flex; align-items:center; gap:.5rem; padding:.4rem .6rem; border-radius:8px; color:var(--text); text-decoration:none; }
                .nav .nav-link:hover { background: rgba(102,126,234,0.06); }

                .content-area { flex:1; padding:20px; margin:18px; background:var(--surface); border-radius:12px; box-shadow:0 10px 30px rgba(0,0,0,0.08); }

                table.table { width:100%; border-collapse:collapse; background:transparent; }
                table.table thead th { background: linear-gradient(45deg,var(--primary-1),var(--primary-2)); color:#fff; padding:.75rem; font-weight:600; text-align:left; border:none; }
                table.table tbody td { padding:.6rem; border-top:1px solid rgba(0,0,0,0.04); vertical-align:middle; }
                table.table tbody tr:hover { background: rgba(102,126,234,0.03); }

                .btn { font-weight:600; border:0; padding:.45rem .9rem; border-radius:999px; cursor:pointer; }
                .btn-primary { background: linear-gradient(45deg,var(--primary-1),var(--primary-2)); color:#fff; }
                .btn-warning { background: linear-gradient(45deg,var(--accent-1),var(--accent-2)); color:#fff; }
                .btn-danger { background: linear-gradient(45deg,var(--danger-1),var(--danger-2)); color:#fff; }
                .btn-sm { padding:.35rem .55rem; border-radius:8px; }

                .form-control { width:100%; padding:.5rem .75rem; border-radius:8px; border:1px solid rgba(0,0,0,0.08); background:#fff; }
                .form-control:focus { outline:none; box-shadow:0 0 0 .12rem rgba(102,126,234,0.12); border-color:var(--primary-1); }

                .login-container, .error-container { max-width:520px; margin:6vh auto; padding:28px; background:#fff; border-radius:12px; box-shadow:0 12px 40px rgba(0,0,0,0.08); }
                .btn-login { width:100%; padding:.75rem; border-radius:999px; }

                .error-code { font-size:3.5rem; font-weight:800; color:var(--primary-1); margin-bottom:.5rem; }
                .btn-home { display:inline-block; padding:.6rem 1rem; border-radius:999px; background:linear-gradient(45deg,var(--primary-1),var(--primary-2)); color:#fff; text-decoration:none; }

                @media (max-width:900px) {
                  .sidebar { display:none; }
                  .content-area { margin:0; border-radius:0; padding:12px; }
                }
                """;
    }
}
