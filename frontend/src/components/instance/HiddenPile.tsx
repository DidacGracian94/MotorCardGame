import { useTranslation } from '@/i18n/LanguageContext'

// Tope de dorsos de carta a dibujar: una mano oculta suele tener pocas cartas (encaja bien un
// dorso por carta), pero un mazo oculto puede tener decenas — por encima de esto ya no aporta
// información visual y solo son más nodos DOM, así que a partir de aquí el recuento en texto es
// la única señal.
const MAX_BACKS_SHOWN = 8

interface Props {
  count: number
}

export default function HiddenPile({ count }: Props) {
  const { t } = useTranslation()
  const backsShown = Math.min(count, MAX_BACKS_SHOWN)

  return (
    <div>
      <div className="flex gap-1 flex-wrap items-center mb-1" aria-hidden>
        {Array.from({ length: backsShown }).map((_, i) => (
          <div key={i} className="w-10 h-14 rounded-md bg-slate-700 border border-slate-800" />
        ))}
      </div>
      <p className="text-slate-500 text-sm">
        {count === 0 ? t('play.emptyZone') : t('play.hiddenCards', { count })}
      </p>
    </div>
  )
}
