import { Fragment, useState } from 'react'
import { CapabilitiesDto } from '@/api/client'
import { useTranslation } from '@/i18n/LanguageContext'
import { defaultScalarValue } from '@/lib/configTransforms'
import { AttributesConfig, CardTemplateConfig, ScalarValue, ZoneConfig } from '@/types/config'
import { ScalarValueInput } from '@/components/editor/ScalarValueField'
import RulesEditor from '@/components/editor/RulesEditor'

interface Props {
  cardTemplates: CardTemplateConfig[]
  zones: ZoneConfig[]
  attributes: AttributesConfig
  catalogs: CapabilitiesDto
  playerActions: string[]
  onChange: (cardTemplates: CardTemplateConfig[]) => void
  readOnly?: boolean
}

export default function CardTemplatesEditor({
  cardTemplates,
  zones,
  attributes,
  catalogs,
  playerActions,
  onChange,
  readOnly = false,
}: Props) {
  const { t } = useTranslation()
  const sharedZones = zones.filter((zone) => zone.ownership === 'SHARED')
  const [expandedIndex, setExpandedIndex] = useState<number | null>(null)

  const updateCard = (index: number, updates: Partial<CardTemplateConfig>) => {
    onChange(cardTemplates.map((card, i) => (i === index ? { ...card, ...updates } : card)))
  }

  const removeCard = (index: number) => {
    onChange(cardTemplates.filter((_, i) => i !== index))
  }

  const addCard = () => {
    onChange([...cardTemplates, { id: '', zone: sharedZones[0]?.name ?? '', attributes: {} }])
  }

  return (
    <div className="overflow-x-auto bg-white rounded-lg shadow">
      <table className="min-w-full divide-y divide-slate-200">
        <thead className="bg-slate-50">
          <tr>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">{t('common.id')}</th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">{t('editor.zone')}</th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">{t('editor.count')}</th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">{t('editor.cardImage')}</th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">{t('editor.attributes')}</th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">{t('editor.cardRules')}</th>
            <th className="px-6 py-3 text-right text-sm font-semibold text-slate-900">{t('common.actions')}</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-200">
          {cardTemplates.map((card, index) => (
            <Fragment key={index}>
            <tr className="hover:bg-slate-50 transition-colors align-top">
              <td className="px-6 py-4 text-sm">
                <input
                  type="text"
                  value={card.id}
                  onChange={(e) => updateCard(index, { id: e.target.value })}
                  placeholder={t('editor.cardIdPlaceholder')}
                  disabled={readOnly}
                  className="w-full px-2 py-1 border border-slate-300 rounded text-sm disabled:bg-slate-50 disabled:text-slate-600"
                />
              </td>
              <td className="px-6 py-4 text-sm">
                <select
                  value={card.zone}
                  onChange={(e) => updateCard(index, { zone: e.target.value })}
                  disabled={readOnly}
                  className="px-2 py-1 border border-slate-300 rounded text-sm disabled:bg-slate-50 disabled:text-slate-600"
                >
                  <option value="">{t('editor.selectZonePlaceholder')}</option>
                  {sharedZones.map((zone) => (
                    <option key={zone.name} value={zone.name}>
                      {zone.name}
                    </option>
                  ))}
                </select>
              </td>
              <td className="px-6 py-4 text-sm">
                <input
                  type="number"
                  value={card.count ?? ''}
                  onChange={(e) =>
                    updateCard(index, {
                      count: e.target.value === '' ? undefined : Number(e.target.value),
                    })
                  }
                  disabled={readOnly}
                  className="w-20 px-2 py-1 border border-slate-300 rounded text-sm disabled:bg-slate-50 disabled:text-slate-600"
                />
              </td>
              <td className="px-6 py-4 text-sm">
                <div className="flex items-center gap-2">
                  {card.image && (
                    <img
                      src={card.image}
                      alt=""
                      className="w-8 h-11 object-cover rounded border border-slate-200 bg-slate-50 shrink-0"
                    />
                  )}
                  <input
                    type="text"
                    value={card.image ?? ''}
                    onChange={(e) => updateCard(index, { image: e.target.value || undefined })}
                    placeholder={t('editor.cardImagePlaceholder')}
                    disabled={readOnly}
                    className="w-40 px-2 py-1 border border-slate-300 rounded text-sm disabled:bg-slate-50 disabled:text-slate-600"
                  />
                </div>
              </td>
              <td className="px-6 py-4 text-sm">
                <CardAttributeAssignments
                  attributes={card.attributes}
                  dictionary={attributes}
                  onChange={(cardAttributes) => updateCard(index, { attributes: cardAttributes })}
                  readOnly={readOnly}
                />
              </td>
              <td className="px-6 py-4 text-sm">
                <button
                  onClick={() => setExpandedIndex(expandedIndex === index ? null : index)}
                  className="text-blue-600 hover:text-blue-700 font-medium text-sm whitespace-nowrap"
                >
                  {t('editor.cardRulesCount', { count: card.rules?.length ?? 0 })}
                </button>
              </td>
              <td className="px-6 py-4 text-right">
                {!readOnly && (
                  <button
                    onClick={() => removeCard(index)}
                    className="text-red-600 hover:text-red-700 font-medium text-sm"
                  >
                    {t('common.remove')}
                  </button>
                )}
              </td>
            </tr>
            {expandedIndex === index && (
              <tr className="bg-slate-50">
                <td colSpan={7} className="px-6 py-4">
                  <RulesEditor
                    rules={card.rules ?? []}
                    onChange={(rules) => updateCard(index, { rules: rules.length > 0 ? rules : undefined })}
                    catalogs={catalogs}
                    zones={zones}
                    attributes={attributes}
                    playerActions={playerActions}
                    readOnly={readOnly}
                  />
                </td>
              </tr>
            )}
            </Fragment>
          ))}
        </tbody>
      </table>
      {!readOnly && (
        <div className="p-4">
          <button
            onClick={addCard}
            className="text-blue-600 hover:text-blue-700 font-medium text-sm"
          >
            {t('editor.addCardTemplate')}
          </button>
        </div>
      )}
    </div>
  )
}

