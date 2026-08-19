import { Navigate, Route, Routes } from 'react-router-dom'
import GameDefinitionsPage from '@/pages/GameDefinitionsPage'
import VersionsPage from '@/pages/VersionsPage'
import GameDefinitionEditorPage from '@/pages/GameDefinitionEditorPage'
import CreateInstancePage from '@/pages/CreateInstancePage'
import GameInstancePage from '@/pages/GameInstancePage'
import LanguageSwitcher from '@/components/LanguageSwitcher'

function App() {
  return (
    <div className="min-h-screen bg-slate-50">
      <LanguageSwitcher />
      <Routes>
        <Route path="/" element={<GameDefinitionsPage />} />
        <Route path="/games/:gameDefinitionId/versions" element={<VersionsPage />} />
        <Route path="/games/:gameDefinitionId/editor" element={<GameDefinitionEditorPage />} />
        <Route path="/games/:gameDefinitionId/instances/new" element={<CreateInstancePage />} />
        <Route path="/instances/:instanceId" element={<GameInstancePage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </div>
  )
}

export default App
