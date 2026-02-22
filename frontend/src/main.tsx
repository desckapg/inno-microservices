import 'bootstrap/dist/css/bootstrap.min.css'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.tsx'
import {BrowserRouter, Route, Routes} from "react-router";
import {OrdersPage} from "./pages/OrdersPage.tsx";
import {ProtectedRoute} from "./components/ProtectedRoute.tsx";
import {AuthProvider} from "react-oidc-context";
import {CompleteProfile} from "./pages/CompleteProfile.tsx";
import {userManager} from "./utils/auth-config.ts";

const oidcConfig = {
    userManager: userManager,
    onSigninCallback: () => {
        // Remove OIDC callback params (code, state, session_state) from the URL
        // to prevent the library from re-processing them on next render/reload
        globalThis.history.replaceState(
            {},
            document.title,
            globalThis.location.pathname
        )
    }
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
      <AuthProvider {...oidcConfig}>
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<App/>}/>
            <Route path="/orders" element={
              <ProtectedRoute>
                <OrdersPage/>
              </ProtectedRoute>
            }/>
              <Route path="/complete-profile" element={<CompleteProfile/>}/>
          </Routes>
        </BrowserRouter>
      </AuthProvider>
  </StrictMode>,
)
