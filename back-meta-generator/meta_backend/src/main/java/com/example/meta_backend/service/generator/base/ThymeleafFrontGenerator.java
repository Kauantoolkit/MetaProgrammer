package com.example.meta_backend.service.generator.base;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

public class ThymeleafFrontGenerator {

    private static final String BASE_DIR = "generated_app/src/main/resources/templates/";

    public void generateTemplates(List<Map<String, Object>> entities) throws IOException {
        // Cria pastas base
        Files.createDirectories(Paths.get(BASE_DIR + "layouts"));
        Files.createDirectories(Paths.get(BASE_DIR + "fragments"));
        Files.createDirectories(Paths.get(BASE_DIR + "backoffice"));

        // Layout base
        Files.writeString(Paths.get(BASE_DIR + "layouts/base.html"), baseLayoutContent());

        // Sidebar
        Files.writeString(Paths.get(BASE_DIR + "fragments/sidebar.html"), sidebarContent(entities));

        // Backoffice index
        Files.writeString(Paths.get(BASE_DIR + "backoffice/index.html"), backofficeIndexContent(entities));

        // Backoffice login
        Files.writeString(Paths.get(BASE_DIR + "backoffice/login.html"), loginContent());

        // Error page
        Files.writeString(Paths.get(BASE_DIR + "error.html"), errorContent());

        // Login page (static)
        Files.createDirectories(Paths.get("generated_app/src/main/resources/static"));
        Files.writeString(Paths.get("generated_app/src/main/resources/static/login.html"), staticLoginContent());

        // Templates por entidade (apenas se CRUD estiver habilitado)
        for (Map<String, Object> entity : entities) {
            Map<String, Object> api = (Map<String, Object>) entity.getOrDefault("api", Map.of());
            List<String> endpoints = (List<String>) api.getOrDefault("endpoints", List.of());

            if (endpoints.contains("crud")) {
                String name = (String) entity.get("name");
                List<Map<String, Object>> attrs = (List<Map<String, Object>>) entity.getOrDefault("attributes", List.of());
                Path entityDir = Paths.get(BASE_DIR + name.toLowerCase());
                Files.createDirectories(entityDir);

                Files.writeString(entityDir.resolve("list.html"), generateListHtml(name, attrs));
                Files.writeString(entityDir.resolve("addEditDialog.html"), generateDialogHtml(name, attrs));
            }
        }
    }

    private String baseLayoutContent() {
        return """
                <!DOCTYPE html>
                <html xmlns:th="http://www.thymeleaf.org" xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout">
                <head>
                    <meta charset="UTF-8">
                    <title>Generated App</title>
                    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"/>
                    <link rel="stylesheet" href="/css/style.css"/>
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
                        // Função para abrir modal e preencher campos via REST
                        function openEditModal(entity, id) {
                            fetch('/api/' + entity + '/' + id)
                                .then(res => res.json())
                                .then(data => {
                                    const form = document.querySelector('#addEditModal-' + entity + ' form');
                                    Object.keys(data).forEach(key => {
                                        if(form[key]) {
                                            if (data[key] === true) {
                                                form[key].value = 'true';
                                            } else if (data[key] === false) {
                                                form[key].value = 'false';
                                            } else {
                                                form[key].value = String(data[key]);
                                            }
                                        }
                                    });
                                    new bootstrap.Modal(document.getElementById('addEditModal-' + entity)).show();
                                });
                        }

                        // Deletar item via REST
                        function deleteItem(entity, id) {
                            if(confirm('Deseja realmente deletar este registro?')) {
                                fetch('/api/' + entity + '/' + id, { method: 'DELETE' })
                                    .then(() => location.reload());
                            }
                        }

                        // Salvar via REST (novo ou editar)
        function submitForm(entity) {
            const form = document.querySelector('#form-' + entity);
            const formData = new FormData(form);
            const data = {};

            for (let [key, value] of formData.entries()) {
                if (value === 'true') {
                    data[key] = true;
                } else if (value === 'false') {
                    data[key] = false;
                } else if (!isNaN(value) && value !== '') {
                    data[key] = Number(value);
                } else {
                    data[key] = value;
                }
            }

            fetch('/api/' + entity, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            })
            .then(res => res.json())
            .then(() => location.reload());
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
            Map<String, Object> api = (Map<String, Object>) entity.getOrDefault("api", Map.of());
            List<String> endpoints = (List<String>) api.getOrDefault("endpoints", List.of());

            if (endpoints.contains("crud")) {
                String name = (String) entity.get("name");
                String icon = switch (name.toLowerCase()) {
                    case "conversiontype" -> "📊";
                    case "conversionjob" -> "⚙️";
                    case "clientusage" -> "👥";
                    default -> "📄";
                };
                sb.append("<li class=\"nav-item mb-2\">\n")
                  .append(String.format("<a class=\"nav-link d-flex align-items-center\" th:href=\"@{'/%s/list'}\">\n", name.toLowerCase()))
                  .append(String.format("<span>%s</span> %s\n", icon, name))
                  .append("</a>\n")
                  .append("</li>\n");
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
                    <title>Backoffice</title>
                </head>
                <body>
                    <div layout:fragment="content" class="dashboard-container">
                        <h2>Painel do Backoffice</h2>
                        <ul>
                """);

