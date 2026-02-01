import {tokenStore} from './token-store'

interface JwtPayload {
  sub: string // ID
  roles: string[]
  type: string
  iat?: number
  exp?: number
  iss?: string
}

export const jwtUtils = {
  decodeToken(token: string): JwtPayload | null {
    try {
      const parts = token.split('.')
      if (parts.length !== 3) {
        return null
      }

      const payload = parts[1]
      const decoded = atob(payload.replaceAll('-', '+').replaceAll('_', '/'))
      return JSON.parse(decoded) as JwtPayload
    } catch (error) {
      console.error('Failed to decode JWT token:', error)
      return null
    }
  },

  getCurrentUserId(): number | null {
    const token = tokenStore.getAccessToken()
    if (!token) {
      return null
    }

    const payload = this.decodeToken(token)
    if (!payload?.sub) {
      return null
    }

    const userId = Number.parseInt(payload.sub, 10)
    return Number.isNaN(userId) ? null : userId
  },

  getCurrentUserRoles(): string[] {
    const token = tokenStore.getAccessToken()
    if (!token) {
      return []
    }

    const payload = this.decodeToken(token)
    return payload?.roles ?? []
  },

  isTokenExpired(token: string): boolean {
    const payload = this.decodeToken(token)
    if (!payload?.exp) {
      return true
    }

    return Date.now() >= payload.exp * 1000
  }
}
