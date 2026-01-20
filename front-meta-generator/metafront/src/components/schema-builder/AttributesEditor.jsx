import React, { useState } from 'react';
import  {Button}  from "@/components/ui/button";
import Input from "@/components/ui/input";
import  {Select, SelectTrigger, SelectValue, SelectContent, SelectItem} from "@/components/ui/select";
import  Badge  from "@/components/ui/badge";
import { Plus, Trash2, GripVertical, ChevronDown, ChevronRight } from 'lucide-react';
import  {cn}  from "@/lib/utils";

const TYPES = [
  { value: 'string', label: 'String', color: 'bg-emerald-500' },
  { value: 'number', label: 'Number', color: 'bg-blue-500' },
  { value: 'boolean', label: 'Boolean', color: 'bg-amber-500' },
  { value: 'date', label: 'Date', color: 'bg-purple-500' },
  { value: 'time', label: 'Time', color: 'bg-pink-500' },
  { value: 'datetime', label: 'DateTime', color: 'bg-rose-500' },
  { value: 'enum', label: 'Enum', color: 'bg-orange-500' },
  { value: 'text', label: 'Text (Long)', color: 'bg-teal-500' },
];

const CONSTRAINTS = [
  { value: 'required', label: 'Required' },
  { value: 'unique', label: 'Unique' },
  { value: 'primary_key', label: 'Primary Key' },
  { value: 'auto_increment', label: 'Auto Increment' },
  { value: 'indexed', label: 'Indexed' },
];

