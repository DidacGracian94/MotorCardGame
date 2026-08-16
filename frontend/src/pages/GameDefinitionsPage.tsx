import { useState } from 'react'
import { GameDefinitionDto } from '@/api/client'
import CreateDefinitionModal from '@/components/CreateDefinitionModal'
import RenameDefinitionModal from '@/components/RenameDefinitionModal'
import GameDefinitionsTable from '@/components/GameDefinitionsTable'

interface Props {
  definitions: GameDefinitionDto[]
  isLoading: boolean
  onViewVersions: (id: string) => void
}

export default function GameDefinitionsPage({
  definitions,
  isLoading,
  onViewVersions,
}: Props) {
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [renameDialogId, setRenameDialogId] = useState<string | null>(null)

  const selectedDefinition = definitions.find((d) => d.id === renameDialogId)

  return (
    <div className="max-w-6xl mx-auto py-8 px-4">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold text-slate-900">Game Definitions</h1>
        <button
          onClick={() => setShowCreateModal(true)}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          Create Definition
        </button>
      </div>

      {isLoading ? (
        <div className="text-center py-12">
          <p className="text-slate-600">Loading...</p>
        </div>
      ) : definitions.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-lg">
          <p className="text-slate-600">No game definitions yet. Create one to start!</p>
        </div>
      ) : (
        <GameDefinitionsTable
          definitions={definitions}
          onViewVersions={onViewVersions}
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
