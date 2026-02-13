import 'bootstrap/dist/css/bootstrap.min.css'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.tsx'
import {BrowserRouter, Route, Routes} from "react-router";
import {LoginPage} from "./pages/LoginPage.tsx";
import {RegisterPage} from "./pages/RegisterPage.tsx";
import {OrdersPage} from "./pages/OrdersPage.tsx";
import {ProtectedRoute} from "./components/ProtectedRoute.tsx";
import {AuthRedirect} from "./components/AuthRedirect.tsx";

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<App/>}/>
        <Route path="/login" element={
          <AuthRedirect>
            <LoginPage/>
          </AuthRedirect>
        }/>
        <Route path="/register" element={
          <AuthRedirect>
            <RegisterPage/>
          </AuthRedirect>
        }/>
        <Route path="/orders" element={
          <ProtectedRoute>
            <OrdersPage/>
          </ProtectedRoute>
        }/>
      </Routes>
    </BrowserRouter>
  </StrictMode>,
)
