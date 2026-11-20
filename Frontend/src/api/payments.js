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
