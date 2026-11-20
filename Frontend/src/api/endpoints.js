/**
 * Gateway routes from gateway/src/main/resources/application.properties
 * and AuthController. The UI calls only the gateway on port 8083.
 *
 * Explicit routes:
 *   /api/orders/**  -> lb://ORDER
 *   /api/payments/** -> lb://PAYMENT
 * Auth is served by the gateway itself:
 *   POST /api/auth/register
 *   POST /api/auth/login
 */
export const endpoints = {
  register: "/api/auth/register",
  login: "/api/auth/login",
  createOrder: "/api/orders",
  getOrder: (orderId) => `/api/orders/${orderId}`,
  createPayment: "/api/payments",
  getPayment: (paymentId) => `/api/payments/${paymentId}`,
};

export const ORDER_STATUS = {
  PENDING: "PENDING",
  PAID: "PAID",
  CANCELLED: "CANCELLED",
};

export const PAYMENT_STATUS = {
  PENDING: "PENDING",
  SUCCESS: "SUCCESS",
  FAILED: "FAILED",
};
