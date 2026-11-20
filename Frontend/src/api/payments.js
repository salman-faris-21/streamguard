import { api } from "./client";
import { endpoints } from "./endpoints";

export function createPayment(orderId, idempotencyKey) {
  return api
    .post(
      endpoints.createPayment,
      { orderId },
      {
        headers: {
          "Idempotency-Key": idempotencyKey,
        },
      }
    )
    .then((response) => response.data);
}

export function getPayment(paymentId) {
  return api.get(endpoints.getPayment(paymentId)).then((response) => response.data);
}
