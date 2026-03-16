import { useState, useRef, useEffect, createContext, useContext } from "react";
import { ChevronDown } from "lucide-react";

// ---------------------------
// CONTEXT
// ---------------------------
const SelectContext = createContext();
const useSelect = () => useContext(SelectContext);

export function Select({ children, value, onValueChange }) {
  const [open, setOpen] = useState(false);

  return (
    <SelectContext.Provider value={{ open, setOpen, value, onValueChange }}>
      <div className="relative inline-block w-full">{children}</div>
    </SelectContext.Provider>
  );
}

// ---------------------------
// TRIGGER
// ---------------------------
export function SelectTrigger({ children }) {
  const { open, setOpen } = useSelect();

  return (
    <button
      type="button"
      onClick={() => setOpen(!open)}
      className="w-full flex items-center justify-between rounded-md border border-zinc-700 bg-zinc-900 px-3 py-2 text-sm text-zinc-300 hover:bg-zinc-800 transition"
    >
      {children}
      <ChevronDown className="h-4 w-4 opacity-70" />
    </button>
  );
}

// ---------------------------
// VALUE
// ---------------------------
export function SelectValue({ placeholder = "Selecione..." }) {
  const { value } = useSelect();
  return (
    <span className="text-sm text-zinc-200">
      {value ? value : <span className="opacity-50">{placeholder}</span>}
    </span>
  );
}

// ---------------------------
// CONTENT (dropdown)
// ---------------------------
export function SelectContent({ children }) {
  const { open, setOpen } = useSelect();
  const ref = useRef(null);

  // Fecha ao clicar fora
  useEffect(() => {
    const handleClick = (e) => {
      if (ref.current && !ref.current.contains(e.target)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, []);

  if (!open) return null;

  return (
    <div
      ref={ref}
      className="absolute z-50 mt-2 w-full rounded-md border border-zinc-700 bg-zinc-900 shadow-xl overflow-hidden"
    >
      {children}
    </div>
  );
}

// ---------------------------
// ITEM
// ---------------------------
export function SelectItem({ value, children }) {
  const { onValueChange, setOpen } = useSelect();

  return (
    <button
      type="button"
      onClick={() => {
        onValueChange(value);
        setOpen(false);
      }}
      className="w-full text-left px-3 py-2 text-sm text-zinc-300 hover:bg-zinc-800 cursor-pointer transition"
    >
      {children}
    </button>
  );
}
