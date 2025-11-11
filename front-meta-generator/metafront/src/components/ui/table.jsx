// src/components/ui/table.jsx
import React from "react";

export function Table({ children, className }) {
  return <table className={`min-w-full border-collapse ${className || ""}`}>{children}</table>;
}

export function TableHead({ children, className }) {
  return <thead className={className}>{children}</thead>;
}

export function TableRow({ children, className, ...props }) {
  return <tr className={`border-b ${className || ""}`} {...props}>{children}</tr>;
}

export function TableCell({ children, className, ...props }) {
  return <td className={`px-2 py-1 ${className || ""}`} {...props}>{children}</td>;
}

export function TableBody({ children, className }) {
  return <tbody className={className}>{children}</tbody>;
}
