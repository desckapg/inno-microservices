import { Navigate } from 'react-router'
import { useAuth } from 'react-oidc-context'
import { useEffect, useRef } from 'react'

function App() {
  const auth = useAuth()
  const hasTriedSignin = useRef(false)

  console.log('App render:', {
    isLoading: auth.isLoading,
    isAuthenticated: auth.isAuthenticated,
    activeNavigator: auth.activeNavigator,
    hasTriedSignin: hasTriedSignin.current,
    error: auth.error
  })

  useEffect(() => {
    // If we are processing a login callback, do not redirect
    if (auth.activeNavigator) {
      console.log('Active navigator detected, skipping redirect')
      return;
    }

    // Only trigger signin redirect once
    if (!auth.isLoading && !auth.isAuthenticated && !hasTriedSignin.current) {
       console.log('Triggering signin redirect...')
       hasTriedSignin.current = true
       auth.signinRedirect().catch((error) => {
         console.error('Signin redirect failed:', error)
         // Reset flag to allow retry
         hasTriedSignin.current = false
       })
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [auth.isLoading, auth.isAuthenticated, auth.activeNavigator])

  if (auth.isLoading || auth.activeNavigator) {
      return <div>Loading...</div>
  }

  if (auth.error) {
      return <div>Authentication error: {auth.error.message}</div>
  }

  if (auth.isAuthenticated) {
      return <Navigate to="/orders" replace />
  }

  return <div>Redirecting to login...</div>
}

export default App
