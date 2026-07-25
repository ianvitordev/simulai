import axios from 'axios'
import { apiClient } from '../lib/apiClient'
import type { CronogramaResponseDTO, GerarCronogramaRequestDTO } from '../types/api'

/**
 * 404 aqui significa "o aluno ainda não gerou nenhum cronograma" — um estado normal da
 * tela (mostra o formulário de geração), não um erro. Convertido em `null` em vez de
 * propagar o reject, pra não acender o estado de erro da query à toa.
 */
export function obterAtual(usuarioId: number) {
  return apiClient
    .get<CronogramaResponseDTO>(`/cronogramas/usuario/${usuarioId}`)
    .then((r) => r.data)
    .catch((error: unknown) => {
      if (axios.isAxiosError(error) && error.response?.status === 404) {
        return null
      }
      throw error
    })
}

export function gerar(usuarioId: number, request: GerarCronogramaRequestDTO) {
  return apiClient
    .post<CronogramaResponseDTO>(`/cronogramas/usuario/${usuarioId}/gerar`, request)
    .then((r) => r.data)
}
