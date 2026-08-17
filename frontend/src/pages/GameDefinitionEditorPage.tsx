import { useEffect, useState } from 'react'
import { useCapabilities } from '@/hooks/useCapabilities'
import { useGameDefinitionVersion, usePublishVersion } from '@/hooks/useGameDefinitions'
import { useTranslation } from '@/i18n/LanguageContext'
import { configToJson, createEmptyConfig, parseConfig } from '@/lib/configTransforms'
import { GameConfig } from '@/types/config'
import ZonesEditor from '@/components/editor/ZonesEditor'
import CardTemplatesEditor from '@/components/editor/CardTemplatesEditor'
import RulesEditor from '@/components/editor/RulesEditor'
import JsonConfigTab from '@/components/editor/JsonConfigTab'

interface Props {
  gameDefinitionId: string
  sourceVersionNumber?: number
  onBack: () => void
}

type Tab = 'visual' | 'json'

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
  const sharedZoneNames = new Set(
    config.zones.filter((z) => z.ownership === 'SHARED').map((z) => z.name)
  )
  for (const card of config.cards) {
    if (!sharedZoneNames.has(card.zone)) {
      return t('editor.validation.cardZoneInvalid', { id: card.id || t('editor.unnamed') })
    }
  }
  if (config.rules.some((r) => !r.event.trim())) {
    return t('editor.validation.ruleEventRequired')
  }
  return undefined
}

export default function GameDefinitionEditorPage({
  gameDefinitionId,
  sourceVersionNumber,
  onBack,
}: Props) {
  const { data: catalogs, isLoading: catalogsLoading } = useCapabilities()
  const { data: sourceVersion } = useGameDefinitionVersion(gameDefinitionId, sourceVersionNumber)
  const publishMutation = usePublishVersion()
  const { t } = useTranslation()

  const [config, setConfig] = useState<GameConfig>(createEmptyConfig())
  const [activeTab, setActiveTab] = useState<Tab>('visual')
  const [jsonText, setJsonText] = useState('')
  const [jsonError, setJsonError] = useState<string>()
  const [validationError, setValidationError] = useState<string>()

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
          <section>
            <h2 className="text-xl font-semibold text-slate-900 mb-3">{t('editor.sectionZones')}</h2>
            <ZonesEditor zones={config.zones} onChange={(zones) => setConfig({ ...config, zones })} />
          </section>
          <section>
            <h2 className="text-xl font-semibold text-slate-900 mb-3">
              {t('editor.sectionCardTemplates')}
            </h2>
            <CardTemplatesEditor
              cardTemplates={config.cards}
              zones={config.zones}
              onChange={(cards) => setConfig({ ...config, cards })}
            />
          </section>
          <section>
            <h2 className="text-xl font-semibold text-slate-900 mb-3">{t('editor.sectionRules')}</h2>
            <RulesEditor
              rules={config.rules}
              onChange={(rules) => setConfig({ ...config, rules })}
              catalogs={catalogs}
              zones={config.zones}
            />
          </section>
        </div>
      ) : (
        <JsonConfigTab value={jsonText} onChange={setJsonText} onApply={handleApplyJson} error={jsonError} />
      )}
    </div>
  )
}
