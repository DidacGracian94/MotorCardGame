import { createContext, ReactNode, useContext, useEffect, useMemo, useState } from 'react'
import { api, UserDto } from '@/api/client'
import {
  clearTokens,
  getRefreshToken,
  setAccessToken,
  setOnAuthExpired,
  setRefreshToken,
} from '@/auth/tokenStore'

type AuthStatus = 'loading' | 'authenticated' | 'anonymous'

interface AuthContextValue {
  user: UserDto | null
  status: AuthStatus
  register: (email: string, password: string, displayName: string) => Promise<void>
  login: (email: string, password: string) => Promise<void>
  loginWithGoogle: (idToken: string) => Promise<void>
  /**
   * El access token ya viene emitido por POST /api/rooms/{code}/join (ver RoomService en el
   * backend) — aquí solo se adopta esa sesión de invitado, sin llamar a ningún /api/auth/**. Sin
   * refresh token: si el access token caduca a media partida, la sesión de invitado no se puede
   * renovar (recargar la página obliga a unirse de nuevo).
   */
  loginAsGuest: (accessToken: string, guestId: string, displayName: string) => void
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserDto | null>(null)
  const [status, setStatus] = useState<AuthStatus>('loading')

  useEffect(() => {
    setOnAuthExpired(() => {
      setUser(null)
      setStatus('anonymous')
    })
    return () => setOnAuthExpired(null)
  }, [])

  // Al cargar la app, si hay un refresh token guardado se intenta canjear por un access token
  // nuevo — así una recarga de página no obliga a volver a loguearse mientras el refresh token
  // siga siendo válido.
  useEffect(() => {
    const storedRefreshToken = getRefreshToken()
    if (!storedRefreshToken) {
      setStatus('anonymous')
      return
    }
    api.auth
      .refresh(storedRefreshToken)
      .then((response) => {
        setAccessToken(response.accessToken)
        setRefreshToken(response.refreshToken)
        setUser(response.user)
        setStatus('authenticated')
      })
      .catch(() => {
        clearTokens()
        setStatus('anonymous')
      })
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      status,
      register: async (email, password, displayName) => {
        const response = await api.auth.register({ email, password, displayName })
        setAccessToken(response.accessToken)
        setRefreshToken(response.refreshToken)
        setUser(response.user)
        setStatus('authenticated')
      },
      login: async (email, password) => {
        const response = await api.auth.login({ email, password })
        setAccessToken(response.accessToken)
        setRefreshToken(response.refreshToken)
        setUser(response.user)
        setStatus('authenticated')
      },
      loginWithGoogle: async (idToken) => {
        const response = await api.auth.loginWithGoogle(idToken)
        setAccessToken(response.accessToken)
        setRefreshToken(response.refreshToken)
        setUser(response.user)
        setStatus('authenticated')
      },
      loginAsGuest: (accessToken, guestId, displayName) => {
        setAccessToken(accessToken)
        setUser({ id: guestId, email: '', displayName, role: 'USER' })
        setStatus('authenticated')
      },
      logout: async () => {
        const refreshToken = getRefreshToken()
        clearTokens()
        setUser(null)
        setStatus('anonymous')
        if (refreshToken) {
          await api.auth.logout(refreshToken).catch(() => {
            // el token ya se ha limpiado localmente; si el backend no llega a revocarlo, expirará solo
          })
        }
      },
    }),
    [user, status]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
