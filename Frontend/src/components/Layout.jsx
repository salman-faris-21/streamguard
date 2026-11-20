import { Link, Outlet } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function Layout() {
  const { token, email, logout } = useAuth();

  return (
    <div className="min-h-screen bg-paper text-ink">
      <header className="border-b border-line bg-[#f7f3eb]/90 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-4 sm:px-6">
          <Link to={token ? "/" : "/login"} className="group">
            <span className="font-display text-3xl leading-none">StreamGuard</span>
            <span className="mt-1 block text-xs uppercase tracking-[0.18em] text-stone-500">
              Payment reliability demo
            </span>
          </Link>
          {token ? (
            <div className="flex items-center gap-3">
              <span className="hidden max-w-[14rem] truncate text-sm text-stone-600 sm:inline">{email}</span>
              <Link
                to="/"
                className="rounded-full border border-line px-4 py-2 text-sm font-medium hover:border-moss hover:text-moss"
              >
                Products
              </Link>
              <button
                type="button"
                onClick={logout}
                className="rounded-full bg-ink px-4 py-2 text-sm font-medium text-paper hover:bg-stone-800"
              >
                Log out
              </button>
            </div>
          ) : (
            <Link
              to="/login"
              className="rounded-full border border-line px-4 py-2 text-sm font-medium hover:border-moss hover:text-moss"
            >
              Sign in
            </Link>
          )}
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-8 sm:px-6 sm:py-12">
        <Outlet />
      </main>
    </div>
  );
}
