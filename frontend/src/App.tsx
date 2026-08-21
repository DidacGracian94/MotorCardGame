import { Navigate, Route, Routes } from 'react-router-dom'
import GameDefinitionsPage from '@/pages/GameDefinitionsPage'
import VersionsPage from '@/pages/VersionsPage'
import GameDefinitionEditorPage from '@/pages/GameDefinitionEditorPage'
import CreateInstancePage from '@/pages/CreateInstancePage'
import GameInstancePage from '@/pages/GameInstancePage'
import CreateRoomPage from '@/pages/CreateRoomPage'
import JoinRoomPage from '@/pages/JoinRoomPage'
import AdminUsersPage from '@/pages/AdminUsersPage'
import LoginPage from '@/pages/LoginPage'
import RegisterPage from '@/pages/RegisterPage'
import ProfileMenu from '@/components/ProfileMenu'
import ProtectedRoute from '@/components/ProtectedRoute'

function App() {
  return (
    <div className="min-h-screen bg-slate-50">
      <ProfileMenu />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/rooms/join" element={<JoinRoomPage />} />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <GameDefinitionsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/games/:gameDefinitionId/versions"
          element={
            <ProtectedRoute>
              <VersionsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/games/:gameDefinitionId/editor"
          element={
            <ProtectedRoute>
              <GameDefinitionEditorPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/games/:gameDefinitionId/instances/new"
          element={
            <ProtectedRoute>
              <CreateInstancePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/games/:gameDefinitionId/rooms/new"
          element={
            <ProtectedRoute>
              <CreateRoomPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/instances/:instanceId"
          element={
            <ProtectedRoute>
              <GameInstancePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/users"
          element={
            <ProtectedRoute requireAdmin>
              <AdminUsersPage />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </div>
  )
}

export default App
