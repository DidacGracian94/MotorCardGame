import { useState } from 'react'
import { useGameDefinitions } from '@/hooks/useGameDefinitions'
import GameDefinitionsPage from '@/pages/GameDefinitionsPage'
import VersionsPage from '@/pages/VersionsPage'

export type PageState =
  | { type: 'list' }
  | { type: 'versions'; gameDefinitionId: string }

function App() {
  const [page, setPage] = useState<PageState>({ type: 'list' })
  const { data: definitions, isLoading, error } = useGameDefinitions()

  if (error) {
    return (
      <div className="flex items-center justify-center h-screen bg-red-50">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-red-900 mb-2">Error</h1>
          <p className="text-red-700">{error.message}</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-slate-50">
      {page.type === 'list' ? (
        <GameDefinitionsPage
          definitions={definitions || []}
          isLoading={isLoading}
          onViewVersions={(id) => setPage({ type: 'versions', gameDefinitionId: id })}
        />
      ) : (
        <VersionsPage
          gameDefinitionId={page.gameDefinitionId}
          onBack={() => setPage({ type: 'list' })}
        />
      )}
    </div>
  )
}

export default App
