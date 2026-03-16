import React from 'react';
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import Switch from "@/components/ui/switch";
import { Plus, Trash2, ArrowLeftRight } from 'lucide-react';
import { cn } from "@/lib/utils";

const RELATION_TYPES = [
  { value: '1:1', label: 'Um para Um' },
  { value: '1:N', label: 'Um (esta) para Muitos (alvo)' },
  { value: 'N:1', label: 'Muitos (esta) para Um (alvo)' },
  { value: 'N:N', label: 'Muitos para Muitos' },
];

const CASCADE_OPTIONS = [
  { value: 'cascade', label: 'Cascade', desc: 'Deleta/atualiza em cascata' },
  { value: 'restrict', label: 'Restrict', desc: 'Impede se houver dependências' },
  { value: 'set_null', label: 'Set Null', desc: 'Define como NULL' },
  { value: 'no_action', label: 'No Action', desc: 'Nenhuma ação automática' },
];

export default function RelationsEditor({ entity, allEntities, onUpdate }) {
  const otherEntities = allEntities.filter(e => e.name !== entity.name);

  const handleAddRelation = () => {
  if (otherEntities.length === 0) return;

  const targetEntity = otherEntities[0];

  const newRelation = {
    target: targetEntity.name,
    type: 'N:1',
    required: false,
    cascade: 'restrict'
  };

  onUpdate({
    ...entity,
    relations: [...entity.relations, newRelation]
  });
};

const handleUpdateRelation = (index, updates) => {
  const newRelations = [...entity.relations];
  const oldRelation = newRelations[index];
  newRelations[index] = { ...oldRelation, ...updates };

  onUpdate({ ...entity, relations: newRelations });
};

const handleDeleteRelation = (index) => {
  const relToRemove = entity.relations[index];

  onUpdate({
    ...entity,
    relations: entity.relations.filter((_, i) => i !== index)
  });
};


  const getRelationColor = (type) => {
    switch (type) {
      case '1:1': return 'from-blue-500 to-cyan-500';
      case '1:N': return 'from-violet-500 to-purple-500';
      case 'N:1': return 'from-emerald-500 to-teal-500';
      case 'N:N': return 'from-orange-500 to-amber-500';
      default: return 'from-slate-500 to-slate-600';
    }
  };

  const getCardinality = (type) => {
    switch (type) {
      case '1:1': return { left: '1', right: '1', symbol: '↔' };
      case '1:N': return { left: '1', right: 'N', symbol: '→' };
      case 'N:1': return { left: 'N', right: '1', symbol: '→' };
      case 'N:N': return { left: 'N', right: 'N', symbol: '↔' };
      default: return { left: '?', right: '?', symbol: '?' };
    }
  };

  const relationsToShow = entity.relations || [];

  return (
    <div className="space-y-4">
      {relationsToShow.length === 0 ? (
        <div className="text-center py-12 text-slate-500">
          <ArrowLeftRight className="w-12 h-12 mx-auto mb-3 opacity-20" />
          <p>Nenhuma relação definida</p>
          <p className="text-sm mt-1">Adicione relações com outras entidades</p>
        </div>
      ) : (
        relationsToShow.map((relation, index) => {
          const card = getCardinality(relation.type);

          return (
            <div key={index} className="rounded-xl border border-slate-800 bg-slate-900/50 p-5">
              <div className="flex items-center justify-center gap-6 mb-6 py-4 bg-slate-800/50 rounded-lg">
                <div className="text-center">
                  <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-violet-500 to-fuchsia-500 flex items-center justify-center mx-auto mb-2">
                    <span className="text-sm font-bold text-white">{entity.name.charAt(0)}</span>
                  </div>
                  <span className="text-xs text-slate-400">{entity.name}</span>
                </div>

                <div className="flex items-center gap-3">
                  <span className="text-lg font-bold text-white">{card.left}</span>
                  <div className={cn(
                    "px-3 py-1 rounded-full text-xs font-bold text-white bg-gradient-to-r",
                    getRelationColor(relation.type)
                  )}>{card.symbol}</div>
                  <span className="text-lg font-bold text-white">{card.right}</span>
                </div>

                <div className="text-center">
                  <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-emerald-500 to-teal-500 flex items-center justify-center mx-auto mb-2">
                    <span className="text-sm font-bold text-white">{relation.target.charAt(0)}</span>
                  </div>
                  <span className="text-xs text-slate-400">{relation.target}</span>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-xs text-slate-500 mb-1.5 block">Entidade Alvo</label>
                  <Select
                    value={relation.target}
                    onValueChange={(value) => handleUpdateRelation(index, { target: value })}
                  >
                    <SelectTrigger className="bg-slate-800 border-slate-700">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {otherEntities.map(e => (
                        <SelectItem key={e.name} value={e.name}>{e.name}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div>
                  <label className="text-xs text-slate-500 mb-1.5 block">Tipo de Relação</label>
                  <Select
                    value={relation.type}
                    onValueChange={(value) => handleUpdateRelation(index, { type: value })}
                  >
                    <SelectTrigger className="bg-slate-800 border-slate-700">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {RELATION_TYPES.map(type => (
                        <SelectItem key={type.value} value={type.value}>{type.label}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>

              <div className="mt-4">
                <label className="text-xs text-slate-500 mb-1.5 block">Comportamento em Delete/Update</label>
                <Select
                  value={relation.cascade}
                  onValueChange={(value) => handleUpdateRelation(index, { cascade: value })}
                >
                  <SelectTrigger className="bg-slate-800 border-slate-700">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {CASCADE_OPTIONS.map(opt => (
                      <SelectItem key={opt.value} value={opt.value}>
                        <div>
                          <span className="font-medium">{opt.label}</span>
                          <span className="text-slate-500 ml-2 text-xs">{opt.desc}</span>
                        </div>
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="flex items-center justify-between mt-4 pt-4 border-t border-slate-800">
                <div className="flex items-center gap-3">
                  <Switch
                    checked={relation.required}
                    onCheckedChange={(checked) => handleUpdateRelation(index, { required: checked })}
                  />
                  <span className="text-sm text-slate-400">Relação Obrigatória</span>
                </div>

                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => handleDeleteRelation(index)}
                  className="text-red-400 hover:text-red-300 hover:bg-red-500/10"
                >
                  <Trash2 className="w-4 h-4 mr-2" />
                  Remover
                </Button>
              </div>
            </div>
          );
        })
      )}

      <Button
        onClick={handleAddRelation}
        variant="outline"
        className="w-full border-dashed border-slate-700 text-slate-400 hover:text-white hover:border-violet-500"
        disabled={otherEntities.length === 0}
      >
        <Plus className="w-4 h-4 mr-2" />
        Adicionar Relação
      </Button>
    </div>
  );
}
