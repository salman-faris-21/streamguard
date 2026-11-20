const RAZORPAY_SRC = "https://checkout.razorpay.com/v1/checkout.js";

export function loadRazorpayScript() {
  if (window.Razorpay) {
    return Promise.resolve(true);
  }

  return new Promise((resolve) => {
    const existing = document.querySelector(`script[src="${RAZORPAY_SRC}"]`);
    if (existing) {
      existing.addEventListener("load", () => resolve(true), { once: true });
      existing.addEventListener("error", () => resolve(false), { once: true });
      return;
    }

    const script = document.createElement("script");
    script.src = RAZORPAY_SRC;
    script.async = true;
    script.onload = () => resolve(true);
    script.onerror = () => resolve(false);
    document.body.appendChild(script);
  });
}

export function openRazorpayCheckout({
  keyId,
  payment,
  description,
  amountInPaise,
  onSignal,
  onDismiss,
}) {
  let closedBySignal = false;

  const checkout = new window.Razorpay({
    key: keyId,
    amount: amountInPaise,
    currency: payment.currency,
    name: "StreamGuard",
    description,
    order_id: payment.razorpayOrderId,
    handler() {
      closedBySignal = true;
      onSignal("handler");
    },
    modal: {
      confirm_close: true,
      ondismiss() {
        if (!closedBySignal) {
          onDismiss();
        }
      },
    },
    theme: {
      color: "#1f6b45",
    },
  });

  checkout.on("payment.failed", () => {
    closedBySignal = true;
    onSignal("failed");
  });

  checkout.open();
}
