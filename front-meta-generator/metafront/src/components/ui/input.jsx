export default function Input({ className = "", ...props }) {
  return (
    <input
      className={`px-3 py-2 rounded-md border border-slate-700 bg-slate-900 text-slate-100 focus:outline-none focus:ring-2 focus:ring-violet-500 ${className}`}
      {...props}
    />
  );
}
