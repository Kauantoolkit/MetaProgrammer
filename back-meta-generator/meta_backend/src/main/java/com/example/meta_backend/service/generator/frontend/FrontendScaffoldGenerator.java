package com.example.meta_backend.service.generator.frontend;

import com.example.meta_backend.service.generator.ProjectWriter;
import com.example.meta_backend.service.generator.model.AppSchema;
import com.example.meta_backend.service.generator.model.Names;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Esqueleto Angular do sistema gerado, copiado do estudio-app/frontend (app de referência): Angular 22 standalone,
 * @basilisk/ng-auth (sessão JWT + guard + *bskCan), @basilisk/ng-shell (menu por módulo) e @basilisk/ng-ui.
 * As libs entram por `file:` apontando para o basilisk-libs desta máquina, como no estudio-app.
 */
@Component
public class FrontendScaffoldGenerator {

    @Value("${generator.basilisk-libs-dir:${user.home}/Desktop/computaria/organizando-meus-codigos/repo-central/repos/basilisk-libs}")
    private String basiliskLibsDir;

    public void generate(AppSchema schema, ProjectWriter out) {
        String libs = basiliskLibsDir.replace('\\', '/');
        String name = Names.kebab(schema.className());
        String title = schema.appName();

        out.frontend("package.json", """
            {
              "name": "@NAME@-frontend",
              "version": "0.0.0",
              "private": true,
              "scripts": {
                "ng": "ng",
                "start": "ng serve",
                "build": "ng build"
              },
              "dependencies": {
                "@angular/common": "22.2.6",
                "@angular/compiler": "22.2.6",
                "@angular/core": "22.2.6",
                "@angular/forms": "22.2.6",
                "@angular/platform-browser": "22.2.6",
                "@angular/router": "22.2.6",
                "@basilisk/ng-auth": "file:@LIBS@/packages/angular/dist/ng-auth",
                "@basilisk/ng-shell": "file:@LIBS@/packages/angular/dist/ng-shell",
                "@basilisk/ng-ui": "file:@LIBS@/packages/angular/dist/ng-ui",
                "@basilisk/themes": "file:@LIBS@/packages/themes",
                "@basilisk/utils": "file:@LIBS@/packages/utils",
                "class-variance-authority": "^0.7.0",
                "rxjs": "~7.8.0",
                "tslib": "^2.3.0"
              },
              "devDependencies": {
                "@angular/build": "22.2.6",
                "@angular/cli": "22.2.6",
                "@angular/compiler-cli": "22.2.6",
                "@basilisk/config": "file:@LIBS@/packages/config",
                "tailwindcss": "^3.4.3",
                "typescript": "~6.0.2"
              },
              "overrides": {
                "@basilisk/themes": "$@basilisk/themes",
                "@basilisk/utils": "$@basilisk/utils"
              }
            }
            """.replace("@NAME@", name).replace("@LIBS@", libs));

        out.frontend("angular.json", """
            {
              "$schema": "./node_modules/@angular/cli/lib/config/schema.json",
              "version": 1,
              "cli": { "packageManager": "npm", "analytics": false },
              "newProjectRoot": "projects",
              "projects": {
                "app": {
                  "projectType": "application",
                  "schematics": {
                    "@schematics/angular:component": { "skipTests": true },
                    "@schematics/angular:service": { "skipTests": true }
                  },
                  "root": "",
                  "sourceRoot": "src",
                  "prefix": "app",
                  "architect": {
                    "build": {
                      "builder": "@angular/build:application",
                      "options": {
                        "browser": "src/main.ts",
                        "tsConfig": "tsconfig.app.json",
                        "styles": ["src/styles.css"],
                        "preserveSymlinks": true
                      },
                      "configurations": {
                        "production": {
                          "budgets": [
                            { "type": "initial", "maximumWarning": "500kB", "maximumError": "1MB" },
                            { "type": "anyComponentStyle", "maximumWarning": "4kB", "maximumError": "8kB" }
                          ],
                          "outputHashing": "all"
                        },
                        "development": { "optimization": false, "extractLicenses": false, "sourceMap": true }
                      },
                      "defaultConfiguration": "production"
                    },
                    "serve": {
                      "builder": "@angular/build:dev-server",
                      "options": { "proxyConfig": "proxy.conf.json" },
                      "configurations": {
                        "production": { "buildTarget": "app:build:production" },
                        "development": { "buildTarget": "app:build:development" }
                      },
                      "defaultConfiguration": "development"
                    }
                  }
                }
              }
            }
            """);

        out.frontend("tsconfig.json", """
            {
              "compileOnSave": false,
              "compilerOptions": {
                "strict": true,
                "noImplicitOverride": true,
                "noPropertyAccessFromIndexSignature": true,
                "noImplicitReturns": true,
                "noFallthroughCasesInSwitch": true,
                "skipLibCheck": true,
                "isolatedModules": true,
                "experimentalDecorators": true,
                "importHelpers": true,
                "preserveSymlinks": true,
                "target": "ES2022",
                "module": "preserve"
              },
              "angularCompilerOptions": {
                "enableI18nLegacyMessageIdFormat": false,
                "strictInjectionParameters": true,
                "strictInputAccessModifiers": true,
                "strictTemplates": true
              },
              "files": [],
              "references": [{ "path": "./tsconfig.app.json" }]
            }
            """);

        out.frontend("tsconfig.app.json", """
            {
              "extends": "./tsconfig.json",
              "compilerOptions": {
                "outDir": "./out-tsc/app",
                "types": []
              },
              "include": ["src/**/*.ts"]
            }
            """);

        out.frontend("tailwind.config.js", """
            const { join } = require('node:path');

            /** @type {import('tailwindcss').Config} */
            module.exports = {
              presets: [require('@basilisk/config/tailwind')],
              // As classes dos componentes vêm das bibliotecas compiladas
              content: [
                join(__dirname, 'src/**/*.{html,ts}'),
                join(__dirname, 'node_modules/@basilisk/ng-ui/**/*.mjs'),
                join(__dirname, 'node_modules/@basilisk/ng-shell/**/*.mjs'),
              ],
            };
            """);

        out.frontend("proxy.conf.json", """
            {
              "/api": {
                "target": "http://localhost:8081",
                "secure": false
              }
            }
            """);

        out.frontend("src/index.html", """
            <!doctype html>
            <html lang="pt-BR">
              <head>
                <meta charset="utf-8" />
                <title>@TITLE@</title>
                <base href="/" />
                <meta name="viewport" content="width=device-width, initial-scale=1" />
              </head>
              <body>
                <app-root></app-root>
              </body>
            </html>
            """.replace("@TITLE@", title));

        out.frontend("src/main.ts", """
            import { bootstrapApplication } from '@angular/platform-browser';
            import { appConfig } from './app/app.config';
            import { App } from './app/app';

            bootstrapApplication(App, appConfig).catch((err) => console.error(err));
            """);

        out.frontend("src/styles.css", """
            /* Tema padrão do Basilisk; para trocar a cara do sistema, importar outro tema (ou um próprio) */
            @import '@basilisk/themes/basilisk.css';

            @tailwind base;
            @tailwind components;
            @tailwind utilities;

            body {
              font-family: var(--bsk-font-body);
              color: var(--bsk-text);
              background: var(--bsk-base);
            }
            """);

        out.frontend("src/app/app.ts", """
            import { Component, effect, inject } from '@angular/core';
            import { Router, RouterOutlet } from '@angular/router';
            import { Auth } from '@basilisk/ng-auth';
            import { ToastContainer } from '@basilisk/ng-ui';

            @Component({
              imports: [RouterOutlet, ToastContainer],
              selector: 'app-root',
              template: `
                <router-outlet />
                <bsk-toast-container />
              `,
            })
            export class App {
              constructor() {
                const auth = inject(Auth);
                const router = inject(Router);
                // Sessão que caiu (botão Sair ou 401) → tela de login. Só na transição: o acesso
                // inicial sem sessão é do authGuard, e navegar aqui cancelaria a navegação inicial.
                let wasAuthenticated = false;
                effect(() => {
                  const authenticated = auth.isAuthenticated();
                  if (wasAuthenticated && !authenticated) void router.navigateByUrl('/login');
                  wasAuthenticated = authenticated;
                });
              }
            }
            """);

        out.frontend("src/app/app.config.ts", """
            import { provideHttpClient, withInterceptors } from '@angular/common/http';
            import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
            import { provideRouter } from '@angular/router';
            import { authInterceptor, provideBasiliskAuth } from '@basilisk/ng-auth';
            import { provideShell } from '@basilisk/ng-shell';

            import { routes } from './app.routes';
            import { shellConfig } from './shell.config';

            export const appConfig: ApplicationConfig = {
              providers: [
                provideBrowserGlobalErrorListeners(),
                provideRouter(routes),
                provideHttpClient(withInterceptors([authInterceptor])),
                provideBasiliskAuth({ loginPath: '/login' }),
                provideShell(shellConfig),
              ],
            };
            """);

        out.frontend("src/app/app.routes.ts", """
            import { Routes } from '@angular/router';
            import { ShellLayout, shellRoutes } from '@basilisk/ng-shell';

            import { Login } from './login';
            import { Register } from './register';
            import { shellConfig } from './shell.config';

            export const routes: Routes = [
              { path: 'login', component: Login },
              { path: 'cadastro', component: Register },
              { path: '', component: ShellLayout, children: shellRoutes(shellConfig) },
              { path: '**', redirectTo: '' },
            ];
            """);

        out.frontend("src/app/api.ts", """
            import { HttpErrorResponse } from '@angular/common/http';

            // Envelope de toda resposta da API (ApiResponse do basilisk-core)
            export interface ApiResponse<T> {
              success: boolean;
              data?: T;
              message?: string;
              errors?: Record<string, string>;
            }

            // Page do Spring Data, como o backend devolve nas listagens
            export interface Page<T> {
              content: T[];
              totalElements: number;
              totalPages: number;
              number: number;
              size: number;
            }

            export const API = '/api/v1';

            /** Mensagem e erros por campo de uma resposta de erro do backend (422 traz `errors`). */
            export function apiError(e: unknown): { message: string; errors: Record<string, string> } {
              if (e instanceof HttpErrorResponse) {
                if (e.status === 0) return { message: 'Servidor fora do ar', errors: {} };
                const body = e.error as Partial<ApiResponse<unknown>> | null;
                return { message: body?.message ?? `Erro ${e.status}`, errors: body?.errors ?? {} };
              }
              return { message: 'Erro inesperado', errors: {} };
            }
            """);

        out.frontend("src/app/auth-api.ts", """
            import { HttpClient } from '@angular/common/http';
            import { Service, inject } from '@angular/core';
            import { AuthUser } from '@basilisk/ng-auth';
            import { firstValueFrom } from 'rxjs';

            import { API, ApiResponse } from './api';

            // AuthResponse do basilisk-user
            interface AuthResponse {
              token: string;
              userId: string;
              name: string;
              email: string;
              tenantId: string;
              tenantName: string;
              roleName: string;
              roleId: string;
            }

            export interface Session {
              token: string;
              user: AuthUser;
            }

            export interface RegisterData {
              name: string;
              email: string;
              password: string;
              tenantName: string;
            }

            @Service()
            export class AuthApi {
              private readonly http = inject(HttpClient);

              async login(email: string, password: string): Promise<Session> {
                const res = await firstValueFrom(
                  this.http.post<ApiResponse<AuthResponse>>(`${API}/auth/login`, { email, password }),
                );
                return toSession(res.data!);
              }

              /** Cria a empresa (tenant) e o usuário dono dela, já logado. */
              async register(data: RegisterData): Promise<Session> {
                const res = await firstValueFrom(
                  this.http.post<ApiResponse<AuthResponse>>(`${API}/auth/register`, {
                    ...data,
                    tenantSlug: slug(data.tenantName),
                  }),
                );
                return toSession(res.data!);
              }
            }

            function toSession(data: AuthResponse): Session {
              return {
                token: data.token,
                // O backend não manda permissões: OWNER/ADMIN passam em tudo no Permissions do ng-auth
                user: {
                  id: data.userId,
                  name: data.name,
                  email: data.email,
                  tenantId: data.tenantId,
                  tenantName: data.tenantName,
                  roleId: data.roleId,
                  roleName: data.roleName,
                },
              };
            }

            function slug(text: string): string {
              const s = text
                .normalize('NFD')
                .replace(/[\\u0300-\\u036f]/g, '')
                .toLowerCase()
                .replace(/[^a-z0-9]+/g, '-')
                .replace(/^-|-$/g, '');
              return (s || 'empresa').slice(0, 48) + '-' + Math.random().toString(36).slice(2, 8);
            }
            """);

        out.frontend("src/app/login.ts", """
            import { HttpErrorResponse } from '@angular/common/http';
            import { Component, inject, signal } from '@angular/core';
            import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
            import { Router, RouterLink } from '@angular/router';
            import { Auth } from '@basilisk/ng-auth';
            import { Button, Card, Input } from '@basilisk/ng-ui';

            import { AuthApi } from './auth-api';

            @Component({
              imports: [Card, Button, Input, ReactiveFormsModule, RouterLink],
              template: `
                <div class="flex min-h-screen items-center justify-center bg-[var(--bsk-base)] p-6">
                  <bsk-card class="w-full max-w-sm">
                    <h1 class="text-2xl font-semibold text-[var(--bsk-text)]">@TITLE@</h1>
                    <form [formGroup]="form" class="mt-6 flex flex-col gap-4" (ngSubmit)="submit()">
                      <bsk-input label="E-mail" type="email" required formControlName="email" />
                      <bsk-input label="Senha" type="password" required formControlName="password" [error]="error()" />
                      <button bskButton type="submit" fullWidth [isLoading]="loading()">Entrar</button>
                    </form>
                    <p class="mt-4 text-center text-sm text-[var(--bsk-text-secondary)]">
                      Primeira vez? <a routerLink="/cadastro" class="underline">Criar conta</a>
                    </p>
                  </bsk-card>
                </div>
              `,
            })
            export class Login {
              private readonly auth = inject(Auth);
              private readonly authApi = inject(AuthApi);
              private readonly router = inject(Router);

              protected readonly loading = signal(false);
              protected readonly error = signal<string | undefined>(undefined);

              protected readonly form = new FormGroup({
                email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
                password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
              });

              protected async submit(): Promise<void> {
                if (this.form.invalid) {
                  this.form.markAllAsTouched();
                  return;
                }
                this.loading.set(true);
                this.error.set(undefined);
                const { email, password } = this.form.getRawValue();
                try {
                  const session = await this.authApi.login(email, password);
                  // O basilisk-user não tem refresh token: a sessão dura o expiration-ms do JWT
                  this.auth.login(session.token, '', session.user);
                  void this.router.navigateByUrl('/');
                } catch (e) {
                  const res = e instanceof HttpErrorResponse ? e : null;
                  if (res?.status === 401 || res?.status === 400) this.error.set('E-mail ou senha incorretos');
                  // 403 = usuário ou empresa desativados: o backend explica em message
                  else if (res?.status === 403) this.error.set(res.error?.message ?? 'Acesso bloqueado');
                  else this.error.set('Servidor fora do ar');
                } finally {
                  this.loading.set(false);
                }
              }
            }
            """.replace("@TITLE@", title));

        out.frontend("src/app/register.ts", """
            import { Component, inject, signal } from '@angular/core';
            import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
            import { Router, RouterLink } from '@angular/router';
            import { Auth } from '@basilisk/ng-auth';
            import { Button, Card, Input } from '@basilisk/ng-ui';

            import { apiError } from './api';
            import { AuthApi } from './auth-api';

            /** Cria a empresa e o usuário dono (OWNER): é por aqui que o sistema ganha o primeiro acesso. */
            @Component({
              imports: [Card, Button, Input, ReactiveFormsModule, RouterLink],
              template: `
                <div class="flex min-h-screen items-center justify-center bg-[var(--bsk-base)] p-6">
                  <bsk-card class="w-full max-w-sm">
                    <h1 class="text-2xl font-semibold text-[var(--bsk-text)]">Criar conta</h1>
                    <form [formGroup]="form" class="mt-6 flex flex-col gap-4" (ngSubmit)="submit()">
                      <bsk-input label="Empresa" required formControlName="tenantName" [error]="errors()['tenantName']" />
                      <bsk-input label="Seu nome" required formControlName="name" [error]="errors()['name']" />
                      <bsk-input label="E-mail" type="email" required formControlName="email" [error]="errors()['email']" />
                      <bsk-input
                        label="Senha"
                        type="password"
                        required
                        formControlName="password"
                        helperText="Mínimo de 8 caracteres"
                        [error]="errors()['password'] ?? message()"
                      />
                      <button bskButton type="submit" fullWidth [isLoading]="loading()">Criar</button>
                    </form>
                    <p class="mt-4 text-center text-sm text-[var(--bsk-text-secondary)]">
                      Já tem conta? <a routerLink="/login" class="underline">Entrar</a>
                    </p>
                  </bsk-card>
                </div>
              `,
            })
            export class Register {
              private readonly auth = inject(Auth);
              private readonly authApi = inject(AuthApi);
              private readonly router = inject(Router);

              protected readonly loading = signal(false);
              protected readonly message = signal<string | undefined>(undefined);
              protected readonly errors = signal<Record<string, string>>({});

              protected readonly form = new FormGroup({
                tenantName: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(2)] }),
                name: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(2)] }),
                email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
                password: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(8)] }),
              });

              protected async submit(): Promise<void> {
                if (this.form.invalid) {
                  this.form.markAllAsTouched();
                  this.message.set('Preencha todos os campos (senha com 8 ou mais caracteres)');
                  return;
                }
                this.loading.set(true);
                this.message.set(undefined);
                this.errors.set({});
                try {
                  const session = await this.authApi.register(this.form.getRawValue());
                  this.auth.login(session.token, '', session.user);
                  void this.router.navigateByUrl('/');
                } catch (e) {
                  const err = apiError(e);
                  this.errors.set(err.errors);
                  this.message.set(err.message);
                } finally {
                  this.loading.set(false);
                }
              }
            }
            """);
    }
}
