import { useState } from "react";
import { useNavigate } from "react-router-dom";
import ProductCard from "../components/ProductCard";
import Alert from "../components/Alert";
import { products } from "../data/products";
import { createOrder } from "../api/orders";
import { getErrorMessage } from "../api/client";
import { saveOrderContext } from "../utils/session";

export default function ProductListPage() {
  const navigate = useNavigate();
  const [booking, setBooking] = useState(null);
  const [error, setError] = useState("");

  async function handleBook(product) {
    setError("");
    setBooking(product.id);
    try {
      const order = await createOrder({
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
      setBooking(null);
    }
  }

  return (
    <div>
      <div className="max-w-2xl">
        <p className="text-xs font-semibold uppercase tracking-[0.18em] text-moss">Catalog</p>
        <h1 className="mt-2 font-display text-4xl text-ink sm:text-5xl">Choose a demonstration product</h1>
        <p className="mt-4 text-sm leading-7 text-stone-600 sm:text-base">
          Booking creates a real order through the API gateway. Payment is confirmed only after the order service reports the order as paid.
        </p>
      </div>
      {error ? (
        <div className="mt-6">
          <Alert>{error}</Alert>
        </div>
      ) : null}
      <div className="mt-8 grid gap-5 sm:grid-cols-2 xl:grid-cols-4">
        {products.map((product) => (
          <ProductCard key={product.id} product={product} booking={booking} onBook={handleBook} />
        ))}
      </div>
    </div>
  );
}
