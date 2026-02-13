import { Navigate } from 'react-router'
import { tokenStore } from '../utils/token-store'

type AuthRedirectProps = {
  children: React.ReactNode
}

export function AuthRedirect({ children }: AuthRedirectProps) {
  const isAuthenticated = tokenStore.hasTokens()

  if (isAuthenticated) {
    return <Navigate to="/orders" replace />
  }

  return <>{children}</>
}
