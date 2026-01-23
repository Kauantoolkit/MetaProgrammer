import React, { useState } from 'react';
import { Button } from "@/components/ui/button";
import { Switch } from "@/components/ui/switch";
import { FileText, Plus, X } from 'lucide-react';
import { cn } from "@/lib/utils";

export default function DtoEditor({ entity, onUpdate }) {
  const [generateDto, setGenerateDto] = useState(entity.generateDto || false);

  const handleToggleDto = (checked) => {
    setGenerateDto(checked);
    onUpdate({
      ...entity,
      generateDto: checked
    });
  };

  return (
    <div className="space-y-6">
      {/* DTO Generation Toggle */}
      <div className="flex items-center justify-between p-4 bg-slate-900/50 rounded-lg border border-slate-800">
        <div className="flex items-center gap-3">
          <FileText className="w-5 h-5 text-slate-400" />
          <div>
            <h3 className="text-sm font-medium">Gerar DTO</h3>
            <p className="text-xs text-slate-500 mt-0.5">
              Criar classes DTO para entrada e saída bem definidas
            </p>
          </div>
        </div>
        <Switch
          checked={generateDto}
          onCheckedChange={handleToggleDto}
        />
      </div>

      {/* DTO Preview */}
      {generateDto && (
        <div className="space-y-4">
          <h4 className="text-sm font-medium text-slate-400">DTOs que serão gerados:</h4>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Request DTO */}
            <div className="p-4 bg-slate-900/50 rounded-lg border border-slate-800">
              <h5 className="text-sm font-medium mb-3 text-emerald-400">
                {entity.name}RequestDto
              </h5>
              <div className="font-mono text-xs text-slate-300 space-y-1">
                <div className="text-slate-500">// Campos de entrada</div>
                {entity.attributes.map(attr => (
                  <div key={attr.name}>
                    private {getJavaType(attr.type)} {attr.name};
                  </div>
                ))}
              </div>
            </div>

            {/* Response DTO */}
            <div className="p-4 bg-slate-900/50 rounded-lg border border-slate-800">
              <h5 className="text-sm font-medium mb-3 text-blue-400">
                {entity.name}ResponseDto
              </h5>
              <div className="font-mono text-xs text-slate-300 space-y-1">
                <div className="text-slate-500">// Campos de saída</div>
                {entity.attributes.map(attr => (
                  <div key={attr.name}>
                    private {getJavaType(attr.type)} {attr.name};
                  </div>
                ))}
                {entity.relations.map(rel => (
                  <div key={rel.target}>
                    private {rel.target}Dto {rel.target.toLowerCase()};
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* API Interface Preview */}
          <div className="p-4 bg-slate-900/50 rounded-lg border border-slate-800">
            <h5 className="text-sm font-medium mb-3 text-violet-400">
              Interface de Serviço Gerada
            </h5>
            <div className="font-mono text-xs text-slate-300 space-y-1">
              <div className="text-slate-500">// Métodos com DTOs bem definidos</div>
              <div>List&lt;{entity.name}ResponseDto&gt; findAll();</div>
              <div>{entity.name}ResponseDto findById(Long id);</div>
              <div>{entity.name}ResponseDto save({entity.name}RequestDto request);</div>
              <div>{entity.name}ResponseDto update(Long id, {entity.name}RequestDto request);</div>
              <div>void deleteById(Long id);</div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function getJavaType(type) {
  switch (type.toLowerCase()) {
    case 'string': return 'String';
    case 'number': return 'Long';
    case 'boolean': return 'Boolean';
    case 'date': return 'LocalDateTime';
    default: return 'String';
  }
}
