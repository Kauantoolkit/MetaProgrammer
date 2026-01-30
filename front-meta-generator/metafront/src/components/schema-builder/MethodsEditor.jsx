import React, { useState } from 'react';
import { Button } from "@/components/ui/button";
import Input from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Plus, Trash2, Edit3 } from 'lucide-react';
import DtoEditor from './DtoEditor';

export default function MethodsEditor({ interface: interf, onUpdate }) {
  const [editingMethod, setEditingMethod] = useState(null);
  const [newMethodName, setNewMethodName] = useState('');

  const handleAddMethod = () => {
    if (!newMethodName.trim()) return;

    const newMethod = {
      name: newMethodName.trim(),
      inputDto: { name: `${newMethodName.trim()}Input`, attributes: [] },
      outputDto: { name: `${newMethodName.trim()}Output`, attributes: [] }
    };

    const updatedInterface = {
      ...interf,
      methods: [...interf.methods, newMethod]
    };

    onUpdate(updatedInterface);
    setNewMethodName('');
  };

  const handleDeleteMethod = (methodIndex) => {
    const updatedInterface = {
      ...interf,
      methods: interf.methods.filter((_, index) => index !== methodIndex)
    };
    onUpdate(updatedInterface);
  };

  const handleUpdateMethod = (methodIndex, updatedMethod) => {
    const updatedInterface = {
      ...interf,
      methods: interf.methods.map((method, index) =>
        index === methodIndex ? updatedMethod : method
      )
    };
    onUpdate(updatedInterface);
  };

  return (
    <div className="space-y-6">
      {/* Add New Method */}
      <Card className="bg-slate-800/50 border-slate-700">
        <CardHeader className="pb-3">
          <CardTitle className="text-sm text-slate-300">Adicionar Método</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex gap-2">
            <Input
              placeholder="Nome do método"
              value={newMethodName}
              onChange={(e) => setNewMethodName(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleAddMethod()}
              className="bg-slate-700 border-slate-600 text-white"
            />
            <Button
              onClick={handleAddMethod}
              disabled={!newMethodName.trim()}
              className="bg-blue-600 hover:bg-blue-700"
            >
              <Plus className="w-4 h-4" />
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Methods List */}
      <div className="space-y-4">
        {interf.methods.map((method, index) => (
          <Card key={index} className="bg-slate-800/50 border-slate-700">
            <CardHeader className="pb-3">
              <div className="flex items-center justify-between">
                <CardTitle className="text-lg text-white">{method.name}</CardTitle>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => handleDeleteMethod(index)}
                  className="text-red-400 hover:text-red-300 hover:bg-red-400/10"
                >
                  <Trash2 className="w-4 h-4" />
                </Button>
              </div>
            </CardHeader>
            <CardContent className="space-y-4">
              {/* Input DTO */}
              <div>
                <h4 className="text-sm font-medium text-slate-300 mb-2">Input DTO</h4>
                <DtoEditor
                  dto={method.inputDto}
                  onUpdate={(updatedDto) => handleUpdateMethod(index, { ...method, inputDto: updatedDto })}
                />
              </div>

              {/* Output DTO */}
              <div>
                <h4 className="text-sm font-medium text-slate-300 mb-2">Output DTO</h4>
                <DtoEditor
                  dto={method.outputDto}
                  onUpdate={(updatedDto) => handleUpdateMethod(index, { ...method, outputDto: updatedDto })}
                />
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {interf.methods.length === 0 && (
        <div className="text-center py-8 text-slate-500">
          <p>Nenhum método definido ainda.</p>
          <p className="text-sm mt-1">Adicione um método acima para começar.</p>
        </div>
      )}
    </div>
  );
}
