import { useMutation, useQuery } from '@tanstack/react-query'
import { api, CreateRoomRequest, JoinRoomRequest } from '@/api/client'

const ROOM_KEY = (code: string) => ['room', code]

/** Sin WebSocket dedicado para la sala (ver decisión de diseño): un corto polling es más simple y
 * suficiente para una lista de jugadores que cambia poco. Se para solo al arrancar la partida. */
const POLL_INTERVAL_MS = 1500

export function useCreateRoom() {
  return useMutation({
    mutationFn: (data: CreateRoomRequest) => api.rooms.create(data),
  })
}

export function useRoom(code: string | undefined) {
  return useQuery({
    queryKey: ROOM_KEY(code ?? ''),
    queryFn: () => api.rooms.get(code!),
    enabled: code != null,
    refetchInterval: (query) => (query.state.data?.status === 'OPEN' ? POLL_INTERVAL_MS : false),
  })
}

export function useJoinRoom() {
  return useMutation({
    mutationFn: ({ code, data }: { code: string; data: JoinRoomRequest }) => api.rooms.join(code, data),
  })
}

export function useStartRoom() {
  return useMutation({
    mutationFn: (code: string) => api.rooms.start(code),
  })
}
