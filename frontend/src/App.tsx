import { useState } from 'react'
import { useGameDefinitions } from '@/hooks/useGameDefinitions'
import { useTranslation } from '@/i18n/LanguageContext'
import GameDefinitionsPage from '@/pages/GameDefinitionsPage'
import VersionsPage from '@/pages/VersionsPage'
import GameDefinitionEditorPage from '@/pages/GameDefinitionEditorPage'
import LanguageSwitcher from '@/components/LanguageSwitcher'

export type PageState =
  | { type: 'list' }
  | { type: 'versions'; gameDefinitionId: string }
  | { type: 'editor'; gameDefinitionId: string; sourceVersionNumber?: number }

function App() {
  const [page, setPage] = useState<PageState>({ type: 'list' })
  const { data: definitions, isLoading, error } = useGameDefinitions()
  const { t } = useTranslation()

  if (error) {
    return (
      <div className="flex items-center justify-center h-screen bg-red-50">
        <LanguageSwitcher />
        <div className="text-center">
          <h1 className="text-2xl font-bold text-red-900 mb-2">{t('common.error')}</h1>
          <p className="text-red-700">{error.message}</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <LanguageSwitcher />
      {page.type === 'list' && (
        <GameDefinitionsPage
          definitions={definitions || []}
          isLoading={isLoading}
          onViewVersions={(id) => setPage({ type: 'versions', gameDefinitionId: id })}
        />
      )}
      {page.type === 'versions' && (
        <VersionsPage
          gameDefinitionId={page.gameDefinitionId}
          onBack={() => setPage({ type: 'list' })}
          onNewVersion={() => setPage({ type: 'editor', gameDefinitionId: page.gameDefinitionId })}
          onEditAsNewVersion={(versionNumber) =>
            setPage({
              type: 'editor',
              gameDefinitionId: page.gameDefinitionId,
              sourceVersionNumber: versionNumber,
            })
          }
        />
      )}
      {page.type === 'editor' && (
        <GameDefinitionEditorPage
          gameDefinitionId={page.gameDefinitionId}
          sourceVersionNumber={page.sourceVersionNumber}
          onBack={() => setPage({ type: 'versions', gameDefinitionId: page.gameDefinitionId })}
        />
      )}
    </div>
  )
}

export default App