        for (Map<String, Object> entity : entities) {
            Map<String, Object> api = (Map<String, Object>) entity.getOrDefault("api", Map.of());
            List<String> endpoints = (List<String>) api.getOrDefault("endpoints", List.of());

            if (endpoints.contains("crud")) {
                String name = (String) entity.get("name");
                sb.append(String.format("<li><a th:href=\"@{'/%s/list'}\">%s</a></li>\n", name.toLowerCase(), name));
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

    private String generateListHtml(String entityName, List<Map<String, Object>> attrs) {
        StringBuilder sb = new StringBuilder();
        String entityPath = entityName.toLowerCase();

        sb.append("<!DOCTYPE html>\n")
          .append("<html xmlns:th=\"http://www.thymeleaf.org\">\n")
          .append("<head>\n")
          .append("<meta charset=\"UTF-8\">\n")
          .append(String.format("<title>%s List</title>\n", entityName))
          .append("<link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css\"/>\n")
          .append("<style>\n")
          .append("body {\n")
          .append("    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n")
          .append("    min-height: 100vh;\n")
          .append("    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n")
          .append("}\n")
          .append(".sidebar {\n")
          .append("    background: rgba(255, 255, 255, 0.95);\n")
          .append("    backdrop-filter: blur(10px);\n")
          .append("}\n")
          .append(".content-area {\n")
          .append("    background: rgba(255, 255, 255, 0.95);\n")
          .append("    border-radius: 15px;\n")
          .append("    box-shadow: 0 10px 30px rgba(0,0,0,0.3);\n")
          .append("    margin: 20px;\n")
          .append("    padding: 30px;\n")
          .append("    backdrop-filter: blur(10px);\n")
          .append("}\n")
          .append("h2 {\n")
          .append("    color: #333;\n")
          .append("    font-weight: 700;\n")
          .append("    margin-bottom: 30px;\n")
          .append("}\n")
          .append(".table {\n")
          .append("    background: white;\n")
          .append("    border-radius: 10px;\n")
          .append("    overflow: hidden;\n")
          .append("    box-shadow: 0 5px 15px rgba(0,0,0,0.1);\n")
          .append("}\n")
          .append(".table thead th {\n")
          .append("    background: linear-gradient(45deg, #667eea, #764ba2);\n")
          .append("    color: white;\n")
          .append("    border: none;\n")
          .append("    font-weight: 600;\n")
          .append("}\n")
          .append(".table tbody tr:hover {\n")
          .append("    background: rgba(102, 126, 234, 0.1);\n")
          .append("}\n")
          .append(".btn-primary {\n")
          .append("    background: linear-gradient(45deg, #667eea, #764ba2);\n")
          .append("    border: none;\n")
          .append("    border-radius: 25px;\n")
          .append("    padding: 10px 25px;\n")
          .append("    font-weight: 600;\n")
          .append("    transition: all 0.3s ease;\n")
          .append("}\n")
          .append(".btn-primary:hover {\n")
          .append("    transform: translateY(-2px);\n")
          .append("    box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);\n")
          .append("}\n")
          .append(".btn-warning {\n")
          .append("    background: linear-gradient(45deg, #f093fb, #f5576c);\n")
          .append("    border: none;\n")
          .append("    border-radius: 20px;\n")
          .append("    transition: all 0.3s ease;\n")
          .append("}\n")
          .append(".btn-warning:hover {\n")
          .append("    transform: translateY(-2px);\n")
          .append("    box-shadow: 0 5px 15px rgba(245, 87, 108, 0.4);\n")
          .append("}\n")
          .append(".btn-danger {\n")
          .append("    background: linear-gradient(45deg, #ff6b6b, #ee5a24);\n")
          .append("    border: none;\n")
          .append("    border-radius: 20px;\n")
          .append("    transition: all 0.3s ease;\n")
          .append("}\n")
          .append(".btn-danger:hover {\n")
          .append("    transform: translateY(-2px);\n")
          .append("    box-shadow: 0 5px 15px rgba(238, 90, 36, 0.4);\n")
          .append("}\n")
          .append("</style>\n")
          .append("</head>\n")
          .append("<body>\n")
          .append("<div class=\"d-flex\">\n")
          .append("<div th:replace=\"fragments/sidebar :: sidebar\"></div>\n")
          .append("<div class=\"content-area\">\n")
          .append(String.format("<h2>Lista de %s</h2>\n", entityName))
          .append(String.format("<button class=\"btn btn-primary mb-3\" data-bs-toggle=\"modal\" data-bs-target=\"#addEditModal-%s\">Adicionar</button>\n", entityPath))
          .append("<table class=\"table table-bordered\">\n<thead>\n<tr>\n");

        for (Map<String, Object> attr : attrs) {
            sb.append("<th>").append(attr.get("name")).append("</th>\n");
        }
        sb.append("<th>Ações</th>\n</tr>\n</thead>\n<tbody>\n<tr th:each=\"item : ${items}\">\n");

        for (Map<String, Object> attr : attrs) {
            sb.append("<td th:text=\"${item.").append(attr.get("name")).append("}\"></td>\n");
        }

        sb.append(String.format("""
            <td>
                <button class="btn btn-sm btn-warning" th:onclick="|openEditModal('%s','${item.id}')|">Editar</button>
                <button class="btn btn-sm btn-danger" th:onclick="|deleteItem('%s','${item.id}')|">Deletar</button>
            </td>
        </tr>
    </tbody>
    </table>
    </div>
    <div th:insert="~{%s/addEditDialog :: modal}"></div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Função para abrir modal e preencher campos via REST
        function openEditModal(entity, id) {
            fetch('/api/' + entity + '/' + id)
                .then(res => res.json())
                .then(data => {
                    const form = document.querySelector('#addEditModal-' + entity + ' form');
                    Object.keys(data).forEach(key => {
                        if(form[key]) form[key].value = data[key];
                    });
                    new bootstrap.Modal(document.getElementById('addEditModal-' + entity)).show();
                });
        }

        // Deletar item via REST
        function deleteItem(entity, id) {
            if(confirm('Deseja realmente deletar este registro?')) {
                fetch('/api/' + entity + '/' + id, { method: 'DELETE' })
                    .then(() => location.reload());
            }
        }

        // Salvar via REST (novo ou editar)
        function submitForm(entity) {
            const form = document.querySelector('#form-' + entity);
            const data = Object.fromEntries(new FormData(form).entries());

            fetch('/api/' + entity, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            })
            .then(res => res.json())
            .then(() => location.reload());
        }
    </script>
    </body>
    </html>
    """, entityPath, entityPath, entityPath));

        return sb.toString();
    }

    private String generateDialogHtml(String entityName, List<Map<String, Object>> attrs) {
        String entityPath = entityName.toLowerCase();
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("""
                <div th:fragment="modal">
                    <div class="modal fade" id="addEditModal-%s" tabindex="-1">
                        <div class="modal-dialog">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <h5 class="modal-title">Adicionar/Editar %s</h5>
                                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                </div>
                                <div class="modal-body">
                                    <form id="form-%s">
                """, entityPath, entityName, entityPath));

        for (Map<String, Object> attr : attrs) {
            String attrName = (String) attr.get("name");
            String type = (String) attr.getOrDefault("type", "string");
            boolean isRelation = type.equalsIgnoreCase("relation");

            sb.append("<div class=\"mb-3\">\n")
              .append("<label class=\"form-label\">").append(attrName).append("</label>\n");

            if (isRelation) {
                String relEntity = (String) attr.get("relationEntity");
                sb.append(String.format("<select class=\"form-select\" name=\"%s\">\n<option th:each=\"e : ${%sList}\" th:value=\"${e.id}\" th:text=\"${e}\"></option>\n</select>\n", attrName, relEntity.toLowerCase()));
            } else if (type.equalsIgnoreCase("boolean")) {
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

        sb.append(String.format("""
                                        <button type="button" class="btn btn-primary" onclick="submitForm('%s')">Salvar</button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                """, entityPath));

        return sb.toString();
    }

    private String loginContent() {
        return """
                <!DOCTYPE html>
                <html xmlns:th="http://www.thymeleaf.org">
                <head>
                    <meta charset="UTF-8">
                    <title>Admin Login</title>
                    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"/>
                    <style>
                        body {
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            min-height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                        }
                        .login-container {
                            background: rgba(255, 255, 255, 0.95);
                            border-radius: 15px;
                            box-shadow: 0 10px 30px rgba(0,0,0,0.3);
                            padding: 40px;
                            width: 100%;
                            max-width: 400px;
                            backdrop-filter: blur(10px);
                        }
                        .btn-login {
                            background: linear-gradient(45deg, #667eea, #764ba2);
                            border: none;
                            border-radius: 25px;
                            padding: 12px 30px;
                            font-weight: 600;
                            width: 100%;
                            transition: all 0.3s ease;
                        }
                        .btn-login:hover {
                            transform: translateY(-2px);
                            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
                        }
                        .form-control:focus {
                            border-color: #667eea;
                            box-shadow: 0 0 0 0.2rem rgba(102, 126, 234, 0.25);
                        }
                    </style>
                </head>
                <body>
                    <div class="login-container">
                        <h2 class="text-center mb-4">Admin Login</h2>
                        <form th:action="@{/login}" method="post">
                            <div class="mb-3">
                                <label for="username" class="form-label">Username</label>
                                <input type="text" class="form-control" id="username" name="username" required>
                            </div>
                            <div class="mb-3">
                                <label for="password" class="form-label">Password</label>
                                <input type="password" class="form-control" id="password" name="password" required>
                            </div>
                            <button type="submit" class="btn btn-primary btn-login">Login</button>
                        </form>
                        <div th:if="${param.error}" class="alert alert-danger mt-3">
                            Invalid username or password.
                        </div>
                        <div th:if="${param.logout}" class="alert alert-success mt-3">
                            You have been logged out successfully.
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String staticLoginContent() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Admin Login</title>
                    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"/>
                    <style>
                        body {
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            min-height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                        }
                        .login-container {
                            background: rgba(255, 255, 255, 0.95);
                            border-radius: 15px;
                            box-shadow: 0 10px 30px rgba(0,0,0,0.3);
                            padding: 40px;
                            width: 100%;
                            max-width: 400px;
                            backdrop-filter: blur(10px);
                        }
                        .btn-login {
                            background: linear-gradient(45deg, #667eea, #764ba2);
                            border: none;
                            border-radius: 25px;
                            padding: 12px 30px;
                            font-weight: 600;
                            width: 100%;
                            transition: all 0.3s ease;
                        }
                        .btn-login:hover {
                            transform: translateY(-2px);
                            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
                        }
                        .form-control:focus {
                            border-color: #667eea;
                            box-shadow: 0 0 0 0.2rem rgba(102, 126, 234, 0.25);
                        }
                    </style>
                </head>
                <body>
                    <div class="login-container">
                        <h2 class="text-center mb-4">Admin Login</h2>
                        <form action="/login" method="post">
                            <div class="mb-3">
                                <label for="username" class="form-label">Username</label>
                                <input type="text" class="form-control" id="username" name="username" required>
                            </div>
                            <div class="mb-3">
                                <label for="password" class="form-label">Password</label>
                                <input type="password" class="form-control" id="password" name="password" required>
                            </div>
                            <button type="submit" class="btn btn-primary btn-login">Login</button>
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
                    <meta charset="UTF-8">
                    <title>Error</title>
                    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"/>
                    <style>
                        body {
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            min-height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        }
                        .error-container {
                            background: rgba(255, 255, 255, 0.95);
                            border-radius: 15px;
                            box-shadow: 0 10px 30px rgba(0,0,0,0.3);
                            padding: 40px;
                            width: 100%;
                            max-width: 600px;
                            backdrop-filter: blur(10px);
                            text-align: center;
                        }
                        .error-code {
                            font-size: 4rem;
                            font-weight: bold;
                            color: #667eea;
                            margin-bottom: 20px;
                        }
                        .error-message {
                            font-size: 1.2rem;
                            color: #333;
                            margin-bottom: 30px;
                        }
                        .btn-home {
                            background: linear-gradient(45deg, #667eea, #764ba2);
                            border: none;
                            border-radius: 25px;
                            padding: 12px 30px;
                            font-weight: 600;
                            color: white;
                            text-decoration: none;
                            transition: all 0.3s ease;
                        }
                        .btn-home:hover {
                            transform: translateY(-2px);
                            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
                            color: white;
                            text-decoration: none;
                        }
                    </style>
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
    }}
