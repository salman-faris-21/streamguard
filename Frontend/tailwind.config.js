/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        ink: "#1c1915",
        paper: "#f3efe6",
        card: "#fffdf8",
        line: "#e4dccf",
        moss: "#1f6b45",
        mossdeep: "#144a30",
        clay: "#9a3412",
        gold: "#a16207",
      },
      fontFamily: {
        display: ["Newsreader", "Georgia", "serif"],
        sans: ["Sora", "Segoe UI", "sans-serif"],
      },
      boxShadow: {
        card: "0 18px 40px -28px rgba(28, 25, 21, 0.45)",
      },
    },
  },
  plugins: [],
};
