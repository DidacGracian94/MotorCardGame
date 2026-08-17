import { useTranslation } from '@/i18n/LanguageContext'
import { Language } from '@/i18n/translations'

const LANGUAGES: { value: Language; label: string }[] = [
  { value: 'es', label: 'Español' },
  { value: 'en', label: 'English' },
]

export default function LanguageSwitcher() {
  const { language, setLanguage } = useTranslation()

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
