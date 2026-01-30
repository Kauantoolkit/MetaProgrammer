import React, { useState, useEffect } from 'react';
import EntityList from './components/schema-builder/EntityList';
import EntityEditor from './components/schema-builder/EntityEditor';
import JsonPreview from './components/schema-builder/JsonPreview';
import RelationDiagram from './components/schema-builder/RelationDiagram';

const STORAGE_KEY = 'schema_builder_entities_v1';
const SELECTED_ENTITY_KEY = 'schema_builder_selected_entity_v1';
const RIGHT_PANEL_KEY = 'schema_builder_right_panel_v1';
const APP_NAME_KEY = 'schema_builder_app_name_v1';
const NODE_POSITIONS_KEY = 'schema_builder_node_positions_v1';


const storage = (() => {
  try {
    const test = '__storage_test__';
    sessionStorage.setItem(test, test);
    sessionStorage.removeItem(test);
    return sessionStorage;
  } catch {
    return null;
  }
})();

const loadEntities = () => {
  if (!storage) return [];
  try {
    const raw = storage.getItem(STORAGE_KEY);
    if (raw) {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed)) {
        return parsed.map(entity => ({
          name: entity.name,
          attributes: (entity.attributes || []).map(attr => ({
            name: attr.name,
            type: attr.type,
            constraints: attr.constraints || [],
            values: attr.values,
            default: attr.default
          })),
          relations: (entity.relations || []).map(rel => ({
            target: rel.target,
            type: rel.type,
            required: rel.required || false,
            cascade: rel.cascade || "restrict"
          })),
          behaviors: entity.behaviors || [],
          api: entity.api || { endpoints: [], auth: "required", roles: ["admin"] }
        }));
      }
    }
  } catch (err) {
    console.warn("Erro ao ler storage:", err);
  }
  return [];
};

const loadNodePositions = () => {
  if (!storage) return [];
  try {
    const raw = storage.getItem(NODE_POSITIONS_KEY);
    if (raw) {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed)) return parsed;
    }
  } catch (err) {
    console.warn("Erro ao ler posições:", err);
  }
  return [];
};

const SYSTEM_USER_ENTITY = {
  name: "User",
  fixed: true,
  attributes: [
    { name: "id", type: "Long", constraints: ["primary_key"] },
    { name: "username", type: "String", constraints: ["unique"] },
    { name: "email", type: "String", constraints: ["unique"] },
    { name: "password", type: "String" },
    { name: "role", type: "Enum" },
    { name: "enabled", type: "Boolean", default: true }
  ],
  relations: [],
  behaviors: [],
  api: { endpoints: ["crud"], auth: "required", roles: ["admin"] }
};

const mirrorType = (type) => {
  switch (type) {
    case "1:N": return "N:1";
    case "N:1": return "1:N";
    case "1:1": return "1:1";
    case "N:N": return "N:N";
    default: return type;
  }
};

const syncRelations = (entities, sourceEntity) => {
  return entities.map(entity => {
    if (entity.name === sourceEntity.name) return entity;

    const relationFromSource = sourceEntity.relations.find(r => r.target === entity.name);

    // ❌ Se não existe mais relação do source → REMOVE espelho antigo
    if (!relationFromSource) {
      return {
        ...entity,
        relations: entity.relations.filter(r => r.target !== sourceEntity.name)
      };
    }

    const expectedType = mirrorType(relationFromSource.type);

    const otherRelations = entity.relations.filter(r => r.target !== sourceEntity.name);

    return {
      ...entity,
      relations: [
        ...otherRelations,
        {
          target: sourceEntity.name,
          type: expectedType,
          required: relationFromSource.required ?? false,
          cascade: relationFromSource.cascade ?? "restrict"
        }
      ]
    };
  });
};






