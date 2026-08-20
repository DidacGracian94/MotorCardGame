import { useAuth } from '@/auth/AuthContext'
import { GameDefinitionDto } from '@/api/client'
import { useSetGameDefinitionVisibility } from '@/hooks/useGameDefinitions'
import { useTranslation } from '@/i18n/LanguageContext'
import { formatDate } from '@/lib/utils'

interface Props {
  definitions: GameDefinitionDto[]
  onViewVersions: (id: string) => void
  onRename: (id: string) => void
}

export default function GameDefinitionsTable({
  definitions,
  onViewVersions,
  onRename,
}: Props) {
  const { t, tf } = useTranslation()
  const { user } = useAuth()
  const setVisibility = useSetGameDefinitionVisibility()

  const canEdit = (def: GameDefinitionDto) => user?.role === 'ADMIN' || def.ownerId === user?.id

  return (
    <div className="overflow-x-auto bg-white rounded-lg shadow">
      <table className="min-w-full divide-y divide-slate-200">
        <thead className="bg-slate-50">
          <tr>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">
              {t('common.name')}
            </th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">
              {t('common.slug')}
            </th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">
              {t('gameDefinitions.columnVisibility')}
            </th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">
              {t('common.created')}
            </th>
            <th className="px-6 py-3 text-right text-sm font-semibold text-slate-900">
              {t('common.actions')}
            </th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-200">
          {definitions.map((def) => (
            <tr key={def.id} className="hover:bg-slate-50 transition-colors">
              <td className="px-6 py-4 text-sm font-medium text-slate-900">
                {def.name}
              </td>
              <td className="px-6 py-4 text-sm text-slate-600">
                {def.slug}
              </td>
              <td className="px-6 py-4 text-sm text-slate-600">
                <span
                  className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                    def.visibility === 'PUBLIC'
                      ? 'bg-green-100 text-green-800'
                      : 'bg-slate-100 text-slate-700'
                  }`}
                >
                  {tf(`enum.${def.visibility}`, def.visibility)}
                </span>
              </td>
              <td className="px-6 py-4 text-sm text-slate-600">
                {formatDate(def.createdAt)}
              </td>
              <td className="px-6 py-4 text-right space-x-2">
                <button
                  onClick={() => onViewVersions(def.id)}
                  className="text-blue-600 hover:text-blue-700 font-medium text-sm"
                >
                  {t('gameDefinitions.viewVersions')}
                </button>
                {canEdit(def) && (
                  <>
                    <button
                      onClick={() => onRename(def.id)}
                      className="text-amber-600 hover:text-amber-700 font-medium text-sm"
                    >
                      {t('common.rename')}
                    </button>
                    <button
                      onClick={() =>
                        setVisibility.mutate({
                          id: def.id,
                          visibility: def.visibility === 'PUBLIC' ? 'PRIVATE' : 'PUBLIC',
                        })
                      }
                      disabled={setVisibility.isPending}
                      className="text-slate-600 hover:text-slate-700 font-medium text-sm disabled:opacity-50"
                    >
                      {def.visibility === 'PUBLIC'
                        ? t('gameDefinitions.makePrivate')
                        : t('gameDefinitions.makePublic')}
                    </button>
                  </>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
