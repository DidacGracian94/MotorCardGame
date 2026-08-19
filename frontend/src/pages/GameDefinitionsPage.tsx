import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useGameDefinitions } from '@/hooks/useGameDefinitions'
import { useTranslation } from '@/i18n/LanguageContext'
import CreateDefinitionModal from '@/components/CreateDefinitionModal'
import RenameDefinitionModal from '@/components/RenameDefinitionModal'
import GameDefinitionsTable from '@/components/GameDefinitionsTable'

export default function GameDefinitionsPage() {
  const navigate = useNavigate()
  const { data: definitions, isLoading, error } = useGameDefinitions()
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [renameDialogId, setRenameDialogId] = useState<string | null>(null)
  const { t } = useTranslation()

  const selectedDefinition = definitions?.find((d) => d.id === renameDialogId)

  if (error) {
    return (
      <div className="flex items-center justify-center h-[calc(100vh-4rem)]">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-red-900 mb-2">{t('common.error')}</h1>
          <p className="text-red-700">{error.message}</p>
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-6xl mx-auto py-8 px-4">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold text-slate-900">{t('gameDefinitions.title')}</h1>
        <button
          onClick={() => setShowCreateModal(true)}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          {t('gameDefinitions.createButton')}
        </button>
      </div>

      {isLoading ? (
        <div className="text-center py-12">
          <p className="text-slate-600">{t('common.loading')}</p>
        </div>
      ) : !definitions || definitions.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-lg">
          <p className="text-slate-600">{t('gameDefinitions.empty')}</p>
        </div>
      ) : (
        <GameDefinitionsTable
          definitions={definitions}
          onViewVersions={(id) => navigate(`/games/${id}/versions`)}
          onRename={setRenameDialogId}
        />
      )}

      <CreateDefinitionModal
        open={showCreateModal}
        onOpenChange={setShowCreateModal}
      />

      {selectedDefinition && (
        <RenameDefinitionModal
          definition={selectedDefinition}
          open={!!renameDialogId}
          onOpenChange={() => setRenameDialogId(null)}
        />
      )}
    </div>
  )
}
