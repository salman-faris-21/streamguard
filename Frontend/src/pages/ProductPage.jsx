import { useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import Alert from "../components/Alert";
import ProductMark from "../components/ProductMark";
import { findProduct } from "../data/products";
import { createOrder } from "../api/orders";
import { getErrorMessage } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import { getCustomerId, saveOrderContext } from "../utils/session";
import { formatMoney } from "../utils/format";

export default function ProductPage() {
  const { productId } = useParams();
  const product = findProduct(productId);
  const navigate = useNavigate();
  const { email } = useAuth();
  const [booking, setBooking] = useState(false);
  const [error, setError] = useState("");

  if (!product) {
    return (
      <div className="max-w-xl">
        <h1 className="font-display text-4xl">Product not found</h1>
        <Link className="mt-6 inline-block text-sm font-medium text-moss" to="/">
          Back to products
        </Link>
      </div>
    );
  }

  async function handleBook() {
    setError("");
    setBooking(true);
    try {
      const order = await createOrder({
        customerId: getCustomerId(email),
        numberOfItems: 1,
        amount: product.price,
        currency: product.currency,
      });
      saveOrderContext(order.id, {
        product,
        idempotencyKey: crypto.randomUUID(),
      });
      navigate(`/orders/${order.id}`);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setBooking(false);
    }
  }

  return (
    <div className="grid gap-8 lg:grid-cols-[minmax(0,1.1fr)_minmax(0,0.9fr)]">
      <section className="rounded-3xl border border-line bg-card p-6 shadow-card sm:p-8">
        <div className="flex h-48 items-center justify-center rounded-2xl bg-[#eef4ea]">
          <ProductMark mark={product.mark} />
        </div>
        <p className="mt-6 text-xs font-semibold uppercase tracking-[0.18em] text-moss">Product summary</p>
        <h1 className="mt-2 font-display text-4xl sm:text-5xl">{product.name}</h1>
        <p className="mt-4 max-w-xl text-sm leading-7 text-stone-600 sm:text-base">{product.description}</p>
      </section>
      <aside className="rounded-3xl border border-line bg-card p-6 shadow-card sm:p-8">
        <h2 className="font-display text-3xl">Book this item</h2>
        <dl className="mt-6 space-y-4 text-sm">
          <div className="flex justify-between gap-4">
            <dt className="text-stone-500">Price</dt>
            <dd className="font-medium">{formatMoney(product.price, product.currency)}</dd>
          </div>
          <div className="flex justify-between gap-4">
            <dt className="text-stone-500">Quantity</dt>
            <dd className="font-medium">1</dd>
          </div>
          <div className="flex justify-between gap-4">
            <dt className="text-stone-500">Availability</dt>
            <dd className="font-medium">{product.available ? "Available" : "Unavailable"}</dd>
          </div>
        </dl>
        <p className="mt-6 text-sm leading-6 text-stone-600">
          Book sends customer id, item count, amount, and currency to the order service. The order id on the next screen comes from that response.
        </p>
        {error ? (
          <div className="mt-4">
            <Alert>{error}</Alert>
          </div>
        ) : null}
        <button
          type="button"
          className="mt-6 inline-flex h-12 w-full items-center justify-center rounded-full bg-moss text-sm font-medium text-white hover:bg-mossdeep disabled:cursor-not-allowed disabled:bg-stone-300"
          onClick={handleBook}
          disabled={!product.available || booking}
        >
          {booking ? "Creating order…" : "Book"}
        </button>
        <Link className="mt-4 block text-center text-sm text-stone-600 hover:text-ink" to="/">
          Return to products
        </Link>
      </aside>
    </div>
  );
}
