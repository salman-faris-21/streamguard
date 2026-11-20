export const products = [
  {
    id: "harbor-headphones",
    name: "Harbor Headphones",
    description: "Closed-back studio headphones for the demo catalog.",
    price: 4999,
    currency: "INR",
    available: true,
    mark: "headphones",
  },
  {
    id: "slate-keyboard",
    name: "Slate Keyboard",
    description: "Compact mechanical keyboard with a quiet switch.",
    price: 3499,
    currency: "INR",
    available: true,
    mark: "keyboard",
  },
  {
    id: "field-watch",
    name: "Field Watch",
    description: "Analog field watch with a canvas strap.",
    price: 8999,
    currency: "INR",
    available: true,
    mark: "watch",
  },
  {
    id: "studio-monitor",
    name: "Studio Monitor",
    description: "Reference display held back from booking in this demo.",
    price: 18999,
    currency: "INR",
    available: false,
    mark: "monitor",
  },
];

export function findProduct(productId) {
  return products.find((product) => product.id === productId) || null;
}
