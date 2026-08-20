import {
  clearTokens,
  getAccessToken,
  getRefreshToken,
  notifyAuthExpired,
  setAccessToken,
  setRefreshToken,
} from '@/auth/tokenStore'

const BASE_URL = '/api'

function rawFetch(endpoint: string, options: RequestInit = {}): Promise<Response> {
  const token = getAccessToken()
  return fetch(`${BASE_URL}${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })
}

// Un 401 en cualquier llamada intenta renovar la sesión una vez con el refresh token antes de
// rendirse — así una recarga de página o un access token caducado a media navegación no fuerzan
// un logout si el refresh token todavía es válido.
async function tryRefresh(): Promise<boolean> {
  const refreshToken = getRefreshToken()
  if (!refreshToken) return false
  const response = await fetch(`${BASE_URL}/auth/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  })
  if (!response.ok) return false
  const data: AuthResponseDto = await response.json()
  setAccessToken(data.accessToken)
  setRefreshToken(data.refreshToken)
  return true
}

export async function apiCall<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  let response = await rawFetch(endpoint, options)

  if (response.status === 401 && endpoint !== '/auth/refresh') {
    const refreshed = await tryRefresh()
    if (refreshed) {
      response = await rawFetch(endpoint, options)
    } else {
      clearTokens()
      notifyAuthExpired()
    }
  }

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
  auth: {
    register: (data: RegisterRequest) =>
      apiCall<AuthResponseDto>('/auth/register', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    login: (data: LoginRequest) =>
      apiCall<AuthResponseDto>('/auth/login', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    loginWithGoogle: (idToken: string) =>
      apiCall<AuthResponseDto>('/auth/google', {
        method: 'POST',
        body: JSON.stringify({ idToken }),
      }),
    refresh: (refreshToken: string) =>
      apiCall<AuthResponseDto>('/auth/refresh', {
        method: 'POST',
        body: JSON.stringify({ refreshToken }),
      }),
    logout: (refreshToken: string) =>
      apiCall<void>('/auth/logout', {
        method: 'POST',
        body: JSON.stringify({ refreshToken }),
      }),
  },
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
  gameInstances: {
    create: (gameDefinitionId: string, data: CreateGameInstanceRequest, asPlayer?: string) =>
      apiCall<GameInstanceDto>(
        `/game-definitions/${gameDefinitionId}/instances${asPlayerQuery(asPlayer)}`,
        { method: 'POST', body: JSON.stringify(data) }
      ),
    get: (instanceId: string, asPlayer?: string) =>
      apiCall<GameInstanceDto>(`/instances/${instanceId}${asPlayerQuery(asPlayer)}`),
    applyAction: (instanceId: string, data: PlayerActionRequest, asPlayer?: string) =>
      apiCall<GameInstanceDto>(
        `/instances/${instanceId}/actions${asPlayerQuery(asPlayer)}`,
        { method: 'POST', body: JSON.stringify(data) }
      ),
  },
}

function asPlayerQuery(asPlayer?: string): string {
  return asPlayer ? `?asPlayer=${encodeURIComponent(asPlayer)}` : ''
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
  name: string
  slug: string
}

export interface UserDto {
  id: string
  email: string
  displayName: string
}

export interface AuthResponseDto {
  accessToken: string
  refreshToken: string
  user: UserDto
}

export interface RegisterRequest {
  email: string
  password: string
  displayName: string
}

export interface LoginRequest {
  email: string
  password: string
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

export interface CardDto {
  id: string
  attributes: Record<string, unknown>
}

// Objeto (no un array ni un número desnudo), para no confundir "zona vacía y visible" ([]) con
// "zona vacía y oculta". Ver GameStateVisibility en el backend — mismo shape para sharedZones
// (p.ej. un mazo boca abajo, visibility HIDDEN) y perPlayerZones (p.ej. la mano de otro jugador).
export type HiddenZoneDto = { hiddenCount: number }

export interface GameInstancePlayerDto {
  id: string
  displayName: string
}

export interface GameInstanceStateDto {
  players: GameInstancePlayerDto[]
  currentPlayerIndex: number
  direction: number
  sharedZones: Record<string, CardDto[] | HiddenZoneDto>
  perPlayerZones: Record<string, Record<string, CardDto[] | HiddenZoneDto>>
}

export interface GameInstanceDto {
  id: string
  gameDefinitionId: string
  gameDefinitionVersionId: string
  state: GameInstanceStateDto
  createdAt: string
  endedAt: string | null
}

export interface CreateGameInstanceRequest {
  versionNumber: number
  players: GameInstancePlayerDto[]
}

export interface PlayerActionRequest {
  playerId: string
  eventType: string
  payload?: Record<string, unknown>
}
