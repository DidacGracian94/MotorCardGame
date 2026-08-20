import { Client, IMessage } from '@stomp/stompjs'
import { GameInstanceDto } from '@/api/client'
import { getAccessToken } from '@/auth/tokenStore'

export interface GameInstanceRealtimeHandlers {
  onSnapshot: (snapshot: GameInstanceDto) => void
  /**
   * Se dispara en cada conexión (incluida una reconexión tras un corte de red o pestaña en
   * segundo plano) — el llamador debe refrescar el snapshot por REST aquí, no fiarse de que el
   * próximo mensaje WS le ponga al día por sí solo (pudo perderse un update mientras estaba
   * desconectado).
   */
  onConnected: () => void
  onDisconnected: () => void
}

/**
 * Conecta a /ws?playerId=... y se suscribe a /topic/games/{id}/public (siempre) y, si hay
 * playerId, también a /user/queue/games/{id}/private. El publisher del backend manda primero el
 * mensaje público y luego el privado de cada jugador (ver GameInstanceRealtimePublisher), así que
 * para el propio jugador el último mensaje recibido en cada actualización es siempre su vista
 * privada — soporta "último mensaje gana" sin lógica de merge.
 */
export function connectToGameInstance(
  instanceId: string,
  playerId: string | null,
  handlers: GameInstanceRealtimeHandlers
): Client {
  const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  // El token se lee en cada intento (no solo al construir el Client) para que una reconexión
  // tras un access token renovado use el token vigente, no el que había al abrir la pestaña.
  const buildUrl = () => {
    const params = new URLSearchParams()
    const token = getAccessToken()
    if (token) params.set('token', token)
    if (playerId) params.set('playerId', playerId)
    const query = params.toString()
    return `${wsProtocol}//${window.location.host}/ws${query ? `?${query}` : ''}`
  }
  const client = new Client({
    webSocketFactory: () => new WebSocket(buildUrl()),
    reconnectDelay: 3000,
    onConnect: () => {
      client.subscribe(`/topic/games/${instanceId}/public`, (message: IMessage) => {
        handlers.onSnapshot(JSON.parse(message.body))
      })
      if (playerId) {
        client.subscribe(`/user/queue/games/${instanceId}/private`, (message: IMessage) => {
          handlers.onSnapshot(JSON.parse(message.body))
        })
      }
      handlers.onConnected()
    },
    onWebSocketClose: () => handlers.onDisconnected(),
  })
  client.activate()
  return client
}
