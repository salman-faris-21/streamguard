export function getCustomerId(email) {
  const key = email ? `streamguard.customerId.${email}` : "streamguard.customerId";
  const existing = localStorage.getItem(key);
  if (existing) {
    return existing;
  }
  const created = crypto.randomUUID();
  localStorage.setItem(key, created);
  return created;
}

function orderKey(orderId) {
  return `streamguard.order.${orderId}`;
}

export function saveOrderContext(orderId, context) {
  sessionStorage.setItem(orderKey(orderId), JSON.stringify(context));
}

export function readOrderContext(orderId) {
  const raw = sessionStorage.getItem(orderKey(orderId));
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}
