import { api } from "./client";
import { endpoints } from "./endpoints";

export function createOrder(payload) {
  return api.post(endpoints.createOrder, payload).then((response) => response.data);
}

export function getOrder(orderId) {
  return api.get(endpoints.getOrder(orderId)).then((response) => response.data);
}
