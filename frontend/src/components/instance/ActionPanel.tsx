import { useTranslation } from '@/i18n/LanguageContext'

interface Props {
  playerActions: string[]
  disabled: boolean
  onTriggerAction: (eventType: string) => void
}

/**
 * Un botón por cada playerAction declarado en la config de la versión. El nombre del evento es
 * texto libre que escribió el diseñador del juego en el editor (p.ej. "PLAY_CARD") — no vocabulario
 * del motor — así que se muestra tal cual, sin pasar por t()/tf().
 */
export default function ActionPanel({ playerActions, disabled, onTriggerAction }: Props) {
  const { t } = useTranslation()

  return (
    <div className="bg-white rounded-lg shadow p-4">
      <h3 className="font-semibold text-slate-900 mb-2">{t('play.actionsTitle')}</h3>
      {playerActions.length === 0 ? (
        <p className="text-slate-500 text-sm">{t('play.noActions')}</p>
      ) : (
        <div className="flex gap-2 flex-wrap">
          {playerActions.map((action) => (
            <button
              key={action}
              disabled={disabled}
              onClick={() => onTriggerAction(action)}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-slate-300 disabled:cursor-not-allowed transition-colors"
            >
              {action}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
