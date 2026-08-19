import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  api,
  CreateGameInstanceRequest,
  PlayerActionRequest,
} from '@/api/client'

const GAME_INSTANCE_KEY = (instanceId: string, asPlayer: string | null) => [
  'gameInstance',
  instanceId,
  asPlayer,
]

export function useCreateGameInstance() {
  return useMutation({
    mutationFn: ({
      gameDefinitionId,
      data,
      asPlayer,
    }: {
      gameDefinitionId: string
      data: CreateGameInstanceRequest
      asPlayer?: string
    }) => api.gameInstances.create(gameDefinitionId, data, asPlayer),
  })
}

export function useGameInstance(
  instanceId: string | undefined,
  asPlayer: string | null
) {
  return useQuery({
    queryKey: GAME_INSTANCE_KEY(instanceId ?? '', asPlayer),
    queryFn: () => api.gameInstances.get(instanceId!, asPlayer ?? undefined),
    enabled: instanceId != null,
  })
}

export function useApplyPlayerAction(
  instanceId: string,
  asPlayer: string | null
) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: PlayerActionRequest) =>
      api.gameInstances.applyAction(instanceId, data, asPlayer ?? undefined),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: GAME_INSTANCE_KEY(instanceId, asPlayer),
      })
    },
  })
}
