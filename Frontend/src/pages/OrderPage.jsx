import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import Alert from "../components/Alert";
import ProductMark from "../components/ProductMark";
import StatusBadge from "../components/StatusBadge";
import { getOrder } from "../api/orders";
import { createPayment, getPayment } from "../api/payments";
import { getErrorMessage } from "../api/client";
import { ORDER_STATUS, PAYMENT_STATUS } from "../api/endpoints";
import { readOrderContext, saveOrderContext } from "../utils/session";
import { formatMoney, formatWhen, toPaise } from "../utils/format";
import { loadRazorpayScript, openRazorpayCheckout } from "../utils/razorpay";

const POLL_INTERVAL_MS = 2000;
const POLL_TIMEOUT_MS = 90000;

export default function OrderPage() {
  const { orderId } = useParams();
  const [order, setOrder] = useState(null);
  const [context, setContext] = useState(() => readOrderContext(orderId));
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [actionError, setActionError] = useState("");
  const [paying, setPaying] = useState(false);
  const [phase, setPhase] = useState("ready");
  const [checkoutNote, setCheckoutNote] = useState("");
  const [payment, setPayment] = useState(null);

  useEffect(() => {
    let ignore = false;

    async function load() {
      setLoading(true);
      setLoadError("");
      try {
        const response = await getOrder(orderId);
        if (!ignore) {
          setOrder(response);
        }
        const storedPaymentId = readOrderContext(orderId)?.paymentId;
        if (storedPaymentId) {
          try {
            const paymentResponse = await getPayment(storedPaymentId);
            if (!ignore) {
              setPayment(paymentResponse);
            }
          } catch {
            // Keep the order visible even if payment status is still loading.
          }
        }
      } catch (err) {
        if (!ignore) {
          setLoadError(getErrorMessage(err));
        }
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    }

    load();
    return () => {
      ignore = true;
    };
  }, [orderId]);

  useEffect(() => {
    if (phase !== "processing") {
      return undefined;
    }

    let ignore = false;
    const started = Date.now();

    async function refresh() {
      try {
        const orderResponse = await getOrder(orderId);
        if (ignore) {
          return;
        }
        setOrder(orderResponse);
        setActionError("");

        const paymentId = payment?.paymentId || context?.paymentId;
        if (paymentId) {
          try {
            const paymentResponse = await getPayment(paymentId);
            if (ignore) {
              return;
            }
            setPayment(paymentResponse);
          } catch (err) {
            if (!ignore) {
              setActionError(getErrorMessage(err));
            }
          }
        }

        if (orderResponse.status === ORDER_STATUS.PAID || orderResponse.status === ORDER_STATUS.CANCELLED) {
          setPhase("settled");
          return;
        }
        if (Date.now() - started >= POLL_TIMEOUT_MS) {
          setPhase("timeout");
        }
      } catch (err) {
        if (!ignore) {
          setActionError(getErrorMessage(err));
        }
      }
    }

    refresh();
    const timer = window.setInterval(refresh, POLL_INTERVAL_MS);
    return () => {
      ignore = true;
      window.clearInterval(timer);
    };
  }, [phase, orderId, context?.paymentId]);

  async function handlePay() {
    setActionError("");
    setCheckoutNote("");
    setPaying(true);

    try {
      const stored = context || { product: null, idempotencyKey: crypto.randomUUID() };
      const idempotencyKey = stored.idempotencyKey || crypto.randomUUID();
      const nextContext = { ...stored, idempotencyKey };
      saveOrderContext(orderId, nextContext);
      setContext(nextContext);

      const paymentResponse = await createPayment(orderId, idempotencyKey);
      setPayment(paymentResponse);
      saveOrderContext(orderId, { ...nextContext, paymentId: paymentResponse.paymentId });
      setContext({ ...nextContext, paymentId: paymentResponse.paymentId });

      if (paymentResponse.status === PAYMENT_STATUS.SUCCESS) {
        setCheckoutNote("The payment service already recorded success. Waiting for the order to update.");
        setPhase("processing");
        return;
      }

      if (paymentResponse.status === PAYMENT_STATUS.FAILED) {
        const retryKey = crypto.randomUUID();
        const retried = await createPayment(orderId, retryKey);
        saveOrderContext(orderId, {
          ...nextContext,
          idempotencyKey: retryKey,
          paymentId: retried.paymentId,
        });
        setContext({
          ...nextContext,
          idempotencyKey: retryKey,
          paymentId: retried.paymentId,
        });
        setPayment(retried);
        await launchCheckout(retried, nextContext.product);
        return;
      }

      await launchCheckout(paymentResponse, nextContext.product);
    } catch (err) {
      setActionError(getErrorMessage(err));
    } finally {
      setPaying(false);
    }
  }

  async function launchCheckout(paymentResponse, product) {
    if (!paymentResponse.razorpayOrderId) {
      setActionError("The payment service did not return a Razorpay order id.");
      return;
    }

    const keyId = import.meta.env.VITE_RAZORPAY_KEY_ID;
    if (!keyId) {
      setActionError(
        "Checkout needs the public Razorpay key id in VITE_RAZORPAY_KEY_ID. The payment response does not include it."
      );
      return;
    }

    const loaded = await loadRazorpayScript();
    if (!loaded || !window.Razorpay) {
      setActionError("Razorpay Checkout could not be loaded.");
      return;
    }

    openRazorpayCheckout({
      keyId,
      payment: paymentResponse,
      description: product?.name || "Order payment",
      amountInPaise: toPaise(paymentResponse.amount),
      onSignal(kind) {
        setCheckoutNote(
          kind === "failed"
            ? "Checkout reported a failed attempt. This screen waits for the order service before treating the payment as failed."
            : "Checkout returned to the browser. This screen waits for the order service before confirming payment."
        );
        setPhase("processing");
      },
      onDismiss() {
        setCheckoutNote("Checkout was closed. The order stays pending until the payment service confirms an outcome.");
        setPhase("dismissed");
      },
    });
  }

  if (loading) {
    return <p className="text-sm text-stone-600">Loading order…</p>;
  }

  if (loadError) {
    return (
      <div className="max-w-xl space-y-4">
        <h1 className="font-display text-4xl">Order unavailable</h1>
        <Alert>{loadError}</Alert>
        <Link className="inline-block text-sm font-medium text-moss" to="/">
          Return to products
        </Link>
      </div>
    );
  }

  const product = context?.product;
  const confirmed = order.status === ORDER_STATUS.PAID;
  const failed = order.status === ORDER_STATUS.CANCELLED;
  const pending = order.status === ORDER_STATUS.PENDING;
  const waiting = phase === "processing";

  return (
    <div className="grid gap-8 lg:grid-cols-[minmax(0,1.1fr)_minmax(0,0.9fr)]">
      <section className="rounded-3xl border border-line bg-card p-6 shadow-card sm:p-8">
        <p className="text-xs font-semibold uppercase tracking-[0.18em] text-moss">
          {confirmed ? "Order confirmed" : "Order"}
        </p>
        <h1 className="mt-2 break-all font-display text-4xl sm:text-5xl">{order.id}</h1>
        <div className="mt-4">
          <StatusBadge status={order.status} />
        </div>
        <dl className="mt-8 grid gap-4 text-sm sm:grid-cols-2">
          <Detail label="Item" value={product?.name || "Selected demo item"} />
          <Detail label="Items" value={order.numberOfItems} />
          <Detail label="Amount" value={formatMoney(order.totalAmount, order.currency)} />
          <Detail label="Currency" value={order.currency} />
          <Detail label="Customer" value={order.customerId} />
          <Detail label="Created" value={formatWhen(order.createdAt)} />
          {payment ? <Detail label="Payment id" value={payment.paymentId} /> : null}
          {payment ? <Detail label="Payment status" value={payment.status} /> : null}
        </dl>
        {product ? (
          <div className="mt-8 flex items-center gap-4 rounded-2xl bg-[#eef4ea] p-4">
            <ProductMark mark={product.mark} />
            <div>
              <p className="font-medium">{product.name}</p>
              <p className="text-sm text-stone-600">{product.description}</p>
            </div>
          </div>
        ) : (
          <p className="mt-8 text-sm leading-6 text-stone-600">
            The order service does not store a product name. This browser session did not keep the catalog item used at booking.
          </p>
        )}
      </section>

      <aside className="rounded-3xl border border-line bg-card p-6 shadow-card sm:p-8" aria-live="polite">
        {confirmed ? <ConfirmedPanel /> : null}
        {failed ? <FailedPanel /> : null}
        {pending && waiting ? (
          <ProcessingPanel note={checkoutNote} paymentStatus={payment?.status} />
        ) : null}
        {pending && phase === "timeout" ? (
          <TimeoutPanel note={checkoutNote} onCheckAgain={() => setPhase("processing")} />
        ) : null}
        {pending && !waiting && phase !== "timeout" ? (
          <PayPanel
            paying={paying}
            error={actionError}
            note={checkoutNote}
            onPay={handlePay}
          />
        ) : null}
        {(waiting || phase === "timeout") && actionError ? (
          <div className="mt-4">
            <Alert>{actionError}</Alert>
          </div>
        ) : null}
        <Link className="mt-6 block text-center text-sm text-stone-600 hover:text-ink" to="/">
          Return to products
        </Link>
      </aside>
    </div>
  );
}

function Detail({ label, value }) {
  return (
    <div className="rounded-2xl border border-line px-4 py-3">
      <dt className="text-xs uppercase tracking-[0.14em] text-stone-500">{label}</dt>
      <dd className="mt-1 break-all font-medium">{value}</dd>
    </div>
  );
}

function PayPanel({ paying, error, note, onPay }) {
  return (
    <div>
      <h2 className="font-display text-3xl">Pay now</h2>
      <p className="mt-3 text-sm leading-6 text-stone-600">
        Pay Now asks the payment service to create the Razorpay order. Checkout uses that order id and amount. Closing Checkout does not confirm or cancel the order.
      </p>
      {note ? (
        <div className="mt-4">
          <Alert tone="warn">{note}</Alert>
        </div>
      ) : null}
      {error ? (
        <div className="mt-4">
          <Alert>{error}</Alert>
        </div>
      ) : null}
      <button
        type="button"
        className="mt-6 inline-flex h-12 w-full items-center justify-center rounded-full bg-moss text-sm font-medium text-white hover:bg-mossdeep disabled:cursor-not-allowed disabled:bg-stone-400"
        onClick={onPay}
        disabled={paying}
      >
        {paying ? "Starting payment…" : "Pay Now"}
      </button>
    </div>
  );
}

function ProcessingPanel({ note, paymentStatus }) {
  const paymentPending = !paymentStatus || paymentStatus === PAYMENT_STATUS.PENDING;
  return (
    <div>
      <h2 className="font-display text-3xl">Verifying payment</h2>
      <p className="mt-3 text-sm leading-6 text-stone-600">
        {paymentPending
          ? "The payment is still PENDING. Razorpay’s webhook has not finished updating the payment service."
          : "The payment service has an outcome. This page still waits for the order service before confirmation."}
      </p>
      {note ? (
        <div className="mt-4">
          <Alert tone="info">{note}</Alert>
        </div>
      ) : null}
      <p className="mt-6 text-sm font-medium text-moss">
        {paymentPending ? "Checking payment status…" : "Checking order status…"}
      </p>
    </div>
  );
}

function TimeoutPanel({ note, onCheckAgain }) {
  return (
    <div>
      <h2 className="font-display text-3xl">Still waiting</h2>
      <p className="mt-3 text-sm leading-6 text-stone-600">
        The order is still PENDING. That can mean the webhook, outbox publisher, or order consumer has not finished. The payment is not confirmed.
      </p>
      {note ? (
        <div className="mt-4">
          <Alert tone="warn">{note}</Alert>
        </div>
      ) : null}
      <button
        type="button"
        className="mt-6 inline-flex h-12 w-full items-center justify-center rounded-full bg-ink text-sm font-medium text-paper hover:bg-stone-800"
        onClick={onCheckAgain}
      >
        Check again
      </button>
    </div>
  );
}

function ConfirmedPanel() {
  return (
    <div>
      <h2 className="font-display text-3xl">Payment confirmed</h2>
      <p className="mt-3 text-sm leading-6 text-stone-600">
        The order service reports this order as PAID. Confirmation comes from that status, after the payment event was consumed.
      </p>
    </div>
  );
}

function FailedPanel() {
  return (
    <div>
      <h2 className="font-display text-3xl">Payment was not completed</h2>
      <p className="mt-3 text-sm leading-6 text-stone-600">
        The order service reports this order as CANCELLED. A new booking is required before another payment can start, because payment initiation accepts only PENDING orders.
      </p>
    </div>
  );
}
