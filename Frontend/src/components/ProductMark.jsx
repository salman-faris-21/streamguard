export default function ProductMark({ mark }) {
  const common = "h-16 w-16 text-moss";

  if (mark === "keyboard") {
    return (
      <svg className={common} viewBox="0 0 64 64" aria-hidden="true">
        <rect x="6" y="16" width="52" height="32" rx="6" fill="none" stroke="currentColor" strokeWidth="2.5" />
        <path d="M14 26h6M24 26h6M34 26h6M44 26h6M14 36h6M24 36h16M44 36h6" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
      </svg>
    );
  }

  if (mark === "watch") {
    return (
      <svg className={common} viewBox="0 0 64 64" aria-hidden="true">
        <rect x="26" y="6" width="12" height="10" rx="2" fill="currentColor" opacity="0.35" />
        <rect x="26" y="48" width="12" height="10" rx="2" fill="currentColor" opacity="0.35" />
        <circle cx="32" cy="32" r="14" fill="none" stroke="currentColor" strokeWidth="2.5" />
        <path d="M32 24v9l6 4" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
      </svg>
    );
  }

  if (mark === "monitor") {
    return (
      <svg className={common} viewBox="0 0 64 64" aria-hidden="true">
        <rect x="8" y="12" width="48" height="32" rx="4" fill="none" stroke="currentColor" strokeWidth="2.5" />
        <path d="M24 52h16M32 44v8" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
      </svg>
    );
  }

  return (
    <svg className={common} viewBox="0 0 64 64" aria-hidden="true">
      <path d="M12 30c8-14 32-14 40 0" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
      <rect x="8" y="28" width="12" height="16" rx="4" fill="none" stroke="currentColor" strokeWidth="2.5" />
      <rect x="44" y="28" width="12" height="16" rx="4" fill="none" stroke="currentColor" strokeWidth="2.5" />
      <path d="M20 44c4 6 20 6 24 0" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
    </svg>
  );
}
