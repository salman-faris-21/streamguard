import { useState } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import Alert from "../components/Alert";
import { useAuth } from "../auth/AuthContext";
import { getErrorMessage } from "../api/client";

export default function LoginPage() {
  const { token, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  if (token) {
    return <Navigate to="/" replace />;
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await login({ email: email.trim(), password });
      const next = location.state?.from?.pathname || "/";
      navigate(next, { replace: true });
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="mx-auto max-w-md">
      <p className="text-xs font-semibold uppercase tracking-[0.18em] text-moss">Account</p>
      <h1 className="mt-2 font-display text-4xl">Sign in</h1>
      <p className="mt-3 text-sm leading-6 text-stone-600">
        Sign in through the API gateway. Orders and payments use the returned token.
      </p>
      {location.state?.reason === "expired" ? (
        <div className="mt-6">
          <Alert tone="warn">Your session expired or the token was rejected. Sign in again.</Alert>
        </div>
      ) : null}
      <form className="mt-6 space-y-4 rounded-3xl border border-line bg-card p-6 shadow-card" onSubmit={handleSubmit}>
        <label className="block text-sm font-medium" htmlFor="email">
          Email
          <input
            id="email"
            name="email"
            type="email"
            autoComplete="email"
            required
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            className="mt-2 h-11 w-full rounded-2xl border border-line bg-paper px-3"
          />
        </label>
        <label className="block text-sm font-medium" htmlFor="password">
          Password
          <input
            id="password"
            name="password"
            type="password"
            autoComplete="current-password"
            required
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            className="mt-2 h-11 w-full rounded-2xl border border-line bg-paper px-3"
          />
        </label>
        {error ? <Alert>{error}</Alert> : null}
        <button
          type="submit"
          disabled={submitting}
          className="inline-flex h-12 w-full items-center justify-center rounded-full bg-moss text-sm font-medium text-white hover:bg-mossdeep disabled:cursor-not-allowed disabled:bg-stone-400"
        >
          {submitting ? "Signing in…" : "Sign in"}
        </button>
      </form>
      <p className="mt-4 text-sm text-stone-600">
        No account yet?{" "}
        <Link className="font-medium text-moss" to="/register">
          Create one
        </Link>
      </p>
    </div>
  );
}
