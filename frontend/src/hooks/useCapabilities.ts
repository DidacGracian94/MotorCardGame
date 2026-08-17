import { useQuery } from '@tanstack/react-query'
import { api } from '@/api/client'

const CAPABILITIES_KEY = ['capabilities']

export function useCapabilities() {
  return useQuery({
    queryKey: CAPABILITIES_KEY,
    queryFn: () => api.capabilities.list(),
    staleTime: Infinity,
  })
}
