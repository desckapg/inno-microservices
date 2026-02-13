import { Navigate } from 'react-router'
import { tokenStore } from './utils/token-store'

function App() {
  const isAuthenticated = tokenStore.hasTokens()

  return <Navigate to={isAuthenticated ? '/orders' : '/login'} replace />
}

export default App
