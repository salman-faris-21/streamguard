import { api } from "./client";
import { endpoints } from "./endpoints";

export function registerAccount(payload) {
  return api.post(endpoints.register, payload).then((response) => response.data);
}

export function loginAccount(payload) {
  return api.post(endpoints.login, payload).then((response) => response.data);
}
