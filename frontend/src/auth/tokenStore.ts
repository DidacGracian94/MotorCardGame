const REFRESH_TOKEN_KEY = 'motorcardgame-refresh-token'

// El access token vive solo en memoria (no en localStorage): es de vida corta y se renueva desde
// el refresh token al cargar la app. api/client.ts lo necesita fuera de React (es una función
// pura, no un hook), de ahí este módulo aparte en vez de guardarlo en el contexto.
let accessToken: string | null = null
let onAuthExpired: (() => void) | null = null

export function getAccessToken(): string | null {
  return accessToken
}

export function setAccessToken(token: string | null): void {
  accessToken = token
}

export function getRefreshToken(): string | null {
  return window.localStorage.getItem(REFRESH_TOKEN_KEY)
}

export function setRefreshToken(token: string | null): void {
  if (token) {
    window.localStorage.setItem(REFRESH_TOKEN_KEY, token)
  } else {
    window.localStorage.removeItem(REFRESH_TOKEN_KEY)
  }
}

export function clearTokens(): void {
  setAccessToken(null)
  setRefreshToken(null)
}

/** AuthContext se suscribe aquí para reaccionar (logout + redirect) cuando apiCall no puede renovar la sesión. */
export function setOnAuthExpired(callback: (() => void) | null): void {
  onAuthExpired = callback
}

export function notifyAuthExpired(): void {
  onAuthExpired?.()
}
