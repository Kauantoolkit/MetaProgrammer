import React, { useState, useEffect } from 'react';
import EntityList from './components/schema-builder/EntityList';
import EntityEditor from './components/schema-builder/EntityEditor';
import FunctionalityList from './components/schema-builder/FunctionalityList';
import FunctionalityEditor from './components/schema-builder/FunctionalityEditor';
import JsonPreview from './components/schema-builder/JsonPreview';
import RelationDiagram from './components/schema-builder/RelationDiagram';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './components/ui/tabs';
import { Database, Zap } from 'lucide-react';
import { toast } from 'sonner';

const STORAGE_KEY = 'schema_builder_entities_v1';
const SELECTED_ENTITY_KEY = 'schema_builder_selected_entity_v1';
const FUNCTIONALITIES_KEY = 'schema_builder_functionalities_v1';
const SELECTED_FUNCTIONALITY_KEY = 'schema_builder_selected_functionality_v1';
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

const STORAGE_UNAVAILABLE = storage === null;

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

const loadFunctionalities = () => {
  if (!storage) return [];
  try {
    const raw = storage.getItem(FUNCTIONALITIES_KEY);
    if (raw) {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed)) {
        return parsed.map(func => ({
          name: func.name,
          input: (func.input || []).map(field => ({
            name: field.name,
            type: field.type
          })),
          output: (func.output || []).map(field => ({
            name: field.name,
            type: field.type
          })),
          entity: func.entity,
          exposeInBackoffice: func.exposeInBackoffice || false
        }));
      }
    }
  } catch (err) {
    console.warn("Erro ao ler funcionalidades:", err);
  }
  return [];
};

