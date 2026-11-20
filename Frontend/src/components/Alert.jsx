export default function Alert({ tone = "error", children }) {
  const tones = {
    error: "border-red-200 bg-red-50 text-red-900",
    info: "border-[#d7e4d4] bg-[#f3f8f1] text-mossdeep",
    warn: "border-amber-200 bg-amber-50 text-amber-950",
  };

  return (
    <div role="alert" className={`rounded-2xl border px-4 py-3 text-sm leading-6 ${tones[tone]}`}>
      {children}
    </div>
  );
}
