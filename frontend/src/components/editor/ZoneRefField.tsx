import { useTranslation } from '@/i18n/LanguageContext'
import { ZoneConfig, ZoneRefValue } from '@/types/config'

interface Props {
  value: ZoneRefValue
  onChange: (value: ZoneRefValue) => void
  zones: ZoneConfig[]
}

export default function ZoneRefField({ value, onChange, zones }: Props) {
  const { t, tf } = useTranslation()

  return (
    <select
      value={value.name}
      onChange={(e) => {
        const zone = zones.find((z) => z.name === e.target.value)
        if (zone) {
          onChange({ name: zone.name, ownership: zone.ownership })
        }
      }}
      className="px-2 py-1 border border-slate-300 rounded text-sm"
    >
      <option value="">{t('editor.selectZonePlaceholder')}</option>
      {zones.map((zone) => (
        <option key={zone.name} value={zone.name}>
          {zone.name} ({tf(`enum.${zone.ownership}`, zone.ownership)})
        </option>
      ))}
    </select>
  )
}
