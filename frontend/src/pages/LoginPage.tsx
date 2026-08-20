import { useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link, useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { useAuth } from '@/auth/AuthContext'
import { useTranslation } from '@/i18n/LanguageContext'
import GoogleLoginButton from '@/components/GoogleLoginButton'

export default function LoginPage() {
  const { t } = useTranslation()
  const { login } = useAuth()
  const navigate = useNavigate()
  const [submitError, setSubmitError] = useState<string | null>(null)

  const schema = useMemo(
    () =>
      z.object({
        email: z.string().min(1, t('auth.errors.emailInvalid')).email(t('auth.errors.emailInvalid')),
        password: z.string().min(1, t('auth.errors.passwordRequired')),
      }),
    [t]
  )

  type FormData = z.infer<typeof schema>

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  const onSubmit = async (data: FormData) => {
    setSubmitError(null)
    try {
      await login(data.email, data.password)
      navigate('/')
    } catch (err) {
      setSubmitError((err as Error).message)
    }
  }

  return (
    <div className="max-w-sm mx-auto py-16 px-4">
      <h1 className="text-2xl font-bold text-slate-900 mb-6">{t('auth.loginTitle')}</h1>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-slate-900 mb-1">
            {t('auth.emailLabel')}
          </label>
          <input
            {...register('email')}
            type="email"
            autoComplete="email"
            className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          {errors.email && <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>}
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-900 mb-1">
            {t('auth.passwordLabel')}
          </label>
          <input
            {...register('password')}
            type="password"
            autoComplete="current-password"
            className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          {errors.password && <p className="mt-1 text-sm text-red-600">{errors.password.message}</p>}
        </div>

        {submitError && <p className="text-sm text-red-600">{submitError}</p>}

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {isSubmitting ? t('auth.loggingIn') : t('auth.loginButton')}
        </button>
      </form>

      <div className="flex items-center gap-3 my-6">
        <div className="h-px bg-slate-200 flex-1" />
        <span className="text-xs text-slate-400 uppercase">{t('auth.orDivider')}</span>
        <div className="h-px bg-slate-200 flex-1" />
      </div>

      <GoogleLoginButton onError={setSubmitError} />

      <p className="mt-6 text-sm text-slate-600">
        {t('auth.noAccountYet')}{' '}
        <Link to="/register" className="text-blue-600 hover:underline">
          {t('auth.goToRegister')}
        </Link>
      </p>
    </div>
  )
}
