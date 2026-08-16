import { GameDefinitionVersionDto } from '@/api/client'
import { formatDate } from '@/lib/utils'

interface Props {
  versions: GameDefinitionVersionDto[]
}

export default function VersionsTable({ versions }: Props) {
  return (
    <div className="overflow-x-auto bg-white rounded-lg shadow">
      <table className="min-w-full divide-y divide-slate-200">
        <thead className="bg-slate-50">
          <tr>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">
              Version
            </th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">
              Published At
            </th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">
              Config
            </th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-200">
          {versions.map((version) => (
            <tr key={version.id} className="hover:bg-slate-50 transition-colors">
              <td className="px-6 py-4 text-sm font-medium text-slate-900">
                v{version.versionNumber}
              </td>
              <td className="px-6 py-4 text-sm text-slate-600">
                {version.publishedAt ? formatDate(version.publishedAt) : '—'}
              </td>
              <td className="px-6 py-4 text-sm text-slate-600">
                <pre className="max-w-xs overflow-x-auto whitespace-pre-wrap font-mono text-xs">
                  {JSON.stringify(version.config, null, 2)}
                </pre>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
