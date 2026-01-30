import React from 'react';
import { Plus, Trash2 } from 'lucide-react';

export default function InterfaceList({ interfaces, selectedInterface, onSelect, onAdd, onDelete }) {
  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <div className="p-4 border-b border-gray-700">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-medium text-gray-300">Interfaces</h3>
          <button
            onClick={onAdd}
            className="p-1.5 rounded bg-purple-600 hover:bg-purple-700 text-white transition-colors"
            title="Adicionar Interface"
          >
            <Plus className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Interface List */}
      <div className="flex-1 overflow-auto">
        {interfaces.length === 0 ? (
          <div className="p-4 text-center text-gray-500">
            <p className="text-sm">Nenhuma interface criada</p>
            <p className="text-xs mt-1">Clique em + para adicionar</p>
          </div>
        ) : (
          <div className="p-2 space-y-1">
            {interfaces.map((interf) => (
              <div
                key={interf.name}
                className={`group relative p-3 rounded-lg border cursor-pointer transition-all ${
                  selectedInterface?.name === interf.name
                    ? 'bg-purple-600/20 border-purple-500'
                    : 'bg-gray-800/50 border-gray-700 hover:bg-gray-700/50 hover:border-gray-600'
                }`}
                onClick={() => onSelect(interf)}
              >
                <div className="flex items-center justify-between">
                  <div className="flex-1 min-w-0">
                    <h4 className="text-sm font-medium text-white truncate">
                      {interf.name}
                    </h4>
                    <p className="text-xs text-gray-400 mt-0.5">
                      {interf.methods.length} método{interf.methods.length !== 1 ? 's' : ''}
                    </p>
                  </div>

                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      onDelete(interf.name);
                    }}
                    className="opacity-0 group-hover:opacity-100 p-1 rounded text-gray-400 hover:text-red-400 hover:bg-red-400/10 transition-all"
                    title="Excluir Interface"
                  >
                    <Trash2 className="w-3 h-3" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