export default function Home() {
  const [nodePositions, setNodePositions] = useState(loadNodePositions);
  const [entities, setEntities] = useState(() => {
  const loaded = loadEntities();
  const hasUser = loaded.some(e => e.name === "User");
  return hasUser ? loaded : [SYSTEM_USER_ENTITY, ...loaded];
});

  const [selectedEntity, setSelectedEntity] = useState(null);
  const [rightPanel, setRightPanel] = useState('json');
  const [appName, setAppName] = useState('my-app');

  useEffect(() => {
    if (storage) {
      const selectedEntityName = storage.getItem(SELECTED_ENTITY_KEY);
      if (selectedEntityName) {
        const selected = entities.find(e => e.name === selectedEntityName);
        setSelectedEntity(selected || entities[0] || null);
      } else {
        setSelectedEntity(entities[0] || null);
      }

      const savedRightPanel = storage.getItem(RIGHT_PANEL_KEY);
      if (savedRightPanel === 'json' || savedRightPanel === 'diagram') {
        setRightPanel(savedRightPanel);
      }

      const savedAppName = storage.getItem(APP_NAME_KEY);
      if (savedAppName) setAppName(savedAppName);
    }
  }, [entities]);

  useEffect(() => {
    storage?.setItem(STORAGE_KEY, JSON.stringify(entities));
  }, [entities]);

  useEffect(() => {
  storage?.setItem(NODE_POSITIONS_KEY, JSON.stringify(nodePositions));
}, [nodePositions]);


  useEffect(() => {
    if (selectedEntity) storage?.setItem(SELECTED_ENTITY_KEY, selectedEntity.name);
    else storage?.removeItem(SELECTED_ENTITY_KEY);
  }, [selectedEntity]);

  useEffect(() => {
    storage?.setItem(RIGHT_PANEL_KEY, rightPanel);
  }, [rightPanel]);

  useEffect(() => {
    storage?.setItem(APP_NAME_KEY, appName);
  }, [appName]);

  const handleUpdateEntity = (updatedEntity) => {
  setEntities(prev => {
    const updatedList = prev.map(e =>
      e.name === updatedEntity.name ? updatedEntity : e
    );

    return syncRelations(updatedList, updatedEntity);
  });

  setSelectedEntity(updatedEntity);
};

  const handleAddEntity = () => {
    const newEntity = {
      name: `NovaEntidade${entities.length + 1}`,
      attributes: [{ name: "id", type: "Long", constraints: ["primary_key", "auto_increment"] }],
      relations: [],
      behaviors: [],
      api: { endpoints: ["crud"], auth: "required", roles: ["admin"] }
    };
    setEntities(prev => [...prev, newEntity]);
    setSelectedEntity(newEntity);
  };

  const handleDeleteEntity = (entityName) => {
    setEntities(prev => prev.filter(e => e.name !== entityName));
    if (selectedEntity?.name === entityName) setSelectedEntity(null);
  };

  const handleRenameEntity = (oldName, newName) => {
    setEntities(prev =>
      prev.map(e => {
        if (e.name === oldName) return { ...e, name: newName };
        return { ...e, relations: e.relations.map(r => r.target === oldName ? { ...r, target: newName } : r) };
      })
    );
    if (selectedEntity?.name === oldName) setSelectedEntity(prev => ({ ...prev, name: newName }));
  };

  const rightPanelWidth =
    rightPanel === 'diagram'
      ? 'w-[42rem] xl:w-[52rem]'   // 🔥 bem mais espaço pro diagrama
      : 'w-96';

  return (
    <div className="min-h-screen bg-gray-900 text-white flex flex-col">
      <header className="border-b border-gray-800 px-6 py-4 flex items-center justify-between bg-gray-800/50 backdrop-blur-sm">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-purple-600 flex items-center justify-center">📦</div>
          <div>
            <h1 className="text-lg font-semibold tracking-tight">Schema Builder</h1>
            <p className="text-xs text-gray-400">Gerador de código determinístico</p>
          </div>
        </div>

        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2">
            <label className="text-xs text-gray-400">App Name:</label>
            <input
              value={appName}
              onChange={(e) => setAppName(e.target.value)}
              className="px-2 py-1 text-xs bg-gray-700 border border-gray-600 rounded"
            />
          </div>
          <div className="flex items-center gap-2 text-xs text-gray-400">
            <span className="px-2 py-1 rounded bg-gray-700">{entities.length} entidades</span>
            <span className="px-2 py-1 rounded bg-gray-700">
              {entities.reduce((acc, e) => acc + e.attributes.length, 0)} atributos
            </span>
          </div>
        </div>
      </header>

      <div className="flex-1 flex overflow-hidden">
        {/* LEFT */}
        <div className="w-64 border-r border-gray-800 bg-gray-800/30 flex flex-col">
          <EntityList
            entities={entities}
            selectedEntity={selectedEntity}
            onSelect={setSelectedEntity}
            onAdd={handleAddEntity}
            onDelete={handleDeleteEntity}
          />
        </div>

        {/* CENTER */}
        <div className="flex-1 min-w-0 overflow-auto">
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

        {/* RIGHT */}
        <div className={`${rightPanelWidth} transition-all duration-300 border-l border-gray-800 bg-gray-800/30 flex flex-col`}>
          <div className="flex gap-2 m-4">
            <button
              className={`flex-1 p-2 rounded ${rightPanel === 'json' ? 'bg-gray-700' : 'bg-gray-600'}`}
              onClick={() => setRightPanel('json')}
            >
              📝 JSON
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
              <JsonPreview
                entities={entities}
                appName={appName}
                onLoadEntities={(loaded) => {
                  setEntities(loaded);
                  setSelectedEntity(loaded[0] || null);
                }}
              />
            ) : (
              <div className="w-full h-full min-h-[600px]">
               <RelationDiagram
  entities={entities}
  nodePositions={nodePositions}
  setNodePositions={setNodePositions}
  onSelectEntity={setSelectedEntity}
/>

              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
