import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '@/auth/AuthContext'
import { useTranslation } from '@/i18n/LanguageContext'
import { Language } from '@/i18n/translations'

const LANGUAGES: { value: Language; label: string }[] = [
  { value: 'es', label: 'Español' },
  { value: 'en', label: 'English' },
]

/**
 * Perfil arriba a la derecha: sin sesión, es solo el selector de idioma (visible también en
 * login/registro); con sesión, un desplegable que agrupa idioma + admin (si aplica) + logout, en
 * vez de repartir estos controles por distintas esquinas de la pantalla.
 */
export default function ProfileMenu() {
  const { user, logout } = useAuth()
  const { language, setLanguage, t } = useTranslation()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)
  const containerRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) return
    const handleClickOutside = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [open])

  if (!user) {
    return (
      <select
        value={language}
        onChange={(e) => setLanguage(e.target.value as Language)}
        aria-label="Language / Idioma"
        className="fixed top-4 right-4 z-50 px-3 py-1.5 bg-white border border-slate-300 rounded-lg shadow-sm text-sm font-medium text-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
      >
        {LANGUAGES.map((lang) => (
          <option key={lang.value} value={lang.value}>
            {lang.label}
          </option>
        ))}
      </select>
    )
  }

  const initial = user.displayName.charAt(0).toUpperCase()

  return (
    <div ref={containerRef} className="fixed top-4 right-4 z-50">
      <button
        onClick={() => setOpen((o) => !o)}
        className="flex items-center gap-2 px-2 py-1.5 pr-3 bg-white border border-slate-300 rounded-lg shadow-sm text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors"
      >
        <span className="flex items-center justify-center w-6 h-6 rounded-full bg-blue-600 text-white text-xs font-semibold">
          {initial}
        </span>
        {user.displayName}
      </button>

      {open && (
        <div className="absolute right-0 mt-2 w-56 bg-white border border-slate-200 rounded-lg shadow-lg py-2 text-sm">
          <div className="px-3 pb-2">
            <p className="text-xs text-slate-400 uppercase tracking-wide mb-1.5">{t('profile.language')}</p>
            <div className="flex gap-2">
              {LANGUAGES.map((lang) => (
                <button
                  key={lang.value}
                  onClick={() => setLanguage(lang.value)}
                  className={`px-2 py-1 rounded-md border text-xs transition-colors ${
                    language === lang.value
                      ? 'border-blue-600 text-blue-600 bg-blue-50'
                      : 'border-slate-300 text-slate-600 hover:bg-slate-50'
                  }`}
                >
                  {lang.label}
                </button>
              ))}
            </div>
          </div>

          {user.role === 'ADMIN' && (
            <>
              <div className="border-t border-slate-100 my-1" />
              <Link
                to="/admin/users"
                onClick={() => setOpen(false)}
                className="block px-3 py-2 text-slate-700 hover:bg-slate-50"
              >
                {t('admin.link')}
              </Link>
            </>
          )}

          <div className="border-t border-slate-100 my-1" />
          <button
            onClick={() => {
              setOpen(false)
              logout().finally(() => navigate('/login'))
            }}
            className="w-full text-left px-3 py-2 text-red-600 hover:bg-red-50"
          >
            {t('auth.logout')}
          </button>
        </div>
      )}
    </div>
  )
}
