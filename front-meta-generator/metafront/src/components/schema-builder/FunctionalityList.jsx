import React from 'react';
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Plus, Zap, Trash2, Database } from 'lucide-react';
import { cn } from "@/lib/utils";

export default function FunctionalityList({ functionalities, selectedFunctionality, onSelect, onAdd, onDelete }) {
  return (
    <>
      <div className="p-4 border-b border-slate-800">
        <Button
          onClick={onAdd}
          className="w-full bg-emerald-600 hover:bg-emerald-700 text-white gap-2"
        >
          <Plus className="w-4 h-4" />
          Nova Funcionalidade
        </Button>
      </div>

      <ScrollArea className="flex-1">
        <div className="p-2 space-y-1">
          {functionalities.map((functionality) => (
            <div
              key={functionality.name}
              className={cn(
                "group relative rounded-lg transition-all cursor-pointer",
                selectedFunctionality?.name === functionality.name
                  ? "bg-emerald-600/20 border border-emerald-500/50"
                  : "hover:bg-slate-800/50 border border-transparent"
              )}
            >
              <button
                onClick={() => onSelect(functionality)}
                className="w-full p-3 text-left"
              >
                <div className="flex items-center gap-3">
                  <div className={cn(
                    "w-8 h-8 rounded-lg flex items-center justify-center",
                    selectedFunctionality?.name === functionality.name
                      ? "bg-emerald-600"
                      : "bg-slate-800"
                  )}>
                    <Zap className="w-4 h-4 text-white" />
                  </div>

                  <div className="flex-1 min-w-0">
                    <p className="font-medium text-sm truncate text-slate-200">
                      {functionality.name}
                    </p>

                    <div className="flex items-center gap-2 mt-0.5">
                      <span className="text-xs text-slate-500">
                        {functionality.input?.length || 0} in
                      </span>
                      <span className="text-xs text-slate-500">
                        {functionality.output?.length || 0} out
                      </span>
                      {functionality.entity && (
                        <span className="text-xs text-emerald-400 flex items-center gap-1">
                          <Database className="w-3 h-3" />
                          {functionality.entity}
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              </button>

              <button
                onClick={(e) => {
                  e.stopPropagation();
                  onDelete(functionality.name);
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
