import { useState } from 'react'
import { useGameDefinitions, useGameDefinitionVersions } from '@/hooks/useGameDefinitions'
import PublishVersionModal from '@/components/PublishVersionModal'
import VersionsTable from '@/components/VersionsTable'

interface Props {
  gameDefinitionId: string
  onBack: () => void
}

export default function VersionsPage({
  gameDefinitionId,
  onBack,
}: Props) {
  const [showPublishModal, setShowPublishModal] = useState(false)
  const { data: definitions } = useGameDefinitions()
  const { data: versions, isLoading } = useGameDefinitionVersions(gameDefinitionId)

  const definition = definitions?.find((d) => d.id === gameDefinitionId)

  return (
    <div className="max-w-6xl mx-auto py-8 px-4">
      <button
        onClick={onBack}
        className="mb-6 px-4 py-2 text-blue-600 hover:text-blue-700 font-medium"
      >
        ← Back to Definitions
      </button>

      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold text-slate-900">Versions</h1>
          {definition && (
            <p className="text-slate-600 mt-1">{definition.name}</p>
          )}
        </div>
        <button
          onClick={() => setShowPublishModal(true)}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          Publish Version
        </button>
      </div>

      {isLoading ? (
        <div className="text-center py-12">
          <p className="text-slate-600">Loading versions...</p>
        </div>
      ) : !versions || versions.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-lg">
          <p className="text-slate-600">No versions yet. Publish one to start!</p>
        </div>
      ) : (
        <VersionsTable versions={versions} />
      )}

      <PublishVersionModal
        gameDefinitionId={gameDefinitionId}
        open={showPublishModal}
        onOpenChange={setShowPublishModal}
      />
    </div>
  )
}
