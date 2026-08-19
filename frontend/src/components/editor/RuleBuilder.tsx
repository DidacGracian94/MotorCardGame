import { CapabilitiesDto } from '@/api/client'
import { useTranslation } from '@/i18n/LanguageContext'
import { AttributesConfig, RuleConfig, ZoneConfig } from '@/types/config'
import CapabilityNodeEditor from '@/components/editor/CapabilityNodeEditor'

interface Props {
  rule: RuleConfig
  onChange: (rule: RuleConfig) => void
  onRemove: () => void
  catalogs: CapabilitiesDto
  zones: ZoneConfig[]
  attributes: AttributesConfig
  playerActions: string[]
  isOpen: boolean
  onToggleOpen: () => void
  readOnly?: boolean
  // Cuando se da, el evento de esta regla está fijado a este valor (p.ej. GAME_STARTED en
  // "reglas genéricas") y no se muestra selector — no tiene sentido ofrecer acciones de jugador
  // como alternativa ahí, para eso ya está la sección de reglas de acción de jugador.
  fixedEvent?: string
}

export default function RuleBuilder({
  rule,
  onChange,
  onRemove,
  catalogs,
  zones,
  attributes,
  playerActions,
  isOpen,
  onToggleOpen,
  readOnly = false,
  fixedEvent,
}: Props) {
  const { t, tf } = useTranslation()
  const eventOptions =
    rule.event && !playerActions.includes(rule.event) && rule.event !== fixedEvent
      ? [rule.event, ...playerActions]
      : playerActions
  const actionLabel = rule.action.type ? tf(`capability.${rule.action.type}`, rule.action.type) : ''

  return (
    <div className="border border-slate-200 rounded-lg bg-white">
      <div className="flex items-center gap-3 p-3">
        <button
          onClick={onToggleOpen}
          className="flex items-center justify-center w-9 h-9 rounded-md text-xl leading-none text-slate-500 hover:text-slate-800 hover:bg-slate-100 shrink-0 transition-colors"
          aria-expanded={isOpen}
          aria-label={isOpen ? t('editor.collapseRule') : t('editor.expandRule')}
        >
          {isOpen ? '▾' : '▸'}
        </button>
        {isOpen ? (
          <div className="flex-1">
            <label className="block text-xs font-medium text-slate-600 mb-1">{t('editor.eventLabel')}</label>
            {fixedEvent ? (
              <div className="px-2 py-1 text-sm text-slate-600">{tf(`event.${fixedEvent}`, fixedEvent)}</div>
            ) : (
              <select
                value={rule.event}
                onChange={(e) => onChange({ ...rule, event: e.target.value })}
                disabled={readOnly}
                className="w-full px-2 py-1 border border-slate-300 rounded text-sm bg-white disabled:bg-slate-50 disabled:text-slate-600"
              >
                <option value="">{t('editor.selectEventPlaceholder')}</option>
                {eventOptions.map((name) => (
                  <option key={name} value={name}>
                    {name}
                    {!playerActions.includes(name) ? ` (${t('editor.attributeUndeclaredOption')})` : ''}
                  </option>
                ))}
              </select>
            )}
          </div>
        ) : (
          <button onClick={onToggleOpen} className="flex-1 text-left min-w-0">
            <span className="text-sm font-medium text-slate-800">
              {rule.event || t('editor.unnamed')}
            </span>
            {actionLabel && <span className="ml-2 text-xs text-slate-400">→ {actionLabel}</span>}
          </button>
        )}
        {!readOnly && (
          <button
            onClick={onRemove}
            className="text-red-600 hover:text-red-700 font-medium text-sm shrink-0"
          >
            {t('editor.removeRule')}
          </button>
        )}
      </div>

      {isOpen && (
        <div className="px-4 pb-4 space-y-4 border-t border-slate-100 pt-4">
          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">{t('editor.ifCondition')}</label>
            <CapabilityNodeEditor
              kind="CONDITION"
              node={rule.condition}
              onChange={(condition) => onChange({ ...rule, condition })}
              catalogs={catalogs}
              zones={zones}
              attributes={attributes}
              depth={0}
              readOnly={readOnly}
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
              attributes={attributes}
              depth={0}
              readOnly={readOnly}
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
              attributes={attributes}
              depth={0}
              readOnly={readOnly}
            />
          </div>
        </div>
      )}
    </div>
  )
}
