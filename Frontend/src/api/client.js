import axios from "axios";
import { clearToken, getValidToken } from "../auth/token";

const baseURL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8083";

export const api = axios.create({
  baseURL,
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use((config) => {
  const token = getValidToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  } else if (config.headers) {
    delete config.headers.Authorization;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status;
    const url = error?.config?.url || "";
    const isAuthCall = url.includes("/api/auth/");
    if (status === 401 && !isAuthCall) {
      clearToken();
      window.dispatchEvent(new Event("streamguard:unauthorized"));
    }
    return Promise.reject(error);
  }
);

export function getErrorMessage(error) {
  const data = error?.response?.data;
  const status = error?.response?.status;

  if (data && typeof data === "string" && data.trim()) {
    return data;
  }

  if (data && Array.isArray(data.errors) && data.errors.length > 0) {
    const messages = data.errors
      .map((item) => item.defaultMessage || item.message)
      .filter(Boolean);
    if (messages.length > 0) {
      return messages.join(" ");
    }
  }

  if (data && typeof data.detail === "string" && data.detail.trim()) {
    return data.detail;
  }

  if (
    data &&
    typeof data.message === "string" &&
    data.message.trim() &&
    data.message !== "No message available"
  ) {
    return data.message;
  }

  if (status === 401) {
    return "Your session is missing or no longer valid. Sign in again.";
  }

  if (!data || (typeof data === "string" && !data.trim())) {
    if (error?.code === "ERR_NETWORK" || error?.code === "ECONNABORTED" || status >= 500) {
      return "The API gateway could not be reached. Confirm it is running at http://localhost:8083.";
    }
    return error?.message || "Request failed.";
  }

  return status ? `Request failed (${status}).` : "Request failed.";
}
