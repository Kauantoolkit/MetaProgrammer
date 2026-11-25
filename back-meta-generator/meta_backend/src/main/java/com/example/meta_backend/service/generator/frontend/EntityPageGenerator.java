package com.example.meta_backend.service.generator.frontend;

import com.example.meta_backend.service.generator.ProjectWriter;
import com.example.meta_backend.service.generator.model.AppSchema;
import com.example.meta_backend.service.generator.model.AppSchema.EntitySpec;
import com.example.meta_backend.service.generator.model.AppSchema.FieldSpec;
import com.example.meta_backend.service.generator.model.AppSchema.ManyToManySpec;
import com.example.meta_backend.service.generator.model.AppSchema.RefSpec;
import com.example.meta_backend.service.generator.model.FieldType;
import com.example.meta_backend.service.generator.model.Names;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Por entidade: model (espelha o Response do backend), api (um método por endpoint gerado) e a tela de CRUD no
 * padrão do ng-demo/clientes.ts — tabela bsk-data-table, formulário em bsk-modal, aviso em Toasts e botões de
 * escrita atrás de *bskCan. Referência a outra entidade vira select carregado da API dela.
 */
@Component
public class EntityPageGenerator {

    public void generate(AppSchema schema, EntitySpec e, ProjectWriter out) {
        if (!e.hasApi()) return;
        String dir = "src/app/" + Names.kebab(e.className()) + "/";
        out.frontend(dir + Names.kebab(e.className()) + ".model.ts", model(e));
        out.frontend(dir + Names.kebab(e.className()) + "-api.ts", api(e));
        out.frontend(dir + Names.kebab(e.className()) + "-page.ts", page(schema, e));
    }

    /** Rótulo de menu e título: o nome da entidade como veio ("PedidoItem" → "Pedido Item"). */
    public static String label(EntitySpec e) {
        return e.className().replaceAll("([a-z0-9])([A-Z])", "$1 $2");
    }

    public static String pageClass(EntitySpec e) {
        return e.className() + "Page";
    }

    public static String pageImport(EntitySpec e) {
        return "./" + Names.kebab(e.className()) + "/" + Names.kebab(e.className()) + "-page";
    }

    // --- model ---

    private String model(EntitySpec e) {
        StringBuilder sb = new StringBuilder("// Espelha ").append(e.className()).append("Response do backend\n");
        for (FieldSpec f : e.fields()) {
            if (f.type() == FieldType.ENUM) {
                sb.append("export type ").append(f.enumClass()).append(" = ")
                  .append(f.enumValues().stream().map(v -> "'" + v + "'").collect(Collectors.joining(" | ")))
                  .append(";\n");
            }
        }
        sb.append("\nexport interface ").append(e.className()).append(" {\n  id: string;\n");
        for (FieldSpec f : e.fields()) {
            sb.append("  ").append(f.name()).append(": ").append(tsType(f)).append(" | null;\n");
        }
        for (RefSpec r : e.refs()) sb.append("  ").append(r.name()).append(": string | null;\n");
        for (ManyToManySpec m : e.manyToMany()) sb.append("  ").append(m.name()).append(": string[];\n");
        if (e.has("timestamps")) sb.append("  createdAt: string;\n  updatedAt: string;\n");
        if (e.has("versioning")) sb.append("  version: number;\n");
        sb.append("}\n\n");

        sb.append("export type ").append(e.className()).append("Input = Omit<").append(e.className()).append(", 'id'");
        if (e.has("timestamps")) sb.append(" | 'createdAt' | 'updatedAt'");
        if (e.has("versioning")) sb.append(" | 'version'");
        sb.append(">;\n");
        return sb.toString();
    }

    private String tsType(FieldSpec f) {
        if (f.type() == FieldType.ENUM) return f.enumClass();
        if (f.type().isNumeric()) return "number";
        if (f.type() == FieldType.BOOLEAN) return "boolean";
        return "string";
    }

