import { useQuery } from '@tanstack/react-query'
import { api } from '@/api/client'

const AI_GUIDE_KEY = ['aiGuide']

export function useAiGuide(enabled = true) {
  return useQuery({
    queryKey: AI_GUIDE_KEY,
    queryFn: () => api.aiGuide.get(),
    staleTime: Infinity,
    enabled,
  })
}
