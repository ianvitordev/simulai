import { apiClient } from '../lib/apiClient'
import type { EstatisticaResponseDTO } from '../types/api'

export function obterPorUsuario(usuarioId: number) {
  return apiClient
    .get<EstatisticaResponseDTO>(`/estatisticas/usuario/${usuarioId}`)
    .then((r) => r.data)
}
