import { Link } from "react-router-dom";
import ProductMark from "./ProductMark";
import { formatMoney } from "../utils/format";

export default function ProductCard({ product, booking, onBook }) {
  const busy = booking === product.id;

  return (
    <article className="flex h-full flex-col rounded-3xl border border-line bg-card p-5 shadow-card">
      <div className="flex h-28 items-center justify-center rounded-2xl bg-[#eef4ea]">
        <ProductMark mark={product.mark} />
      </div>
      <div className="mt-5 flex items-start justify-between gap-3">
        <h2 className="font-display text-2xl leading-tight text-ink">
          <Link className="block hover:text-moss" to={`/products/${product.id}`}>
            {product.name}
          </Link>
        </h2>
        <p className="shrink-0 text-sm font-medium text-ink">
          {formatMoney(product.price, product.currency)}
        </p>
      </div>
      <p className="mt-2 flex-1 text-sm leading-6 text-stone-600">{product.description}</p>
      <p className="mt-4 text-xs font-medium uppercase tracking-[0.14em] text-stone-500">
        {product.available ? "Available" : "Unavailable"}
      </p>
      <button
        type="button"
        className="mt-4 inline-flex h-11 items-center justify-center rounded-full bg-moss px-4 text-sm font-medium text-white transition hover:bg-mossdeep disabled:cursor-not-allowed disabled:bg-stone-300"
        onClick={() => onBook(product)}
        disabled={!product.available || Boolean(booking)}
      >
        {busy ? "Creating order…" : "Book"}
      </button>
    </article>
  );
}
