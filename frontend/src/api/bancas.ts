import { apiClient } from '../lib/apiClient'
import type { BancaRequestDTO, BancaResponseDTO } from '../types/api'

export function listarTodas() {
  return apiClient.get<BancaResponseDTO[]>('/bancas').then((r) => r.data)
}

export function cadastrar(request: BancaRequestDTO) {
  return apiClient.post<BancaResponseDTO>('/bancas', request).then((r) => r.data)
}

export function atualizar(id: number, request: BancaRequestDTO) {
  return apiClient.put<BancaResponseDTO>(`/bancas/${id}`, request).then((r) => r.data)
}

export function deletar(id: number) {
  return apiClient.delete(`/bancas/${id}`)
}
