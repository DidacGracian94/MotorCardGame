import { useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/auth/AuthContext'
import { useTranslation } from '@/i18n/LanguageContext'

const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID

// A nivel de módulo (no de componente) porque Google Identity Services solo debe inicializarse
// una vez por página: React StrictMode ejecuta los efectos dos veces en desarrollo, y llamar a
// `initialize()` más de una vez deja el SDK de Google en un estado inconsistente (el botón queda
// roto — logueaba pero se quedaba sin resolver la sesión).
let googleIdentityInitialized = false

interface Props {
  onError?: (message: string) => void
}

/**
 * Botón de Google Identity Services (script cargado en index.html). Se usa la API vanilla de
 * Google directamente en vez de una librería npm — coherente con mantener pocas dependencias
 * externas en este proyecto. Sondea `window.google` un rato porque el script puede tardar en
 * cargar respecto al montaje de React.
 */
export default function GoogleLoginButton({ onError }: Props) {
  const { loginWithGoogle } = useAuth()
  const { t } = useTranslation()
  const navigate = useNavigate()
  const containerRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!GOOGLE_CLIENT_ID || !containerRef.current) return

    let cancelled = false
    let attempts = 0

    const tryRender = () => {
      if (cancelled) return
      if (window.google?.accounts?.id && containerRef.current) {
        if (!googleIdentityInitialized) {
          window.google.accounts.id.initialize({
            client_id: GOOGLE_CLIENT_ID,
            callback: (response) => {
              loginWithGoogle(response.credential)
                .then(() => navigate('/'))
                .catch((err) => onError?.((err as Error).message))
            },
          })
          googleIdentityInitialized = true
        }
        window.google.accounts.id.renderButton(containerRef.current, {
          theme: 'outline',
          size: 'large',
          width: 320,
        })
        return
      }
      attempts += 1
      if (attempts < 20) setTimeout(tryRender, 150)
    }

    tryRender()
    return () => {
      cancelled = true
    }
    // loginWithGoogle/navigate/onError solo importan en la primera inicialización real (ver
    // googleIdentityInitialized arriba) — no relanzar el sondeo en cada cambio de referencia.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  if (!GOOGLE_CLIENT_ID) {
    return <p className="text-xs text-slate-400">{t('auth.googleNotConfigured')}</p>
  }

  return <div ref={containerRef} />
}
