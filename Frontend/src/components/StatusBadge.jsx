const tones = {
  PENDING: "bg-amber-100 text-amber-950",
  PAID: "bg-emerald-100 text-emerald-950",
  CANCELLED: "bg-red-100 text-red-900",
  SUCCESS: "bg-emerald-100 text-emerald-950",
  FAILED: "bg-red-100 text-red-900",
};

export default function StatusBadge({ status }) {
  const label = status || "UNKNOWN";
  return (
    <span className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold tracking-wide ${tones[label] || "bg-stone-200 text-stone-800"}`}>
      {label}
    </span>
  );
}
