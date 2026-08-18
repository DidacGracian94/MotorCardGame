import { useCapabilities } from '@/hooks/useCapabilities'
import { useGameDefinitions, useGameDefinitionVersions } from '@/hooks/useGameDefinitions'
import { useTranslation } from '@/i18n/LanguageContext'
import VersionsTable from '@/components/VersionsTable'

interface Props {
  gameDefinitionId: string
  onBack: () => void
  onNewVersion: () => void
  onEditAsNewVersion: (versionNumber: number) => void
}

export default function VersionsPage({
  gameDefinitionId,
  onBack,
  onNewVersion,
  onEditAsNewVersion,
}: Props) {
  const { data: definitions } = useGameDefinitions()
  const { data: versions, isLoading } = useGameDefinitionVersions(gameDefinitionId)
  const { data: catalogs, isLoading: catalogsLoading } = useCapabilities()
  const { t } = useTranslation()

  const definition = definitions?.find((d) => d.id === gameDefinitionId)

  return (
    <div className="max-w-6xl mx-auto py-8 px-4">
      <button
        onClick={onBack}
        className="mb-6 px-4 py-2 text-blue-600 hover:text-blue-700 font-medium"
      >
        {t('common.backToDefinitions')}
      </button>

      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold text-slate-900">{t('versions.title')}</h1>
          {definition && (
            <p className="text-slate-600 mt-1">{definition.name}</p>
          )}
        </div>
        <button
          onClick={onNewVersion}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          {t('versions.newVersionButton')}
        </button>
      </div>

      {isLoading || catalogsLoading ? (
        <div className="text-center py-12">
          <p className="text-slate-600">{t('versions.loading')}</p>
        </div>
      ) : !versions || versions.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-lg">
          <p className="text-slate-600">{t('versions.empty')}</p>
        </div>
      ) : catalogs ? (
        <VersionsTable versions={versions} catalogs={catalogs} onEditAsNewVersion={onEditAsNewVersion} />
      ) : null}
    </div>
  )
}
