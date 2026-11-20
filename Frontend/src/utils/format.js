export function formatMoney(amount, currency = "INR") {
  const value = Number(amount);
  if (!Number.isFinite(value)) {
    return `${currency} ${amount ?? ""}`.trim();
  }
  try {
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency,
      maximumFractionDigits: 2,
    }).format(value);
  } catch {
    return `${currency} ${value.toFixed(2)}`;
  }
}

export function formatWhen(value) {
  if (!value) {
    return "—";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return String(value);
  }
  return new Intl.DateTimeFormat("en-IN", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

export function toPaise(amount) {
  const normalized = Number(amount).toFixed(2);
  const [whole, fraction] = normalized.split(".");
  return Number(whole) * 100 + Number(fraction);
}
