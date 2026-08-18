import { useState } from 'react'
import { CapabilitiesDto } from '@/api/client'
import { useTranslation } from '@/i18n/LanguageContext'
import { parseConfig } from '@/lib/configTransforms'
import { GAME_STARTED_EVENT } from '@/types/config'
import AttributesEditor from '@/components/editor/AttributesEditor'
import PlayerActionsEditor from '@/components/editor/PlayerActionsEditor'
import ZonesEditor from '@/components/editor/ZonesEditor'
import CardTemplatesEditor from '@/components/editor/CardTemplatesEditor'
import RulesEditor from '@/components/editor/RulesEditor'
import CollapsibleSection from '@/components/editor/CollapsibleSection'

interface Props {
  config: unknown
  catalogs: CapabilitiesDto
}

type ViewTab = 'visual' | 'json'

type SectionKey =
  | 'zones'
  | 'attributes'
  | 'cardTemplates'
  | 'playerActions'
  | 'playerActionRules'
  | 'genericRules'

// Nunca se llama en la práctica — todos los editores reciben readOnly, así que sus inputs están
// deshabilitados y no disparan onChange — pero cada uno sigue exigiendo la prop por tipo.
function noop() {}

// Vista de solo lectura de una GameDefinitionVersion ya publicada: reutiliza exactamente los
// mismos componentes del editor visual (en modo readOnly) para que una versión se vea "como si
// fuera edit", con la opción de pasar a JSON crudo sin salir de esta vista.
export default function VersionConfigViewer({ config, catalogs }: Props) {
  const { t } = useTranslation()
  const [tab, setTab] = useState<ViewTab>('visual')
  const [collapsedSections, setCollapsedSections] = useState<Set<SectionKey>>(new Set())
  const parsedConfig = parseConfig(config)

  const toggleSection = (key: SectionKey) => {
    setCollapsedSections((prev) => {
      const next = new Set(prev)
      if (next.has(key)) next.delete(key)
      else next.add(key)
      return next
    })
  }

  return (
    <div className="space-y-6">
      <div className="flex gap-4 border-b border-slate-200">
        <button
          onClick={() => setTab('visual')}
          className={`px-3 py-2 text-sm font-medium ${
            tab === 'visual'
              ? 'border-b-2 border-blue-600 text-blue-600'
              : 'text-slate-600 hover:text-slate-900'
          }`}
        >
          {t('editor.tabVisual')}
        </button>
        <button
          onClick={() => setTab('json')}
          className={`px-3 py-2 text-sm font-medium ${
            tab === 'json'
              ? 'border-b-2 border-blue-600 text-blue-600'
              : 'text-slate-600 hover:text-slate-900'
          }`}
        >
          {t('editor.tabJson')}
        </button>
      </div>

      {tab === 'visual' ? (
        <div className="space-y-8">
          <CollapsibleSection
            title={t('editor.sectionZones')}
            isOpen={!collapsedSections.has('zones')}
            onToggleOpen={() => toggleSection('zones')}
          >
            <ZonesEditor zones={parsedConfig.zones} onChange={noop} readOnly />
          </CollapsibleSection>
          <CollapsibleSection
            title={t('editor.sectionAttributes')}
            isOpen={!collapsedSections.has('attributes')}
            onToggleOpen={() => toggleSection('attributes')}
          >
            <AttributesEditor attributes={parsedConfig.attributes} onChange={noop} readOnly />
          </CollapsibleSection>
          <CollapsibleSection
            title={t('editor.sectionCardTemplates')}
            isOpen={!collapsedSections.has('cardTemplates')}
            onToggleOpen={() => toggleSection('cardTemplates')}
          >
            <CardTemplatesEditor
              cardTemplates={parsedConfig.cards}
              zones={parsedConfig.zones}
              attributes={parsedConfig.attributes}
              catalogs={catalogs}
              playerActions={parsedConfig.playerActions}
              onChange={noop}
              readOnly
            />
          </CollapsibleSection>
          <CollapsibleSection
            title={t('editor.sectionPlayerActions')}
            isOpen={!collapsedSections.has('playerActions')}
            onToggleOpen={() => toggleSection('playerActions')}
          >
            <PlayerActionsEditor playerActions={parsedConfig.playerActions} onChange={noop} readOnly />
          </CollapsibleSection>
          <CollapsibleSection
            title={t('editor.sectionPlayerActionRules')}
            isOpen={!collapsedSections.has('playerActionRules')}
            onToggleOpen={() => toggleSection('playerActionRules')}
          >
            <RulesEditor
              rules={parsedConfig.rules}
              onChange={noop}
              catalogs={catalogs}
              zones={parsedConfig.zones}
              attributes={parsedConfig.attributes}
              playerActions={parsedConfig.playerActions}
              filter={(rule) => rule.event !== GAME_STARTED_EVENT}
              readOnly
            />
          </CollapsibleSection>
          <CollapsibleSection
            title={t('editor.sectionGenericRules')}
            isOpen={!collapsedSections.has('genericRules')}
            onToggleOpen={() => toggleSection('genericRules')}
          >
            <RulesEditor
              rules={parsedConfig.rules}
              onChange={noop}
              catalogs={catalogs}
              zones={parsedConfig.zones}
              attributes={parsedConfig.attributes}
              playerActions={parsedConfig.playerActions}
              filter={(rule) => rule.event === GAME_STARTED_EVENT}
              readOnly
            />
          </CollapsibleSection>
        </div>
      ) : (
        <pre className="w-full overflow-x-auto bg-white border border-slate-200 rounded-lg p-4 font-mono text-xs">
          {JSON.stringify(config, null, 2)}
        </pre>
      )}
    </div>
  )
}
