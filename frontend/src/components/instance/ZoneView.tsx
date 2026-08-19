import { CardDto, HiddenZoneDto } from '@/api/client'
import { useTranslation } from '@/i18n/LanguageContext'
import HiddenPile from '@/components/instance/HiddenPile'

interface Props {
  name: string
  cards: CardDto[] | HiddenZoneDto
}

function isHidden(cards: CardDto[] | HiddenZoneDto): cards is HiddenZoneDto {
  return !Array.isArray(cards)
}

export default function ZoneView({ name, cards }: Props) {
  const { t } = useTranslation()

  return (
    <div className="bg-white rounded-lg shadow p-4">
      <h3 className="font-semibold text-slate-900 mb-2">{name}</h3>
      {isHidden(cards) ? (
        <HiddenPile count={cards.hiddenCount} />
      ) : cards.length === 0 ? (
        <p className="text-slate-500 text-sm">{t('play.emptyZone')}</p>
      ) : (
        <div className="space-y-2">
          <p className="text-sm text-slate-600">{t('play.cardCount', { count: cards.length })}</p>
          <CardFace card={cards[0]} />
        </div>
      )}
    </div>
  )
}

function CardFace({ card }: { card: CardDto }) {
  return (
    <div className="inline-block border border-slate-300 rounded-md px-3 py-2 bg-slate-50 text-sm">
      <div className="font-mono text-xs text-slate-500">{card.id}</div>
      {Object.entries(card.attributes).map(([key, value]) => (
        <div key={key} className="text-slate-800">
          {key}: {String(value)}
        </div>
      ))}
    </div>
  )
}