    // --- api ---

    private String api(EntitySpec e) {
        String c = e.className();
        String base = "`${API}/" + e.resource() + "`";
        StringBuilder m = new StringBuilder();
        m.append("  async list(page = 0, size = 20): Promise<Page<").append(c).append(">> {\n")
         .append("    const res = await firstValueFrom(\n")
         .append("      this.http.get<ApiResponse<Page<").append(c).append(">>>(").append(base)
         .append(", { params: { page, size, sort: 'id' } }),\n")
         .append("    );\n")
         .append("    return res.data!;\n")
         .append("  }\n\n")
         .append("  /** Todos (até 1000), para preencher selects de outras telas. */\n")
         .append("  async all(): Promise<").append(c).append("[]> {\n")
         .append("    return (await this.list(0, 1000)).content;\n")
         .append("  }\n");
        if (e.writable()) {
            m.append("\n  async create(input: ").append(c).append("Input): Promise<").append(c).append("> {\n")
             .append("    const res = await firstValueFrom(this.http.post<ApiResponse<").append(c).append(">>(")
             .append(base).append(", input));\n")
             .append("    return res.data!;\n")
             .append("  }\n\n")
             .append("  async update(id: string, input: ").append(c).append("Input): Promise<").append(c).append("> {\n")
             .append("    const res = await firstValueFrom(this.http.put<ApiResponse<").append(c).append(">>(`${API}/")
             .append(e.resource()).append("/${id}`, input));\n")
             .append("    return res.data!;\n")
             .append("  }\n\n")
             .append("  async remove(id: string): Promise<void> {\n")
             .append("    await firstValueFrom(this.http.delete<ApiResponse<void>>(`${API}/").append(e.resource())
             .append("/${id}`));\n")
             .append("  }\n");
        }
        if (searchable(e)) {
            m.append("\n  async search(q: string, page = 0, size = 20): Promise<Page<").append(c).append(">> {\n")
             .append("    const res = await firstValueFrom(\n")
             .append("      this.http.get<ApiResponse<Page<").append(c).append(">>>(`${API}/").append(e.resource())
             .append("/search`, { params: { q, page, size } }),\n")
             .append("    );\n")
             .append("    return res.data!;\n")
             .append("  }\n");
        }
        if (e.exposes("export_csv")) {
            m.append("\n  async exportCsv(): Promise<Blob> {\n")
             .append("    return firstValueFrom(this.http.get(`${API}/").append(e.resource())
             .append("/export`, { responseType: 'blob' }));\n")
             .append("  }\n");
        }
        return "import { HttpClient } from '@angular/common/http';\n"
                + "import { Service, inject } from '@angular/core';\n"
                + "import { firstValueFrom } from 'rxjs';\n\n"
                + "import { API, ApiResponse, Page } from '../api';\n"
                + "import { " + c + ", " + c + "Input } from './" + Names.kebab(c) + ".model';\n\n"
                + "@Service()\n"
                + "export class " + c + "Api {\n"
                + "  private readonly http = inject(HttpClient);\n\n"
                + m
                + "}\n";
    }

    private boolean searchable(EntitySpec e) {
        return e.exposes("search") && !e.textFields().isEmpty();
    }

    // --- página ---

