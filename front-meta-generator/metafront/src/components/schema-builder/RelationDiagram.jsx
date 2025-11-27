import React, { useMemo } from 'react';
import { cn } from "@/lib/utils";

export default function RelationDiagram({ entities, onSelectEntity }) {
  // Calculate positions in a circle
  const positions = useMemo(() => {
    const centerX = 220;
    const centerY = 200;
    const radius = 150;
    
    return entities.map((_, index) => {
      const angle = (2 * Math.PI * index) / entities.length - Math.PI / 2;
      return {
        x: centerX + radius * Math.cos(angle),
        y: centerY + radius * Math.sin(angle)
      };
    });
  }, [entities.length]);

  // Generate connections
  const connections = useMemo(() => {
    const conns = [];
    entities.forEach((entity, fromIndex) => {
      entity.relations.forEach((relation) => {
        const toIndex = entities.findIndex(e => e.name === relation.target);
        if (toIndex !== -1 && fromIndex < toIndex) { // Avoid duplicate lines
          conns.push({
            from: positions[fromIndex],
            to: positions[toIndex],
            type: relation.type,
            fromEntity: entity.name,
            toEntity: relation.target
          });
        }
      });
    });
    return conns;
  }, [entities, positions]);

  const getRelationColor = (type) => {
    switch (type) {
      case '1:1': return '#3b82f6';
      case '1:N': return '#8b5cf6';
      case 'N:1': return '#10b981';
      case 'N:N': return '#f59e0b';
      default: return '#64748b';
    }
  };

  return (
    <div className="h-full flex flex-col">
      <div className="p-4 border-b border-slate-800">
        <h3 className="text-sm font-medium text-slate-400">Diagrama de Relações</h3>
      </div>
      
      <div className="flex-1 overflow-auto p-4">
        <svg width="440" height="400" className="mx-auto">
          {/* Connection Lines */}
          {connections.map((conn, index) => {
            const midX = (conn.from.x + conn.to.x) / 2;
            const midY = (conn.from.y + conn.to.y) / 2;
            
            return (
              <g key={index}>
                <line
                  x1={conn.from.x}
                  y1={conn.from.y}
                  x2={conn.to.x}
                  y2={conn.to.y}
                  stroke={getRelationColor(conn.type)}
                  strokeWidth="2"
                  strokeDasharray={conn.type === 'N:N' ? '5,5' : 'none'}
                  opacity="0.6"
                />
                <rect
                  x={midX - 16}
                  y={midY - 10}
                  width="32"
                  height="20"
                  rx="4"
                  fill="#1e293b"
                  stroke={getRelationColor(conn.type)}
                  strokeWidth="1"
                />
                <text
                  x={midX}
                  y={midY + 4}
                  textAnchor="middle"
                  className="text-[10px] fill-slate-300 font-mono"
                >
                  {conn.type}
                </text>
              </g>
            );
          })}
          
          {/* Entity Nodes */}
          {entities.map((entity, index) => {
            const pos = positions[index];
            const colors = [
              'from-violet-500 to-fuchsia-500',
              'from-emerald-500 to-teal-500',
              'from-blue-500 to-cyan-500',
              'from-orange-500 to-amber-500',
              'from-pink-500 to-rose-500',
            ];
            
            return (
              <g
                key={entity.name}
                className="cursor-pointer"
                onClick={() => onSelectEntity(entity)}
              >
                <defs>
                  <linearGradient id={`grad-${index}`} x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" className={cn("stop-color-violet-500", colors[index % colors.length].includes('violet') && "stop-color-violet-500")} style={{ stopColor: ['#8b5cf6', '#10b981', '#3b82f6', '#f97316', '#ec4899'][index % 5] }} />
                    <stop offset="100%" style={{ stopColor: ['#d946ef', '#14b8a6', '#06b6d4', '#f59e0b', '#f43f5e'][index % 5] }} />
                  </linearGradient>
                </defs>
                
                {/* Glow effect */}
                <circle
                  cx={pos.x}
                  cy={pos.y}
                  r="38"
                  fill={`url(#grad-${index})`}
                  opacity="0.2"
                  className="transition-all hover:opacity-40"
                />
                
                {/* Main circle */}
                <circle
                  cx={pos.x}
                  cy={pos.y}
                  r="32"
                  fill={`url(#grad-${index})`}
                  className="transition-all hover:r-[35]"
                />
                
                {/* Entity initial */}
                <text
                  x={pos.x}
                  y={pos.y + 5}
                  textAnchor="middle"
                  className="text-lg font-bold fill-white"
                >
                  {entity.name.charAt(0)}
                </text>
                
                {/* Entity name */}
                <text
                  x={pos.x}
                  y={pos.y + 55}
                  textAnchor="middle"
                  className="text-xs fill-slate-400"
                >
                  {entity.name}
                </text>
                
                {/* Attribute count */}
                <text
                  x={pos.x}
                  y={pos.y + 68}
                  textAnchor="middle"
                  className="text-[10px] fill-slate-600"
                >
                  {entity.attributes.length} attrs
                </text>
              </g>
            );
          })}
        </svg>
      </div>

      {/* Legend */}
      <div className="p-4 border-t border-slate-800">
        <p className="text-xs text-slate-500 mb-2">Legenda:</p>
        <div className="flex flex-wrap gap-3 text-xs">
          {[
            { type: '1:1', color: '#3b82f6', label: 'Um para Um' },
            { type: '1:N', color: '#8b5cf6', label: 'Um para Muitos' },
            { type: 'N:1', color: '#10b981', label: 'Muitos para Um' },
            { type: 'N:N', color: '#f59e0b', label: 'Muitos para Muitos' },
          ].map((item) => (
            <div key={item.type} className="flex items-center gap-1.5">
              <div 
                className="w-4 h-0.5 rounded"
                style={{ 
                  backgroundColor: item.color,
                  borderStyle: item.type === 'N:N' ? 'dashed' : 'solid'
                }}
              />
              <span className="text-slate-500">{item.label}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}