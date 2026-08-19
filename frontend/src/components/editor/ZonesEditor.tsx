import { useTranslation } from '@/i18n/LanguageContext'
import { Ownership, ZoneConfig, ZoneVisibility } from '@/types/config'

interface Props {
  zones: ZoneConfig[]
  onChange: (zones: ZoneConfig[]) => void
  readOnly?: boolean
}

// OWNER_ONLY/ALL_BUT_OWNER solo tienen sentido con ownership PER_PLAYER (el motor los rechaza en
// una zona SHARED) — cada ownership ofrece solo las opciones que el motor aceptará al guardar.
const VISIBILITY_OPTIONS_BY_OWNERSHIP: Record<Ownership, ZoneVisibility[]> = {
  SHARED: ['PUBLIC', 'HIDDEN'],
  PER_PLAYER: ['OWNER_ONLY', 'HIDDEN', 'ALL_BUT_OWNER', 'PUBLIC'],
}

export default function ZonesEditor({ zones, onChange, readOnly = false }: Props) {
  const { t, tf } = useTranslation()
  const updateZone = (index: number, updates: Partial<ZoneConfig>) => {
    onChange(zones.map((zone, i) => (i === index ? { ...zone, ...updates } : zone)))
  }

  const updateOwnership = (index: number, ownership: Ownership) => {
    onChange(
      zones.map((zone, i) => {
        if (i !== index) return zone
        // Si la visibilidad actual ya no es válida para el nuevo ownership, vuelve al valor por
        // defecto del motor (undefined) en vez de dejar una combinación que el motor rechazaría.
        const visibilityStillValid =
          !zone.visibility || VISIBILITY_OPTIONS_BY_OWNERSHIP[ownership].includes(zone.visibility)
        return { ...zone, ownership, visibility: visibilityStillValid ? zone.visibility : undefined }
      })
    )
  }

  const removeZone = (index: number) => {
    onChange(zones.filter((_, i) => i !== index))
  }

  const addZone = () => {
    onChange([...zones, { name: '', ownership: 'SHARED', shuffle: false }])
  }

  return (
    <div className="overflow-x-auto bg-white rounded-lg shadow">
      <table className="min-w-full divide-y divide-slate-200">
        <thead className="bg-slate-50">
          <tr>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">{t('common.name')}</th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">{t('editor.ownership')}</th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">{t('editor.visibility')}</th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">{t('editor.shuffle')}</th>
            <th className="px-6 py-3 text-right text-sm font-semibold text-slate-900">{t('common.actions')}</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-200">
          {zones.map((zone, index) => (
            <tr key={index} className="hover:bg-slate-50 transition-colors">
              <td className="px-6 py-4 text-sm">
                <input
                  type="text"
                  value={zone.name}
                  onChange={(e) => updateZone(index, { name: e.target.value })}
                  placeholder={t('editor.zoneNamePlaceholder')}
                  disabled={readOnly}
                  className="w-full px-2 py-1 border border-slate-300 rounded text-sm disabled:bg-slate-50 disabled:text-slate-600"
                />
              </td>
              <td className="px-6 py-4 text-sm">
                <select
                  value={zone.ownership}
                  onChange={(e) => updateOwnership(index, e.target.value as Ownership)}
                  disabled={readOnly}
                  className="px-2 py-1 border border-slate-300 rounded text-sm disabled:bg-slate-50 disabled:text-slate-600"
                >
                  <option value="SHARED">{tf('enum.SHARED', 'SHARED')}</option>
                  <option value="PER_PLAYER">{tf('enum.PER_PLAYER', 'PER_PLAYER')}</option>
                </select>
              </td>
              <td className="px-6 py-4 text-sm">
                <select
                  value={zone.visibility ?? ''}
                  onChange={(e) =>
                    updateZone(index, {
                      visibility: e.target.value === '' ? undefined : (e.target.value as ZoneVisibility),
                    })
                  }
                  disabled={readOnly}
                  className="px-2 py-1 border border-slate-300 rounded text-sm disabled:bg-slate-50 disabled:text-slate-600"
                >
                  <option value="">{t('editor.visibilityDefault')}</option>
                  {VISIBILITY_OPTIONS_BY_OWNERSHIP[zone.ownership].map((option) => (
                    <option key={option} value={option}>
                      {tf(`enum.${option}`, option)}
                    </option>
                  ))}
                </select>
              </td>
              <td className="px-6 py-4 text-sm">
                <input
                  type="checkbox"
                  checked={zone.shuffle ?? false}
                  onChange={(e) => updateZone(index, { shuffle: e.target.checked })}
                  disabled={readOnly}
                  className="h-4 w-4"
                />
              </td>
              <td className="px-6 py-4 text-right">
                {!readOnly && (
                  <button
                    onClick={() => removeZone(index)}
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
          <button
            onClick={addZone}
            className="text-blue-600 hover:text-blue-700 font-medium text-sm"
          >
            {t('editor.addZone')}
          </button>
        </div>
      )}
    </div>
  )
}
