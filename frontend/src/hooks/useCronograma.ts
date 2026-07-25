import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import * as cronogramasApi from '../api/cronogramas'
import { useAuth } from './useAuth'
import type { GerarCronogramaRequestDTO } from '../types/api'

export function useCronogramaAtual() {
  const { claims } = useAuth()

  return useQuery({
    queryKey: ['cronograma', 'usuario', claims?.usuarioId],
    queryFn: () => cronogramasApi.obterAtual(claims!.usuarioId),
    enabled: claims !== null,
  })
}

export function useGerarCronograma() {
  const { claims } = useAuth()
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: GerarCronogramaRequestDTO) => cronogramasApi.gerar(claims!.usuarioId, request),
    onSuccess: (data) => {
      queryClient.setQueryData(['cronograma', 'usuario', claims?.usuarioId], data)
    },
  })
}
