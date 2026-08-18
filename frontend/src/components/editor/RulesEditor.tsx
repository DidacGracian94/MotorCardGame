import { useState } from 'react'
import { CapabilitiesDto } from '@/api/client'
import { useTranslation } from '@/i18n/LanguageContext'
import { createDefaultNode } from '@/lib/configTransforms'
import { AttributesConfig, RuleConfig, ZoneConfig } from '@/types/config'
import RuleBuilder from '@/components/editor/RuleBuilder'

interface Props {
  rules: RuleConfig[]
  onChange: (rules: RuleConfig[]) => void
  catalogs: CapabilitiesDto
  zones: ZoneConfig[]
  attributes: AttributesConfig
  playerActions: string[]
  // Sin filtro: edita "rules" entero (p.ej. las reglas propias de una carta). Con filtro: solo
  // muestra/edita el subconjunto que cumple el predicado (p.ej. "reglas genéricas" = evento
  // GAME_STARTED), pero sigue operando sobre el mismo array completo por debajo — una regla que
  // cambia de evento simplemente pasa a aparecer en la otra vista, no se pierde.
  filter?: (rule: RuleConfig) => boolean
  // Evento con el que nace una regla nueva añadida desde esta vista.
  defaultEvent?: string
  readOnly?: boolean
}

export default function RulesEditor({
  rules,
  onChange,
  catalogs,
  zones,
  attributes,
  playerActions,
  filter,
  defaultEvent,
  readOnly = false,
}: Props) {
  const { t } = useTranslation()
  const visible = rules.map((rule, index) => ({ rule, index })).filter(({ rule }) => !filter || filter(rule))
  // Plegado por índice de regla en el array completo — todo empieza plegado (listas largas de
  // reglas ya guardadas no abruman la pantalla) salvo la que se acaba de añadir, que se abre sola.
  const [openIndices, setOpenIndices] = useState<Set<number>>(new Set())

  const toggleOpen = (index: number) => {
    setOpenIndices((prev) => {
      const next = new Set(prev)
      if (next.has(index)) next.delete(index)
      else next.add(index)
      return next
    })
  }

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
    const newIndex = rules.length
    onChange([
      ...rules,
      {
        event: defaultEvent ?? '',
        condition: createDefaultNode(condition, zones, catalogs),
        target: createDefaultNode(target, zones, catalogs),
        action: createDefaultNode(action, zones, catalogs),
      },
    ])
    setOpenIndices((prev) => new Set(prev).add(newIndex))
  }

  return (
    <div className="space-y-4">
      {visible.map(({ rule, index }) => (
        <RuleBuilder
          key={index}
          rule={rule}
          onChange={(r) => updateAt(index, r)}
          onRemove={() => removeAt(index)}
          catalogs={catalogs}
          zones={zones}
          attributes={attributes}
          playerActions={playerActions}
          isOpen={openIndices.has(index)}
          onToggleOpen={() => toggleOpen(index)}
          readOnly={readOnly}
        />
      ))}
      {!readOnly && (
        <button
          onClick={addRule}
          className="px-4 py-2 text-blue-600 hover:text-blue-700 font-medium text-sm"
        >
          {t('editor.addRule')}
        </button>
      )}
    </div>
  )
}
