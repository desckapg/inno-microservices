import { authority, clientId, userManager } from './auth-config.ts';
import { User } from 'oidc-client-ts';

export const tokenStore = {
  // Helper to get user from local storage
  getUser(): User | null {
    const oidcStorage = localStorage.getItem(`oidc.user:${authority}:${clientId}`);
    if (!oidcStorage) {
        return null;
    }

    try {
        return User.fromStorageString(oidcStorage);
    } catch {
        return null; // Invalid JSON
    }
  },

  getAccessToken(): string | null {
    const user = this.getUser();
    return user?.access_token || null;
  },

  clearTokens() {
      userManager.removeUser()
  },

}