    private String page(AppSchema schema, EntitySpec e) {
        String c = e.className();
        String title = label(e);
        boolean writable = e.writable();
        String write = e.resource() + ":write";

        // Entidades referenciadas que têm API: viram select; sem API (ou usuário), campo de texto com o UUID
        Set<String> targets = new LinkedHashSet<>();
        for (RefSpec r : e.refs()) if (!r.toUser() && hasApi(schema, r.targetClass())) targets.add(r.targetClass());
        for (ManyToManySpec m : e.manyToMany()) if (hasApi(schema, m.targetClass())) targets.add(m.targetClass());

        StringBuilder imports = new StringBuilder();
        imports.append("import { Component, inject, signal } from '@angular/core';\n")
               .append("import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';\n")
               .append("import { Can } from '@basilisk/ng-auth';\n")
               .append("import { Button, CellDef, Column, DataTable, Input, Modal, ModalFooter, Select, SelectOption, Toasts } from '@basilisk/ng-ui';\n\n")
               .append("import { apiError } from '../api';\n")
               .append("import { ").append(c).append("Api } from './").append(Names.kebab(c)).append("-api';\n")
               .append("import { ").append(c).append(", ").append(c).append("Input");
        if (writable) {
            // Tipos de enum só aparecem no toInput(), que só existe com escrita
            e.fields().stream().filter(f -> f.type() == FieldType.ENUM).forEach(f -> imports.append(", ").append(f.enumClass()));
        }
        imports.append(" } from './").append(Names.kebab(c)).append(".model';\n");
        for (String t : targets) {
            if (t.equals(c)) continue;
            imports.append("import { ").append(t).append("Api } from '../").append(Names.kebab(t)).append("/")
                   .append(Names.kebab(t)).append("-api';\n");
        }

        // --- colunas: até 6 campos + referências
        List<String> columns = new ArrayList<>();
        int shown = 0;
        for (FieldSpec f : e.fields()) {
            if (shown++ >= 6) break;
            if (f.type() == FieldType.TEXT) continue;
            String acc;
            if (f.type() == FieldType.BOOLEAN) acc = "(r) => (r." + f.name() + " == null ? '—' : r." + f.name() + " ? 'Sim' : 'Não')";
            else if (f.type() == FieldType.DATETIME) acc = "(r) => (r." + f.name() + " ? r." + f.name() + ".replace('T', ' ').slice(0, 16) : '—')";
            else acc = "(r) => r." + f.name() + " ?? '—'";
            columns.add("    { header: '" + Names.pascal(f.name()).replaceAll("([a-z0-9])([A-Z])", "$1 $2") + "', accessor: " + acc
                    + (f.type().isNumeric() ? ", align: 'right'" : "") + " },");
        }
        for (RefSpec r : e.refs()) {
            String header = r.toUser() ? "Usuário" : label(schema.entity(r.targetClass()));
            String acc = targets.contains(r.targetClass()) && !r.toUser()
                    ? "(r) => this.labelOf(this." + optionsVar(r.targetClass()) + "(), r." + r.name() + ")"
                    : "(r) => r." + r.name() + "?.slice(0, 8) ?? '—'";
            columns.add("    { header: '" + header + "', accessor: " + acc + " },");
        }
        if (writable) columns.add("    { header: '', accessor: () => '', key: 'acoes', align: 'right', width: '1%' },");

        // --- controles do formulário
        List<String> controls = new ArrayList<>();
        StringBuilder fields = new StringBuilder();
        for (FieldSpec f : e.fields()) {
            String validators = f.required() ? "validators: [Validators.required]" : "";
            String label = Names.pascal(f.name()).replaceAll("([a-z0-9])([A-Z])", "$1 $2");
            String req = f.required() ? " required" : "";
            String error = " [error]=\"fieldError('" + f.name() + "')\"";
            if (f.type() == FieldType.BOOLEAN) {
                controls.add("    " + f.name() + ": new FormControl(false, { nonNullable: true }),");
                fields.append("          <label class=\"flex items-center gap-2 text-sm text-[var(--bsk-text)]\">\n")
                      .append("            <input type=\"checkbox\" formControlName=\"").append(f.name()).append("\" /> ")
                      .append(label).append("\n          </label>\n");
                continue;
            }
            controls.add("    " + f.name() + ": new FormControl('', { nonNullable: true" + (validators.isEmpty() ? "" : ", " + validators) + " }),");
            if (f.type() == FieldType.ENUM) {
                fields.append("          <bsk-select label=\"").append(label).append("\"").append(req)
                      .append(" placeholder=\"Escolha\" [options]=\"").append(f.name()).append("Options\" formControlName=\"")
                      .append(f.name()).append("\"").append(error).append(" />\n");
            } else {
                String type = switch (f.type()) {
                    case NUMBER, INTEGER, LONG -> "number";
                    case DATE -> "date";
                    case TIME -> "time";
                    case DATETIME -> "datetime-local";
                    default -> "text";
                };
                fields.append("          <bsk-input label=\"").append(label).append("\" type=\"").append(type).append("\"").append(req)
                      .append(" formControlName=\"").append(f.name()).append("\"").append(error).append(" />\n");
            }
        }
        for (RefSpec r : e.refs()) {
            String validators = r.required() ? ", validators: [Validators.required]" : "";
            controls.add("    " + r.name() + ": new FormControl('', { nonNullable: true" + validators + " }),");
            String req = r.required() ? " required" : "";
            String error = " [error]=\"fieldError('" + r.name() + "')\"";
            if (!r.toUser() && targets.contains(r.targetClass())) {
                fields.append("          <bsk-select label=\"").append(label(schema.entity(r.targetClass()))).append("\"").append(req)
                      .append(" placeholder=\"Escolha\" [options]=\"").append(optionsVar(r.targetClass()))
                      .append("()\" formControlName=\"").append(r.name()).append("\"").append(error).append(" />\n");
            } else {
                fields.append("          <bsk-input label=\"").append(r.toUser() ? "Usuário (id)" : label(schema.entity(r.targetClass())) + " (id)")
                      .append("\"").append(req).append(" formControlName=\"").append(r.name()).append("\"").append(error).append(" />\n");
            }
        }
        for (ManyToManySpec m : e.manyToMany()) {
            controls.add("    " + m.name() + ": new FormControl<string[]>([], { nonNullable: true }),");
            if (targets.contains(m.targetClass())) {
                fields.append("          <fieldset class=\"flex flex-col gap-1 text-sm text-[var(--bsk-text)]\">\n")
                      .append("            <legend class=\"font-medium\">").append(label(schema.entity(m.targetClass()))).append("</legend>\n")
                      .append("            @for (opt of ").append(optionsVar(m.targetClass())).append("(); track opt.value) {\n")
                      .append("              <label class=\"flex items-center gap-2\">\n")
                      .append("                <input type=\"checkbox\" [checked]=\"form.controls.").append(m.name())
                      .append(".value.includes(opt.value + '')\" (change)=\"toggle('").append(m.name()).append("', opt.value + '')\" />\n")
                      .append("                {{ opt.label }}\n")
                      .append("              </label>\n")
                      .append("            }\n")
                      .append("          </fieldset>\n");
            }
        }

        // --- conversão formulário → Input do backend (o formulário guarda texto)
        StringBuilder toInput = new StringBuilder();
        toInput.append("    const v = this.form.getRawValue();\n    return {\n");
        for (FieldSpec f : e.fields()) {
            String expr;
            if (f.type() == FieldType.BOOLEAN) expr = "v." + f.name();
            else if (f.type().isNumeric()) expr = "v." + f.name() + " === '' ? null : Number(v." + f.name() + ")";
            else if (f.type() == FieldType.ENUM) expr = "(v." + f.name() + " || null) as " + f.enumClass() + " | null";
            else expr = "v." + f.name() + " || null";
            toInput.append("      ").append(f.name()).append(": ").append(expr).append(",\n");
        }
        for (RefSpec r : e.refs()) toInput.append("      ").append(r.name()).append(": v.").append(r.name()).append(" || null,\n");
        for (ManyToManySpec m : e.manyToMany()) toInput.append("      ").append(m.name()).append(": v.").append(m.name()).append(",\n");
        toInput.append("    };\n");

        StringBuilder fill = new StringBuilder();
        fill.append("    this.form.reset();\n");
        fill.append("    if (row) {\n      this.form.patchValue({\n");
        for (FieldSpec f : e.fields()) {
            String expr;
            if (f.type() == FieldType.BOOLEAN) expr = "row." + f.name() + " ?? false";
            else if (f.type() == FieldType.DATETIME) expr = "row." + f.name() + "?.slice(0, 16) ?? ''";
            else if (f.type().isNumeric()) expr = "row." + f.name() + " == null ? '' : String(row." + f.name() + ")";
            else expr = "row." + f.name() + " ?? ''";
            fill.append("        ").append(f.name()).append(": ").append(expr).append(",\n");
        }
        for (RefSpec r : e.refs()) fill.append("        ").append(r.name()).append(": row.").append(r.name()).append(" ?? '',\n");
        for (ManyToManySpec m : e.manyToMany()) fill.append("        ").append(m.name()).append(": [...row.").append(m.name()).append("],\n");
        fill.append("      });\n    }\n");

        // --- opções de enum e de referência
        StringBuilder props = new StringBuilder();
        for (FieldSpec f : e.fields()) {
            if (f.type() != FieldType.ENUM) continue;
            props.append("  protected readonly ").append(f.name()).append("Options: SelectOption[] = [\n");
            for (String v : f.enumValues()) props.append("    { value: '").append(v).append("', label: '").append(v).append("' },\n");
            props.append("  ];\n");
        }
        StringBuilder loadRefs = new StringBuilder();
        for (String t : targets) {
            String apiVar = t.equals(c) ? "this.api" : "this." + Names.lowerFirst(t) + "Api";
            if (!t.equals(c)) props.append("  private readonly ").append(Names.lowerFirst(t)).append("Api = inject(").append(t).append("Api);\n");
            props.append("  protected readonly ").append(optionsVar(t)).append(" = signal<SelectOption[]>([]);\n");
            EntitySpec te = schema.entity(t);
            String labelField = te.fields().stream().filter(f -> f.type() == FieldType.STRING).map(FieldSpec::name).findFirst().orElse(null);
            String labelExpr = labelField != null ? "x." + labelField + " ?? x.id.slice(0, 8)" : "x.id.slice(0, 8)";
            loadRefs.append("      ").append(apiVar).append(".all().then((list) => this.").append(optionsVar(t))
                    .append(".set(list.map((x) => ({ value: x.id, label: String(").append(labelExpr).append(") })))),\n");
        }

        String search = searchable(e) ? """
            <div class="mb-4 flex gap-2">
              <bsk-input class="flex-1" placeholder="Buscar" [formControl]="query" (keyup.enter)="load(0)" />
              <button bskButton variant="secondary" (click)="load(0)">Buscar</button>
            </div>
            """ : "";
        String exportBtn = e.exposes("export_csv")
                ? "\n          <button bskButton variant=\"secondary\" (click)=\"exportCsv()\">Exportar CSV</button>" : "";
        String newBtn = writable
                ? "\n          <button bskButton *bskCan=\"'" + write + "'\" (click)=\"openForm(null)\">Novo</button>" : "";

        String actions = writable ? """

                  <ng-template bskCell="acoes" let-row>
                    <div class="flex justify-end gap-2" *bskCan="'@WRITE@'">
                      <button bskButton size="sm" variant="ghost" (click)="openForm(row)">Editar</button>
                      <button bskButton size="sm" variant="ghost" (click)="remove(row)">Excluir</button>
                    </div>
                  </ng-template>""".replace("@WRITE@", write) : "";

        String modal = writable ? """

                <bsk-modal [isOpen]="open()" [title]="editing() ? 'Editar @TITLE@' : 'Novo @TITLE@'" (close)="open.set(false)">
                  <form [formGroup]="form" class="flex flex-col gap-4" (ngSubmit)="save()">
            @FIELDS@        </form>
                  <div bskModalFooter>
                    <button bskButton variant="ghost" (click)="open.set(false)">Cancelar</button>
                    <button bskButton [isLoading]="saving()" (click)="save()">Salvar</button>
                  </div>
                </bsk-modal>""".replace("@TITLE@", title).replace("@FIELDS@", fields.toString()) : "";

        String writeMethods = writable ? """

              protected openForm(row: @C@ | null): void {
                this.editing.set(row);
                this.errors.set({});
            @FILL@    this.open.set(true);
              }

              protected async save(): Promise<void> {
                if (this.form.invalid) {
                  this.form.markAllAsTouched();
                  this.toasts.error('Preencha os campos obrigatórios');
                  return;
                }
                this.saving.set(true);
                this.errors.set({});
                try {
                  const row = this.editing();
                  if (row) await this.api.update(row.id, this.toInput());
                  else await this.api.create(this.toInput());
                  this.open.set(false);
                  this.toasts.success(row ? 'Salvo' : 'Criado');
                  await this.load(this.page());
                } catch (e) {
                  const err = apiError(e);
                  this.errors.set(err.errors);
                  this.toasts.error(err.message);
                } finally {
                  this.saving.set(false);
                }
              }

              protected async remove(row: @C@): Promise<void> {
                if (!confirm('Excluir este registro?')) return;
                try {
                  await this.api.remove(row.id);
                  this.toasts.success('Excluído');
                  await this.load(this.page());
                } catch (e) {
                  this.toasts.error(apiError(e).message);
                }
              }

              protected fieldError(name: string): string | undefined {
                const control = this.form.get(name);
                if (control?.touched && control.invalid) return 'Obrigatório';
                return this.errors()[name];
              }

              private toInput(): @C@Input {
            @TOINPUT@  }
            """.replace("@C@", c).replace("@FILL@", fill.toString()).replace("@TOINPUT@", toInput.toString()) : "";

        String toggle = e.manyToMany().isEmpty() ? "" : """

              protected toggle(name: '@NAMES@', id: string): void {
                const control = this.form.controls[name];
                const ids = control.value;
                control.setValue(ids.includes(id) ? ids.filter((x) => x !== id) : [...ids, id]);
              }
            """.replace("@NAMES@", e.manyToMany().stream().map(ManyToManySpec::name).collect(Collectors.joining("' | '")));

        String labelOf = targets.isEmpty() ? "" : """

              protected labelOf(options: SelectOption[], id: string | null): string {
                if (!id) return '—';
                return options.find((o) => o.value === id)?.label ?? id.slice(0, 8);
              }
            """;

        String exportMethod = e.exposes("export_csv") ? """

              protected async exportCsv(): Promise<void> {
                try {
                  const blob = await this.api.exportCsv();
                  const a = document.createElement('a');
                  a.href = URL.createObjectURL(blob);
                  a.download = '@RES@.csv';
                  a.click();
                  URL.revokeObjectURL(a.href);
                } catch (e) {
                  this.toasts.error(apiError(e).message);
                }
              }
            """.replace("@RES@", e.resource()) : "";

        String loadCall = searchable(e)
                ? "const q = this.query.value.trim();\n      const result = q ? await this.api.search(q, page) : await this.api.list(page);"
                : "const result = await this.api.list(page);";

        // Só o que o template usa: o Angular avisa (NG8113) de componente importado e não usado
        boolean usesSelect = writable && (e.fields().stream().anyMatch(f -> f.type() == FieldType.ENUM)
                || e.refs().stream().anyMatch(r -> !r.toUser() && targets.contains(r.targetClass())));
        List<String> used = new ArrayList<>(List.of("DataTable", "Button"));
        if (writable) used.add("CellDef");
        if (writable || searchable(e)) used.add("Input");
        if (usesSelect) used.add("Select");
        if (writable) used.addAll(List.of("Modal", "ModalFooter"));
        used.add("ReactiveFormsModule");
        if (writable) used.add("Can");
        String imported = String.join(", ", used);

        String template = """
            <div class="mb-6 flex items-center justify-between gap-4">
              <div>
                <h1 class="text-2xl font-semibold text-[var(--bsk-text)]">@TITLE@</h1>
                <p class="text-sm text-[var(--bsk-text-secondary)]">{{ total() }} registro(s)</p>
              </div>
              <div class="flex gap-2">@EXPORT@@NEW@
              </div>
            </div>
            @SEARCH@
            <bsk-data-table [columns]="columns" [data]="rows()" rowKey="id" [isLoading]="loading()">@ACTIONS@
            </bsk-data-table>

            @if (totalPages() > 1) {
              <div class="mt-4 flex items-center justify-end gap-2 text-sm text-[var(--bsk-text-secondary)]">
                <button bskButton size="sm" variant="ghost" [disabled]="page() === 0" (click)="load(page() - 1)">Anterior</button>
                <span>{{ page() + 1 }} de {{ totalPages() }}</span>
                <button bskButton size="sm" variant="ghost" [disabled]="page() + 1 >= totalPages()" (click)="load(page() + 1)">Próxima</button>
              </div>
            }@MODAL@
            """.replace("@TITLE@", title).replace("@EXPORT@", exportBtn).replace("@NEW@", newBtn)
                .replace("@SEARCH@", search).replace("@ACTIONS@", actions).replace("@MODAL@", modal);

        String indentedTemplate = template.lines().map(l -> l.isEmpty() ? l : "    " + l).collect(Collectors.joining("\n"));

        return imports + "\n"
                + "@Component({\n"
                + "  imports: [" + imported + "],\n"
                + "  template: `\n" + indentedTemplate + "\n  `,\n"
                + "})\n"
                + "export class " + c + "Page {\n"
                + "  private readonly api = inject(" + c + "Api);\n"
                + "  protected readonly toasts = inject(Toasts);\n\n"
                + "  protected readonly rows = signal<" + c + "[]>([]);\n"
                + "  protected readonly loading = signal(true);\n"
                + "  protected readonly page = signal(0);\n"
                + "  protected readonly totalPages = signal(0);\n"
                + "  protected readonly total = signal(0);\n"
                + "  protected readonly open = signal(false);\n"
                + "  protected readonly saving = signal(false);\n"
                + "  protected readonly editing = signal<" + c + " | null>(null);\n"
                + "  protected readonly errors = signal<Record<string, string>>({});\n"
                + (searchable(e) ? "  protected readonly query = new FormControl('', { nonNullable: true });\n" : "")
                + props + "\n"
                + "  protected readonly columns: Column<" + c + ">[] = [\n" + String.join("\n", columns) + "\n  ];\n\n"
                + "  protected readonly form = new FormGroup({\n" + String.join("\n", controls) + "\n  });\n\n"
                + "  constructor() {\n"
                + "    void Promise.all([\n"
                + "      this.load(0),\n"
                + loadRefs
                + "    ]);\n"
                + "  }\n\n"
                + "  protected async load(page: number): Promise<void> {\n"
                + "    this.loading.set(true);\n"
                + "    try {\n"
                + "      " + loadCall + "\n"
                + "      this.rows.set(result.content);\n"
                + "      this.page.set(result.number);\n"
                + "      this.totalPages.set(result.totalPages);\n"
                + "      this.total.set(result.totalElements);\n"
                + "    } catch (e) {\n"
                + "      this.toasts.error(apiError(e).message);\n"
                + "    } finally {\n"
                + "      this.loading.set(false);\n"
                + "    }\n"
                + "  }\n"
                + writeMethods
                + toggle
                + labelOf
                + exportMethod
                + "}\n";
    }

    private static String optionsVar(String targetClass) {
        return Names.lowerFirst(targetClass) + "Options";
    }

    private static boolean hasApi(AppSchema schema, String className) {
        EntitySpec t = schema.entity(className);
        return t != null && t.hasApi();
    }
}
