import { createContext, ReactNode, useContext, useEffect, useMemo, useState } from 'react'
import { Language, translations } from '@/i18n/translations'

const STORAGE_KEY = 'motorcardgame-lang'
const DEFAULT_LANGUAGE: Language = 'es'

function isLanguage(value: string | null): value is Language {
  return value === 'en' || value === 'es'
}

function detectInitialLanguage(): Language {
  if (typeof window === 'undefined') return DEFAULT_LANGUAGE
  const stored = window.localStorage.getItem(STORAGE_KEY)
  return isLanguage(stored) ? stored : DEFAULT_LANGUAGE
}

interface LanguageContextValue {
  language: Language
  setLanguage: (language: Language) => void
  t: (key: string, vars?: Record<string, string | number>) => string
  /**
   * Like `t`, but falls back to `fallback` (not the raw dotted key) when no translation exists.
   * Used for technical identifiers (capability names, field names, enum values) so a value the
   * dictionaries don't know about yet still renders as itself instead of as a missing-key string.
   */
  tf: (key: string, fallback: string, vars?: Record<string, string | number>) => string
}

const LanguageContext = createContext<LanguageContextValue | undefined>(undefined)

export function LanguageProvider({ children }: { children: ReactNode }) {
  const [language, setLanguage] = useState<Language>(detectInitialLanguage)

  useEffect(() => {
    window.localStorage.setItem(STORAGE_KEY, language)
    document.documentElement.lang = language
  }, [language])

  const value = useMemo<LanguageContextValue>(() => {
    const dict = translations[language]
    const fallbackDict = translations[DEFAULT_LANGUAGE]

    const interpolate = (text: string, vars?: Record<string, string | number>) => {
      if (!vars) return text
      let result = text
      for (const [name, varValue] of Object.entries(vars)) {
        result = result.split(`{{${name}}}`).join(String(varValue))
      }
      return result
    }

    const t = (key: string, vars?: Record<string, string | number>) =>
      interpolate(dict[key] ?? fallbackDict[key] ?? key, vars)

    const tf = (key: string, fallback: string, vars?: Record<string, string | number>) =>
      interpolate(dict[key] ?? fallbackDict[key] ?? fallback, vars)

    return { language, setLanguage, t, tf }
  }, [language])

  return <LanguageContext.Provider value={value}>{children}</LanguageContext.Provider>
}

export function useTranslation() {
  const context = useContext(LanguageContext)
  if (!context) {
    throw new Error('useTranslation must be used within a LanguageProvider')
  }
  return context
}
