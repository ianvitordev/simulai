import { apiClient } from '../lib/apiClient'
import type { AssuntoRequestDTO, AssuntoResponseDTO } from '../types/api'

export function listarTodos() {
  return apiClient.get<AssuntoResponseDTO[]>('/assuntos').then((r) => r.data)
}

export function listarPorDisciplina(disciplinaId: number) {
  return apiClient.get<AssuntoResponseDTO[]>(`/assuntos/disciplina/${disciplinaId}`).then((r) => r.data)
}

export function cadastrar(request: AssuntoRequestDTO) {
  return apiClient.post<AssuntoResponseDTO>('/assuntos', request).then((r) => r.data)
}

export function atualizar(id: number, request: AssuntoRequestDTO) {
  return apiClient.put<AssuntoResponseDTO>(`/assuntos/${id}`, request).then((r) => r.data)
}

export function deletar(id: number) {
  return apiClient.delete(`/assuntos/${id}`)
}
