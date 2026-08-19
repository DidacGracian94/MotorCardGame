import { create } from 'zustand'
import { GameInstanceDto } from '@/api/client'

export type ConnectionStatus = 'disconnected' | 'connecting' | 'connected'

interface GameInstanceStoreState {
  snapshot: GameInstanceDto | null
  viewerPlayerId: string | null
  connectionStatus: ConnectionStatus
  setSnapshot: (snapshot: GameInstanceDto) => void
  setViewerPlayerId: (viewerPlayerId: string | null) => void
  setConnectionStatus: (status: ConnectionStatus) => void
  reset: () => void
}

export const useGameInstanceStore = create<GameInstanceStoreState>((set) => ({
  snapshot: null,
  viewerPlayerId: null,
  connectionStatus: 'disconnected',
  setSnapshot: (snapshot) => set({ snapshot }),
  setViewerPlayerId: (viewerPlayerId) => set({ viewerPlayerId }),
  setConnectionStatus: (connectionStatus) => set({ connectionStatus }),
  reset: () => set({ snapshot: null, viewerPlayerId: null, connectionStatus: 'disconnected' }),
}))
