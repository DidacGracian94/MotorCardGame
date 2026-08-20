import { useQuery, useQueryClient, useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/auth/AuthContext'
import { api, UserRole } from '@/api/client'
import { useTranslation } from '@/i18n/LanguageContext'

const ADMIN_USERS_KEY = ['admin', 'users']

export default function AdminUsersPage() {
  const { t } = useTranslation()
  const { user: currentUser } = useAuth()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { data: users, isLoading, error } = useQuery({
    queryKey: ADMIN_USERS_KEY,
    queryFn: () => api.admin.users.list(),
  })
  const changeRole = useMutation({
    mutationFn: ({ id, role }: { id: string; role: UserRole }) => api.admin.users.changeRole(id, role),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ADMIN_USERS_KEY }),
  })

  return (
    <div className="max-w-4xl mx-auto py-8 px-4">
      <button
        onClick={() => navigate('/')}
        className="mb-6 px-4 py-2 text-blue-600 hover:text-blue-700 font-medium"
      >
        {t('common.backToDefinitions')}
      </button>

      <h1 className="text-3xl font-bold text-slate-900 mb-8">{t('admin.usersTitle')}</h1>

      {isLoading && <p className="text-slate-600">{t('common.loading')}</p>}
      {error && <p className="text-red-600">{(error as Error).message}</p>}

      {users && (
        <div className="overflow-x-auto bg-white rounded-lg shadow">
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">
                  {t('admin.columnDisplayName')}
                </th>
                <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">
                  {t('admin.columnEmail')}
                </th>
                <th className="px-6 py-3 text-left text-sm font-semibold text-slate-900">
                  {t('admin.columnRole')}
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200">
              {users.map((u) => {
                const isSelf = u.id === currentUser?.id
                return (
                  <tr key={u.id} className="hover:bg-slate-50 transition-colors">
                    <td className="px-6 py-4 text-sm font-medium text-slate-900">{u.displayName}</td>
                    <td className="px-6 py-4 text-sm text-slate-600">{u.email}</td>
                    <td className="px-6 py-4 text-sm">
                      <select
                        value={u.role}
                        disabled={isSelf || changeRole.isPending}
                        title={isSelf ? t('admin.cannotChangeOwnRole') : undefined}
                        onChange={(e) =>
                          changeRole.mutate({ id: u.id, role: e.target.value as UserRole })
                        }
                        className="px-2 py-1 border border-slate-300 rounded-lg text-sm disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none focus:ring-2 focus:ring-blue-500"
                      >
                        <option value="USER">{t('admin.role.USER')}</option>
                        <option value="ADMIN">{t('admin.role.ADMIN')}</option>
                      </select>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}

      {changeRole.isError && (
        <p className="mt-4 text-sm text-red-600">{(changeRole.error as Error).message}</p>
      )}
    </div>
  )
}
