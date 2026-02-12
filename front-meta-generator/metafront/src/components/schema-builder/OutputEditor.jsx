import React, { useState } from 'react';
import { Button } from "@/components/ui/button";
import Input from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Plus, Trash2, Type, Hash, CheckSquare, Calendar } from 'lucide-react';

const TYPE_OPTIONS = [
  { value: 'String', label: 'String', icon: Type },
  { value: 'Integer', label: 'Integer', icon: Hash },
  { value: 'Long', label: 'Long', icon: Hash },
  { value: 'Boolean', label: 'Boolean', icon: CheckSquare },
  { value: 'Double', label: 'Double', icon: Hash },
];

export default function OutputEditor({ functionality, onUpdate }) {
  const [newFieldName, setNewFieldName] = useState('');
  const [newFieldType, setNewFieldType] = useState('String');

  const outputFields = functionality.output || [];

  const addField = () => {
    if (!newFieldName.trim()) return;

    const newField = {
      name: newFieldName.trim(),
      type: newFieldType
    };

    const updatedFunctionality = {
      ...functionality,
      output: [...outputFields, newField]
    };

    onUpdate(updatedFunctionality);
    setNewFieldName('');
  };

  const removeField = (index) => {
    const updatedOutput = outputFields.filter((_, i) => i !== index);
    const updatedFunctionality = {
      ...functionality,
      output: updatedOutput
    };
    onUpdate(updatedFunctionality);
  };

  const updateField = (index, field) => {
    const updatedOutput = outputFields.map((f, i) => i === index ? field : f);
    const updatedFunctionality = {
      ...functionality,
      output: updatedOutput
    };
    onUpdate(updatedFunctionality);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-semibold text-slate-200">Campos de Saída</h3>
        <span className="text-sm text-slate-500">{outputFields.length} campos</span>
      </div>

      {/* Add New Field */}
      <div className="p-4 bg-slate-800/50 rounded-lg border border-slate-700">
        <h4 className="text-sm font-medium text-slate-300 mb-3">Adicionar Campo</h4>
        <div className="flex gap-3">
          <Input
            placeholder="Nome do campo"
            value={newFieldName}
            onChange={(e) => setNewFieldName(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && addField()}
            className="flex-1 bg-slate-900 border-slate-600"
          />
          <Select value={newFieldType} onValueChange={setNewFieldType}>
            <SelectTrigger className="w-32 bg-slate-900 border-slate-600">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {TYPE_OPTIONS.map((option) => {
                const Icon = option.icon;
                return (
                  <SelectItem key={option.value} value={option.value}>
                    <div className="flex items-center gap-2">
                      <Icon className="w-4 h-4" />
                      {option.label}
                    </div>
                  </SelectItem>
                );
              })}
            </SelectContent>
          </Select>
          <Button onClick={addField} className="bg-emerald-600 hover:bg-emerald-700">
            <Plus className="w-4 h-4" />
          </Button>
        </div>
      </div>

      {/* Existing Fields */}
      <div className="space-y-3">
        {outputFields.map((field, index) => (
          <div key={index} className="p-4 bg-slate-800/30 rounded-lg border border-slate-700">
            <div className="flex items-center gap-3">
              <div className="flex-1">
                <Input
                  value={field.name}
                  onChange={(e) => updateField(index, { ...field, name: e.target.value })}
                  className="bg-slate-900 border-slate-600 text-slate-200"
                  placeholder="Nome do campo"
                />
              </div>
              <Select
                value={field.type}
                onValueChange={(value) => updateField(index, { ...field, type: value })}
              >
                <SelectTrigger className="w-32 bg-slate-900 border-slate-600">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {TYPE_OPTIONS.map((option) => {
                    const Icon = option.icon;
                    return (
                      <SelectItem key={option.value} value={option.value}>
                        <div className="flex items-center gap-2">
                          <Icon className="w-4 h-4" />
                          {option.label}
                        </div>
                      </SelectItem>
                    );
                  })}
                </SelectContent>
              </Select>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => removeField(index)}
                className="text-red-400 hover:text-red-300 hover:bg-red-500/20"
              >
                <Trash2 className="w-4 h-4" />
              </Button>
            </div>
          </div>
        ))}

        {outputFields.length === 0 && (
          <div className="text-center py-8 text-slate-500">
            <Type className="w-12 h-12 mx-auto mb-3 opacity-50" />
            <p>Nenhum campo de saída definido</p>
            <p className="text-sm mt-1">Adicione campos acima</p>
          </div>
        )}
      </div>
    </div>
  );
}
