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
