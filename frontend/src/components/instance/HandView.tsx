import { CardDto, HiddenHandDto } from '@/api/client'
import { useTranslation } from '@/i18n/LanguageContext'

interface Props {
  playerDisplayName: string
  hand: CardDto[] | HiddenHandDto
  isOwn: boolean
  selectedCardId: string | null
  onSelectCard: (cardId: string) => void
}

function isHidden(hand: CardDto[] | HiddenHandDto): hand is HiddenHandDto {
  return !Array.isArray(hand)
}

export default function HandView({
  playerDisplayName,
  hand,
  isOwn,
  selectedCardId,
  onSelectCard,
}: Props) {
  const { t } = useTranslation()

  return (
    <div className="bg-white rounded-lg shadow p-4">
      <h3 className="font-semibold text-slate-900 mb-2">
        {isOwn
          ? t('play.yourHandTitle')
          : t('play.opponentHandTitle', { player: playerDisplayName })}
      </h3>
      {isHidden(hand) ? (
        <div>
          <div className="flex gap-1 flex-wrap items-center mb-1" aria-hidden>
            {Array.from({ length: hand.hiddenCount }).map((_, i) => (
              <div key={i} className="w-10 h-14 rounded-md bg-slate-700 border border-slate-800" />
            ))}
          </div>
          <p className="text-slate-500 text-sm">
            {hand.hiddenCount === 0 ? t('play.emptyZone') : t('play.hiddenCards', { count: hand.hiddenCount })}
          </p>
        </div>
      ) : (
        <div className="flex gap-2 flex-wrap">
          {hand.length === 0 && <p className="text-slate-500 text-sm">{t('play.emptyZone')}</p>}
          {hand.map((card) => (
            <button
              key={card.id}
              onClick={() => onSelectCard(card.id)}
              className={`border rounded-md px-3 py-2 text-sm text-left transition-colors ${
                selectedCardId === card.id
                  ? 'border-blue-600 bg-blue-50 ring-2 ring-blue-300'
                  : 'border-slate-300 bg-slate-50 hover:bg-slate-100'
              }`}
            >
              <div className="font-mono text-xs text-slate-500">{card.id}</div>
              {Object.entries(card.attributes).map(([key, value]) => (
                <div key={key} className="text-slate-800">
                  {key}: {String(value)}
                </div>
              ))}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
