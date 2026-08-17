import { useTranslation } from '@/i18n/LanguageContext'

interface Props {
  value: string
  onChange: (value: string) => void
  onApply: () => void
  error?: string
}

export default function JsonConfigTab({ value, onChange, onApply, error }: Props) {
  const { t } = useTranslation()

  return (
    <div className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-slate-900 mb-1">
          {t('editor.configJsonLabel')}
        </label>
        <textarea
          value={value}
          onChange={(e) => onChange(e.target.value)}
          rows={24}
          className="w-full px-3 py-2 border border-slate-300 rounded-lg font-mono text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        {error && <p className="mt-1 text-sm text-red-600">{error}</p>}
      </div>
      <button
        type="button"
        onClick={onApply}
        className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
      >
        {t('editor.applyJsonButton')}
      </button>
    </div>
  )
}
