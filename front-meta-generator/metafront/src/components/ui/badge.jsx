export default function Badge({ children, color = "default", className = "" }) {
  const colors = {
    default: "bg-slate-700 text-slate-200",
    success: "bg-green-600 text-white",
    warning: "bg-yellow-500 text-black",
    danger: "bg-red-600 text-white",
    info: "bg-blue-600 text-white",
    violet: "bg-violet-600 text-white",
  };

  return (
    <span
      className={`
        px-2 py-0.5 text-xs font-medium rounded-full inline-flex items-center
        ${colors[color] || colors.default}
        ${className}
      `}
    >
      {children}
    </span>
  );
}
