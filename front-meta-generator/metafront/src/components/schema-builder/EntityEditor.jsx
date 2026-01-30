import React, { useState, useEffect } from 'react';
import { ScrollArea } from "@/components/ui/scroll-area";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Database, Shield, Zap, Settings, Lock } from 'lucide-react';
import AttributesEditor from './AttributesEditor';
import RelationsEditor from './RelationsEditor';
import BehaviorsEditor from './BehaviorsEditor';
import ApiConfigEditor from './ApiConfigEditor';
import Input from "../ui/input";

export default function EntityEditor({ entity, allEntities, onUpdate, onRename }) {
  const [activeTab, setActiveTab] = useState('attributes');
  const [editingName, setEditingName] = useState(false);
  const [tempName, setTempName] = useState(entity.name);

  const isFixed = entity.fixed === true || entity.name === "User";

  useEffect(() => {
    setTempName(entity.name);
  }, [entity.name]);

  const handleNameSubmit = () => {
    if (isFixed) return;

    if (tempName && tempName !== entity.name) {
      onRename(entity.name, tempName);
    }
    setEditingName(false);
  };

  return (
    <div className="h-full flex flex-col">
      {/* Entity Header */}
      <div className="p-6 border-b border-slate-800 bg-slate-900/30">
        <div className="flex items-center gap-4">
          <div className={`w-12 h-12 rounded-xl flex items-center justify-center ${
            isFixed
              ? "bg-gradient-to-br from-blue-600 to-indigo-600"
              : "bg-gradient-to-br from-violet-500 to-fuchsia-500"
          }`}>
            {isFixed ? (
              <Lock className="w-6 h-6 text-white" />
            ) : (
              <Database className="w-6 h-6 text-white" />
            )}
          </div>

          <div className="flex-1">
            {editingName && !isFixed ? (
              <Input
                value={tempName}
                onChange={(e) => setTempName(e.target.value)}
                onBlur={handleNameSubmit}
                onKeyDown={(e) => e.key === 'Enter' && handleNameSubmit()}
                className="text-xl font-bold bg-slate-800 border-slate-700 h-9 w-64"
                autoFocus
              />
            ) : (
              <h2
                className={`text-xl font-bold transition-colors ${
                  isFixed
                    ? "text-blue-400 cursor-default"
                    : "cursor-pointer hover:text-violet-400"
                }`}
                onClick={() => {
                  if (isFixed) return;
                  setTempName(entity.name);
                  setEditingName(true);
                }}
              >
                {entity.name}
                {isFixed && (
                  <span className="ml-2 text-xs text-blue-400 align-middle">
                    (sistema)
                  </span>
                )}
              </h2>
            )}

            <p className="text-sm text-slate-500 mt-0.5">
              {entity.attributes.length} atributos • {entity.relations.length} relações • {entity.behaviors.length} behaviors
            </p>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab} className="flex-1 flex flex-col overflow-hidden">
        <div className="border-b border-slate-800 px-6">
          <TabsList className="bg-transparent h-12 p-0 gap-1">
            <TabsTrigger
              value="attributes"
              className="gap-2 data-[state=active]:bg-slate-800 data-[state=active]:shadow-none rounded-b-none border-b-2 border-transparent data-[state=active]:border-violet-500"
            >
              <Database className="w-4 h-4" />
              Atributos
            </TabsTrigger>
            <TabsTrigger
              value="relations"
              className="gap-2 data-[state=active]:bg-slate-800 data-[state=active]:shadow-none rounded-b-none border-b-2 border-transparent data-[state=active]:border-violet-500"
            >
              <Settings className="w-4 h-4" />
              Relações
            </TabsTrigger>
            <TabsTrigger
              value="behaviors"
              className="gap-2 data-[state=active]:bg-slate-800 data-[state=active]:shadow-none rounded-b-none border-b-2 border-transparent data-[state=active]:border-violet-500"
            >
              <Zap className="w-4 h-4" />
              Behaviors
            </TabsTrigger>
            <TabsTrigger
              value="api"
              className="gap-2 data-[state=active]:bg-slate-800 data-[state=active]:shadow-none rounded-b-none border-b-2 border-transparent data-[state=active]:border-violet-500"
            >
              <Shield className="w-4 h-4" />
              API Config
            </TabsTrigger>
          </TabsList>
        </div>

        <ScrollArea className="flex-1">
          <TabsContent value="attributes" className="m-0 p-6">
            <AttributesEditor entity={entity} onUpdate={onUpdate} />
          </TabsContent>
          <TabsContent value="relations" className="m-0 p-6">
            <RelationsEditor entity={entity} allEntities={allEntities} onUpdate={onUpdate} />
          </TabsContent>
          <TabsContent value="behaviors" className="m-0 p-6">
            <BehaviorsEditor entity={entity} onUpdate={onUpdate} />
          </TabsContent>
          <TabsContent value="api" className="m-0 p-6">
            <ApiConfigEditor entity={entity} onUpdate={onUpdate} />
          </TabsContent>
        </ScrollArea>
      </Tabs>
    </div>
  );
}
