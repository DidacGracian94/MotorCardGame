import { CardDto } from '@/api/client'
import { useTranslation } from '@/i18n/LanguageContext'

interface Props {
  name: string
  cards: CardDto[]
}

export default function ZoneView({ name, cards }: Props) {
  const { t } = useTranslation()
  const topCard = cards[0]

  return (
    <div className="bg-white rounded-lg shadow p-4">
      <h3 className="font-semibold text-slate-900 mb-2">{name}</h3>
      {cards.length === 0 ? (
        <p className="text-slate-500 text-sm">{t('play.emptyZone')}</p>
      ) : (
        <div className="space-y-2">
          <p className="text-sm text-slate-600">{t('play.cardCount', { count: cards.length })}</p>
          <CardFace card={topCard} />
        </div>
      )}
    </div>
  )
}

export function CardFace({ card }: { card: CardDto }) {
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
