import { useState } from 'react'
import { useTranslation } from '@/i18n/LanguageContext'
import { AttributeDefinition, AttributesConfig, ScalarType } from '@/types/config'

interface Props {
  attributes: AttributesConfig
  onChange: (attributes: AttributesConfig) => void
  readOnly?: boolean
}

export default function AttributesEditor({ attributes, onChange, readOnly = false }: Props) {
  const { t } = useTranslation()
  const entries = Object.entries(attributes)

  const renameAttribute = (oldName: string, newName: string) => {
    const next: AttributesConfig = {}
    for (const [name, definition] of entries) {
      next[name === oldName ? newName : name] = definition
    }
    onChange(next)
  }

  const updateDefinition = (name: string, definition: AttributeDefinition) => {
    onChange({ ...attributes, [name]: definition })
  }

  const updateType = (name: string, type: ScalarType) => {
    updateDefinition(name, { type })
  }

  const removeAttribute = (name: string) => {
    const next = { ...attributes }
    delete next[name]
    onChange(next)
  }

  const addAttribute = () => {
    // Nace sin nombre (el input queda vacío, mostrando el placeholder "p. ej. color") en vez de
    // con un texto real que haya que borrar antes de escribir el nombre de verdad. Solo puede
    // haber un atributo sin nombrar a la vez — nómbralo antes de añadir el siguiente.
    if ('' in attributes) return
    onChange({ ...attributes, '': { type: 'string' } })
  }

  return (
    <div className="overflow-x-auto bg-white rounded-lg shadow">
      <table className="min-w-full divide-y divide-slate-200">
        <thead className="bg-slate-50">
          <tr>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">{t('common.name')}</th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">
              {t('editor.attributeType')}
            </th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">
              {t('editor.attributeOptions')}
            </th>
            <th className="px-6 py-3 text-right text-sm font-semibold text-slate-900">{t('common.actions')}</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-200">
          {entries.map(([name, definition], index) => (
            <tr key={index} className="hover:bg-slate-50 transition-colors align-top">
              <td className="px-6 py-4 text-sm">
                <input
                  type="text"
                  value={name}
                  onChange={(e) => renameAttribute(name, e.target.value)}
                  placeholder={t('editor.attributeNamePlaceholder')}
                  disabled={readOnly}
                  className="w-full px-2 py-1 border border-slate-300 rounded text-sm disabled:bg-slate-50 disabled:text-slate-600"
                />
              </td>
              <td className="px-6 py-4 text-sm">
                <select
                  value={definition.type}
                  onChange={(e) => updateType(name, e.target.value as ScalarType)}
                  disabled={readOnly}
                  className="px-2 py-1 border border-slate-300 rounded text-sm disabled:bg-slate-50 disabled:text-slate-600"
                >
                  <option value="string">{t('common.scalarType.text')}</option>
                  <option value="number">{t('common.scalarType.number')}</option>
                  <option value="boolean">{t('common.scalarType.boolean')}</option>
                </select>
              </td>
              <td className="px-6 py-4 text-sm">
                {definition.type === 'string' ? (
                  <AttributeOptionsEditor
                    options={definition.options ?? []}
                    onChange={(options) => updateDefinition(name, { ...definition, options })}
                    readOnly={readOnly}
                  />
                ) : (
                  <span className="text-xs text-slate-400">—</span>
                )}
              </td>
              <td className="px-6 py-4 text-right">
                {!readOnly && (
                  <button
                    onClick={() => removeAttribute(name)}
                    className="text-red-600 hover:text-red-700 font-medium text-sm"
                  >
                    {t('common.remove')}
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {!readOnly && (
        <div className="p-4">
          <button onClick={addAttribute} className="text-blue-600 hover:text-blue-700 font-medium text-sm">
            {t('editor.addAttribute')}
          </button>
        </div>
      )}
    </div>
  )
}

interface AttributeOptionsEditorProps {
  options: string[]
  onChange: (options: string[] | undefined) => void
  readOnly?: boolean
}

// Lista cerrada de valores permitidos para un atributo de texto (p.ej. color -> RED/BLUE/GREEN).
// Sin opciones declaradas, el atributo sigue siendo texto libre en el resto del editor — esto es
// opt-in, no obligatorio, para no forzar una lista en atributos que de verdad son texto abierto
// (p.ej. un nombre de carta).
function AttributeOptionsEditor({ options, onChange, readOnly = false }: AttributeOptionsEditorProps) {
  const { t } = useTranslation()
  const [draft, setDraft] = useState('')

  const commitDraft = () => {
    const value = draft.trim()
    if (value && !options.includes(value)) {
      onChange([...options, value])
    }
    setDraft('')
  }

  const removeOption = (index: number) => {
    const next = options.filter((_, i) => i !== index)
    onChange(next.length > 0 ? next : undefined)
  }

  return (
    <div className="flex flex-wrap gap-1 items-center max-w-xs">
      {options.map((option, index) => (
        <span
          key={index}
          className="inline-flex items-center gap-1 pl-2 pr-1 py-0.5 bg-slate-100 rounded text-xs"
        >
          {option}
          {!readOnly && (
            <button
              onClick={() => removeOption(index)}
              className="text-slate-500 hover:text-red-600"
              aria-label={t('common.remove')}
            >
              ×
            </button>
          )}
        </span>
      ))}
      {!readOnly && (
        <input
          type="text"
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              e.preventDefault()
              commitDraft()
            }
          }}
          onBlur={commitDraft}
          placeholder={t('editor.addOptionPlaceholder')}
          className="w-24 px-2 py-0.5 border border-slate-300 rounded text-xs"
        />
      )}
    </div>
  )
}
