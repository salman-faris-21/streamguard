const TOKEN_KEY = "streamguard.token";

export function readToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function storeToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

export function decodeToken(token) {
  if (!token) {
    return null;
  }
  const parts = token.split(".");
  if (parts.length < 2) {
    return null;
  }
  try {
    const payload = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const padded = payload + "=".repeat((4 - (payload.length % 4)) % 4);
    return JSON.parse(atob(padded));
  } catch {
    return null;
  }
}

export function isTokenExpired(token, skewSeconds = 5) {
  const claims = decodeToken(token);
  if (!claims || typeof claims.exp !== "number") {
    return true;
  }
  return claims.exp * 1000 <= Date.now() + skewSeconds * 1000;
}

export function getValidToken() {
  const token = readToken();
  if (!token) {
    return null;
  }
  if (isTokenExpired(token)) {
    clearToken();
    return null;
  }
  return token;
}

export function tokenSubject(token) {
  const claims = decodeToken(token);
  return typeof claims?.sub === "string" ? claims.sub : "";
}
