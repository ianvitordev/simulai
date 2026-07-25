import { useQuery } from '@tanstack/react-query'
import * as estatisticasApi from '../api/estatisticas'
import { useAuth } from './useAuth'

export function useEstatisticas() {
  const { claims } = useAuth()

  return useQuery({
    queryKey: ['estatisticas', 'usuario', claims?.usuarioId],
    queryFn: () => estatisticasApi.obterPorUsuario(claims!.usuarioId),
    enabled: claims !== null,
  })
}
