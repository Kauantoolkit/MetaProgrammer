import React from 'react';
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Plus, Database, Trash2, Link2 } from 'lucide-react';
import { cn } from "@/lib/utils";

export default function EntityList({ entities, selectedEntity, onSelect, onAdd, onDelete }) {
  return (
    <>
      <div className="p-4 border-b border-slate-800">
        <Button
          onClick={onAdd}
          className="w-full bg-violet-600 hover:bg-violet-700 text-white gap-2"
        >
          <Plus className="w-4 h-4" />
          Nova Entidade
        </Button>
      </div>
      <ScrollArea className="flex-1">
        <div className="p-2 space-y-1">
          {entities.map((entity) => (
            <div
              key={entity.name}
              className={cn(
                "group relative rounded-lg transition-all cursor-pointer",
                selectedEntity?.name === entity.name
                  ? "bg-violet-600/20 border border-violet-500/50"
                  : "hover:bg-slate-800/50 border border-transparent"
              )}
            >
              <button
                onClick={() => onSelect(entity)}
                className="w-full p-3 text-left"
              >
                <div className="flex items-center gap-3">
                  <div className={cn(
                    "w-8 h-8 rounded-lg flex items-center justify-center",
                    selectedEntity?.name === entity.name
                      ? "bg-violet-600"
                      : "bg-slate-800"
                  )}>
                    <Database className="w-4 h-4" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-medium text-sm truncate">{entity.name}</p>
                    <div className="flex items-center gap-2 mt-0.5">
                      <span className="text-xs text-slate-500">
                        {entity.attributes.length} attrs
                      </span>
                      {entity.relations.length > 0 && (
                        <span className="text-xs text-slate-500 flex items-center gap-1">
                          <Link2 className="w-3 h-3" />
                          {entity.relations.length}
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              </button>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  onDelete(entity.name);
                }}
                className="absolute right-2 top-1/2 -translate-y-1/2 p-1.5 rounded-md opacity-0 group-hover:opacity-100 hover:bg-red-500/20 text-red-400 transition-all"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            </div>
          ))}
        </div>
      </ScrollArea>
    </>
  );
}