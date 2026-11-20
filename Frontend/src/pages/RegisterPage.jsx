import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import Alert from "../components/Alert";
import { useAuth } from "../auth/AuthContext";
import { getErrorMessage } from "../api/client";

export default function RegisterPage() {
  const { token, register } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
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
    if (username.trim().length < 3 || username.trim().length > 30) {
      setError("Username must be between 3 and 30 characters.");
      return;
    }
    if (password.length < 8) {
      setError("Password must be at least 8 characters.");
      return;
    }
    setSubmitting(true);
    try {
      await register({
        username: username.trim(),
        email: email.trim(),
        password,
      });
      navigate("/", { replace: true });
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="mx-auto max-w-md">
      <p className="text-xs font-semibold uppercase tracking-[0.18em] text-moss">Account</p>
      <h1 className="mt-2 font-display text-4xl">Create an account</h1>
      <p className="mt-3 text-sm leading-6 text-stone-600">
        Registration calls the gateway and keeps the JWT for later order and payment requests.
      </p>
      <form className="mt-6 space-y-4 rounded-3xl border border-line bg-card p-6 shadow-card" onSubmit={handleSubmit}>
        <label className="block text-sm font-medium" htmlFor="username">
          Username
          <input
            id="username"
            name="username"
            type="text"
            autoComplete="username"
            required
            minLength={3}
            maxLength={30}
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            className="mt-2 h-11 w-full rounded-2xl border border-line bg-paper px-3"
          />
        </label>
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
            autoComplete="new-password"
            required
            minLength={8}
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
          {submitting ? "Creating account…" : "Create account"}
        </button>
      </form>
      <p className="mt-4 text-sm text-stone-600">
        Already registered?{" "}
        <Link className="font-medium text-moss" to="/login">
          Sign in
        </Link>
      </p>
    </div>
  );
}
