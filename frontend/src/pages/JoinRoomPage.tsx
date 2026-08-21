import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '@/auth/AuthContext'
import { useJoinRoom, useRoom } from '@/hooks/useRoom'
import { useTranslation } from '@/i18n/LanguageContext'

interface JoinedSession {
  code: string
  playerId: string
}

export default function JoinRoomPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { loginAsGuest } = useAuth()
  const [searchParams] = useSearchParams()
  const [code, setCode] = useState(() => searchParams.get('code')?.toUpperCase() ?? '')
  const [displayName, setDisplayName] = useState('')
  const [session, setSession] = useState<JoinedSession | null>(null)

  const joinRoom = useJoinRoom()
  const { data: room } = useRoom(session?.code)

  useEffect(() => {
    if (room?.status === 'STARTED' && room.instanceId && session) {
      navigate(`/instances/${room.instanceId}?asPlayer=${encodeURIComponent(session.playerId)}`)
    }
  }, [room, session, navigate])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const trimmedCode = code.trim().toUpperCase()
    const trimmedName = displayName.trim()
    if (!trimmedCode || !trimmedName) return
    joinRoom.mutate(
      { code: trimmedCode, data: { displayName: trimmedName } },
      {
        onSuccess: (response) => {
          loginAsGuest(response.accessToken, response.playerId, response.displayName)
          setSession({ code: trimmedCode, playerId: response.playerId })
        },
      }
    )
  }

  if (session && room) {
    return (
      <div className="max-w-sm mx-auto py-16 px-4 text-center">
        <p className="text-slate-500 mb-2">{t('room.joinedAs', { name: displayName })}</p>
        <p className="text-4xl font-bold tracking-[0.3em] text-blue-600 mb-6">{room.code}</p>
        <p className="text-slate-600">{t('room.waitingForHost')}</p>
        <ul className="mt-6 flex flex-wrap justify-center gap-2">
          {room.players.map((p) => (
            <li
              key={p.id}
              className="px-3 py-1.5 bg-slate-100 rounded-full text-sm font-medium text-slate-700"
            >
              {p.displayName}
            </li>
          ))}
        </ul>
      </div>
    )
  }

  return (
    <div className="max-w-sm mx-auto py-16 px-4">
      <h1 className="text-2xl font-bold text-slate-900 mb-6">{t('room.joinTitle')}</h1>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-slate-900 mb-1">{t('room.codeLabel')}</label>
          <input
            value={code}
            onChange={(e) => setCode(e.target.value)}
            placeholder={t('room.codePlaceholder')}
            maxLength={6}
            className="w-full px-3 py-2 border border-slate-300 rounded-lg uppercase tracking-widest text-center text-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-slate-900 mb-1">{t('room.displayNameLabel')}</label>
          <input
            value={displayName}
            onChange={(e) => setDisplayName(e.target.value)}
            placeholder={t('instance.displayNamePlaceholder')}
            className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {joinRoom.isError && <p className="text-sm text-red-600">{(joinRoom.error as Error).message}</p>}

        <button
          type="submit"
          disabled={joinRoom.isPending || !code.trim() || !displayName.trim()}
          className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {joinRoom.isPending ? t('room.joining') : t('room.joinButton')}
        </button>
      </form>
    </div>
  )
}
