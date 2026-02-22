import { UserManager, WebStorageStateStore } from 'oidc-client-ts';

export const authority = import.meta.env.VITE_OIDC_AUTHORITY;
export const clientId = "frontend";

console.log('Auth config:', { authority, clientId, redirect_uri: globalThis.location.origin });

export const userManager = new UserManager({
    authority,
    client_id: clientId,
    redirect_uri: globalThis.location.origin,
    post_logout_redirect_uri: globalThis.location.origin,
    response_type: 'code',
    scope: 'openid profile email',
    userStore: new WebStorageStateStore({ store: globalThis.localStorage }),
    automaticSilentRenew: true,
});

