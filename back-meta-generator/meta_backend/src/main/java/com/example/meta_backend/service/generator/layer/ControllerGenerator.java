package com.example.meta_backend.service.generator.layer;

import com.example.meta_backend.service.generator.ProjectWriter;
import com.example.meta_backend.service.generator.model.AppSchema.EntitySpec;
import com.example.meta_backend.service.generator.model.AppSchema.FieldSpec;
import com.example.meta_backend.service.generator.model.FieldType;
import org.springframework.stereotype.Component;

/**
 * Controller em /api/v1/<recurso>, resposta no envelope ApiResponse do basilisk-core e cada endpoint com
 * @RequiresPermission("<recurso>:read|write") — OWNER e ADMIN passam sempre; MEMBER precisa de papel com a
 * permissão. Antes o controller devolvia a entidade crua e não tinha checagem nenhuma.
 */
@Component
public class ControllerGenerator {

    public void generate(EntitySpec e, ProjectWriter out) {
        if (!e.hasApi()) return;
        String c = e.className();
        String resp = c + "Response";
        String req = c + "Request";
        String read = "\"" + e.resource() + ":read\"";
        String write = "\"" + e.resource() + ":write\"";

        StringBuilder m = new StringBuilder();
        m.append("    @GetMapping\n")
         .append("    @RequiresPermission(").append(read).append(")\n")
         .append("    public ResponseEntity<ApiResponse<Page<").append(resp).append(">>> list(Pageable pageable) {\n")
         .append("        return ResponseEntity.ok(ApiResponse.ok(service.list(pageable)));\n")
         .append("    }\n\n")
         .append("    @GetMapping(\"/{id}\")\n")
         .append("    @RequiresPermission(").append(read).append(")\n")
         .append("    public ResponseEntity<ApiResponse<").append(resp).append(">> get(@PathVariable UUID id) {\n")
         .append("        return ResponseEntity.ok(ApiResponse.ok(service.get(id)));\n")
         .append("    }\n");

        if (e.writable()) {
            m.append("\n    @PostMapping\n")
             .append("    @RequiresPermission(").append(write).append(")\n")
             .append("    public ResponseEntity<ApiResponse<").append(resp).append(">> create(@Valid @RequestBody ")
             .append(req).append(" request) {\n")
             .append("        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.create(request), \"")
             .append(c).append(" criado\"));\n")
             .append("    }\n\n")
             .append("    @PutMapping(\"/{id}\")\n")
             .append("    @RequiresPermission(").append(write).append(")\n")
             .append("    public ResponseEntity<ApiResponse<").append(resp).append(">> update(@PathVariable UUID id, @Valid @RequestBody ")
             .append(req).append(" request) {\n")
             .append("        return ResponseEntity.ok(ApiResponse.ok(service.update(id, request), \"").append(c).append(" atualizado\"));\n")
             .append("    }\n\n")
             .append("    @DeleteMapping(\"/{id}\")\n")
             .append("    @RequiresPermission(").append(write).append(")\n")
             .append("    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {\n")
             .append("        service.delete(id);\n")
             .append("        return ResponseEntity.ok(ApiResponse.ok(null, \"").append(c).append(" excluído\"));\n")
             .append("    }\n");
        }
        if (e.exposes("search") && !e.textFields().isEmpty()) {
            m.append("\n    @GetMapping(\"/search\")\n")
             .append("    @RequiresPermission(").append(read).append(")\n")
             .append("    public ResponseEntity<ApiResponse<Page<").append(resp).append(">>> search(@RequestParam(defaultValue = \"\") String q, Pageable pageable) {\n")
             .append("        return ResponseEntity.ok(ApiResponse.ok(service.search(q, pageable)));\n")
             .append("    }\n");
        }
        FieldSpec date = e.dateField();
        if (e.exposes("filter_by_date_range") && (date != null || e.has("timestamps"))) {
            String type = date != null ? date.javaType() : "Instant";
            String format = date == null ? "" : date.type() == FieldType.DATE
                    ? "@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) "
                    : "@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ";
            m.append("\n    @GetMapping(\"/filter\")\n")
             .append("    @RequiresPermission(").append(read).append(")\n")
             .append("    public ResponseEntity<ApiResponse<Page<").append(resp).append(">>> filterByDateRange(\n")
             .append("            @RequestParam ").append(format).append(type).append(" start,\n")
             .append("            @RequestParam ").append(format).append(type).append(" end,\n")
             .append("            Pageable pageable) {\n")
             .append("        return ResponseEntity.ok(ApiResponse.ok(service.filterByDateRange(start, end, pageable)));\n")
             .append("    }\n");
        }
        if (e.exposes("bulk_create")) {
            m.append("\n    @PostMapping(\"/bulk\")\n")
             .append("    @RequiresPermission(").append(write).append(")\n")
             .append("    public ResponseEntity<ApiResponse<List<").append(resp).append(">>> createAll(@Valid @RequestBody List<@Valid ")
             .append(req).append("> requests) {\n")
             .append("        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.createAll(requests)));\n")
             .append("    }\n");
        }
        if (e.exposes("bulk_delete")) {
            m.append("\n    @PostMapping(\"/bulk-delete\")\n")
             .append("    @RequiresPermission(").append(write).append(")\n")
             .append("    public ResponseEntity<ApiResponse<Integer>> deleteAll(@RequestBody List<UUID> ids) {\n")
             .append("        return ResponseEntity.ok(ApiResponse.ok(service.deleteAll(ids)));\n")
             .append("    }\n");
        }
        if (e.exposes("export_csv")) {
            m.append("\n    @GetMapping(\"/export\")\n")
             .append("    @RequiresPermission(").append(read).append(")\n")
             .append("    public ResponseEntity<byte[]> exportCsv() {\n")
             .append("        return ResponseEntity.ok()\n")
             .append("                .header(HttpHeaders.CONTENT_DISPOSITION, \"attachment; filename=\\\"").append(e.resource()).append(".csv\\\"\")\n")
             .append("                .contentType(new MediaType(\"text\", \"csv\", StandardCharsets.UTF_8))\n")
             .append("                .body(service.exportCsv().getBytes(StandardCharsets.UTF_8));\n")
             .append("    }\n");
        }
        if (e.exposes("import_csv")) {
            m.append("\n    @PostMapping(\"/import\")\n")
             .append("    @RequiresPermission(").append(write).append(")\n")
             .append("    public ResponseEntity<ApiResponse<List<").append(resp).append(">>> importCsv(@RequestParam(\"file\") MultipartFile file) {\n")
             .append("        return ResponseEntity.ok(ApiResponse.ok(service.importCsv(file)));\n")
             .append("    }\n");
        }

        String code = "package " + ProjectWriter.pkg("controller") + ";\n\n"
                + "import com.basilisk.core.dto.ApiResponse;\n"
                + "import com.basilisk.permission.annotation.RequiresPermission;\n"
                + "import " + ProjectWriter.pkg("dto") + ".*;\n"
                + "import " + ProjectWriter.pkg("service") + "." + c + "Service;\n"
                + "import jakarta.validation.Valid;\n"
                + "import lombok.RequiredArgsConstructor;\n"
                + "import org.springframework.data.domain.Page;\n"
                + "import org.springframework.data.domain.Pageable;\n"
                + "import org.springframework.format.annotation.DateTimeFormat;\n"
                + "import org.springframework.http.HttpHeaders;\n"
                + "import org.springframework.http.HttpStatus;\n"
                + "import org.springframework.http.MediaType;\n"
                + "import org.springframework.http.ResponseEntity;\n"
                + "import org.springframework.web.bind.annotation.*;\n"
                + "import org.springframework.web.multipart.MultipartFile;\n\n"
                + "import java.nio.charset.StandardCharsets;\n"
                + "import java.time.*;\n"
                + "import java.util.*;\n\n"
                + "@RestController\n"
                + "@RequestMapping(\"/api/v1/" + e.resource() + "\")\n"
                + "@RequiredArgsConstructor\n"
                + "public class " + c + "Controller {\n\n"
                + "    private final " + c + "Service service;\n\n"
                + m
                + "}\n";
        out.java("controller", c + "Controller", code);
    }
}
