import React, { useState } from 'react';
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Copy, Check, Download, Minimize2, Maximize2 } from 'lucide-react';
import { toast } from "sonner";

export default function JsonPreview({ entities }) {
  const [copied, setCopied] = useState(false);
  const [showSimplified, setShowSimplified] = useState(false);

  // Generate simplified JSON (like original format)
  const simplifiedJson = entities.map(e => ({
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
  }));

  // Full enriched JSON
  const enrichedJson = entities.map(e => ({
    name: e.name,
    attributes: e.attributes.map(a => {
      const attr = { name: a.name, type: a.type };
      if (a.constraints?.length > 0) attr.constraints = a.constraints;
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
  }));

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

  // Syntax highlighting
  const highlightJson = (json) => {
    return json
      .replace(/"([^"]+)":/g, '<span class="text-violet-400">"$1"</span>:')
      .replace(/: "([^"]+)"/g, ': <span class="text-emerald-400">"$1"</span>')
      .replace(/: (\d+)/g, ': <span class="text-amber-400">$1</span>')
      .replace(/: (true|false)/g, ': <span class="text-cyan-400">$1</span>')
      .replace(/: (null)/g, ': <span class="text-slate-500">$1</span>');
  };

  return (
    <div className="h-full flex flex-col">
      {/* Header */}
      <div className="p-4 border-b border-slate-800 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Button
            variant={!showSimplified ? "secondary" : "ghost"}
            size="sm"
            onClick={() => setShowSimplified(false)}
            className="text-xs"
          >
            <Maximize2 className="w-3 h-3 mr-1.5" />
            Enriquecido
          </Button>
          <Button
            variant={showSimplified ? "secondary" : "ghost"}
            size="sm"
            onClick={() => setShowSimplified(true)}
            className="text-xs"
          >
            <Minimize2 className="w-3 h-3 mr-1.5" />
            Simplificado
          </Button>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="ghost"
            size="sm"
            onClick={downloadJson}
            className="text-slate-400 hover:text-white"
          >
            <Download className="w-4 h-4" />
          </Button>
          <Button
            variant="ghost"
            size="sm"
            onClick={copyToClipboard}
            className="text-slate-400 hover:text-white"
          >
            {copied ? <Check className="w-4 h-4 text-emerald-500" /> : <Copy className="w-4 h-4" />}
          </Button>
        </div>
      </div>

      {/* JSON Content */}
      <ScrollArea className="flex-1">
        <pre className="p-4 text-xs leading-relaxed">
          <code 
            dangerouslySetInnerHTML={{ __html: highlightJson(jsonString) }}
          />
        </pre>
      </ScrollArea>

      {/* Footer Stats */}
      <div className="p-3 border-t border-slate-800 flex items-center justify-between text-xs text-slate-500">
        <span>{entities.length} entidades</span>
        <span>{jsonString.length.toLocaleString()} caracteres</span>
      </div>
    </div>
  );
}