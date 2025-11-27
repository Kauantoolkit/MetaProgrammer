import React, { createContext, useContext } from "react";

const TabsContext = createContext();

export function Tabs({ value, onValueChange, children, className = "" }) {
  return (
    <TabsContext.Provider value={{ value, onValueChange }}>
      <div className={className}>{children}</div>
    </TabsContext.Provider>
  );
}

export function TabsList({ children, className = "" }) {
  return (
    <div className={`flex items-center gap-1 ${className}`}>
      {children}
    </div>
  );
}

export function TabsTrigger({ value, children, className = "" }) {
  const ctx = useContext(TabsContext);
  const active = ctx.value === value;

  return (
    <button
      onClick={() => ctx.onValueChange(value)}
      data-state={active ? "active" : "inactive"}
      className={`
        px-3 py-2 text-sm font-medium
        transition-colors rounded-md
        ${active ? "bg-slate-800 text-white" : "text-slate-400 hover:text-slate-200"}
        ${className}
      `}
    >
      {children}
    </button>
  );
}

export function TabsContent({ value, children, className = "" }) {
  const ctx = useContext(TabsContext);

  if (ctx.value !== value) return null;

  return <div className={className}>{children}</div>;
}
