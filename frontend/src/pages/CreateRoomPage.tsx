import { useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { useCreateRoom, useRoom, useStartRoom } from '@/hooks/useRoom'
import { useTranslation } from '@/i18n/LanguageContext'

export default function CreateRoomPage() {
  const { gameDefinitionId = '' } = useParams<{ gameDefinitionId: string }>()
  const [searchParams] = useSearchParams()
  const versionNumber = Number(searchParams.get('version'))
  const navigate = useNavigate()
  const { t } = useTranslation()
  const [linkCopied, setLinkCopied] = useState(false)

  const createRoom = useCreateRoom()
  const startRoom = useStartRoom()
  const code = createRoom.data?.code
  const { data: room } = useRoom(code)
  const joinUrl = code ? `${window.location.origin}/rooms/join?code=${code}` : ''

  const handleCopyLink = () => {
    navigator.clipboard.writeText(joinUrl).then(() => {
      setLinkCopied(true)
      setTimeout(() => setLinkCopied(false), 2000)
    })
  }

  const handleCreate = () => {
    createRoom.mutate({ gameDefinitionId, versionNumber })
  }

  const handleStart = () => {
    if (!code) return
    startRoom.mutate(code, {
      onSuccess: (started) => {
        if (started.instanceId) navigate(`/instances/${started.instanceId}`)
      },
    })
  }

  return (
    <div className="max-w-2xl mx-auto py-8 px-4">
      <button
        onClick={() => navigate(`/games/${gameDefinitionId}/versions`)}
        className="mb-6 px-4 py-2 text-blue-600 hover:text-blue-700 font-medium"
      >
        {t('instance.back')}
      </button>
      <h1 className="text-3xl font-bold text-slate-900 mb-6">
        {t('room.createTitle', { version: versionNumber })}
      </h1>

      {!room ? (
        <div className="bg-white rounded-lg shadow p-6 space-y-4">
          <p className="text-slate-600">{t('room.createExplanation')}</p>
          {createRoom.isError && (
            <p className="text-red-600 text-sm">{(createRoom.error as Error).message}</p>
          )}
          <button
            onClick={handleCreate}
            disabled={createRoom.isPending}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-slate-300 transition-colors"
          >
            {createRoom.isPending ? t('common.creating') : t('room.createButton')}
          </button>
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow p-6 space-y-6">
          <div className="text-center">
            <p className="text-sm text-slate-500 mb-1">{t('room.codeLabel')}</p>
            <p className="text-5xl font-bold tracking-[0.3em] text-blue-600">{room.code}</p>
          </div>

          <div className="bg-slate-50 rounded-lg p-3 space-y-2">
            <p className="text-xs text-slate-500">{t('room.shareLinkLabel')}</p>
            <div className="flex items-center gap-2">
              <input
                readOnly
                value={joinUrl}
                onClick={(e) => e.currentTarget.select()}
                className="flex-1 min-w-0 px-3 py-2 border border-slate-300 rounded-lg bg-white text-sm text-slate-700"
              />
              <button
                onClick={handleCopyLink}
                className="shrink-0 px-3 py-2 bg-slate-200 hover:bg-slate-300 rounded-lg text-sm font-medium text-slate-700 transition-colors"
              >
                {linkCopied ? t('room.linkCopied') : t('room.copyLink')}
              </button>
            </div>
          </div>

          <div>
            <h2 className="text-sm font-semibold text-slate-700 mb-2">
              {t('room.playersJoined', { count: room.players.length })}
            </h2>
            {room.players.length === 0 ? (
              <p className="text-slate-500 text-sm">{t('room.waitingForPlayers')}</p>
            ) : (
              <ul className="flex flex-wrap gap-2">
                {room.players.map((p) => (
                  <li
                    key={p.id}
                    className="px-3 py-1.5 bg-slate-100 rounded-full text-sm font-medium text-slate-700"
                  >
                    {p.displayName}
                  </li>
                ))}
              </ul>
            )}
          </div>

          {startRoom.isError && (
            <p className="text-red-600 text-sm">{(startRoom.error as Error).message}</p>
          )}

          <button
            onClick={handleStart}
            disabled={room.players.length === 0 || startRoom.isPending}
            className="w-full px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:bg-slate-300 transition-colors"
          >
            {startRoom.isPending ? t('room.starting') : t('room.startButton')}
          </button>
        </div>
      )}
    </div>
  )
}
