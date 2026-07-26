import { apiClient } from '../lib/apiClient'
import type { ConcursoRequestDTO, ConcursoResponseDTO } from '../types/api'

export function listarTodos() {
  return apiClient.get<ConcursoResponseDTO[]>('/concursos').then((r) => r.data)
}

export function listarProvasDisponiveis() {
  return apiClient.get<ConcursoResponseDTO[]>('/concursos/provas').then((r) => r.data)
}

export function cadastrar(request: ConcursoRequestDTO) {
  return apiClient.post<ConcursoResponseDTO>('/concursos', request).then((r) => r.data)
}

export function atualizar(id: number, request: ConcursoRequestDTO) {
  return apiClient.put<ConcursoResponseDTO>(`/concursos/${id}`, request).then((r) => r.data)
}

export function deletar(id: number) {
  return apiClient.delete(`/concursos/${id}`)
}

export function adicionarDisciplina(concursoId: number, disciplinaId: number) {
  return apiClient
    .post<ConcursoResponseDTO>(`/concursos/${concursoId}/disciplinas/${disciplinaId}`)
    .then((r) => r.data)
}

export function removerDisciplina(concursoId: number, disciplinaId: number) {
  return apiClient
    .delete<ConcursoResponseDTO>(`/concursos/${concursoId}/disciplinas/${disciplinaId}`)
    .then((r) => r.data)
}

export function indexarEdital(id: number) {
  return apiClient
    .post<{ chunksIndexados: number }>(`/concursos/${id}/edital/indexar`)
    .then((r) => r.data)
}
