import { apiClient } from '../lib/apiClient'
import type {
  Dificuldade,
  GerarQuestaoIARequestDTO,
  QuestaoAdminResponseDTO,
  QuestaoRequestDTO,
  QuestaoResponseDTO,
} from '../types/api'

export function listarTodas() {
  return apiClient.get<QuestaoResponseDTO[]>('/questoes').then((r) => r.data)
}

export function listarTodasParaModeracao() {
  return apiClient.get<QuestaoAdminResponseDTO[]>('/questoes/moderacao').then((r) => r.data)
}

export function listarPorDisciplina(disciplinaId: number) {
  return apiClient.get<QuestaoResponseDTO[]>(`/questoes/disciplina/${disciplinaId}`).then((r) => r.data)
}

export function listarPorAssunto(assuntoId: number) {
  return apiClient.get<QuestaoResponseDTO[]>(`/questoes/assunto/${assuntoId}`).then((r) => r.data)
}

export function listarPorDificuldade(dificuldade: Dificuldade) {
  return apiClient.get<QuestaoResponseDTO[]>(`/questoes/dificuldade/${dificuldade}`).then((r) => r.data)
}

export function atualizar(id: number, request: QuestaoRequestDTO) {
  return apiClient.put<QuestaoResponseDTO>(`/questoes/${id}`, request).then((r) => r.data)
}

export function deletar(id: number) {
  return apiClient.delete(`/questoes/${id}`)
}

export function gerarViaIA(request: GerarQuestaoIARequestDTO) {
  return apiClient.post<QuestaoResponseDTO[]>('/questoes/gerar-ia', request).then((r) => r.data)
}
