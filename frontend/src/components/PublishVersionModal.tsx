import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { usePublishVersion } from '@/hooks/useGameDefinitions'
import Dialog from '@/components/ui/Dialog'

const schema = z.object({
  config: z.string().min(1, 'Config is required').refine(
    (value) => {
      try {
        JSON.parse(value)
        return true
      } catch {
        return false
      }
    },
    { message: 'Config must be valid JSON' }
  ),
})

type FormData = z.infer<typeof schema>

interface Props {
  gameDefinitionId: string
  open: boolean
  onOpenChange: (open: boolean) => void
}

export default function PublishVersionModal({
  gameDefinitionId,
  open,
  onOpenChange,
}: Props) {
  const { register, handleSubmit, formState: { errors }, reset } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      config: '{}',
    },
  })
  const publishMutation = usePublishVersion()

  const onSubmit = async (data: FormData) => {
    try {
      await publishMutation.mutateAsync({
        gameDefinitionId,
        config: JSON.parse(data.config),
      })
      reset()
      onOpenChange(false)
    } catch {
      // surfaced via publishMutation.error below
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange} title="Publish Version">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-slate-900 mb-1">
            Config (JSON)
          </label>
          <textarea
            {...register('config')}
            rows={8}
            placeholder='{"cardsPerPlayer": 7}'
            className="w-full px-3 py-2 border border-slate-300 rounded-lg font-mono text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          {errors.config && (
            <p className="mt-1 text-sm text-red-600">{errors.config.message}</p>
          )}
        </div>

        {publishMutation.isError && (
          <p className="text-sm text-red-600">{publishMutation.error.message}</p>
        )}

        <div className="flex justify-end gap-3 pt-4">
          <button
            type="button"
            onClick={() => onOpenChange(false)}
            className="px-4 py-2 text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={publishMutation.isPending}
            className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {publishMutation.isPending ? 'Publishing...' : 'Publish'}
          </button>
        </div>
      </form>
    </Dialog>
  )
}
