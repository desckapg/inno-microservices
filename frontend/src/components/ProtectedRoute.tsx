import { Navigate } from 'react-router'
import { useAuth } from 'react-oidc-context'
import { useEffect, useState, useRef } from 'react'
import {getProfile} from "../api/services/user-service.ts";
import axios from "axios";

type ProtectedRouteProps = Readonly<{
  children: React.ReactNode
}>

export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const auth = useAuth()
  const [profileStatus, setProfileStatus] = useState<'loading' | 'exists' | 'missing'>('loading')
  const hasCheckedProfile = useRef(false)

  useEffect(() => {
    if (auth.isLoading) return; // Wait for loading to finish

    if (!auth.isAuthenticated) {
      // Reset check flag when user is not authenticated
      hasCheckedProfile.current = false
      setProfileStatus('loading')
      return;
    }

    if (auth.user?.profile.sub && auth.user?.access_token && !hasCheckedProfile.current) {
      hasCheckedProfile.current = true
      getProfile(auth.user.profile.sub)
      .then(() => setProfileStatus('exists'))
      .catch((error: unknown) => {
        if (axios.isAxiosError(error) && error.response?.status === 404) {
          setProfileStatus('missing')
        } else if (axios.isAxiosError(error) && error.response?.status === 401) {
          // Token is invalid, will be redirected to login
          console.error('Unauthorized while fetching profile')
          hasCheckedProfile.current = false
        } else {
             console.error("Failed to fetch profile", error)
             // potentially handle error state (e.g. server down)
             // For now, let's treat generic error as blocking or maybe missing too?
             // Requirement only says 404 -> complete profile.
             // If other error, maybe stay on loading or show error.
        }
      })
    }
  }, [auth.isLoading, auth.isAuthenticated, auth.user?.profile.sub, auth.user?.access_token])

  if (auth.isLoading) {
    return <div>Loading...</div>
  }

  if (!auth.isAuthenticated) {
     return <Navigate to="/" replace />
  }

  if (profileStatus === 'loading') {
      return <div>Checking profile...</div>
  }

  if (profileStatus === 'missing') {
    return <Navigate to="/complete-profile" replace />
  }

  return <>{children}</>
}
