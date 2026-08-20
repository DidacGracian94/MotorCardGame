import { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '@/auth/AuthContext'
import { useTranslation } from '@/i18n/LanguageContext'

export default function ProtectedRoute({ children }: { children: ReactNode }) {
  const { status } = useAuth()
  const { t } = useTranslation()

  if (status === 'loading') {
    return <div className="max-w-5xl mx-auto py-8 px-4 text-slate-600">{t('common.loading')}</div>
  }

  if (status === 'anonymous') {
    return <Navigate to="/login" replace />
  }

  return <>{children}</>
}
