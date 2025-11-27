import React, { useState } from 'react';
import EntityList from './components/schema-builder/EntityList';
import EntityEditor from './components/schema-builder/EntityEditor';
import JsonPreview from './components/schema-builder/JsonPreview';
import RelationDiagram from './components/schema-builder/RelationDiagram';

const initialEntities = [
  {
    name: "Medico",
    attributes: [
      { name: "id", type: "number", constraints: ["primary_key", "auto_increment"] },
      { name: "nome", type: "string", constraints: ["required", "max_length:100"] },
      { name: "crm", type: "string", constraints: ["required", "unique"] },
      { name: "especialidade", type: "string", constraints: [] }
    ],
    relations: [
      { target: "Sala", type: "1:N", required: false, cascade: "set_null" },
      { target: "Consulta", type: "1:N", required: false, cascade: "cascade" }
    ],
    behaviors: [],
    api: { endpoints: ["crud"], auth: "required", roles: ["admin", "medico"] }
  },
  {
    name: "Sala",
    attributes: [
      { name: "id", type: "number", constraints: ["primary_key", "auto_increment"] },
      { name: "numero", type: "string", constraints: ["required", "unique"] },
      { name: "andar", type: "string", constraints: ["required"] },
      { name: "tipo", type: "string", constraints: [] }
    ],
    relations: [
      { target: "Medico", type: "N:1", required: false, cascade: "restrict" }
    ],
    behaviors: [],
    api: { endpoints: ["crud"], auth: "required", roles: ["admin"] }
  },
  {
    name: "Paciente",
    attributes: [
      { name: "id", type: "number", constraints: ["primary_key", "auto_increment"] },
      { name: "nome", type: "string", constraints: ["required", "max_length:100"] },
      { name: "cpf", type: "string", constraints: ["required", "unique", "regex:^\\d{11}$"] },
      { name: "dataNascimento", type: "date", constraints: ["required"] }
    ],
    relations: [
      { target: "Consulta", type: "1:N", required: false, cascade: "cascade" }
    ],
    behaviors: ["soft_delete"],
    api: { endpoints: ["crud", "search"], auth: "required", roles: ["admin", "recepcionista"] }
  },
  {
    name: "Consulta",
    attributes: [
      { name: "id", type: "number", constraints: ["primary_key", "auto_increment"] },
      { name: "data", type: "date", constraints: ["required"] },
      { name: "hora", type: "time", constraints: ["required"] },
      { name: "diagnostico", type: "string", constraints: ["max_length:500"] },
      { name: "status", type: "enum", values: ["agendada", "realizada", "cancelada"], constraints: ["required"], default: "agendada" }
    ],
    relations: [
      { target: "Medico", type: "N:1", required: true, cascade: "restrict" },
      { target: "Paciente", type: "N:1", required: true, cascade: "restrict" }
    ],
    behaviors: ["audit_log", { type: "state_machine", field: "status", transitions: [
      { from: "agendada", to: ["realizada", "cancelada"] },
      { from: "realizada", to: [] },
      { from: "cancelada", to: ["agendada"] }
    ]}],
    api: { endpoints: ["crud", "filter_by_date_range"], auth: "required", roles: ["admin", "medico", "recepcionista"] }
  },
  {
    name: "Funcionario",
    attributes: [
      { name: "id", type: "number", constraints: ["primary_key", "auto_increment"] },
      { name: "nome", type: "string", constraints: ["required"] },
      { name: "cargo", type: "string", constraints: ["required"] },
      { name: "salario", type: "number", constraints: ["required", "min:0"] }
    ],
    relations: [],
    behaviors: ["soft_delete", "audit_log"],
    api: { endpoints: ["crud"], auth: "required", roles: ["admin"] }
  }
];

