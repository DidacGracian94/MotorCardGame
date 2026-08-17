import { CapabilitiesDto } from '@/api/client'
import { useTranslation } from '@/i18n/LanguageContext'
import { createDefaultNode } from '@/lib/configTransforms'
import { RuleConfig, ZoneConfig } from '@/types/config'
import RuleBuilder from '@/components/editor/RuleBuilder'

interface Props {
  rules: RuleConfig[]
  onChange: (rules: RuleConfig[]) => void
  catalogs: CapabilitiesDto
  zones: ZoneConfig[]
}

export default function RulesEditor({ rules, onChange, catalogs, zones }: Props) {
  const { t } = useTranslation()

  const updateAt = (index: number, rule: RuleConfig) => {
    onChange(rules.map((r, i) => (i === index ? rule : r)))
  }

  const removeAt = (index: number) => {
    onChange(rules.filter((_, i) => i !== index))
  }

  const addRule = () => {
    const condition = catalogs.conditions.find((c) => c.name === 'ZONE_IS_EMPTY')
    const target = catalogs.targets.find((t) => t.name === 'CURRENT_PLAYER')
    const action = catalogs.actions.find((a) => a.name === 'NEXT_PLAYER')
    if (!condition || !target || !action) return
    onChange([
      ...rules,
      {
        event: '',
        condition: createDefaultNode(condition, zones, catalogs),
        target: createDefaultNode(target, zones, catalogs),
        action: createDefaultNode(action, zones, catalogs),
      },
    ])
  }

  return (
    <div className="space-y-4">
      {rules.map((rule, index) => (
        <RuleBuilder
          key={index}
          rule={rule}
          onChange={(r) => updateAt(index, r)}
          onRemove={() => removeAt(index)}
          catalogs={catalogs}
          zones={zones}
        />
      ))}
      <button
        onClick={addRule}
        className="px-4 py-2 text-blue-600 hover:text-blue-700 font-medium text-sm"
      >
        {t('editor.addRule')}
      </button>
    </div>
  )
}
