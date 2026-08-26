import { useEffect, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { useApplyPlayerAction, useGameInstance } from '@/hooks/useGameInstance'
import { useTranslation } from '@/i18n/LanguageContext'
import { connectToGameInstance } from '@/realtime/stompClient'
import { ConnectionStatus, useGameInstanceStore } from '@/store/gameInstanceStore'
import ActionPanel from '@/components/instance/ActionPanel'
import HandView from '@/components/instance/HandView'
import ZoneView from '@/components/instance/ZoneView'

export default function GameInstancePage() {
  const { instanceId = '' } = useParams<{ instanceId: string }>()
  const [searchParams] = useSearchParams()
  const viewerPlayerId = searchParams.get('asPlayer')
  const navigate = useNavigate()
  const onBack = () => navigate('/')
  const { t } = useTranslation()
  const { data, refetch } = useGameInstance(instanceId, viewerPlayerId)
  const snapshot = useGameInstanceStore((s) => s.snapshot)
  const setSnapshot = useGameInstanceStore((s) => s.setSnapshot)
  const connectionStatus = useGameInstanceStore((s) => s.connectionStatus)
  const setConnectionStatus = useGameInstanceStore((s) => s.setConnectionStatus)
  const reset = useGameInstanceStore((s) => s.reset)
  const [selectedCardId, setSelectedCardId] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const applyAction = useApplyPlayerAction(instanceId, viewerPlayerId)

  useEffect(() => {
    if (data) setSnapshot(data)
  }, [data, setSnapshot])

  useEffect(() => {
    setConnectionStatus('connecting')
    const client = connectToGameInstance(instanceId, viewerPlayerId, {
      onSnapshot: setSnapshot,
      onConnected: () => {
        setConnectionStatus('connected')
        refetch()
      },
      onDisconnected: () => setConnectionStatus('disconnected'),
    })
    return () => {
      client.deactivate()
      reset()
    }
    // instanceId/viewerPlayerId son la identidad de esta pantalla — un cambio en cualquiera
    // reconecta desde cero, así que no hace falta seguir setSnapshot/refetch/reset como deps.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [instanceId, viewerPlayerId])

  const playerActions = snapshot?.playerActions ?? []

  if (!snapshot) {
    return (
      <div className="max-w-5xl mx-auto py-8 px-4 space-y-6">
        <button onClick={onBack} className="px-4 py-2 text-blue-600 hover:text-blue-700 font-medium">
          {t('play.back')}
        </button>
        <p className="text-slate-600">{t('play.loading')}</p>
      </div>
    )
  }

  const state = snapshot.state
  const currentPlayer = state.players[state.currentPlayerIndex]
  const isMyTurn = viewerPlayerId != null && currentPlayer?.id === viewerPlayerId
  const winners = state.players.filter((player) => state.winners.includes(player.id))

  const handleTriggerAction = (eventType: string) => {
    if (!viewerPlayerId) return
    setActionError(null)
    applyAction.mutate(
      { playerId: viewerPlayerId, eventType, payload: selectedCardId ? { cardId: selectedCardId } : {} },
      {
        onSuccess: () => setSelectedCardId(null),
        onError: (err) => setActionError(t('play.actionFailedPrefix') + (err as Error).message),
      }
    )
  }

  return (
    <div className="max-w-5xl mx-auto py-8 px-4 space-y-6">
      <div className="flex items-center justify-between">
        <button onClick={onBack} className="px-4 py-2 text-blue-600 hover:text-blue-700 font-medium">
          {t('play.back')}
        </button>
        <ConnectionBadge status={connectionStatus} />
      </div>

      <div className="flex items-center justify-between flex-wrap gap-2">
        <h1 className="text-2xl font-bold text-slate-900">
          {state.ended
            ? t('play.gameOverTitle')
            : t('play.turnIndicator', { player: currentPlayer?.displayName ?? '' })}
        </h1>
        <p className="text-slate-600 text-sm">
          {viewerPlayerId
            ? t('play.viewingAs', { player: viewerPlayerId })
            : t('play.viewingAsSpectator')}
        </p>
      </div>

      {state.ended && (
        <div className="bg-amber-50 border border-amber-200 rounded-lg p-4 text-amber-900 font-medium">
          {winners.length === 0
            ? t('play.noWinnerAnnouncement')
            : t(winners.length > 1 ? 'play.winnersAnnouncement' : 'play.winnerAnnouncement', {
                players: winners.map((player) => player.displayName).join(', '),
              })}
        </div>
      )}

      <section>
        <h2 className="text-lg font-semibold text-slate-800 mb-2">{t('play.sharedZonesTitle')}</h2>
        <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
          {Object.entries(state.sharedZones).map(([name, cards]) => (
            <ZoneView key={name} name={name} cards={cards} />
          ))}
        </div>
      </section>

      {Object.entries(state.perPlayerZones).map(([zoneName, byOwner]) => (
        <section key={zoneName}>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {state.players.map((player) => {
              const hand = byOwner[player.id]
              if (!hand) return null
              return (
                <HandView
                  key={player.id}
                  playerDisplayName={player.displayName}
                  hand={hand}
                  isOwn={player.id === viewerPlayerId}
                  selectedCardId={selectedCardId}
                  onSelectCard={(cardId) =>
                    setSelectedCardId((prev) => (prev === cardId ? null : cardId))
                  }
                />
              )
            })}
          </div>
        </section>
      ))}

      {selectedCardId && (
        <p className="text-sm text-slate-600">
          {t('play.selectedCard', { id: selectedCardId })}{' '}
          <button onClick={() => setSelectedCardId(null)} className="text-blue-600 hover:underline">
            {t('play.clearSelection')}
          </button>
        </p>
      )}

      {actionError && <p className="text-red-600 text-sm">{actionError}</p>}

      <ActionPanel
        playerActions={playerActions}
        disabled={!viewerPlayerId || !isMyTurn || applyAction.isPending || state.ended}
        onTriggerAction={handleTriggerAction}
      />
    </div>
  )
}

function ConnectionBadge({ status }: { status: ConnectionStatus }) {
  const { t } = useTranslation()
  const colors: Record<ConnectionStatus, string> = {
    connected: 'bg-green-100 text-green-800',
    connecting: 'bg-amber-100 text-amber-800',
    disconnected: 'bg-red-100 text-red-800',
  }
  const labels: Record<ConnectionStatus, string> = {
    connected: t('play.connection.connected'),
    connecting: t('play.connection.connecting'),
    disconnected: t('play.connection.disconnected'),
  }
  return (
    <span className={`px-3 py-1 rounded-full text-xs font-medium ${colors[status]}`}>
      {labels[status]}
    </span>
  )
}
