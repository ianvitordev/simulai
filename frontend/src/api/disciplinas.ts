import { apiClient } from '../lib/apiClient'
import type { DisciplinaRequestDTO, DisciplinaResponseDTO } from '../types/api'

export function listarTodas() {
  return apiClient.get<DisciplinaResponseDTO[]>('/disciplinas').then((r) => r.data)
}

export function cadastrar(request: DisciplinaRequestDTO) {
  return apiClient.post<DisciplinaResponseDTO>('/disciplinas', request).then((r) => r.data)
}

export function atualizar(id: number, request: DisciplinaRequestDTO) {
  return apiClient.put<DisciplinaResponseDTO>(`/disciplinas/${id}`, request).then((r) => r.data)
}

export function deletar(id: number) {
  return apiClient.delete(`/disciplinas/${id}`)
}
