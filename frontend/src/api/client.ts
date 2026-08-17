const BASE_URL = '/api'

export async function apiCall<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${BASE_URL}${endpoint}`
  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  })

  if (!response.ok) {
    const error = await response.text()
    throw new Error(`API error: ${response.status} - ${error}`)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json()
}

export const api = {
  gameDefinitions: {
    list: () => apiCall<GameDefinitionDto[]>('/game-definitions'),
    create: (data: CreateGameDefinitionRequest) =>
      apiCall<GameDefinitionDto>('/game-definitions', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    rename: (id: string, name: string) =>
      apiCall<void>(`/game-definitions/${id}`, {
        method: 'PUT',
        body: JSON.stringify({ name }),
      }),
    versions: {
      list: (id: string) =>
        apiCall<GameDefinitionVersionDto[]>(
          `/game-definitions/${id}/versions`
        ),
      publish: (id: string, data: PublishVersionRequest) =>
        apiCall<GameDefinitionVersionDto>(
          `/game-definitions/${id}/versions`,
          {
            method: 'POST',
            body: JSON.stringify(data),
          }
        ),
      get: (gameDefinitionId: string, versionNumber: number) =>
        apiCall<GameDefinitionVersionDto>(
          `/game-definitions/${gameDefinitionId}/versions/${versionNumber}`
        ),
    },
  },
  capabilities: {
    list: () => apiCall<CapabilitiesDto>('/capabilities'),
  },
}

export interface GameDefinitionDto {
  id: string
  ownerId: string
  name: string
  slug: string
  createdAt: string
  updatedAt: string
}

export interface GameDefinitionVersionDto {
  id: string
  gameDefinitionId: string
  versionNumber: number
  config: unknown
  createdAt: string
  publishedAt: string | null
}

export interface CreateGameDefinitionRequest {
  ownerId: string
  name: string
  slug: string
}

export interface PublishVersionRequest {
  config: unknown
}

export interface CapabilityFieldDto {
  name: string
  kind:
    | 'ZONE_REF'
    | 'TEXT'
    | 'INTEGER'
    | 'BOOLEAN'
    | 'SCALAR'
    | 'ENUM'
    | 'CONDITION'
    | 'CONDITION_LIST'
    | 'ACTION'
    | 'ACTION_LIST'
  required: boolean
  defaultValue: string | null
  enumValues: string[] | null
}

export interface CapabilityDto {
  name: string
  fields: CapabilityFieldDto[]
}

export interface CapabilitiesDto {
  actions: CapabilityDto[]
  conditions: CapabilityDto[]
  targets: CapabilityDto[]
}
