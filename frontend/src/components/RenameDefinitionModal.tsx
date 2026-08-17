import { useMemo } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { GameDefinitionDto } from '@/api/client'
import { useRenameGameDefinition } from '@/hooks/useGameDefinitions'
import { useTranslation } from '@/i18n/LanguageContext'
import Dialog from '@/components/ui/Dialog'

interface Props {
  definition: GameDefinitionDto
  open: boolean
  onOpenChange: (open: boolean) => void
}

export default function RenameDefinitionModal({
  definition,
  open,
  onOpenChange,
}: Props) {
  const { t } = useTranslation()

  const schema = useMemo(
    () =>
      z.object({
        name: z.string().min(1, t('common.errors.nameRequired')).max(120),
      }),
    [t]
  )

  type FormData = z.infer<typeof schema>

  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      name: definition.name,
    },
  })
  const renameMutation = useRenameGameDefinition()

  const onSubmit = async (data: FormData) => {
    try {
      await renameMutation.mutateAsync({ id: definition.id, name: data.name })
      onOpenChange(false)
    } catch {
      // surfaced via renameMutation.error below
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange} title={t('renameDefinition.title')}>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-slate-900 mb-1">
            {t('common.name')}
          </label>
          <input
            {...register('name')}
            type="text"
            className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          {errors.name && (
            <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>
          )}
        </div>

        {renameMutation.isError && (
          <p className="text-sm text-red-600">{renameMutation.error.message}</p>
        )}

        <div className="flex justify-end gap-3 pt-4">
          <button
            type="button"
            onClick={() => onOpenChange(false)}
            className="px-4 py-2 text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
          >
            {t('common.cancel')}
          </button>
          <button
            type="submit"
            disabled={renameMutation.isPending}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {renameMutation.isPending ? t('common.updating') : t('common.update')}
          </button>
        </div>
      </form>
    </Dialog>
  )
}
