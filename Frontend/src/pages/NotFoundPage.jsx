import { Link } from "react-router-dom";

export default function NotFoundPage() {
  return (
    <div className="max-w-xl">
      <h1 className="font-display text-4xl">Page not found</h1>
      <Link className="mt-6 inline-block text-sm font-medium text-moss" to="/">
        Back to products
      </Link>
    </div>
  );
}
