import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/auth/AuthContext'
import { useTranslation } from '@/i18n/LanguageContext'

export default function UserMenu() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const { t } = useTranslation()

  if (!user) return null

  return (
    <div className="fixed top-4 left-4 z-50 flex items-center gap-3 px-3 py-1.5 bg-white border border-slate-300 rounded-lg shadow-sm text-sm">
      <span className="font-medium text-slate-700">{user.displayName}</span>
      <button
        onClick={() => {
          logout().finally(() => navigate('/login'))
        }}
        className="text-blue-600 hover:underline"
      >
        {t('auth.logout')}
      </button>
    </div>
  )
}
