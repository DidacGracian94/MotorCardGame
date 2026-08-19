import { CardDto, HiddenZoneDto } from '@/api/client'
import { useTranslation } from '@/i18n/LanguageContext'
import HiddenPile from '@/components/instance/HiddenPile'

interface Props {
  playerDisplayName: string
  hand: CardDto[] | HiddenZoneDto
  isOwn: boolean
  selectedCardId: string | null
  onSelectCard: (cardId: string) => void
}

function isHidden(hand: CardDto[] | HiddenZoneDto): hand is HiddenZoneDto {
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
        <HiddenPile count={hand.hiddenCount} />
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
