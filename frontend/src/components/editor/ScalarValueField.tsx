import { useTranslation } from '@/i18n/LanguageContext'
import { ScalarValue } from '@/types/config'

type ScalarType = 'string' | 'number' | 'boolean'

interface Props {
  value: ScalarValue
  onChange: (value: ScalarValue) => void
}

function typeOf(value: ScalarValue): ScalarType {
  if (typeof value === 'boolean') return 'boolean'
  if (typeof value === 'number') return 'number'
  return 'string'
}

export default function ScalarValueField({ value, onChange }: Props) {
  const { t } = useTranslation()
  const type = typeOf(value)

  const handleTypeChange = (newType: ScalarType) => {
    if (newType === 'string') onChange('')
    else if (newType === 'number') onChange(0)
    else onChange(false)
  }

  return (
    <div className="flex gap-2 items-center">
      <select
        value={type}
        onChange={(e) => handleTypeChange(e.target.value as ScalarType)}
        className="px-2 py-1 border border-slate-300 rounded text-sm"
      >
        <option value="string">{t('common.scalarType.text')}</option>
        <option value="number">{t('common.scalarType.number')}</option>
        <option value="boolean">{t('common.scalarType.boolean')}</option>
      </select>
      {type === 'string' && (
        <input
          type="text"
          value={value as string}
          onChange={(e) => onChange(e.target.value)}
          className="flex-1 px-2 py-1 border border-slate-300 rounded text-sm"
        />
      )}
      {type === 'number' && (
        <input
          type="number"
          value={value as number}
          onChange={(e) => onChange(Number(e.target.value))}
          className="flex-1 px-2 py-1 border border-slate-300 rounded text-sm"
        />
      )}
      {type === 'boolean' && (
        <input
          type="checkbox"
          checked={value as boolean}
          onChange={(e) => onChange(e.target.checked)}
          className="h-4 w-4"
        />
      )}
    </div>
  )
}
