import React, { useState } from 'react';
import { Button } from "@/components/ui/button";
import Input from "@/components/ui/input";
import Badge from "@/components/ui/badge";
import Switch from "@/components/ui/switch";
import { Plus, X, Shield, Globe, Key, Users } from 'lucide-react';
import { cn } from "@/lib/utils";

const ENDPOINT_OPTIONS = [
  { value: 'crud', label: 'CRUD Completo', desc: 'GET, POST, PUT, DELETE' },
  { value: 'read_only', label: 'Somente Leitura', desc: 'GET apenas' },
  { value: 'search', label: 'Busca', desc: 'Endpoint de busca com filtros' },
  { value: 'filter_by_date_range', label: 'Filtro por Data', desc: 'Filtrar por período' },
  { value: 'bulk_create', label: 'Criação em Lote', desc: 'POST com array' },
  { value: 'bulk_delete', label: 'Deleção em Lote', desc: 'DELETE com array de IDs' },
  { value: 'export_csv', label: 'Exportar CSV', desc: 'Download em CSV' },
  { value: 'import_csv', label: 'Importar CSV', desc: 'Upload de CSV' },
];

const AUTH_OPTIONS = [
  { value: 'none', label: 'Público', icon: Globe, desc: 'Sem autenticação' },
  { value: 'required', label: 'Autenticado', icon: Key, desc: 'Usuário logado' },
  { value: 'api_key', label: 'API Key', icon: Shield, desc: 'Chave de API' },
];

