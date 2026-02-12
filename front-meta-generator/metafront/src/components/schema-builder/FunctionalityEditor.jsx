import React, { useState, useEffect } from 'react';
import { ScrollArea } from "@/components/ui/scroll-area";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Zap, Database, Settings, LogIn as InputIcon, ArrowRight } from 'lucide-react';
import InputEditor from './InputEditor';
import OutputEditor from './OutputEditor';
import FunctionalityConfigEditor from './FunctionalityConfigEditor';
import Input from "../ui/input";

export default function FunctionalityEditor({ functionality, allEntities, onUpdate, onRename }) {
  const [activeTab, setActiveTab] = useState('input');
  const [editingName, setEditingName] = useState(false);
  const [tempName, setTempName] = useState(functionality.name);

  useEffect(() => {
    setTempName(functionality.name);
  }, [functionality.name]);

  const handleNameSubmit = () => {
    if (tempName && tempName !== functionality.name) {
      onRename(functionality.name, tempName);
    }
    setEditingName(false);
  };

  return (
    <div className="h-full flex flex-col">
      {/* Functionality Header */}
      <div className="p-6 border-b border-slate-800 bg-slate-900/30">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl flex items-center justify-center bg-gradient-to-br from-emerald-500 to-teal-500">
            <Zap className="w-6 h-6 text-white" />
          </div>

          <div className="flex-1">
            {editingName ? (
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
                className="text-xl font-bold cursor-pointer hover:text-emerald-400 transition-colors"
                onClick={() => {
                  setTempName(functionality.name);
                  setEditingName(true);
                }}
              >
                {functionality.name}
              </h2>
            )}

            <p className="text-sm text-slate-500 mt-0.5">
              {functionality.input?.length || 0} campos de entrada • {functionality.output?.length || 0} campos de saída
              {functionality.entity && ` • Vinculada a ${functionality.entity}`}
            </p>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab} className="flex-1 flex flex-col overflow-hidden">
        <div className="border-b border-slate-800 px-6">
          <TabsList className="bg-transparent h-12 p-0 gap-1">
            <TabsTrigger
              value="input"
              className="gap-2 data-[state=active]:bg-slate-800 data-[state=active]:shadow-none rounded-b-none border-b-2 border-transparent data-[state=active]:border-emerald-500"
            >
              <Input className="w-4 h-4" />
              Entrada
            </TabsTrigger>
            <TabsTrigger
              value="output"
              className="gap-2 data-[state=active]:bg-slate-800 data-[state=active]:shadow-none rounded-b-none border-b-2 border-transparent data-[state=active]:border-emerald-500"
            >
              <ArrowRight className="w-4 h-4" />
              Saída
            </TabsTrigger>
            <TabsTrigger
              value="config"
              className="gap-2 data-[state=active]:bg-slate-800 data-[state=active]:shadow-none rounded-b-none border-b-2 border-transparent data-[state=active]:border-emerald-500"
            >
              <Settings className="w-4 h-4" />
              Configuração
            </TabsTrigger>
          </TabsList>
        </div>

        <ScrollArea className="flex-1">
          <TabsContent value="input" className="m-0 p-6">
            <InputEditor functionality={functionality} onUpdate={onUpdate} />
          </TabsContent>
          <TabsContent value="output" className="m-0 p-6">
            <OutputEditor functionality={functionality} onUpdate={onUpdate} />
          </TabsContent>
          <TabsContent value="config" className="m-0 p-6">
            <FunctionalityConfigEditor functionality={functionality} allEntities={allEntities} onUpdate={onUpdate} />
          </TabsContent>
        </ScrollArea>
      </Tabs>
    </div>
  );
}
