import React, { useState, useMemo, useEffect, useRef } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Search, Plus, Trash2, Edit, Save, Copy } from "lucide-react";

/* cores por tipo (usando classes Tailwind-like — ajuste se necessário) */
const TYPE_COLORS = {
  string: "bg-blue-700/20 text-blue-300 border border-blue-700/40",
  number: "bg-green-700/20 text-green-300 border border-green-700/40",
  boolean: "bg-violet-700/20 text-violet-300 border border-violet-700/40",
  date: "bg-amber-700/20 text-amber-300 border border-amber-700/40",
};

function makeId() {
  return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`;
}

const STORAGE_KEY = "metafront_entities_v1";

export default function EntityManager() {
  // começa vazio — se houver dados no localStorage, vai carregar no useEffect abaixo
  const [entities, setEntities] = useState([]);

  // contador para garantir nomes padrão únicos (NovaEntidade-1, -2, ...)
  const newCounter = useRef(1);

  const [selectedId, setSelectedId] = useState(null);
  const [query, setQuery] = useState("");

  // carrega do localStorage no mount
  useEffect(() => {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (raw) {
        const parsed = JSON.parse(raw);
        if (Array.isArray(parsed)) {
          setEntities(parsed);
          // ajustar contador para não colidir com nomes existentes (opcional)
          const maxIndex = parsed.reduce((max, e) => {
            const m = (e.name || "").match(/NovaEntidade-(\d+)/);
            if (m) return Math.max(max, Number(m[1]));
            return max;
          }, 0);
          newCounter.current = maxIndex + 1;
          setSelectedId(parsed[0]?.id ?? null);
          return;
        }
      }
    } catch (err) {
      console.warn("Erro ao ler localStorage:", err);
    }
    // se nada no storage, mantemos vazio
    setEntities([]);
    setSelectedId(null);
  }, []);

  // salva no localStorage quando entities muda
  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(entities));
    } catch (err) {
      console.warn("Erro ao salvar localStorage:", err);
    }
  }, [entities]);

  // selected sempre reflete o estado atualizado
  const selected = useMemo(() => entities.find((e) => e.id === selectedId) ?? null, [entities, selectedId]);

  // helpers de estado
  const setEntityById = (id, patchOrFn) => {
    setEntities((prev) =>
      prev.map((e) => {
        if (e.id !== id) return e;
        const patched = typeof patchOrFn === "function" ? patchOrFn(e) : { ...e, ...patchOrFn };
        return patched;
      })
    );
  };

  const replaceEntity = (updated) => {
    setEntities((prev) => prev.map((e) => (e.id === updated.id ? updated : e)));
  };

  // cria nova entidade com nome único
  const addEntity = () => {
    const base = `NovaEntidade-${newCounter.current++}`;
    const newEnt = { id: makeId(), name: base, attributes: [], relations: [] };
    setEntities((prev) => [newEnt, ...prev]);
    setSelectedId(newEnt.id);
  };

  const deleteEntity = (id) => {
    const ent = entities.find((e) => e.id === id);
    if (!ent || !window.confirm(`Deletar a entidade "${ent.name}"?`)) return;
    setEntities((prev) => prev.filter((e) => e.id !== id));
    setSelectedId((prevId) => (prevId === id ? entities[0]?.id ?? null : prevId));
  };

  const addAttribute = (entityId, attr) => {
    setEntityById(entityId, (e) => ({ ...e, attributes: [...(e.attributes || []), attr] }));
  };

  const updateAttribute = (entityId, attrName, patch) => {
    setEntityById(entityId, (e) => {
      const attrs = (e.attributes || []).map((a) => (a.name === attrName ? { ...a, ...patch } : a));
      return { ...e, attributes: attrs };
    });
  };

  const removeAttribute = (entityId, attrName) => {
    if (!window.confirm(`Remover atributo "${attrName}"?`)) return;
    setEntityById(entityId, (e) => ({ ...e, attributes: e.attributes.filter((a) => a.name !== attrName) }));
  };

  const addRelation = (entityId, target, type) => {
    setEntityById(entityId, (e) => ({ ...e, relations: [...(e.relations || []), { target, type }] }));
  };

  const removeRelation = (entityId, idx) => {
    setEntityById(entityId, (e) => {
      const copy = [...(e.relations || [])];
      copy.splice(idx, 1);
      return { ...e, relations: copy };
    });
  };

  // export JSON (download)
  const exportJSON = () => {
    const json = JSON.stringify(entities, null, 2);
    const blob = new Blob([json], { type: "application/json" });
    const link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.download = "entities.json";
    link.click();
  };

  // importa entidades (usa o mesmo setter do componente, valida e normaliza)
  const importEntities = (imported) => {
    if (!Array.isArray(imported)) return alert("JSON inválido: deve ser um array de entidades");
    // normalized: garantir id, attributes, relations
    const normalized = imported.map((ent) => ({
      id: ent.id || makeId(),
      name: ent.name || `Entidade-${makeId()}`,
      attributes: Array.isArray(ent.attributes) ? ent.attributes.map((a) => ({ name: a.name ?? "attr", type: a.type ?? "string" })) : [],
      relations: Array.isArray(ent.relations) ? ent.relations.map((r) => ({ target: r.target ?? "", type: r.type ?? "1:1" })) : [],
    }));
    setEntities(normalized);
    setSelectedId(normalized[0]?.id ?? null);
    // ajustar contador (opcional)
    const maxIndex = normalized.reduce((max, e) => {
      const m = (e.name || "").match(/NovaEntidade-(\d+)/);
      if (m) return Math.max(max, Number(m[1]));
      return max;
    }, 0);
    newCounter.current = Math.max(newCounter.current, maxIndex + 1);
  };

  const filteredEntities = entities.filter((e) => e.name.toLowerCase().includes(query.toLowerCase()));

  return (
    <div className="flex gap-6 p-6 h-[calc(100vh-48px)] bg-[#0b0f14] text-gray-100">
      {/* LEFT */}
      <div className="flex-none w-[20vw] min-w-[180px]">
        <Card className="h-full bg-[#0f1720] border border-gray-800">
          <CardContent className="flex flex-col h-full">
            <div className="flex items-center gap-2 mb-3">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-2 w-4 h-4 text-gray-500" />
                <input
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  placeholder="Procurar entidades..."
                  className="pl-10 pr-3 py-2 w-full rounded bg-[#0b1116] border border-gray-800 text-sm text-gray-200 focus:ring-2 focus:ring-sky-600 outline-none"
                />
              </div>
              <Button onClick={addEntity} className="whitespace-nowrap bg-sky-600 hover:bg-sky-500">
                <Plus className="w-4 h-4 mr-1" /> Nova
              </Button>
            </div>

            <div className="flex flex-col gap-2 overflow-auto" style={{ maxHeight: "calc(100% - 60px)" }}>
              {filteredEntities.length === 0 && <div className="text-sm text-gray-500 p-2">Nenhuma entidade</div>}
              {filteredEntities.map((e) => (
                <div
                  key={e.id}
                  onClick={() => setSelectedId(e.id)}
                  className={`flex items-center justify-between gap-2 p-3 rounded cursor-pointer transition ${
                    selectedId === e.id ? "bg-sky-900/30 border border-sky-700" : "hover:bg-gray-800/30"
                  }`}
                >
                  <div className="flex flex-col">
                    <div className="flex items-center gap-2">
                      <div className="text-sm font-semibold">{e.name}</div>
                      <div className="text-xs text-gray-400">{e.attributes.length} attrs</div>
                    </div>
                    <div className="flex gap-1 mt-2 flex-wrap">
                      {e.attributes.slice(0, 6).map((a) => (
                        <span key={a.name} className={`text-xs px-2 py-0.5 rounded-full ${TYPE_COLORS[a.type]}`}>
                          {a.name}
                        </span>
                      ))}
                      {e.attributes.length > 6 && <span className="text-xs px-2 py-0.5 rounded-full bg-gray-800">+{e.attributes.length - 6}</span>}
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <Button size="sm" variant="outline" onClick={(ev) => ev.stopPropagation()}>
                      <Edit className="w-4 h-4" />
                    </Button>
                    <Button size="sm" variant="destructive" onClick={(ev) => { ev.stopPropagation(); deleteEntity(e.id); }}>
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* CENTER */}
      <div className="flex-1 min-w-[40vw]">
        <Card className="h-full bg-[#0f1720] border border-gray-800">
          <CardContent className="flex flex-col h-full overflow-auto">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-2xl font-bold">Visão geral</h2>
              <div className="text-sm text-gray-400">{entities.length} entidades</div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {entities.map((e) => (
                <div key={e.id} className="p-3 rounded-lg bg-[#0b1116] border border-gray-800 shadow-sm">
                  <div className="flex items-start justify-between">
                    <div>
                      <div className="font-semibold text-lg">{e.name}</div>
                      <div className="text-xs text-gray-400">{e.attributes.length} atributos • {e.relations.length} relações</div>
                    </div>
                    <div className="flex items-center gap-2">
                      <Button size="sm" variant="outline" onClick={() => setSelectedId(e.id)}>Editar</Button>
                    </div>
                  </div>

                  <div className="mt-3 flex flex-wrap gap-2">
                    {e.attributes.length ? (
                      e.attributes.map((a) => (
                        <span key={a.name} className={`px-2 py-0.5 rounded-full text-xs font-medium ${TYPE_COLORS[a.type]}`}>
                          {a.name}: {a.type}
                        </span>
                      ))
                    ) : (
                      <div className="text-xs text-gray-500">Sem atributos</div>
                    )}
                  </div>

                  <div className="mt-3 text-sm text-gray-400">
                    {e.relations.length ? e.relations.map((r) => `${r.type}→${r.target}`).join(", ") : "-"}
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* RIGHT: editor com key baseada no id (garante remount) */}
      <div className="flex-none w-[35vw] min-w-[300px]">
        <Card className="h-full bg-[#10151a] border border-gray-800">
          <CardContent className="flex flex-col h-full overflow-hidden">
            {!selected ? (
              <div className="flex flex-col items-center justify-center h-full text-gray-500">
                <span className="text-sm">Selecione uma entidade para editar</span>
              </div>
            ) : (
              <EditorPanel
                key={selected.id}
                entity={selected}
                entities={entities}
                updateAttribute={(name, patch) => updateAttribute(selected.id, name, patch)}
                addAttribute={(attr) => addAttribute(selected.id, attr)}
                removeAttribute={(name) => removeAttribute(selected.id, name)}
                addRelation={(target, type) => addRelation(selected.id, target, type)}
                removeRelation={(idx) => removeRelation(selected.id, idx)}
                rename={(newName) => setEntityById(selected.id, { name: newName })}
                deleteEntity={() => deleteEntity(selected.id)}
                exportJSON={exportJSON}
                importEntities={importEntities} // <-- passa a função de importação
              />
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

/* EditorPanel: permite editar nome (rename), atributos, relações, importar/exportar e copiar JSON */
function EditorPanel({
  entity,
  entities,
  updateAttribute,
  addAttribute,
  removeAttribute,
  addRelation,
  removeRelation,
  rename,
  deleteEntity,
  exportJSON,
  importEntities,
}) {
  const [editingName, setEditingName] = useState(entity.name);
  useEffect(() => setEditingName(entity.name), [entity.id]); // garante atualização quando muda entidade

  const [newAttrName, setNewAttrName] = useState("");
  const [newAttrType, setNewAttrType] = useState("string");
  const [newRelTarget, setNewRelTarget] = useState("");
  const [newRelType, setNewRelType] = useState("1:1");

  const jsonStr = JSON.stringify(entities, null, 2);
  const copyJSON = () => { navigator.clipboard.writeText(jsonStr).then(() => alert("JSON copiado!")); };

  const saveName = () => {
    const trimmed = editingName.trim();
    if (!trimmed) return alert("Nome não pode ser vazio");
    rename(trimmed);
  };

  // handler de import que usa importEntities passado pelo pai
  const handleImportClick = () => {
    const input = document.createElement("input");
    input.type = "file";
    input.accept = "application/json";
    input.onchange = async (e) => {
      const file = e.target.files[0];
      if (!file) return;
      const text = await file.text();
      try {
        const imported = JSON.parse(text);
        importEntities(imported);
        alert("Importado com sucesso!");
      } catch (err) {
        console.error(err);
        alert("Erro ao ler JSON.");
      }
    };
    input.click();
  };

  return (
    <div className="flex flex-col h-full text-gray-100">
      <div className="flex items-center justify-between mb-3 gap-2">
        <input
          className="border border-gray-600 bg-[#0d1117] px-2 py-1 rounded w-2/3"
          value={editingName}
          onChange={(e) => setEditingName(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && saveName()}
        />
        <div className="flex gap-2">
          <Button onClick={saveName} className="bg-sky-700 hover:bg-sky-600"><Save className="w-4 h-4 mr-1" />Salvar</Button>
          <Button size="sm" variant="destructive" onClick={() => { if (confirm("Excluir entidade?")) deleteEntity(); }}>
            <Trash2 className="w-4 h-4" />
          </Button>
        </div>
      </div>

      <h4 className="font-semibold mb-2">Atributos</h4>
      <div className="flex flex-col gap-2 mb-3 overflow-auto max-h-[25vh]">
        {entity.attributes.map((a) => (
          <div key={a.name} className="flex items-center justify-between gap-2 p-2 bg-gray-800 rounded">
            <div className="flex items-center gap-2">
              <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${TYPE_COLORS[a.type]}`}>{a.type}</span>
              <input
                className="border px-2 py-1 rounded w-36 bg-[#0b1116] text-gray-100"
                value={a.name}
                onChange={(e) => updateAttribute(a.name, { name: e.target.value })}
                onBlur={(e) => { if (!e.target.value.trim()) alert("Nome não pode ficar vazio"); }}
              />
            </div>
            <div className="flex items-center gap-2">
              <select value={a.type} onChange={(e) => updateAttribute(a.name, { type: e.target.value })} className="border px-2 py-1 rounded bg-[#0b1116] text-gray-100">
                <option value="string">string</option>
                <option value="number">number</option>
                <option value="boolean">boolean</option>
                <option value="date">date</option>
              </select>
              <Button size="sm" variant="destructive" onClick={() => removeAttribute(a.name)}><Trash2 className="w-4 h-4" /></Button>
            </div>
          </div>
        ))}
      </div>

      <div className="flex gap-2 mb-4">
        <input
          value={newAttrName}
          onChange={(e) => setNewAttrName(e.target.value)}
          placeholder="Nome do atributo"
          className="border px-2 py-1 rounded flex-1 bg-[#0b1116] text-gray-100"
          onKeyDown={(e) => { if (e.key === "Enter" && newAttrName.trim()) { addAttribute({ name: newAttrName.trim(), type: newAttrType }); setNewAttrName(""); } }}
        />
        <select value={newAttrType} onChange={(e) => setNewAttrType(e.target.value)} className="border px-2 py-1 rounded bg-[#0b1116] text-gray-100">
          <option>string</option>
          <option>number</option>
          <option>boolean</option>
          <option>date</option>
        </select>
        <Button onClick={() => { if (!newAttrName.trim()) return; addAttribute({ name: newAttrName.trim(), type: newAttrType }); setNewAttrName(""); }} className="bg-sky-700 hover:bg-sky-600">
          <Plus className="w-4 h-4 mr-1" />Adicionar
        </Button>
      </div>

      <h4 className="font-semibold mb-2">Relacionamentos</h4>
      <div className="flex flex-col gap-2 mb-3 max-h-36 overflow-auto">
        {entity.relations.map((r, idx) => (
          <div key={idx} className="flex items-center justify-between p-2 rounded bg-gray-800">
            <div className="text-sm">{r.type} → {r.target}</div>
            <Button size="sm" variant="destructive" onClick={() => removeRelation(idx)}><Trash2 className="w-4 h-4" /></Button>
          </div>
        ))}
        {entity.relations.length === 0 && <div className="text-sm text-gray-400">Nenhuma relação</div>}
      </div>

      <div className="flex gap-2 mb-4">
        <select value={newRelTarget} onChange={(e) => setNewRelTarget(e.target.value)} className="border px-2 py-1 rounded flex-1 bg-[#0b1116] text-gray-100">
          <option value="">Selecionar entidade</option>
          {entities.filter(e => e.id !== entity.id).map(e => (<option key={e.id} value={e.name}>{e.name}</option>))}
        </select>
        <select value={newRelType} onChange={(e) => setNewRelType(e.target.value)} className="border px-2 py-1 rounded bg-[#0b1116] text-gray-100">
          <option>1:1</option><option>1:N</option><option>N:1</option><option>N:N</option>
        </select>
        <Button onClick={() => { if (!newRelTarget) return; addRelation(newRelTarget, newRelType); setNewRelTarget(""); }} className="bg-sky-700 hover:bg-sky-600">
          <Plus className="w-4 h-4 mr-1" />Adicionar
        </Button>
      </div>

      <h4 className="font-semibold mb-2">JSON</h4>
      <textarea readOnly value={jsonStr} className="border rounded p-2 h-36 font-mono text-xs overflow-auto resize-none bg-[#0b1116] text-gray-200" />
      <div className="mt-2 flex justify-end gap-2">
        <Button size="sm" onClick={copyJSON} className="bg-green-600 hover:bg-green-500"><Copy className="w-4 h-4 mr-1" />Copiar JSON</Button>
        <Button size="sm" onClick={exportJSON} className="bg-slate-600 hover:bg-slate-500">Download</Button>
        <Button size="sm" onClick={handleImportClick} className="bg-purple-600 hover:bg-purple-500">Importar</Button>
      </div>
    </div>
  );
}
