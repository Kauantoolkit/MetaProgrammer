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

        // Templates por entidade
        for (Map<String, Object> entity : entities) {
            String name = (String) entity.get("name");
            List<Map<String, Object>> attrs = (List<Map<String, Object>>) entity.getOrDefault("attributes", List.of());
            Path entityDir = Paths.get(BASE_DIR + name.toLowerCase());
            Files.createDirectories(entityDir);

            Files.writeString(entityDir.resolve("list.html"), generateListHtml(name, attrs));
            Files.writeString(entityDir.resolve("addEditDialog.html"), generateDialogHtml(name, attrs));
        }
    }

    private String baseLayoutContent() {
        return """
                <!DOCTYPE html>
                <html xmlns:th="http://www.thymeleaf.org">
                <head>
                    <meta charset="UTF-8">
                    <title>Generated App</title>
                    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"/>
                </head>
                <body>
                    <div class="d-flex">
                        <div th:replace="fragments/sidebar :: sidebar"></div>
                        <div class="flex-grow-1 p-3" th:insert="~{::content}"></div>
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
                """;
    }

    private String sidebarContent(List<Map<String, Object>> entities) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                <div th:fragment="sidebar" class="bg-light p-3" style="width:200px; height:100vh;">
                    <h5>Menu</h5>
                    <ul class="nav flex-column">
                """);

        sb.append("<li class=\"nav-item\">\n")
          .append("<a class=\"nav-link\" th:href=\"@{'/backoffice'}\">Backoffice</a>\n")
          .append("</li>\n");

        for (Map<String, Object> entity : entities) {
            String name = (String) entity.get("name");
            sb.append("<li class=\"nav-item\">\n")
              .append(String.format("<a class=\"nav-link\" th:href=\"@{'/%s/list'}\">%s</a>\n", name.toLowerCase(), name))
              .append("</li>\n");
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
                <div th:fragment="content" class="container">
                    <h2>Painel do Backoffice</h2>
                    <ul>
                """);

        for (Map<String, Object> entity : entities) {
            String name = (String) entity.get("name");
            sb.append(String.format("<li><a th:href=\"@{'/%s/list'}\">%s</a></li>\n", name.toLowerCase(), name));
        }

        sb.append("""
                    </ul>
                </div>
                """);
        return sb.toString();
    }

    private String generateListHtml(String entityName, List<Map<String, Object>> attrs) {
        StringBuilder sb = new StringBuilder();
        String entityPath = entityName.toLowerCase();

        sb.append("<div th:fragment=\"content\" class=\"container\">\n")
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
            } else {
                String inputType = switch (type.toLowerCase()) {
                    case "number" -> "number";
                    case "boolean" -> "checkbox";
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
}
