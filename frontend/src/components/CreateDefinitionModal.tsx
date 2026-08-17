import { useMemo } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useCreateGameDefinition } from '@/hooks/useGameDefinitions'
import { useTranslation } from '@/i18n/LanguageContext'
import Dialog from '@/components/ui/Dialog'

interface Props {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export default function CreateDefinitionModal({ open, onOpenChange }: Props) {
  const { t } = useTranslation()

  const schema = useMemo(
    () =>
      z.object({
        ownerId: z.string().uuid(t('createDefinition.errors.ownerIdInvalid')),
        name: z.string().min(1, t('common.errors.nameRequired')).max(120),
        slug: z
          .string()
          .min(1, t('createDefinition.errors.slugRequired'))
          .max(120)
          .regex(/^[a-z0-9]+(-[a-z0-9]+)*$/, t('createDefinition.errors.slugPattern')),
      }),
    [t]
  )

  type FormData = z.infer<typeof schema>

  const { register, handleSubmit, formState: { errors }, reset } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      ownerId: crypto.randomUUID(),
    },
  })
  const createMutation = useCreateGameDefinition()

  const onSubmit = async (data: FormData) => {
    try {
      await createMutation.mutateAsync(data)
      reset({ ownerId: crypto.randomUUID(), name: '', slug: '' })
      onOpenChange(false)
    } catch {
      // surfaced via createMutation.error below
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange} title={t('createDefinition.title')}>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-slate-900 mb-1">
            {t('createDefinition.ownerIdLabel')}
          </label>
          <input
            {...register('ownerId')}
            type="text"
            placeholder={t('createDefinition.ownerIdPlaceholder')}
            className="w-full px-3 py-2 border border-slate-300 rounded-lg font-mono text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <p className="mt-1 text-xs text-slate-500">
            {t('createDefinition.ownerIdHelp')}
          </p>
          {errors.ownerId && (
            <p className="mt-1 text-sm text-red-600">{errors.ownerId.message}</p>
          )}
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-900 mb-1">
            {t('common.name')}
          </label>
          <input
            {...register('name')}
            type="text"
            placeholder={t('createDefinition.namePlaceholder')}
            className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          {errors.name && (
            <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>
          )}
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-900 mb-1">
            {t('common.slug')}
          </label>
          <input
            {...register('slug')}
            type="text"
            placeholder={t('createDefinition.slugPlaceholder')}
            className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          {errors.slug && (
            <p className="mt-1 text-sm text-red-600">{errors.slug.message}</p>
          )}
        </div>

        {createMutation.isError && (
          <p className="text-sm text-red-600">{createMutation.error.message}</p>
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
            disabled={createMutation.isPending}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {createMutation.isPending ? t('common.creating') : t('common.create')}
          </button>
        </div>
      </form>
    </Dialog>
  )
}
