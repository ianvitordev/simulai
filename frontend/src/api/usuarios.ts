import { apiClient } from '../lib/apiClient'
import type { AlterarRoleRequestDTO, UsuarioRequestDTO, UsuarioResponseDTO } from '../types/api'

export function cadastrar(request: UsuarioRequestDTO) {
  return apiClient.post<UsuarioResponseDTO>('/usuarios', request).then((r) => r.data)
}

export function buscarPorId(id: number) {
  return apiClient.get<UsuarioResponseDTO>(`/usuarios/${id}`).then((r) => r.data)
}

export function listarTodos() {
  return apiClient.get<UsuarioResponseDTO[]>('/usuarios').then((r) => r.data)
}

export function alterarRole(id: number, request: AlterarRoleRequestDTO) {
  return apiClient.patch<UsuarioResponseDTO>(`/usuarios/${id}/role`, request).then((r) => r.data)
}
