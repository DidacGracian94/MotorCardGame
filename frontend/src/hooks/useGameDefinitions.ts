import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api, CreateGameDefinitionRequest } from '@/api/client'

const GAME_DEFINITIONS_KEY = ['gameDefinitions']
const GAME_DEFINITION_VERSIONS_KEY = (id: string) => [
  'gameDefinitions',
  id,
  'versions',
]

export function useGameDefinitions() {
  return useQuery({
    queryKey: GAME_DEFINITIONS_KEY,
    queryFn: () => api.gameDefinitions.list(),
  })
}

export function useCreateGameDefinition() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: CreateGameDefinitionRequest) =>
      api.gameDefinitions.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: GAME_DEFINITIONS_KEY })
    },
  })
}

export function useRenameGameDefinition() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, name }: { id: string; name: string }) =>
      api.gameDefinitions.rename(id, name),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: GAME_DEFINITIONS_KEY })
      queryClient.invalidateQueries({
        queryKey: GAME_DEFINITION_VERSIONS_KEY(id),
      })
    },
  })
}

export function useGameDefinitionVersions(gameDefinitionId: string) {
  return useQuery({
    queryKey: GAME_DEFINITION_VERSIONS_KEY(gameDefinitionId),
    queryFn: () => api.gameDefinitions.versions.list(gameDefinitionId),
  })
}

export function usePublishVersion() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({
      gameDefinitionId,
      config,
    }: {
      gameDefinitionId: string
      config: unknown
    }) => api.gameDefinitions.versions.publish(gameDefinitionId, { config }),
    onSuccess: (_, { gameDefinitionId }) => {
      queryClient.invalidateQueries({
        queryKey: GAME_DEFINITION_VERSIONS_KEY(gameDefinitionId),
      })
    },
  })
}
