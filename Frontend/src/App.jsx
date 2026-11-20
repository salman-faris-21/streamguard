import { Route, Routes } from "react-router-dom";
import { AuthProvider } from "./auth/AuthContext";
import Layout from "./components/Layout";
import RequireAuth from "./components/RequireAuth";
import ProductListPage from "./pages/ProductListPage";
import ProductPage from "./pages/ProductPage";
import OrderPage from "./pages/OrderPage";
import NotFoundPage from "./pages/NotFoundPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route element={<Layout />}>
          <Route path="login" element={<LoginPage />} />
          <Route path="register" element={<RegisterPage />} />
          <Route element={<RequireAuth />}>
            <Route index element={<ProductListPage />} />
            <Route path="products/:productId" element={<ProductPage />} />
            <Route path="orders/:orderId" element={<OrderPage />} />
          </Route>
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
    </AuthProvider>
  );
}