export default function ApiConfigEditor({ entity, onUpdate }) {
  const [newRole, setNewRole] = useState('');

  const toggleEndpoint = (endpoint) => {
    const current = entity.api?.endpoints || [];
    const newEndpoints = current.includes(endpoint)
      ? current.filter(e => e !== endpoint)
      : [...current, endpoint];

    onUpdate({
      ...entity,
      api: { ...entity.api, endpoints: newEndpoints }
    });
  };

  const setAuth = (auth) => {
    onUpdate({
      ...entity,
      api: { ...entity.api, auth }
    });
  };

  const addRole = () => {
    if (!newRole.trim()) return;
    const current = entity.api?.roles || [];
    if (current.includes(newRole.trim())) return;

    onUpdate({
      ...entity,
      api: { ...entity.api, roles: [...current, newRole.trim()] }
    });
    setNewRole('');
  };

  const removeRole = (role) => {
    onUpdate({
      ...entity,
      api: { ...entity.api, roles: entity.api.roles.filter(r => r !== role) }
    });
  };

  return (
    <div className="space-y-6">

      {/* Endpoints */}
      <div>
        <h3 className="text-sm font-medium mb-3 flex items-center gap-2">
          <Globe className="w-4 h-4 text-slate-400" />
          Endpoints Expostos
        </h3>

        <div className="grid grid-cols-2 gap-2">
          {ENDPOINT_OPTIONS.map((opt) => {
            const isActive = entity.api?.endpoints?.includes(opt.value);
            return (
              <div
                key={opt.value}
                role="button"
                tabIndex={0}
                onClick={() => toggleEndpoint(opt.value)}
                className={cn(
                  "p-3 rounded-lg border text-left transition-all cursor-pointer",
                  isActive
                    ? "border-emerald-500/50 bg-emerald-600/10"
                    : "border-slate-800 bg-slate-900/50 hover:border-slate-700"
                )}
              >
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">{opt.label}</span>
                  <Switch checked={isActive} />
                </div>
                <p className="text-xs text-slate-500 mt-1">{opt.desc}</p>
              </div>
            );
          })}
        </div>
      </div>

      {/* Authentication */}
      <div>
        <h3 className="text-sm font-medium mb-3 flex items-center gap-2">
          <Key className="w-4 h-4 text-slate-400" />
          Autenticação
        </h3>

        <div className="grid grid-cols-3 gap-2">
          {AUTH_OPTIONS.map((opt) => {
            const isActive = entity.api?.auth === opt.value;
            const Icon = opt.icon;

            return (
              <div
                key={opt.value}
                role="button"
                tabIndex={0}
                onClick={() => setAuth(opt.value)}
                className={cn(
                  "p-4 rounded-lg border text-center transition-all cursor-pointer",
                  isActive
                    ? "border-violet-500/50 bg-violet-600/10"
                    : "border-slate-800 bg-slate-900/50 hover:border-slate-700"
                )}
              >
                <Icon className={cn(
                  "w-6 h-6 mx-auto mb-2",
                  isActive ? "text-violet-400" : "text-slate-500"
                )} />
                <span className="text-sm font-medium">{opt.label}</span>
                <p className="text-xs text-slate-500 mt-1">{opt.desc}</p>
              </div>
            );
          })}
        </div>
      </div>

      {/* Roles */}
      <div>
        <h3 className="text-sm font-medium mb-3 flex items-center gap-2">
          <Users className="w-4 h-4 text-slate-400" />
          Roles com Acesso
        </h3>

        <div className="flex flex-wrap gap-2 mb-3">
          {entity.api?.roles?.map((role) => (
            <Badge
              key={role}
              variant="outline"
              className="border-slate-700 pl-3 pr-1.5 py-1.5 gap-2"
            >
              {role}
              <button
                onClick={() => removeRole(role)}
                className="hover:bg-slate-700 rounded-full p-0.5"
              >
                <X className="w-3 h-3" />
              </button>
            </Badge>
          ))}

          {(!entity.api?.roles || entity.api.roles.length === 0) && (
            <span className="text-sm text-slate-500">Nenhuma role definida</span>
          )}
        </div>

        <div className="flex gap-2">
          <Input
            value={newRole}
            onChange={(e) => setNewRole(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && addRole()}
            placeholder="Nome da role (ex: admin, editor)"
            className="bg-slate-900 border-slate-700"
          />

          <Button onClick={addRole} variant="outline" className="border-slate-700">
            <Plus className="w-4 h-4" />
          </Button>
        </div>
      </div>

      {/* Preview of generated routes */}
      <div>
        <h3 className="text-sm font-medium mb-3 text-slate-400">Preview das Rotas</h3>

        <div className="bg-slate-900 rounded-lg p-4 font-mono text-xs space-y-1.5">

          {entity.api?.endpoints?.includes('crud') && (
            <>
              <div className="flex items-center gap-3">
                <Badge className="bg-emerald-600 text-[10px] w-14 justify-center">GET</Badge>
                <span className="text-slate-300">/api/{entity.name.toLowerCase()}</span>
              </div>

              <div className="flex items-center gap-3">
                <Badge className="bg-emerald-600 text-[10px] w-14 justify-center">GET</Badge>
                <span className="text-slate-300">/api/{entity.name.toLowerCase()}/{'{id}'}</span>
              </div>

              <div className="flex items-center gap-3">
                <Badge className="bg-blue-600 text-[10px] w-14 justify-center">POST</Badge>
                <span className="text-slate-300">/api/{entity.name.toLowerCase()}</span>
              </div>

              <div className="flex items-center gap-3">
                <Badge className="bg-amber-600 text-[10px] w-14 justify-center">PUT</Badge>
                <span className="text-slate-300">/api/{entity.name.toLowerCase()}/{'{id}'}</span>
              </div>

              <div className="flex items-center gap-3">
                <Badge className="bg-red-600 text-[10px] w-14 justify-center">DELETE</Badge>
                <span className="text-slate-300">/api/{entity.name.toLowerCase()}/{'{id}'}</span>
              </div>
            </>
          )}

          {entity.api?.endpoints?.includes('search') && (
            <div className="flex items-center gap-3">
              <Badge className="bg-emerald-600 text-[10px] w-14 justify-center">GET</Badge>
              <span className="text-slate-300">/api/{entity.name.toLowerCase()}/search</span>
            </div>
          )}

          {entity.api?.endpoints?.includes('bulk_create') && (
            <div className="flex items-center gap-3">
              <Badge className="bg-blue-600 text-[10px] w-14 justify-center">POST</Badge>
              <span className="text-slate-300">/api/{entity.name.toLowerCase()}/bulk</span>
            </div>
          )}

          {entity.api?.endpoints?.includes('export_csv') && (
            <div className="flex items-center gap-3">
              <Badge className="bg-emerald-600 text-[10px] w-14 justify-center">GET</Badge>
              <span className="text-slate-300">/api/{entity.name.toLowerCase()}/export</span>
            </div>
          )}

        </div>
      </div>
    </div>
  );
}
