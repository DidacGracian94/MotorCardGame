import { useEffect, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { useCapabilities } from '@/hooks/useCapabilities'
import { useGameDefinitionVersion, usePublishVersion } from '@/hooks/useGameDefinitions'
import { useTranslation } from '@/i18n/LanguageContext'
import { configToJson, createEmptyConfig, parseConfig } from '@/lib/configTransforms'
import { CapabilityNode, GAME_STARTED_EVENT, GameConfig, RuleConfig } from '@/types/config'
import AttributesEditor from '@/components/editor/AttributesEditor'
import PlayerActionsEditor from '@/components/editor/PlayerActionsEditor'
import ZonesEditor from '@/components/editor/ZonesEditor'
import CardTemplatesEditor from '@/components/editor/CardTemplatesEditor'
import RulesEditor from '@/components/editor/RulesEditor'
import JsonConfigTab from '@/components/editor/JsonConfigTab'
import CollapsibleSection from '@/components/editor/CollapsibleSection'

type Tab = 'visual' | 'json'

type SectionKey =
  | 'zones'
  | 'attributes'
  | 'cardTemplates'
  | 'playerActions'
  | 'playerActionRules'
  | 'genericRules'

function collectAttributeReferences(node: CapabilityNode): string[] {
  const refs: string[] = []
  for (const [key, value] of Object.entries(node.fields)) {
    if (key === 'attribute' && typeof value === 'string' && value) {
      refs.push(value)
    } else if (Array.isArray(value)) {
      for (const item of value) {
        refs.push(...collectAttributeReferences(item))
      }
    } else if (value && typeof value === 'object' && 'type' in value) {
      refs.push(...collectAttributeReferences(value))
    }
  }
  return refs
}

// Empareja el campo "attribute" de un nodo con su literal "equals" hermano (el único caso hoy es
// CARD_ATTRIBUTE_EQUALS) para poder validar ese valor contra las opciones declaradas del atributo.
function collectAttributeValueChecks(node: CapabilityNode): { attribute: string; value: unknown }[] {
  const checks: { attribute: string; value: unknown }[] = []
  const attributeName = node.fields.attribute
  if (typeof attributeName === 'string' && attributeName && 'equals' in node.fields) {
    checks.push({ attribute: attributeName, value: node.fields.equals })
  }
  for (const value of Object.values(node.fields)) {
    if (Array.isArray(value)) {
      for (const item of value) {
        checks.push(...collectAttributeValueChecks(item))
      }
    } else if (value && typeof value === 'object' && 'type' in value) {
      checks.push(...collectAttributeValueChecks(value as CapabilityNode))
    }
  }
  return checks
}

// Junta las reglas globales con las de cada carta — el motor las trata exactamente igual, la
// única diferencia es dónde vive cada una dentro del JSON, así que toda validación de reglas
// debe cubrir ambas fuentes.
function allRules(config: GameConfig): RuleConfig[] {
  return [...config.rules, ...config.cards.flatMap((card) => card.rules ?? [])]
}

function validateConfig(
  config: GameConfig,
  t: (key: string, vars?: Record<string, string | number>) => string
): string | undefined {
  const zoneNames = config.zones.map((z) => z.name)
  if (zoneNames.some((name) => !name.trim())) {
    return t('editor.validation.zoneNameRequired')
  }
  if (new Set(zoneNames).size !== zoneNames.length) {
    return t('editor.validation.zoneNameUnique')
  }
  if (config.cards.some((c) => !c.id.trim())) {
    return t('editor.validation.cardIdRequired')
  }
  const sharedZoneNames = new Set(
    config.zones.filter((z) => z.ownership === 'SHARED').map((z) => z.name)
  )
  for (const card of config.cards) {
    if (!sharedZoneNames.has(card.zone)) {
      return t('editor.validation.cardZoneInvalid', { id: card.id || t('editor.unnamed') })
    }
  }
  if (Object.keys(config.attributes).some((name) => !name.trim())) {
    return t('editor.validation.attributeNameRequired')
  }
  if (config.playerActions.some((name) => !name.trim())) {
    return t('editor.validation.playerActionNameRequired')
  }
  const rules = allRules(config)
  if (rules.some((r) => !r.event.trim())) {
    return t('editor.validation.ruleEventRequired')
  }
  const knownEvents = new Set([GAME_STARTED_EVENT, ...config.playerActions])
  for (const rule of rules) {
    if (!knownEvents.has(rule.event)) {
      return t('editor.validation.eventUndeclared', { name: rule.event })
    }
  }
  const attributeNames = new Set(Object.keys(config.attributes))
  for (const card of config.cards) {
    for (const [key, value] of Object.entries(card.attributes)) {
      const definition = config.attributes[key]
      if (!definition) {
        return t('editor.validation.attributeUndeclared', { name: key })
      }
      if (definition.options && definition.options.length > 0 && !definition.options.includes(String(value))) {
        return t('editor.validation.attributeValueNotAllowed', { name: key, value: String(value) })
      }
    }
  }
  for (const rule of rules) {
    const refs = [
      ...collectAttributeReferences(rule.condition),
      ...collectAttributeReferences(rule.target),
      ...collectAttributeReferences(rule.action),
    ]
    for (const ref of refs) {
      if (!attributeNames.has(ref)) {
        return t('editor.validation.attributeUndeclared', { name: ref })
      }
    }
    const valueChecks = [
      ...collectAttributeValueChecks(rule.condition),
      ...collectAttributeValueChecks(rule.target),
      ...collectAttributeValueChecks(rule.action),
    ]
    for (const check of valueChecks) {
      const definition = config.attributes[check.attribute]
      if (definition?.options && definition.options.length > 0 && !definition.options.includes(String(check.value))) {
        return t('editor.validation.attributeValueNotAllowed', {
          name: check.attribute,
          value: String(check.value),
        })
      }
    }
  }
  return undefined
}

export default function GameDefinitionEditorPage() {
  const { gameDefinitionId = '' } = useParams<{ gameDefinitionId: string }>()
  const [searchParams] = useSearchParams()
  const sourceVersionNumberParam = searchParams.get('sourceVersion')
  const sourceVersionNumber = sourceVersionNumberParam ? Number(sourceVersionNumberParam) : undefined
  const navigate = useNavigate()
  const onBack = () => navigate(`/games/${gameDefinitionId}/versions`)

  const { data: catalogs, isLoading: catalogsLoading } = useCapabilities()
  const { data: sourceVersion } = useGameDefinitionVersion(gameDefinitionId, sourceVersionNumber)
  const publishMutation = usePublishVersion()
  const { t } = useTranslation()

  const [config, setConfig] = useState<GameConfig>(createEmptyConfig())
  const [activeTab, setActiveTab] = useState<Tab>('visual')
  const [jsonText, setJsonText] = useState('')
  const [jsonError, setJsonError] = useState<string>()
  const [validationError, setValidationError] = useState<string>()
  // Secciones plegadas por clave — vacío al inicio (todas abiertas); cada juego suele necesitar
  // ver de golpe sus zonas/atributos/reglas al recuperar una versión existente para editarla.
  const [collapsedSections, setCollapsedSections] = useState<Set<SectionKey>>(new Set())

  const toggleSection = (key: SectionKey) => {
    setCollapsedSections((prev) => {
      const next = new Set(prev)
      if (next.has(key)) next.delete(key)
      else next.add(key)
      return next
    })
  }

  useEffect(() => {
    if (sourceVersion) {
      setConfig(parseConfig(sourceVersion.config))
    }
  }, [sourceVersion])

  const handleTabChange = (tab: Tab) => {
    if (tab === 'json' && activeTab === 'visual') {
      setJsonText(JSON.stringify(configToJson(config), null, 2))
      setJsonError(undefined)
    }
    setActiveTab(tab)
  }

  const handleApplyJson = () => {
    try {
      const parsed = JSON.parse(jsonText)
      setConfig(parseConfig(parsed))
      setJsonError(undefined)
    } catch (e) {
      setJsonError(e instanceof Error ? e.message : 'Invalid JSON')
    }
  }

  const handleSave = async () => {
    setValidationError(undefined)
    let configJson: unknown
    if (activeTab === 'json') {
      try {
        configJson = JSON.parse(jsonText)
      } catch (e) {
        setJsonError(e instanceof Error ? e.message : 'Invalid JSON')
        return
      }
    } else {
      const error = validateConfig(config, t)
      if (error) {
        setValidationError(error)
        return
      }
      configJson = configToJson(config)
    }

    try {
      await publishMutation.mutateAsync({ gameDefinitionId, config: configJson })
      onBack()
    } catch {
      // surfaced via publishMutation.error below
    }
  }

  if (catalogsLoading || !catalogs) {
    return (
      <div className="max-w-7xl mx-auto py-8 px-4">
        <p className="text-center py-12 text-slate-600">{t('editor.loadingCapabilities')}</p>
      </div>
    )
  }

  return (
    <div className="max-w-7xl mx-auto py-8 px-4">
      <button
        onClick={onBack}
        className="mb-6 px-4 py-2 text-blue-600 hover:text-blue-700 font-medium"
      >
        {t('editor.backToVersions')}
      </button>

      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold text-slate-900">
          {sourceVersionNumber
            ? t('editor.editAsNewVersionTitle', { version: sourceVersionNumber })
            : t('editor.newVersionTitle')}
        </h1>
        <button
          onClick={handleSave}
          disabled={publishMutation.isPending}
          className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {publishMutation.isPending ? t('common.saving') : t('common.save')}
        </button>
      </div>

      <div className="flex gap-4 border-b border-slate-200 mb-6">
        <button
          onClick={() => handleTabChange('visual')}
          className={`px-4 py-2 text-sm font-medium ${
            activeTab === 'visual'
              ? 'border-b-2 border-blue-600 text-blue-600'
              : 'text-slate-600 hover:text-slate-900'
          }`}
        >
          {t('editor.tabVisual')}
        </button>
        <button
          onClick={() => handleTabChange('json')}
          className={`px-4 py-2 text-sm font-medium ${
            activeTab === 'json'
              ? 'border-b-2 border-blue-600 text-blue-600'
              : 'text-slate-600 hover:text-slate-900'
          }`}
        >
          {t('editor.tabJson')}
        </button>
      </div>

      {validationError && <p className="mb-4 text-sm text-red-600">{validationError}</p>}
      {publishMutation.isError && (
        <p className="mb-4 text-sm text-red-600">{publishMutation.error.message}</p>
      )}

      {activeTab === 'visual' ? (
        <div className="space-y-8">
          <CollapsibleSection
            title={t('editor.sectionZones')}
            isOpen={!collapsedSections.has('zones')}
            onToggleOpen={() => toggleSection('zones')}
          >
            <ZonesEditor zones={config.zones} onChange={(zones) => setConfig({ ...config, zones })} />
          </CollapsibleSection>
          <CollapsibleSection
            title={t('editor.sectionPlayerActions')}
            isOpen={!collapsedSections.has('playerActions')}
            onToggleOpen={() => toggleSection('playerActions')}
          >
            <PlayerActionsEditor
              playerActions={config.playerActions}
              onChange={(playerActions) => setConfig({ ...config, playerActions })}
            />
          </CollapsibleSection>
          <CollapsibleSection
            title={t('editor.sectionAttributes')}
            isOpen={!collapsedSections.has('attributes')}
            onToggleOpen={() => toggleSection('attributes')}
          >
            <AttributesEditor
              attributes={config.attributes}
              onChange={(attributes) => setConfig({ ...config, attributes })}
            />
          </CollapsibleSection>
          <CollapsibleSection
            title={t('editor.sectionCardTemplates')}
            isOpen={!collapsedSections.has('cardTemplates')}
            onToggleOpen={() => toggleSection('cardTemplates')}
          >
            <CardTemplatesEditor
              cardTemplates={config.cards}
              zones={config.zones}
              attributes={config.attributes}
              catalogs={catalogs}
              playerActions={config.playerActions}
              onChange={(cards) => setConfig({ ...config, cards })}
            />
          </CollapsibleSection>
          <CollapsibleSection
            title={t('editor.sectionPlayerActionRules')}
            isOpen={!collapsedSections.has('playerActionRules')}
            onToggleOpen={() => toggleSection('playerActionRules')}
          >
            <RulesEditor
              rules={config.rules}
              onChange={(rules) => setConfig({ ...config, rules })}
              catalogs={catalogs}
              zones={config.zones}
              attributes={config.attributes}
              playerActions={config.playerActions}
              filter={(rule) => rule.event !== GAME_STARTED_EVENT}
            />
          </CollapsibleSection>
          <CollapsibleSection
            title={t('editor.sectionGenericRules')}
            isOpen={!collapsedSections.has('genericRules')}
            onToggleOpen={() => toggleSection('genericRules')}
          >
            <RulesEditor
              rules={config.rules}
              onChange={(rules) => setConfig({ ...config, rules })}
              catalogs={catalogs}
              zones={config.zones}
              attributes={config.attributes}
              playerActions={config.playerActions}
              filter={(rule) => rule.event === GAME_STARTED_EVENT}
              defaultEvent={GAME_STARTED_EVENT}
            />
          </CollapsibleSection>
        </div>
      ) : (
        <JsonConfigTab value={jsonText} onChange={setJsonText} onApply={handleApplyJson} error={jsonError} />
      )}
    </div>
  )
}
