import { useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { GameInstancePlayerDto } from '@/api/client'
import { useCreateGameInstance } from '@/hooks/useGameInstance'
import { useTranslation } from '@/i18n/LanguageContext'

export default function CreateInstancePage() {
  const { gameDefinitionId = '' } = useParams<{ gameDefinitionId: string }>()
  const [searchParams] = useSearchParams()
  const versionNumber = Number(searchParams.get('version'))
  const navigate = useNavigate()
  const { t } = useTranslation()
  const [players, setPlayers] = useState<GameInstancePlayerDto[]>([
    { id: '', displayName: '' },
    { id: '', displayName: '' },
  ])
  const [validationError, setValidationError] = useState<string | null>(null)
  const [createdInstanceId, setCreatedInstanceId] = useState<string | null>(null)
  const createInstance = useCreateGameInstance()

  const updatePlayer = (index: number, field: 'id' | 'displayName', value: string) => {
    setPlayers((prev) => prev.map((p, i) => (i === index ? { ...p, [field]: value } : p)))
  }

  const addPlayer = () => setPlayers((prev) => [...prev, { id: '', displayName: '' }])
  const removePlayer = (index: number) => setPlayers((prev) => prev.filter((_, i) => i !== index))

  const validate = (): string | null => {
    if (players.some((p) => p.id.trim() === '')) return t('instance.errors.playerIdRequired')
    if (players.some((p) => p.displayName.trim() === '')) return t('instance.errors.displayNameRequired')
    const ids = players.map((p) => p.id.trim())
    if (new Set(ids).size !== ids.length) return t('instance.errors.duplicatePlayerId')
    return null
  }

  const handleCreate = () => {
    const error = validate()
    if (error) {
      setValidationError(error)
      return
    }
    setValidationError(null)
    createInstance.mutate(
      { gameDefinitionId, data: { versionNumber, players } },
      { onSuccess: (created) => setCreatedInstanceId(created.id) }
    )
  }

  if (createdInstanceId) {
    return (
      <div className="max-w-2xl mx-auto py-8 px-4">
        <div className="bg-white rounded-lg shadow p-6 space-y-4">
          <p className="text-green-700">{t('instance.createdSuccessfully')}</p>
          <p className="text-slate-500 text-sm">{t('instance.newTabHint')}</p>
          <ul className="space-y-2">
            {players.map((p) => (
              <li key={p.id}>
                <Link
                  to={`/instances/${createdInstanceId}?asPlayer=${encodeURIComponent(p.id)}`}
                  className="text-blue-600 hover:text-blue-700 font-medium"
                >
                  {t('instance.enterAs', { player: p.displayName })}
                </Link>
              </li>
            ))}
            <li>
              <Link
                to={`/instances/${createdInstanceId}`}
                className="text-slate-600 hover:text-slate-700 font-medium"
              >
                {t('instance.enterAsSpectator')}
              </Link>
            </li>
          </ul>
        </div>
      </div>
    )
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
        {t('instance.createTitle', { version: versionNumber })}
      </h1>
      <div className="bg-white rounded-lg shadow p-6 space-y-4">
        <label className="block text-sm font-medium text-slate-700">{t('instance.playersLabel')}</label>
        {players.map((player, index) => (
          <div key={index} className="flex gap-2 items-center">
            <input
              value={player.id}
              onChange={(e) => updatePlayer(index, 'id', e.target.value)}
              placeholder={t('instance.playerIdPlaceholder')}
              className="flex-1 border border-slate-300 rounded-md px-3 py-2"
            />
            <input
              value={player.displayName}
              onChange={(e) => updatePlayer(index, 'displayName', e.target.value)}
              placeholder={t('instance.displayNamePlaceholder')}
              className="flex-1 border border-slate-300 rounded-md px-3 py-2"
            />
            {players.length > 1 && (
              <button
                onClick={() => removePlayer(index)}
                aria-label={t('instance.removePlayer')}
                className="text-red-600 hover:text-red-700 px-2"
              >
                ✕
              </button>
            )}
          </div>
        ))}
        <button onClick={addPlayer} className="text-blue-600 hover:text-blue-700 font-medium text-sm">
          {t('instance.addPlayer')}
        </button>

        {validationError && <p className="text-red-600 text-sm">{validationError}</p>}
        {createInstance.isError && (
          <p className="text-red-600 text-sm">{(createInstance.error as Error).message}</p>
        )}

        <button
          onClick={handleCreate}
          disabled={createInstance.isPending}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-slate-300 transition-colors"
        >
          {createInstance.isPending ? t('common.creating') : t('common.create')}
        </button>
      </div>
    </div>
  )
}
