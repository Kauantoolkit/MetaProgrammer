import React, { useState } from 'react';
import { ScrollArea } from "@/components/ui/scroll-area";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Code, Settings } from 'lucide-react';
import MethodsEditor from './MethodsEditor';
import Input from "../ui/input";

export default function InterfaceEditor({ interface: interf, activeTab, onTabChange, onUpdate, onRename }) {
  const [editingName, setEditingName] = useState(false);
  const [tempName, setTempName] = useState(interf.name);

  const handleNameSubmit = () => {
    if (tempName && tempName !== interf.name) {
      onRename(interf.name, tempName);
    }
    setEditingName(false);
  };

  return (
    <div className="h-full flex flex-col">
      {/* Interface Header */}
      <div className="p-6 border-b border-slate-800 bg-slate-900/30">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-blue-500 to-cyan-500 flex items-center justify-center">
            <Code className="w-6 h-6 text-white" />
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
                className="text-xl font-bold cursor-pointer hover:text-blue-400 transition-colors"
                onClick={() => {
                  setTempName(interf.name);
                  setEditingName(true);
                }}
              >
                {interf.name}
              </h2>
            )}
            <p className="text-sm text-slate-500 mt-0.5">
              {interf.methods.length} método{interf.methods.length !== 1 ? 's' : ''}
            </p>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={onTabChange} className="flex-1 flex flex-col overflow-hidden">
        <div className="border-b border-slate-800 px-6">
          <TabsList className="bg-transparent h-12 p-0 gap-1">
            <TabsTrigger
              value="methods"
              className="gap-2 data-[state=active]:bg-slate-800 data-[state=active]:shadow-none rounded-b-none border-b-2 border-transparent data-[state=active]:border-blue-500"
            >
              <Settings className="w-4 h-4" />
              Métodos
            </TabsTrigger>
          </TabsList>
        </div>

        <ScrollArea className="flex-1">
          <TabsContent value="methods" className="m-0 p-6">
            <MethodsEditor interface={interf} onUpdate={onUpdate} />
          </TabsContent>
        </ScrollArea>
      </Tabs>
    </div>
  );
}
