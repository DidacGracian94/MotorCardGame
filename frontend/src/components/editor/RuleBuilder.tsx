import { CapabilitiesDto } from '@/api/client'
import { useTranslation } from '@/i18n/LanguageContext'
import { RuleConfig, ZoneConfig } from '@/types/config'
import CapabilityNodeEditor from '@/components/editor/CapabilityNodeEditor'

interface Props {
  rule: RuleConfig
  onChange: (rule: RuleConfig) => void
  onRemove: () => void
  catalogs: CapabilitiesDto
  zones: ZoneConfig[]
}

export default function RuleBuilder({ rule, onChange, onRemove, catalogs, zones }: Props) {
  const { t } = useTranslation()

  return (
    <div className="border border-slate-200 rounded-lg p-4 bg-white space-y-4">
      <div className="flex justify-between items-start gap-4">
        <div className="flex-1">
          <label className="block text-xs font-medium text-slate-600 mb-1">{t('editor.eventLabel')}</label>
          <input
            type="text"
            value={rule.event}
            onChange={(e) => onChange({ ...rule, event: e.target.value })}
            placeholder={t('editor.eventPlaceholder')}
            className="w-full px-2 py-1 border border-slate-300 rounded text-sm"
          />
        </div>
        <button
          onClick={onRemove}
          className="text-red-600 hover:text-red-700 font-medium text-sm mt-5"
        >
          {t('editor.removeRule')}
        </button>
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-600 mb-1">{t('editor.ifCondition')}</label>
        <CapabilityNodeEditor
          kind="CONDITION"
          node={rule.condition}
          onChange={(condition) => onChange({ ...rule, condition })}
          catalogs={catalogs}
          zones={zones}
          depth={0}
        />
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-600 mb-1">{t('editor.thenTarget')}</label>
        <CapabilityNodeEditor
          kind="TARGET"
          node={rule.target}
          onChange={(target) => onChange({ ...rule, target })}
          catalogs={catalogs}
          zones={zones}
          depth={0}
        />
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-600 mb-1">{t('editor.thenAction')}</label>
        <CapabilityNodeEditor
          kind="ACTION"
          node={rule.action}
          onChange={(action) => onChange({ ...rule, action })}
          catalogs={catalogs}
          zones={zones}
          depth={0}
        />
      </div>
    </div>
  )
}