export default function Home() {
  const [entities, setEntities] = useState(initialEntities);
  const [selectedEntity, setSelectedEntity] = useState(null);
  const [rightPanel, setRightPanel] = useState('json');

  const handleUpdateEntity = (updatedEntity) => {
    setEntities(prev => prev.map(e => e.name === updatedEntity.name ? updatedEntity : e));
    setSelectedEntity(updatedEntity);
  };

  const handleAddEntity = () => {
    const newEntity = {
      name: `NovaEntidade${entities.length + 1}`,
      attributes: [
        { name: "id", type: "number", constraints: ["primary_key", "auto_increment"] }
      ],
      relations: [],
      behaviors: [],
      api: { endpoints: ["crud"], auth: "required", roles: ["admin"] }
    };
    setEntities(prev => [...prev, newEntity]);
    setSelectedEntity(newEntity);
  };

  const handleDeleteEntity = (entityName) => {
    setEntities(prev => prev.filter(e => e.name !== entityName));
    if (selectedEntity?.name === entityName) {
      setSelectedEntity(null);
    }
  };

  const handleRenameEntity = (oldName, newName) => {
    setEntities(prev => {
      return prev.map(e => {
        if (e.name === oldName) return { ...e, name: newName };
        return { ...e, relations: e.relations.map(r => r.target === oldName ? { ...r, target: newName } : r) };
      });
    });
    if (selectedEntity?.name === oldName) {
      setSelectedEntity(prev => ({ ...prev, name: newName }));
    }
  };

  return (
    <div className="min-h-screen bg-gray-900 text-white flex flex-col">
      {/* Header */}
      <header className="border-b border-gray-800 px-6 py-4 flex items-center justify-between bg-gray-800/50 backdrop-blur-sm">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-purple-600 flex items-center justify-center">
            📦
          </div>
          <div>
            <h1 className="text-lg font-semibold tracking-tight">Schema Builder</h1>
            <p className="text-xs text-gray-400">Gerador de código determinístico</p>
          </div>
        </div>
        <div className="flex items-center gap-2 text-xs text-gray-400">
          <span className="px-2 py-1 rounded bg-gray-700">{entities.length} entidades</span>
          <span className="px-2 py-1 rounded bg-gray-700">{entities.reduce((acc, e) => acc + e.attributes.length, 0)} atributos</span>
        </div>
      </header>

      {/* Main Content */}
      <div className="flex-1 flex overflow-hidden">
        {/* Left Panel */}
        <div className="w-64 border-r border-gray-800 bg-gray-800/30 flex flex-col">
          <EntityList
            entities={entities}
            selectedEntity={selectedEntity}
            onSelect={setSelectedEntity}
            onAdd={handleAddEntity}
            onDelete={handleDeleteEntity}
          />
        </div>

        {/* Center Panel */}
        <div className="flex-1 overflow-auto">
          {selectedEntity ? (
            <EntityEditor
              entity={selectedEntity}
              allEntities={entities}
              onUpdate={handleUpdateEntity}
              onRename={handleRenameEntity}
            />
          ) : (
            <div className="h-full flex items-center justify-center text-gray-500">
              <div className="text-center">
                📦
                <p>Selecione uma entidade para editar</p>
                <p className="text-sm mt-1">ou crie uma nova</p>
              </div>
            </div>
          )}
        </div>

        {/* Right Panel */}
        <div className="w-96 border-l border-gray-800 bg-gray-800/30 flex flex-col">
          <div className="flex gap-2 m-4">
            <button
              className={`flex-1 p-2 rounded ${rightPanel === 'json' ? 'bg-gray-700' : 'bg-gray-600'}`}
              onClick={() => setRightPanel('json')}
            >
              📝 JSON Output
            </button>
            <button
              className={`flex-1 p-2 rounded ${rightPanel === 'diagram' ? 'bg-gray-700' : 'bg-gray-600'}`}
              onClick={() => setRightPanel('diagram')}
            >
              🌐 Diagrama
            </button>
          </div>
          <div className="flex-1 overflow-auto p-4">
            {rightPanel === 'json' ? (
              <JsonPreview entities={entities} />
            ) : (
              <RelationDiagram entities={entities} onSelectEntity={setSelectedEntity} />
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
