import { useState } from "react";

export default function Switch({ checked, onChange, className = "" }) {
  const [internalChecked, setInternalChecked] = useState(checked || false);

  const toggle = () => {
    const newValue = !internalChecked;
    setInternalChecked(newValue);
    onChange && onChange(newValue);
  };

  return (
    <button
      type="button"
      onClick={toggle}
      className={`
        relative inline-flex h-6 w-11 items-center rounded-full
        transition-colors duration-200 focus:outline-none
        ${internalChecked ? "bg-violet-600" : "bg-slate-700"}
        ${className}
      `}
    >
      <span
        className={`
          inline-block h-5 w-5 transform rounded-full bg-white shadow
          transition-transform duration-200
          ${internalChecked ? "translate-x-5" : "translate-x-1"}
        `}
      />
    </button>
  );
}
