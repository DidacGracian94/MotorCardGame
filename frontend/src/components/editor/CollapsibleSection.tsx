import { ReactNode } from 'react'
import { useTranslation } from '@/i18n/LanguageContext'

interface Props {
  title: string
  isOpen: boolean
  onToggleOpen: () => void
  children: ReactNode
}

// Cabecera de sección plegable reutilizada en las secciones de primer nivel del editor visual
// (zonas, atributos, plantillas de carta, acciones de jugador, reglas...). Todo el encabezado
// —icono + título— es un único botón para maximizar el área pulsable, no solo el icono.
export default function CollapsibleSection({ title, isOpen, onToggleOpen, children }: Props) {
  const { t } = useTranslation()

  return (
    <section>
      <h2 className="mb-3">
        <button
          onClick={onToggleOpen}
          aria-expanded={isOpen}
          aria-label={isOpen ? t('editor.collapseSection') : t('editor.expandSection')}
          className="flex items-center gap-2 w-full text-left group"
        >
          <span
            aria-hidden="true"
            className="flex items-center justify-center w-9 h-9 rounded-md text-xl leading-none text-slate-500 shrink-0 transition-colors group-hover:text-slate-800 group-hover:bg-slate-100"
          >
            {isOpen ? '▾' : '▸'}
          </span>
          <span className="text-xl font-semibold text-slate-900">{title}</span>
        </button>
      </h2>
      {isOpen && <div>{children}</div>}
    </section>
  )
}
