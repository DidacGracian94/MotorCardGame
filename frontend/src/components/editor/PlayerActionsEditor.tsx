import { useTranslation } from '@/i18n/LanguageContext'

interface Props {
  playerActions: string[]
  onChange: (playerActions: string[]) => void
  readOnly?: boolean
}

export default function PlayerActionsEditor({ playerActions, onChange, readOnly = false }: Props) {
  const { t } = useTranslation()

  const renameAt = (index: number, name: string) => {
    onChange(playerActions.map((existing, i) => (i === index ? name : existing)))
  }

  const removeAt = (index: number) => {
    onChange(playerActions.filter((_, i) => i !== index))
  }

  const addPlayerAction = () => {
    // Nace sin nombre (el input queda vacío, mostrando el placeholder) en vez de con un texto
    // real que haya que borrar. Solo puede haber una acción sin nombrar a la vez.
    if (playerActions.includes('')) return
    onChange([...playerActions, ''])
  }

  return (
    <div className="overflow-x-auto bg-white rounded-lg shadow">
      <table className="min-w-full divide-y divide-slate-200">
        <thead className="bg-slate-50">
          <tr>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">{t('common.name')}</th>
            <th className="px-6 py-3 text-right text-sm font-semibold text-slate-900">{t('common.actions')}</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-200">
          {playerActions.map((name, index) => (
            <tr key={index} className="hover:bg-slate-50 transition-colors">
              <td className="px-6 py-4 text-sm">
                <input
                  type="text"
                  value={name}
                  onChange={(e) => renameAt(index, e.target.value)}
                  placeholder={t('editor.playerActionNamePlaceholder')}
                  disabled={readOnly}
                  className="w-full px-2 py-1 border border-slate-300 rounded text-sm disabled:bg-slate-50 disabled:text-slate-600"
                />
              </td>
              <td className="px-6 py-4 text-right">
                {!readOnly && (
                  <button
                    onClick={() => removeAt(index)}
                    className="text-red-600 hover:text-red-700 font-medium text-sm"
                  >
                    {t('common.remove')}
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {!readOnly && (
        <div className="p-4">
          <button onClick={addPlayerAction} className="text-blue-600 hover:text-blue-700 font-medium text-sm">
            {t('editor.addPlayerAction')}
          </button>
        </div>
      )}
    </div>
  )
}
