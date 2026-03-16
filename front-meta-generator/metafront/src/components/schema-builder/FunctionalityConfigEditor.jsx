import React from 'react';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Switch } from "@/components/ui/switch";
import { Label } from "@/components/ui/label";
import { Database, Settings } from 'lucide-react';

export default function FunctionalityConfigEditor({ functionality, allEntities, onUpdate }) {
  const handleEntityChange = (entityName) => {
    const updatedFunctionality = {
      ...functionality,
      entity: entityName || undefined
    };
    onUpdate(updatedFunctionality);
  };

  const handleBackofficeToggle = (checked) => {
    const updatedFunctionality = {
      ...functionality,
      exposeInBackoffice: checked
    };
    onUpdate(updatedFunctionality);
  };

  const availableEntities = allEntities.filter(e => e.name !== "Users");

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3 mb-6">
        <Settings className="w-5 h-5 text-slate-400" />
        <h3 className="text-lg font-semibold text-slate-200">Configuração da Funcionalidade</h3>
      </div>

      {/* Entity Association */}
      <div className="p-4 bg-slate-800/50 rounded-lg border border-slate-700">
        <Label className="text-sm font-medium text-slate-300 mb-3 block">
          Vinculação com Entidade (Opcional)
        </Label>
        <p className="text-xs text-slate-500 mb-3">
          Vincule esta funcionalidade a uma entidade para acessar seus repositórios e serviços.
        </p>
        <Select
          value={functionality.entity || ""}
          onValueChange={handleEntityChange}
        >
          <SelectTrigger className="bg-slate-900 border-slate-600">
            <SelectValue placeholder="Selecione uma entidade (opcional)" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="">
              <div className="flex items-center gap-2">
                <Database className="w-4 h-4 opacity-50" />
                Nenhuma (funcionalidade independente)
              </div>
            </SelectItem>
            {availableEntities.map((entity) => (
              <SelectItem key={entity.name} value={entity.name}>
                <div className="flex items-center gap-2">
                  <Database className="w-4 h-4" />
                  {entity.name}
                </div>
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        {functionality.entity && (
          <p className="text-xs text-emerald-400 mt-2">
            ✅ Vinculada à entidade "{functionality.entity}"
          </p>
        )}
      </div>

      {/* Backoffice Exposure */}
      <div className="p-4 bg-slate-800/50 rounded-lg border border-slate-700">
        <div className="flex items-center justify-between">
          <div className="flex-1">
            <Label className="text-sm font-medium text-slate-300">
              Expor no Backoffice
            </Label>
            <p className="text-xs text-slate-500 mt-1">
              Permite executar esta funcionalidade através da interface administrativa.
            </p>
          </div>
          <Switch
            checked={functionality.exposeInBackoffice || false}
            onCheckedChange={handleBackofficeToggle}
          />
        </div>
        {functionality.exposeInBackoffice && (
          <p className="text-xs text-blue-400 mt-2">
            🔗 Esta funcionalidade será acessível no backoffice
          </p>
        )}
      </div>

      {/* Summary */}
      <div className="p-4 bg-slate-800/30 rounded-lg border border-slate-700">
        <h4 className="text-sm font-medium text-slate-300 mb-2">Resumo</h4>
        <div className="text-xs text-slate-400 space-y-1">
          <p>• Entrada: {functionality.input?.length || 0} campos</p>
          <p>• Saída: {functionality.output?.length || 0} campos</p>
          <p>• Entidade: {functionality.entity || "Independente"}</p>
          <p>• Backoffice: {functionality.exposeInBackoffice ? "Sim" : "Não"}</p>
        </div>
      </div>
    </div>
  );
}
