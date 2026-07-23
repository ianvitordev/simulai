import { apiClient } from '../lib/apiClient'
import type {
  GerarSimuladoRequestDTO,
  RespostaUsuarioRequestDTO,
  RespostaUsuarioResponseDTO,
  SimuladoResponseDTO,
  SimuladoResultadoDTO,
  SimuladoRevisaoDTO,
} from '../types/api'

export function gerar(request: GerarSimuladoRequestDTO) {
  return apiClient.post<SimuladoResponseDTO>('/simulados/gerar', request).then((r) => r.data)
}

export function buscarPorId(id: number) {
  return apiClient.get<SimuladoResponseDTO>(`/simulados/${id}`).then((r) => r.data)
}

export function listarPorUsuario(usuarioId: number) {
  return apiClient
    .get<SimuladoResponseDTO[]>(`/simulados/usuario/${usuarioId}`)
    .then((r) => r.data)
}

export function iniciar(id: number) {
  return apiClient.patch<SimuladoResponseDTO>(`/simulados/${id}/iniciar`).then((r) => r.data)
}

export function responderQuestao(id: number, request: RespostaUsuarioRequestDTO) {
  return apiClient
    .post<RespostaUsuarioResponseDTO>(`/simulados/${id}/respostas`, request)
    .then((r) => r.data)
}

export function finalizar(id: number) {
  return apiClient
    .patch<SimuladoResultadoDTO>(`/simulados/${id}/finalizar`)
    .then((r) => r.data)
}

export function cancelar(id: number) {
  return apiClient.patch<SimuladoResponseDTO>(`/simulados/${id}/cancelar`).then((r) => r.data)
}

export function revisar(id: number) {
  return apiClient.get<SimuladoRevisaoDTO>(`/simulados/${id}/revisao`).then((r) => r.data)
}
