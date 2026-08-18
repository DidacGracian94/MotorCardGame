import { useTranslation } from '@/i18n/LanguageContext'
import { ScalarType, ScalarValue } from '@/types/config'

interface TypedProps {
  type: ScalarType
  value: ScalarValue
  onChange: (value: ScalarValue) => void
  options?: string[]
  readOnly?: boolean
}

export function ScalarValueInput({ type, value, onChange, options, readOnly = false }: TypedProps) {
  if (type === 'number') {
    return (
      <input
        type="number"
        value={(value as number | undefined) ?? 0}
        onChange={(e) => onChange(Number(e.target.value))}
        disabled={readOnly}
        className="flex-1 px-2 py-1 border border-slate-300 rounded text-sm disabled:bg-slate-50 disabled:text-slate-600"
      />
    )
  }
  if (type === 'boolean') {
    return (
      <input
        type="checkbox"
        checked={(value as boolean | undefined) ?? false}
        onChange={(e) => onChange(e.target.checked)}
        disabled={readOnly}
        className="h-4 w-4"
      />
    )
  }
  if (options && options.length > 0) {
    const current = (value as string | undefined) ?? ''
    return (
      <select
        value={current}
        onChange={(e) => onChange(e.target.value)}
        disabled={readOnly}
        className="flex-1 px-2 py-1 border border-slate-300 rounded text-sm bg-white disabled:bg-slate-50 disabled:text-slate-600"
      >
        {!options.includes(current) && <option value={current}>{current || '—'}</option>}
        {options.map((option) => (
          <option key={option} value={option}>
            {option}
          </option>
        ))}
      </select>
    )
  }
  return (
    <input
      type="text"
      value={(value as string | undefined) ?? ''}
      onChange={(e) => onChange(e.target.value)}
      disabled={readOnly}
      className="flex-1 px-2 py-1 border border-slate-300 rounded text-sm disabled:bg-slate-50 disabled:text-slate-600"
    />
  )
}

interface Props {
  value: ScalarValue
  onChange: (value: ScalarValue) => void
  // Cuando se conoce el atributo referenciado (p.ej. el "equals" de CARD_ATTRIBUTE_EQUALS junto a
  // su campo "attribute"), se fuerza su tipo/opciones y se oculta el selector de tipo — ya no hace
  // falta elegirlo a mano ni se puede escribir un valor que no encaje.
  type?: ScalarType
  options?: string[]
  readOnly?: boolean
}

function typeOf(value: ScalarValue): ScalarType {
  if (typeof value === 'boolean') return 'boolean'
  if (typeof value === 'number') return 'number'
  return 'string'
}

export default function ScalarValueField({ value, onChange, type: forcedType, options, readOnly = false }: Props) {
  const { t } = useTranslation()
  const type = forcedType ?? typeOf(value)

  const handleTypeChange = (newType: ScalarType) => {
    if (newType === 'string') onChange('')
    else if (newType === 'number') onChange(0)
    else onChange(false)
  }

  return (
    <div className="flex gap-2 items-center">
      {!forcedType && (
        <select
          value={type}
          onChange={(e) => handleTypeChange(e.target.value as ScalarType)}
          disabled={readOnly}
          className="px-2 py-1 border border-slate-300 rounded text-sm disabled:bg-slate-50 disabled:text-slate-600"
        >
          <option value="string">{t('common.scalarType.text')}</option>
          <option value="number">{t('common.scalarType.number')}</option>
          <option value="boolean">{t('common.scalarType.boolean')}</option>
        </select>
      )}
      <ScalarValueInput type={type} value={value} onChange={onChange} options={options} readOnly={readOnly} />
    </div>
  )
}
