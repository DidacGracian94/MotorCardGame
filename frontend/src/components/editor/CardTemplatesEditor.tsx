import { useTranslation } from '@/i18n/LanguageContext'
import { CardTemplateConfig, ScalarValue, ZoneConfig } from '@/types/config'
import ScalarValueField from '@/components/editor/ScalarValueField'

interface Props {
  cardTemplates: CardTemplateConfig[]
  zones: ZoneConfig[]
  onChange: (cardTemplates: CardTemplateConfig[]) => void
}

export default function CardTemplatesEditor({ cardTemplates, zones, onChange }: Props) {
  const { t } = useTranslation()
  const sharedZones = zones.filter((zone) => zone.ownership === 'SHARED')

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
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">{t('editor.attributes')}</th>
            <th className="px-6 py-3 text-right text-sm font-semibold text-slate-900">{t('common.actions')}</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-200">
          {cardTemplates.map((card, index) => (
            <tr key={index} className="hover:bg-slate-50 transition-colors align-top">
              <td className="px-6 py-4 text-sm">
                <input
                  type="text"
                  value={card.id}
                  onChange={(e) => updateCard(index, { id: e.target.value })}
                  className="w-full px-2 py-1 border border-slate-300 rounded text-sm"
                />
              </td>
              <td className="px-6 py-4 text-sm">
                <select
                  value={card.zone}
                  onChange={(e) => updateCard(index, { zone: e.target.value })}
                  className="px-2 py-1 border border-slate-300 rounded text-sm"
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
                  className="w-20 px-2 py-1 border border-slate-300 rounded text-sm"
                />
              </td>
              <td className="px-6 py-4 text-sm">
                <AttributesEditor
                  attributes={card.attributes}
                  onChange={(attributes) => updateCard(index, { attributes })}
                />
              </td>
              <td className="px-6 py-4 text-right">
                <button
                  onClick={() => removeCard(index)}
                  className="text-red-600 hover:text-red-700 font-medium text-sm"
                >
                  {t('common.remove')}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className="p-4">
        <button
          onClick={addCard}
          className="text-blue-600 hover:text-blue-700 font-medium text-sm"
        >
          {t('editor.addCardTemplate')}
        </button>
      </div>
    </div>
  )
}

interface AttributesEditorProps {
  attributes: Record<string, ScalarValue>
  onChange: (attributes: Record<string, ScalarValue>) => void
}

function AttributesEditor({ attributes, onChange }: AttributesEditorProps) {
  const { t } = useTranslation()
  const entries = Object.entries(attributes)

  const updateKey = (oldKey: string, newKey: string) => {
    const next: Record<string, ScalarValue> = {}
    for (const [key, value] of entries) {
      next[key === oldKey ? newKey : key] = value
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
    let key = 'attribute'
    let suffix = 1
    while (key in attributes) {
      key = `attribute${suffix}`
      suffix += 1
    }
    onChange({ ...attributes, [key]: '' })
  }

  return (
    <div className="space-y-2">
      {entries.map(([key, value]) => (
        <div key={key} className="flex gap-2 items-center">
          <input
            type="text"
            value={key}
            onChange={(e) => updateKey(key, e.target.value)}
            className="w-28 px-2 py-1 border border-slate-300 rounded text-sm"
          />
          <ScalarValueField value={value} onChange={(v) => updateValue(key, v)} />
          <button
            onClick={() => removeAttribute(key)}
            className="text-red-600 hover:text-red-700 text-sm"
          >
            ×
          </button>
        </div>
      ))}
      <button
        onClick={addAttribute}
        className="text-blue-600 hover:text-blue-700 font-medium text-xs"
      >
        {t('editor.addAttribute')}
      </button>
    </div>
  )
}
