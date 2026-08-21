import { Fragment, useState } from 'react'
import { CapabilitiesDto, GameDefinitionVersionDto } from '@/api/client'
import { useTranslation } from '@/i18n/LanguageContext'
import { formatDate } from '@/lib/utils'
import VersionConfigViewer from '@/components/VersionConfigViewer'

interface Props {
  versions: GameDefinitionVersionDto[]
  catalogs: CapabilitiesDto
  onEditAsNewVersion: (versionNumber: number) => void
  onCreateInstance: (versionNumber: number) => void
  onCreateRoom: (versionNumber: number) => void
}

export default function VersionsTable({
  versions,
  catalogs,
  onEditAsNewVersion,
  onCreateInstance,
  onCreateRoom,
}: Props) {
  const { t } = useTranslation()
  const [expandedId, setExpandedId] = useState<string | null>(null)

  const toggle = (id: string) => setExpandedId((prev) => (prev === id ? null : id))

  return (
    <div className="overflow-x-auto bg-white rounded-lg shadow">
      <table className="min-w-full divide-y divide-slate-200">
        <thead className="bg-slate-50">
          <tr>
            <th className="w-12 px-3 py-3" />
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">
              {t('versions.columnVersion')}
            </th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">
              {t('versions.columnPublishedAt')}
            </th>
            <th className="px-6 py-3 text-right text-sm font-semibold text-slate-900">
              {t('common.actions')}
            </th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-200">
          {versions.map((version) => {
            const isOpen = expandedId === version.id
            return (
              <Fragment key={version.id}>
                <tr
                  onClick={() => toggle(version.id)}
                  className="hover:bg-slate-50 transition-colors cursor-pointer"
                >
                  <td className="px-3 py-4" onClick={(e) => e.stopPropagation()}>
                    <button
                      onClick={() => toggle(version.id)}
                      aria-expanded={isOpen}
                      aria-label={isOpen ? t('editor.collapseSection') : t('editor.expandSection')}
                      className="flex items-center justify-center w-9 h-9 rounded-md text-xl leading-none text-slate-500 hover:text-slate-800 hover:bg-slate-100 transition-colors"
                    >
                      {isOpen ? '▾' : '▸'}
                    </button>
                  </td>
                  <td className="px-6 py-4 text-sm font-medium text-slate-900">
                    v{version.versionNumber}
                  </td>
                  <td className="px-6 py-4 text-sm text-slate-600">
                    {version.publishedAt ? formatDate(version.publishedAt) : '—'}
                  </td>
                  <td className="px-6 py-4 text-right space-x-4" onClick={(e) => e.stopPropagation()}>
                    <button
                      onClick={() => onCreateInstance(version.versionNumber)}
                      className="text-blue-600 hover:text-blue-700 font-medium text-sm"
                    >
                      {t('instance.createButton')}
                    </button>
                    <button
                      onClick={() => onCreateRoom(version.versionNumber)}
                      className="text-green-600 hover:text-green-700 font-medium text-sm"
                    >
                      {t('room.createButton')}
                    </button>
                    <button
                      onClick={() => onEditAsNewVersion(version.versionNumber)}
                      className="text-amber-600 hover:text-amber-700 font-medium text-sm"
                    >
                      {t('versions.editAsNewVersion')}
                    </button>
                  </td>
                </tr>
                {isOpen && (
                  <tr className="bg-slate-50">
                    <td colSpan={4} className="px-6 py-4">
                      <VersionConfigViewer config={version.config} catalogs={catalogs} />
                    </td>
                  </tr>
                )}
              </Fragment>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