const SYSTEM_USERS_ENTITY = {
  name: "Users",
  fixed: true,
  attributes: [
    { name: "id", type: "Long", constraints: ["primary_key"] },
    { name: "username", type: "String", constraints: ["unique"] },
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






// Returns an error message if the entity name is invalid for a Java class, or null if valid.
const validateEntityName = (name) => {
  if (!name || name.trim() === '') return 'O nome não pode ser vazio.';
  if (!/^[a-zA-Z][a-zA-Z0-9]*$/.test(name))
    return 'O nome deve começar com letra e conter apenas letras e números (sem espaços ou caracteres especiais).';
  return null;
};

export default function Home() {
  const [nodePositions, setNodePositions] = useState(loadNodePositions);
  const [entities, setEntities] = useState(() => {
    const loaded = loadEntities();
    const hasUsers = loaded.some(e => e.name === "Users");
    return hasUsers ? loaded : [SYSTEM_USERS_ENTITY, ...loaded];
  });

  useEffect(() => {
    if (STORAGE_UNAVAILABLE) {
      toast.warning('Armazenamento de sessão indisponível. As alterações não serão salvas ao fechar a aba.');
    }
  }, []);

  const [selectedEntity, setSelectedEntity] = useState(null);
  const [rightPanel, setRightPanel] = useState('json');
  const [appName, setAppName] = useState('my-app');
  const [functionalities, setFunctionalities] = useState(loadFunctionalities);
  const [selectedFunctionality, setSelectedFunctionality] = useState(null);
  const [leftPanelTab, setLeftPanelTab] = useState('entities');

  useEffect(() => {
    if (storage) {
      const selectedEntityName = storage.getItem(SELECTED_ENTITY_KEY);
      if (selectedEntityName) {
        const selected = entities.find(e => e.name === selectedEntityName);
        setSelectedEntity(selected || entities[0] || null);
      } else {
        setSelectedEntity(entities[0] || null);
      }

      const selectedFunctionalityName = storage.getItem(SELECTED_FUNCTIONALITY_KEY);
      if (selectedFunctionalityName) {
        const selected = functionalities.find(f => f.name === selectedFunctionalityName);
        setSelectedFunctionality(selected || functionalities[0] || null);
      } else {
        setSelectedFunctionality(functionalities[0] || null);
      }

      const savedRightPanel = storage.getItem(RIGHT_PANEL_KEY);
      if (savedRightPanel === 'json' || savedRightPanel === 'diagram') {
        setRightPanel(savedRightPanel);
      }

      const savedAppName = storage.getItem(APP_NAME_KEY);
      if (savedAppName) setAppName(savedAppName);
    }
  }, [entities, functionalities]);

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

  useEffect(() => {
    storage?.setItem(FUNCTIONALITIES_KEY, JSON.stringify(functionalities));
  }, [functionalities]);

  useEffect(() => {
    if (selectedFunctionality) storage?.setItem(SELECTED_FUNCTIONALITY_KEY, selectedFunctionality.name);
    else storage?.removeItem(SELECTED_FUNCTIONALITY_KEY);
  }, [selectedFunctionality]);

  useEffect(() => {
    if (leftPanelTab === 'functionalities') {
      if (functionalities.length > 0 && !selectedFunctionality) {
        setSelectedFunctionality(functionalities[0]);
        setSelectedEntity(null);
      }
    } else if (leftPanelTab === 'entities') {
      if (entities.length > 0 && !selectedEntity) {
        setSelectedEntity(entities[0]);
        setSelectedFunctionality(null);
      }
    }
  }, [leftPanelTab, functionalities, entities, selectedFunctionality, selectedEntity]);

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
    const baseName = `Entidade${entities.length + 1}`;
    const newEntity = {
      name: baseName,
      attributes: [{ name: "id", type: "Long", constraints: ["primary_key", "auto_increment"] }],
      relations: [],
      behaviors: [],
      api: { endpoints: ["crud"], auth: "required", roles: ["admin"] }
    };
    setEntities(prev => [...prev, newEntity]);
    setSelectedEntity(newEntity);
    setSelectedFunctionality(null);
  };

  const handleDeleteEntity = (entityName) => {
  if (entityName === "Users") return;
  setEntities(prev =>
    prev
      .filter(e => e.name !== entityName)
      .map(e => ({
        ...e,
        relations: e.relations.filter(r => r.target !== entityName)
      }))
  );

  if (selectedEntity?.name === entityName) setSelectedEntity(null);
};


  const handleRenameEntity = (oldName, newName) => {
    const error = validateEntityName(newName);
    if (error) {
      toast.error(`Nome inválido: ${error}`);
      return;
    }
    if (entities.some(e => e.name === newName && e.name !== oldName)) {
      toast.error(`Já existe uma entidade com o nome "${newName}".`);
      return;
    }

    setEntities(prev => {
      const renamed = prev.map(e => {
        if (e.name === oldName) return { ...e, name: newName };
        return {
          ...e,
          relations: e.relations.map(r =>
            r.target === oldName ? { ...r, target: newName } : r
          )
        };
      });

      const updatedEntity = renamed.find(e => e.name === newName);
      return syncRelations(renamed, updatedEntity);
    });

    if (selectedEntity?.name === oldName)
      setSelectedEntity(prev => ({ ...prev, name: newName }));
  };

  const handleAddFunctionality = () => {
    const newFunctionality = {
      name: `NovaFuncionalidade${functionalities.length + 1}`,
      input: [],
      output: [],
      entity: null,
      exposeInBackoffice: false
    };
    setFunctionalities(prev => [...prev, newFunctionality]);
    setSelectedFunctionality(newFunctionality);
    setSelectedEntity(null);
  };

  const handleDeleteFunctionality = (functionalityName) => {
    setFunctionalities(prev => prev.filter(f => f.name !== functionalityName));
    if (selectedFunctionality?.name === functionalityName) setSelectedFunctionality(null);
  };

  const handleUpdateFunctionality = (updatedFunctionality) => {
    setFunctionalities(prev => prev.map(f => f.name === updatedFunctionality.name ? updatedFunctionality : f));
    setSelectedFunctionality(updatedFunctionality);
  };

  const handleRenameFunctionality = (oldName, newName) => {
    setFunctionalities(prev => prev.map(f => f.name === oldName ? { ...f, name: newName } : f));
    if (selectedFunctionality?.name === oldName) setSelectedFunctionality(prev => ({ ...prev, name: newName }));
  };

  const handleSelectEntity = (entity) => {
    // Create a new object reference to ensure React properly re-renders EntityEditor
    // with the correct entity data (behaviors, API config, etc.)
    setSelectedEntity({ ...entity });
    setSelectedFunctionality(null);
  };

  const handleSelectFunctionality = (functionality) => {
    setSelectedFunctionality(functionality);
    setSelectedEntity(null);
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
              onChange={(e) => {
                const val = e.target.value;
                setAppName(val);
                if (val && !/^[a-zA-Z0-9\-_]+$/.test(val)) {
                  toast.warning('App Name deve conter apenas letras, números, hífens e underscores.');
                }
              }}
              className={`px-2 py-1 text-xs bg-gray-700 border rounded ${
                appName && !/^[a-zA-Z0-9\-_]+$/.test(appName)
                  ? 'border-red-500'
                  : 'border-gray-600'
              }`}
            />
          </div>
          <div className="flex items-center gap-2 text-xs text-gray-400">
            <span className="px-2 py-1 rounded bg-gray-700">{entities.length} entidades</span>
            <span className="px-2 py-1 rounded bg-gray-700">
              {entities.reduce((acc, e) => acc + e.attributes.length, 0)} atributos
            </span>
            <span className="px-2 py-1 rounded bg-gray-700">{functionalities.length} funcionalidades</span>
          </div>
        </div>
      </header>

      <div className="flex-1 flex overflow-hidden">
        {/* LEFT */}
        <div className="w-64 border-r border-gray-800 bg-gray-800/30 flex flex-col">
          <Tabs value={leftPanelTab} onValueChange={setLeftPanelTab} className="flex-1 flex flex-col">
            <div className="border-b border-slate-800 px-4">
              <TabsList className="bg-transparent h-12 p-0 gap-1">
                <TabsTrigger
                  value="entities"
                  className="gap-2 data-[state=active]:bg-slate-800 data-[state=active]:shadow-none rounded-b-none border-b-2 border-transparent data-[state=active]:border-purple-500"
                >
                  <Database className="w-4 h-4" />
                  Entidades
                </TabsTrigger>
                <TabsTrigger
                  value="functionalities"
                  className="gap-2 data-[state=active]:bg-slate-800 data-[state=active]:shadow-none rounded-b-none border-b-2 border-transparent data-[state=active]:border-emerald-500"
                >
                  <Zap className="w-4 h-4" />
                  Funcionalidades
                </TabsTrigger>
              </TabsList>
            </div>

            <TabsContent value="entities" className="m-0 flex-1">
              <EntityList
                entities={entities}
                selectedEntity={selectedEntity}
                onSelect={handleSelectEntity}
                onAdd={handleAddEntity}
                onDelete={handleDeleteEntity}
              />
            </TabsContent>

            <TabsContent value="functionalities" className="m-0 flex-1">
              <FunctionalityList
                functionalities={functionalities}
                selectedFunctionality={selectedFunctionality}
                onSelect={handleSelectFunctionality}
                onAdd={handleAddFunctionality}
                onDelete={handleDeleteFunctionality}
              />
            </TabsContent>
          </Tabs>
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
          ) : selectedFunctionality ? (
            <FunctionalityEditor
              functionality={selectedFunctionality}
              allEntities={entities}
              onUpdate={handleUpdateFunctionality}
              onRename={handleRenameFunctionality}
            />
          ) : (
            <div className="h-full flex items-center justify-center text-gray-500">
              <div className="text-center">
                📦
                <p>Selecione uma entidade ou funcionalidade para editar</p>
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
  functionalities={functionalities}
  appName={appName}
  nodePositions={nodePositions}
  onLoadProject={(data) => {
  if (!data) return;

  const loadedEntities = data.entities || [];
  const hasUsers = loadedEntities.some(e => e.name === "Users");

  const finalEntities = hasUsers
    ? loadedEntities
    : [SYSTEM_USERS_ENTITY, ...loadedEntities];

  setAppName(data.appName || "my-app");
  setNodePositions(data.nodePositions || []);
  setEntities(finalEntities);
  setSelectedEntity(finalEntities[0] || null);
  setFunctionalities(data.functionalities || []);
  setSelectedFunctionality((data.functionalities || [])[0] || null);
}}
/>
            ) : (
              <div className="w-full h-full min-h-[600px]">
               <RelationDiagram
  entities={entities}
  nodePositions={nodePositions}
  setNodePositions={setNodePositions}
  onSelectEntity={handleSelectEntity}
/>

              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