interface CardAttributeAssignmentsProps {
  attributes: Record<string, ScalarValue>
  dictionary: AttributesConfig
  onChange: (attributes: Record<string, ScalarValue>) => void
  readOnly?: boolean
}

function CardAttributeAssignments({ attributes, dictionary, onChange, readOnly = false }: CardAttributeAssignmentsProps) {
  const { t, tf } = useTranslation()
  const entries = Object.entries(attributes)
  const dictionaryNames = Object.keys(dictionary)

  const updateKey = (oldKey: string, newKey: string) => {
    const next: Record<string, ScalarValue> = {}
    for (const [key, value] of entries) {
      next[key === oldKey ? newKey : key] = key === oldKey ? defaultScalarValue(dictionary[newKey]) : value
    }
    onChange(next)
  }

  const updateValue = (key: string, value: ScalarValue) => {
    onChange({ ...attributes, [key]: value })
  }

  const removeAttribute = (key: string) => {
    const next = { ...attributes }
    delete next[key]
    onChange(next)
  }

  const addAttribute = () => {
    const name = dictionaryNames.find((candidate) => !(candidate in attributes)) ?? dictionaryNames[0]
    if (!name) return
    onChange({ ...attributes, [name]: defaultScalarValue(dictionary[name]) })
  }

  if (dictionaryNames.length === 0) {
    return <p className="text-xs text-slate-500">{t('editor.noAttributesDeclared')}</p>
  }

  return (
    <div className="space-y-2">
      {entries.map(([key, value]) => (
        <div key={key} className="flex gap-2 items-center">
          <select
            value={key}
            onChange={(e) => updateKey(key, e.target.value)}
            disabled={readOnly}
            className="w-32 px-2 py-1 border border-slate-300 rounded text-sm disabled:bg-slate-50 disabled:text-slate-600"
          >
            {!dictionaryNames.includes(key) && (
              <option key={key} value={key}>
                {tf(`attribute.${key}`, key)} ({t('editor.attributeUndeclaredOption')})
              </option>
            )}
            {dictionaryNames.map((name) => (
              <option key={name} value={name}>
                {tf(`attribute.${name}`, name)}
              </option>
            ))}
          </select>
          <ScalarValueInput
            type={dictionary[key]?.type ?? 'string'}
            options={dictionary[key]?.options}
            value={value}
            onChange={(v) => updateValue(key, v)}
            readOnly={readOnly}
          />
          {!readOnly && (
            <button
              onClick={() => removeAttribute(key)}
              className="text-red-600 hover:text-red-700 text-sm"
            >
              ×
            </button>
          )}
        </div>
      ))}
      {!readOnly && (
        <button
          onClick={addAttribute}
          className="text-blue-600 hover:text-blue-700 font-medium text-xs"
        >
          {t('editor.assignAttribute')}
        </button>
      )}
    </div>
  )
}
