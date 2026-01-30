import React, { useMemo, useState, useEffect } from 'react';
import { cn } from "@/lib/utils";





export default function RelationDiagram({ entities, 
  nodePositions, 
  setNodePositions, 
  onSelectEntity  }) {

  const getCardinalitySides = (type) => {
    if (!type || !type.includes(':')) return ['?', '?'];
    return type.split(':');
  };

  // 🔹 Layout automático inicial
  const layout = useMemo(() => {
    if (!entities.length) return { positions: [], width: 400, height: 300 };

    const isJoinTable = (entity) => {
      const rels = entity.relations || [];
      if (rels.length < 2) return false;
      return rels.every(r => r.type === 'N:1' || r.type === '1:N');
    };

    const left = [];
    const middle = [];
    const right = [];

    entities.forEach(entity => {
      if (isJoinTable(entity)) middle.push(entity.name);
      else {
        const hasChildren = entity.relations?.some(r => r.type === '1:N');
        if (hasChildren) left.push(entity.name);
        else right.push(entity.name);
      }
    });

    const columns = [left, middle, right];
    const xSpacing = 220;
    const ySpacing = 140;
    const marginX = 120;
    const marginY = 100;

    const maxRows = Math.max(...columns.map(col => col.length));
    const width = marginX * 2 + (columns.length - 1) * xSpacing;
    const height = marginY * 2 + maxRows * ySpacing;

    const pos = {};

    columns.forEach((col, colIndex) => {
      const columnHeight = col.length * ySpacing;
      const offsetY = (height - columnHeight) / 2;

      col.forEach((name, i) => {
        pos[name] = {
          x: marginX + colIndex * xSpacing,
          y: offsetY + i * ySpacing
        };
      });
    });

    return {
      positions: entities.map(e => pos[e.name]),
      width,
      height
    };
  }, [entities]);



  useEffect(() => {
  setNodePositions(prev => {
    const newPositions = [...prev];

    entities.forEach((entity, index) => {
      // Se já existe posição salva → mantém
      if (newPositions[index]) return;

      // Se é entidade nova → usa posição do layout automático
      newPositions[index] = layout.positions[index];
    });

    return newPositions;
  });
}, [entities, layout.positions]);


  // 🔹 Drag state
  const [draggingIndex, setDraggingIndex] = useState(null);

  const handleMouseDown = (index) => setDraggingIndex(index);
  const handleMouseUp = () => setDraggingIndex(null);

  const handleMouseMove = (e) => {
    if (draggingIndex === null) return;

    const svg = e.currentTarget;
    const rect = svg.getBoundingClientRect();

    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    setNodePositions(prev => {
      const updated = [...prev];
      updated[draggingIndex] = { x, y };
      return updated;
    });
  };

  // 🔹 Conexões usam posições dinâmicas
  const connections = useMemo(() => {
  const conns = [];
  const seen = new Set();

  entities.forEach((entity, fromIndex) => {
    entity.relations?.forEach((relation) => {
      const toIndex = entities.findIndex(e => e.name === relation.target);
      if (toIndex === -1) return;

      // 🔹 Normaliza 1:N ↔ N:1 para evitar duplicidade
      let key;
      if ((relation.type === '1:N' || relation.type === 'N:1')) {
        key = [entity.name, relation.target].sort().join('|'); // ignora tipo
      } else {
        key = [entity.name, relation.target, relation.type].sort().join('|');
      }

      if (seen.has(key)) return;
      seen.add(key);

      conns.push({
        from: nodePositions[fromIndex],
        to: nodePositions[toIndex],
        type: relation.type,
      });
    });
  });

  return conns;
}, [entities, nodePositions]);


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
        <svg
          width="100vw"
          height="100vh"
          className="mx-auto"
          onMouseMove={handleMouseMove}
          onMouseUp={handleMouseUp}
          onMouseLeave={handleMouseUp}
        >

          {/* 🔗 Conexões */}
          {connections.map((conn, index) => {
            if (!conn.from || !conn.to) return null;

            const [fromCard, toCard] = getCardinalitySides(conn.type);

            const dx = conn.to.x - conn.from.x;
            const dy = conn.to.y - conn.from.y;
            const length = Math.sqrt(dx * dx + dy * dy);
            if (!length) return null;

            const nodeRadius = 38;
            const startX = conn.from.x + (dx / length) * nodeRadius;
            const startY = conn.from.y + (dy / length) * nodeRadius;
            const endX = conn.to.x - (dx / length) * nodeRadius;
            const endY = conn.to.y - (dy / length) * nodeRadius;

            const curveStrength = 0.25;
            const controlX = (startX + endX) / 2 - dy * curveStrength;
            const controlY = (startY + endY) / 2 + dx * curveStrength;

            const cardOffset = 16;
            const fromCardX = startX + (dx / length) * cardOffset;
            const fromCardY = startY + (dy / length) * cardOffset;
            const toCardX = endX - (dx / length) * cardOffset;
            const toCardY = endY - (dy / length) * cardOffset;

            return (
              <g key={index}>
                <path
                  d={`M ${startX} ${startY} Q ${controlX} ${controlY} ${endX} ${endY}`}
                  fill="none"
                  stroke={getRelationColor(conn.type)}
                  strokeWidth="3"
                  strokeLinecap="round"
                  opacity="0.9"
                  strokeDasharray={conn.type === 'N:N' ? '6 4' : undefined}
                />

                <rect x={fromCardX - 11} y={fromCardY - 11} width="22" height="22" rx="6"
                  fill="#0f172a" stroke={getRelationColor(conn.type)} strokeWidth="1.5" />
                <text x={fromCardX} y={fromCardY + 4} textAnchor="middle" fontSize="12" fontWeight="bold"
                  fill={getRelationColor(conn.type)}>{fromCard}</text>

                <rect x={toCardX - 11} y={toCardY - 11} width="22" height="22" rx="6"
                  fill="#0f172a" stroke={getRelationColor(conn.type)} strokeWidth="1.5" />
                <text x={toCardX} y={toCardY + 4} textAnchor="middle" fontSize="12" fontWeight="bold"
                  fill={getRelationColor(conn.type)}>{toCard}</text>
              </g>
            );
          })}

          {/* 🔵 Nós */}
          {entities.map((entity, index) => {
            const pos = nodePositions[index];
            if (!pos) return null;

            const startColors = ['#8b5cf6', '#10b981', '#3b82f6', '#f97316', '#ec4899'];
            const endColors = ['#d946ef', '#14b8a6', '#06b6d4', '#f59e0b', '#f43f5e'];

            return (
              <g
                key={entity.name}
                className="cursor-move"
                onMouseDown={() => handleMouseDown(index)}
                onClick={() => onSelectEntity(entity)}
              >
                <defs>
                  <linearGradient id={`grad-${index}`} x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style={{ stopColor: startColors[index % 5] }} />
                    <stop offset="100%" style={{ stopColor: endColors[index % 5] }} />
                  </linearGradient>
                </defs>

                <circle cx={pos.x} cy={pos.y} r="38" fill={`url(#grad-${index})`} opacity="0.2" />
                <circle cx={pos.x} cy={pos.y} r="32" fill={`url(#grad-${index})`} />

                <text x={pos.x} y={pos.y + 5} textAnchor="middle" className="text-lg font-bold fill-white">
                  {entity.name.charAt(0)}
                </text>

                <text x={pos.x} y={pos.y + 55} textAnchor="middle" className="text-xs fill-slate-400">
                  {entity.name}
                </text>

                <text x={pos.x} y={pos.y + 68} textAnchor="middle" className="text-[10px] fill-slate-600">
                  {entity.attributes.length} attrs
                </text>
              </g>
            );
          })}
        </svg>
      </div>
    </div>
  );
}
