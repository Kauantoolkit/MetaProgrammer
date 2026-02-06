import React, { useState } from 'react';
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Copy, Check, Download, Minimize2, Maximize2, Rocket, Upload } from 'lucide-react';
import { toast } from "sonner";

export default function JsonPreview({ entities, appName, nodePositions, onLoadProject }) {
  const [copied, setCopied] = useState(false);
  const [showSimplified, setShowSimplified] = useState(false);
  const [showLoadModal, setShowLoadModal] = useState(false);
  const [pastedJson, setPastedJson] = useState('');

  const simplifiedJson = {
    appName,
    nodePositions,
    entities: entities.map(e => ({
      name: e.name,
      attributes: e.attributes.map(a => ({
        name: a.name,
        type: a.type,
        ...(a.values && { values: a.values })
      })),
      relations: e.relations.map(r => ({
        target: r.target,
        type: r.type
      }))
    }))
  };

  const enrichedJson = {
    appName,
    nodePositions,
    entities: entities.map(e => ({
      name: e.name,
      attributes: e.attributes.map(a => {
        const attr = { name: a.name, type: a.type };
        if (a.constraints?.length) attr.constraints = a.constraints;
        if (a.values) attr.values = a.values;
        if (a.default !== undefined) attr.default = a.default;
        return attr;
      }),
      relations: e.relations.map(r => ({
        target: r.target,
        type: r.type,
        required: r.required,
        cascade: r.cascade
      })),
      behaviors: e.behaviors,
      api: e.api
    }))
  };

  const currentJson = showSimplified ? simplifiedJson : enrichedJson;
  const jsonString = JSON.stringify(currentJson, null, 2);

  const copyToClipboard = async () => {
    await navigator.clipboard.writeText(jsonString);
    setCopied(true);
    toast.success('JSON copiado!');
    setTimeout(() => setCopied(false), 2000);
  };

  const downloadJson = () => {
    const blob = new Blob([jsonString], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `schema${showSimplified ? '-simplified' : '-enriched'}.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const generateBackend = async () => {
    try {
      const response = await fetch("http://localhost:8080/generate", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(currentJson),
      });

      if (!response.ok) throw new Error(`Erro HTTP ${response.status}`);

      const text = await response.text();
      toast.success("Backend gerado com sucesso!");
      console.log("Resposta do backend:", text);
    } catch (err) {
      console.error(err);
      toast.error("Falha ao gerar backend: " + err.message);
    }
  };

  const highlightJson = (json) => {
    return json
      .replace(/"([^"]+)":/g, '<span class="text-violet-400">"$1"</span>:')
      .replace(/: "([^"]+)"/g, ': <span class="text-emerald-400">"$1"</span>')
      .replace(/: (\d+)/g, ': <span class="text-amber-400">$1</span>')
      .replace(/: (true|false)/g, ': <span class="text-cyan-400">$1</span>')
      .replace(/: (null)/g, ': <span class="text-slate-500">$1</span>');
  };

  return (
    <div className="h-full flex flex-col relative">
      <div className="p-4 border-b border-slate-800 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Button variant={!showSimplified ? "secondary" : "ghost"} size="sm" onClick={() => setShowSimplified(false)} className="text-xs">
            <Maximize2 className="w-3 h-3 mr-1.5" /> Enriquecido
          </Button>
          <Button variant={showSimplified ? "secondary" : "ghost"} size="sm" onClick={() => setShowSimplified(true)} className="text-xs">
            <Minimize2 className="w-3 h-3 mr-1.5" /> Simplificado
          </Button>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="ghost" size="sm" onClick={() => setShowLoadModal(true)} className="text-blue-400 hover:text-blue-300">
            <Upload className="w-4 h-4" />
          </Button>
          <Button variant="ghost" size="sm" onClick={generateBackend} className="text-emerald-400 hover:text-emerald-300">
            <Rocket className="w-4 h-4" />
          </Button>
          <Button variant="ghost" size="sm" onClick={downloadJson} className="text-slate-400 hover:text-white">
            <Download className="w-4 h-4" />
          </Button>
          <Button variant="ghost" size="sm" onClick={copyToClipboard} className="text-slate-400 hover:text-white">
            {copied ? <Check className="w-4 h-4 text-emerald-500" /> : <Copy className="w-4 h-4" />}
          </Button>
        </div>
      </div>

      <ScrollArea className="flex-1">
        <pre className="p-4 text-xs leading-relaxed">
          <code dangerouslySetInnerHTML={{ __html: highlightJson(jsonString) }} />
        </pre>
      </ScrollArea>

      {showLoadModal && (
        <div className="absolute inset-0 bg-black/50 flex items-center justify-center p-4 z-10">
          <div className="bg-slate-800 rounded-lg p-6 w-full max-w-2xl max-h-[80vh] overflow-auto">
            <h3 className="text-lg font-semibold mb-4">Load from JSON</h3>
            <textarea
              value={pastedJson}
              onChange={(e) => setPastedJson(e.target.value)}
              placeholder="Paste your JSON here..."
              className="w-full h-64 p-3 bg-slate-900 border border-slate-600 rounded text-sm font-mono text-slate-200 resize-none"
            />
            <div className="flex justify-end gap-3 mt-4">
              <Button variant="ghost" onClick={() => { setShowLoadModal(false); setPastedJson(''); }}>
                Cancel
              </Button>
              <Button
                onClick={() => {
                  try {
                    const parsed = JSON.parse(pastedJson);

                    if (parsed.entities) {
                      onLoadProject(parsed);
                      toast.success('Projeto carregado com sucesso!');
                      setShowLoadModal(false);
                      setPastedJson('');
                      return;
                    }

                    if (Array.isArray(parsed)) {
                      onLoadProject({ appName: "my-app", entities: parsed, nodePositions: [] });
                      toast.success('Entities carregadas (formato antigo)!');
                      setShowLoadModal(false);
                      setPastedJson('');
                      return;
                    }

                    toast.error('JSON inválido');
                  } catch (err) {
                    toast.error('Invalid JSON: ' + err.message);
                  }
                }}
                className="bg-blue-600 hover:bg-blue-500"
              >
                Load JSON
              </Button>
            </div>
          </div>
        </div>
      )}

      <div className="p-3 border-t border-slate-800 flex items-center justify-between text-xs text-slate-500">
        <span>{entities.length} entidades</span>
        <span>{jsonString.length.toLocaleString()} caracteres</span>
      </div>
    </div>
  );
}
