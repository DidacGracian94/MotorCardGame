import { GameDefinitionDto } from '@/api/client'
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
  return (
    <div className="overflow-x-auto bg-white rounded-lg shadow">
      <table className="min-w-full divide-y divide-slate-200">
        <thead className="bg-slate-50">
          <tr>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">
              Name
            </th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">
              Slug
            </th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">
              Created
            </th>
            <th className="px-6 py-3 text-right text-sm font-semibold text-slate-900">
              Actions
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
                {formatDate(def.createdAt)}
              </td>
              <td className="px-6 py-4 text-right space-x-2">
                <button
                  onClick={() => onViewVersions(def.id)}
                  className="text-blue-600 hover:text-blue-700 font-medium text-sm"
                >
                  Versions
                </button>
                <button
                  onClick={() => onRename(def.id)}
                  className="text-amber-600 hover:text-amber-700 font-medium text-sm"
                >
                  Rename
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
