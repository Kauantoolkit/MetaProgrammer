import React, { useState } from 'react';
import { Button } from "@/components/ui/button";
import  Input  from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import  Switch  from "@/components/ui/switch";
import  Badge  from "@/components/ui/badge";
import { Plus, Trash2, Zap, Clock, Shield, History, GitBranch, X } from 'lucide-react';
import { cn } from "@/lib/utils";

const BEHAVIOR_TYPES = [
  { 
    value: 'soft_delete', 
    label: 'Soft Delete', 
    icon: Trash2,
    description: 'Registros são marcados como deletados ao invés de removidos',
    color: 'from-red-500 to-rose-500'
  },
  { 
    value: 'audit_log', 
    label: 'Audit Log', 
    icon: History,
    description: 'Registra todas as alterações feitas na entidade',
    color: 'from-blue-500 to-cyan-500'
  },
  { 
    value: 'versioning', 
    label: 'Versionamento', 
    icon: GitBranch,
    description: 'Mantém histórico de versões dos registros',
    color: 'from-purple-500 to-violet-500'
  },
  { 
    value: 'timestamps', 
    label: 'Timestamps', 
    icon: Clock,
    description: 'Adiciona created_at e updated_at automaticamente',
    color: 'from-emerald-500 to-teal-500'
  },
  { 
    value: 'state_machine', 
    label: 'State Machine', 
    icon: GitBranch,
    description: 'Define transições de estado válidas para um campo',
    color: 'from-orange-500 to-amber-500'
  },
];

