import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { loginAccount, registerAccount } from "../api/auth";
import { clearToken, getValidToken, storeToken, tokenSubject } from "./token";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const navigate = useNavigate();
  const [token, setToken] = useState(() => getValidToken());

  useEffect(() => {
    function onUnauthorized() {
      setToken(null);
      navigate("/login", { replace: true, state: { reason: "expired" } });
    }

    window.addEventListener("streamguard:unauthorized", onUnauthorized);
    return () => window.removeEventListener("streamguard:unauthorized", onUnauthorized);
  }, [navigate]);

  const value = useMemo(() => {
    function acceptToken(nextToken) {
      if (!nextToken) {
        throw new Error("The gateway did not return a token.");
      }
      storeToken(nextToken);
      setToken(nextToken);
    }

    return {
      token,
      email: token ? tokenSubject(token) : "",
      async register(payload) {
        const body = await registerAccount(payload);
        acceptToken(body?.token);
      },
      async login(payload) {
        const body = await loginAccount(payload);
        acceptToken(body?.token);
      },
      logout() {
        clearToken();
        setToken(null);
        navigate("/login", { replace: true });
      },
    };
  }, [navigate, token]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