export default function AttributesEditor({ entity, onUpdate }) {
  const [expandedAttr, setExpandedAttr] = useState(null);

  const handleAddAttribute = () => {
    const newAttr = {
      name: `campo${entity.attributes.length + 1}`,
      type: 'string',
      constraints: []
    };
    onUpdate({
      ...entity,
      attributes: [...entity.attributes, newAttr]
    });
    setExpandedAttr(newAttr.name);
  };

  const handleUpdateAttribute = (index, updates) => {
    const newAttributes = [...entity.attributes];
    newAttributes[index] = { ...newAttributes[index], ...updates };
    onUpdate({ ...entity, attributes: newAttributes });
    if (updates.name) {
      setExpandedAttr(updates.name);
    }
  };

  const handleDeleteAttribute = (index) => {
    onUpdate({
      ...entity,
      attributes: entity.attributes.filter((_, i) => i !== index)
    });
  };

  const handleToggleConstraint = (index, constraint) => {
    const attr = entity.attributes[index];
    const hasConstraint = attr.constraints.some(c => 
      typeof c === 'string' ? c === constraint : c.startsWith(constraint)
    );
    
    let newConstraints;
    if (hasConstraint) {
      newConstraints = attr.constraints.filter(c => 
        typeof c === 'string' ? c !== constraint : !c.startsWith(constraint)
      );
    } else {
      newConstraints = [...attr.constraints, constraint];
    }
    
    handleUpdateAttribute(index, { constraints: newConstraints });
  };

  const getTypeColor = (type) => {
    return TYPES.find(t => t.value === type)?.color || 'bg-slate-500';
  };

  return (
    <div className="space-y-3">
      {entity.attributes.map((attr, index) => (
        <div
          key={index}
          className={cn(
            "rounded-xl border transition-all",
            expandedAttr === attr.name
              ? "border-violet-500/50 bg-slate-800/50"
              : "border-slate-800 bg-slate-900/50 hover:border-slate-700"
          )}
        >
          {/* Collapsed View */}
          <div
            className="p-4 flex items-center gap-3 cursor-pointer"
            onClick={() => setExpandedAttr(expandedAttr === attr.name ? null : attr.name)}
          >
            <GripVertical className="w-4 h-4 text-slate-600" />
            <div className={cn("w-2 h-2 rounded-full", getTypeColor(attr.type))} />
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2">
                <span className="font-mono text-sm">{attr.name}</span>
                <span className="text-xs text-slate-500">{attr.type}</span>
              </div>
              {attr.constraints.length > 0 && (
                <div className="flex gap-1 mt-1 flex-wrap">
                  {attr.constraints.slice(0, 3).map((c, i) => (
                    <Badge key={i} variant="outline" className="text-[10px] px-1.5 py-0 h-4 border-slate-700 text-slate-400">
                      {typeof c === 'string' ? c : c.split(':')[0]}
                    </Badge>
                  ))}
                  {attr.constraints.length > 3 && (
                    <Badge variant="outline" className="text-[10px] px-1.5 py-0 h-4 border-slate-700 text-slate-400">
                      +{attr.constraints.length - 3}
                    </Badge>
                  )}
                </div>
              )}
            </div>
            {expandedAttr === attr.name ? (
              <ChevronDown className="w-4 h-4 text-slate-500" />
            ) : (
              <ChevronRight className="w-4 h-4 text-slate-500" />
            )}
          </div>

          {/* Expanded View */}
          {expandedAttr === attr.name && (
            <div className="px-4 pb-4 space-y-4 border-t border-slate-800 pt-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-xs text-slate-500 mb-1.5 block">Nome do Campo</label>
                  <Input
                    value={attr.name}
                    onChange={(e) => handleUpdateAttribute(index, { name: e.target.value })}
                    className="bg-slate-900 border-slate-700 font-mono"
                  />
                </div>
                <div>
                  <label className="text-xs text-slate-500 mb-1.5 block">Tipo</label>
                  <Select
                    value={attr.type}
                    onValueChange={(value) => handleUpdateAttribute(index, { type: value })}
                  >
                    <SelectTrigger className="bg-slate-900 border-slate-700">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {TYPES.map((type) => (
                        <SelectItem key={type.value} value={type.value}>
                          <div className="flex items-center gap-2">
                            <div className={cn("w-2 h-2 rounded-full", type.color)} />
                            {type.label}
                          </div>
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>

              {attr.type === 'enum' && (
                <div>
                  <label className="text-xs text-slate-500 mb-1.5 block">Valores (separados por vírgula)</label>
                  <Input
                    value={attr.values?.join(', ') || ''}
                    onChange={(e) => handleUpdateAttribute(index, { 
                      values: e.target.value.split(',').map(v => v.trim()).filter(Boolean)
                    })}
                    placeholder="valor1, valor2, valor3"
                    className="bg-slate-900 border-slate-700 font-mono"
                  />
                </div>
              )}

              <div>
                <label className="text-xs text-slate-500 mb-2 block">Constraints</label>
                <div className="flex flex-wrap gap-2">
                  {CONSTRAINTS.map((constraint) => {
                    const isActive = attr.constraints.includes(constraint.value);
                    return (
                      <button
                        key={constraint.value}
                        onClick={() => handleToggleConstraint(index, constraint.value)}
                        className={cn(
                          "px-3 py-1.5 rounded-lg text-xs font-medium transition-all",
                          isActive
                            ? "bg-violet-600 text-white"
                            : "bg-slate-800 text-slate-400 hover:bg-slate-700"
                        )}
                      >
                        {constraint.label}
                      </button>
                    );
                  })}
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-xs text-slate-500 mb-1.5 block">Min Length / Value</label>
                  <Input
                    type="number"
                    placeholder="min"
                    value={attr.constraints.find(c => c.startsWith?.('min:'))?.split(':')[1] || ''}
                    onChange={(e) => {
                      const newConstraints = attr.constraints.filter(c => !c.startsWith?.('min:'));
                      if (e.target.value) newConstraints.push(`min:${e.target.value}`);
                      handleUpdateAttribute(index, { constraints: newConstraints });
                    }}
                    className="bg-slate-900 border-slate-700"
                  />
                </div>
                <div>
                  <label className="text-xs text-slate-500 mb-1.5 block">Max Length / Value</label>
                  <Input
                    type="number"
                    placeholder="max"
                    value={attr.constraints.find(c => c.startsWith?.('max_length:') || c.startsWith?.('max:'))?.split(':')[1] || ''}
                    onChange={(e) => {
                      const newConstraints = attr.constraints.filter(c => !c.startsWith?.('max_length:') && !c.startsWith?.('max:'));
                      if (e.target.value) newConstraints.push(`max_length:${e.target.value}`);
                      handleUpdateAttribute(index, { constraints: newConstraints });
                    }}
                    className="bg-slate-900 border-slate-700"
                  />
                </div>
              </div>

              <div>
                <label className="text-xs text-slate-500 mb-1.5 block">Valor Padrão</label>
                <Input
                  value={attr.default || ''}
                  onChange={(e) => handleUpdateAttribute(index, { default: e.target.value || undefined })}
                  placeholder="Deixe vazio para sem default"
                  className="bg-slate-900 border-slate-700"
                />
              </div>

              <div className="flex justify-end">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => handleDeleteAttribute(index)}
                  className="text-red-400 hover:text-red-300 hover:bg-red-500/10"
                >
                  <Trash2 className="w-4 h-4 mr-2" />
                  Remover Atributo
                </Button>
              </div>
            </div>
          )}
        </div>
      ))}

      <Button
        onClick={handleAddAttribute}
        variant="outline"
        className="w-full border-dashed border-slate-700 text-slate-400 hover:text-white hover:border-violet-500"
      >
        <Plus className="w-4 h-4 mr-2" />
        Adicionar Atributo
      </Button>
    </div>
  );
}