export default function BehaviorsEditor({ entity, onUpdate }) {
  const [showStateMachineConfig, setShowStateMachineConfig] = useState(false);

  const getBehaviorConfig = (behaviorValue) => {
    const behavior = entity.behaviors.find(b => 
      typeof b === 'string' ? b === behaviorValue : b.type === behaviorValue
    );
    return behavior;
  };

  const hasBehavior = (behaviorValue) => {
    return entity.behaviors.some(b => 
      typeof b === 'string' ? b === behaviorValue : b.type === behaviorValue
    );
  };

  const toggleBehavior = (behaviorValue) => {
    if (behaviorValue === 'state_machine') {
      if (hasBehavior('state_machine')) {
        onUpdate({
          ...entity,
          behaviors: entity.behaviors.filter(b => 
            typeof b === 'string' ? b !== 'state_machine' : b.type !== 'state_machine'
          )
        });
      } else {
        setShowStateMachineConfig(true);
      }
      return;
    }

    if (hasBehavior(behaviorValue)) {
      onUpdate({
        ...entity,
        behaviors: entity.behaviors.filter(b => b !== behaviorValue)
      });
    } else {
      onUpdate({
        ...entity,
        behaviors: [...entity.behaviors, behaviorValue]
      });
    }
  };

  const addStateMachine = (field, states) => {
    const transitions = states.map(state => ({
      from: state,
      to: states.filter(s => s !== state)
    }));

    const stateMachine = {
      type: 'state_machine',
      field,
      transitions
    };

    onUpdate({
      ...entity,
      behaviors: [...entity.behaviors.filter(b => 
        typeof b === 'string' ? true : b.type !== 'state_machine'
      ), stateMachine]
    });
    setShowStateMachineConfig(false);
  };

  const updateStateMachineTransition = (fromState, toStates) => {
    const sm = getBehaviorConfig('state_machine');
    if (!sm || typeof sm === 'string') return;

    const newTransitions = sm.transitions.map(t => 
      t.from === fromState ? { ...t, to: toStates } : t
    );

    onUpdate({
      ...entity,
      behaviors: entity.behaviors.map(b => 
        typeof b === 'object' && b.type === 'state_machine'
          ? { ...b, transitions: newTransitions }
          : b
      )
    });
  };

  const enumFields = entity.attributes.filter(a => a.type === 'enum' && a.values?.length > 0);
  const stateMachine = getBehaviorConfig('state_machine');

  return (
    <div className="space-y-4">
      {/* Simple Behaviors */}
      <div className="grid grid-cols-2 gap-3">
        {BEHAVIOR_TYPES.filter(b => b.value !== 'state_machine').map((behavior) => {
          const isActive = hasBehavior(behavior.value);
          const Icon = behavior.icon;
          
          return (
            <button
              key={behavior.value}
              onClick={() => toggleBehavior(behavior.value)}
              className={cn(
                "p-4 rounded-xl border text-left transition-all",
                isActive
                  ? "border-violet-500/50 bg-violet-600/10"
                  : "border-slate-800 bg-slate-900/50 hover:border-slate-700"
              )}
            >
              <div className="flex items-start gap-3">
                <div className={cn(
                  "w-10 h-10 rounded-lg flex items-center justify-center bg-gradient-to-br",
                  isActive ? behavior.color : "from-slate-700 to-slate-800"
                )}>
                  <Icon className="w-5 h-5 text-white" />
                </div>
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-sm">{behavior.label}</span>
                    {isActive && (
                      <Badge className="bg-violet-600 text-[10px] px-1.5 py-0">Ativo</Badge>
                    )}
                  </div>
                  <p className="text-xs text-slate-500 mt-1">{behavior.description}</p>
                </div>
              </div>
            </button>
          );
        })}
      </div>

      {/* State Machine - Special */}
      <div className={cn(
        "rounded-xl border p-5 transition-all",
        hasBehavior('state_machine')
          ? "border-orange-500/50 bg-orange-600/5"
          : "border-slate-800 bg-slate-900/50"
      )}>
        <div className="flex items-start justify-between mb-4">
          <div className="flex items-start gap-3">
            <div className={cn(
              "w-10 h-10 rounded-lg flex items-center justify-center bg-gradient-to-br",
              hasBehavior('state_machine') ? "from-orange-500 to-amber-500" : "from-slate-700 to-slate-800"
            )}>
              <GitBranch className="w-5 h-5 text-white" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-medium">State Machine</span>
                {hasBehavior('state_machine') && (
                  <Badge className="bg-orange-600 text-[10px] px-1.5 py-0">Ativo</Badge>
                )}
              </div>
              <p className="text-xs text-slate-500 mt-1">Define transições de estado válidas</p>
            </div>
          </div>
          {hasBehavior('state_machine') && (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => toggleBehavior('state_machine')}
              className="text-red-400 hover:text-red-300"
            >
              <Trash2 className="w-4 h-4" />
            </Button>
          )}
        </div>

        {!hasBehavior('state_machine') ? (
          enumFields.length > 0 ? (
            <div className="space-y-3">
              <p className="text-sm text-slate-400">Selecione um campo enum para usar como estado:</p>
              <div className="flex flex-wrap gap-2">
                {enumFields.map((field) => (
                  <Button
                    key={field.name}
                    variant="outline"
                    size="sm"
                    onClick={() => addStateMachine(field.name, field.values)}
                    className="border-slate-700 hover:border-orange-500"
                  >
                    {field.name}
                    <span className="ml-2 text-slate-500">({field.values.length} estados)</span>
                  </Button>
                ))}
              </div>
            </div>
          ) : (
            <p className="text-sm text-slate-500">
              Adicione um atributo do tipo <span className="font-mono text-orange-400">enum</span> para usar state machine
            </p>
          )
        ) : typeof stateMachine === 'object' && (
          <div className="space-y-4">
            <div className="flex items-center gap-2">
              <span className="text-sm text-slate-400">Campo:</span>
              <Badge variant="outline" className="border-orange-500/50 text-orange-400">
                {stateMachine.field}
              </Badge>
            </div>
            
            <div className="space-y-3">
              <p className="text-xs text-slate-500">Transições permitidas:</p>
              {stateMachine.transitions.map((transition, idx) => {
                const allStates = stateMachine.transitions.map(t => t.from);
                return (
                  <div key={idx} className="flex items-center gap-3 p-3 bg-slate-800/50 rounded-lg">
                    <Badge className="bg-slate-700">{transition.from}</Badge>
                    <span className="text-slate-500">→</span>
                    <div className="flex-1 flex flex-wrap gap-1.5">
                      {allStates.filter(s => s !== transition.from).map((state) => {
                        const isAllowed = transition.to.includes(state);
                        return (
                          <button
                            key={state}
                            onClick={() => {
                              const newTo = isAllowed
                                ? transition.to.filter(s => s !== state)
                                : [...transition.to, state];
                              updateStateMachineTransition(transition.from, newTo);
                            }}
                            className={cn(
                              "px-2 py-1 rounded text-xs font-medium transition-all",
                              isAllowed
                                ? "bg-emerald-600 text-white"
                                : "bg-slate-700 text-slate-400 hover:bg-slate-600"
                            )}
                          >
                            {state}
                          </button>
                        );
                      })}
                      {transition.to.length === 0 && (
                        <span className="text-xs text-slate-500 italic">estado final</span>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}