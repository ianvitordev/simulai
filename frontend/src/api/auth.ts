import { apiClient } from '../lib/apiClient'
import type {
  ConfirmarCadastroRequestDTO,
  EsqueciSenhaRequestDTO,
  LoginRequestDTO,
  RedefinirSenhaRequestDTO,
  ReenviarCodigoRequestDTO,
  TokenResponseDTO,
} from '../types/api'

export function login(request: LoginRequestDTO) {
  return apiClient.post<TokenResponseDTO>('/auth/login', request).then((r) => r.data)
}

export function confirmarCadastro(request: ConfirmarCadastroRequestDTO) {
  return apiClient.post<TokenResponseDTO>('/auth/confirmar-cadastro', request).then((r) => r.data)
}

export function esqueciSenha(request: EsqueciSenhaRequestDTO) {
  return apiClient.post<void>('/auth/esqueci-senha', request).then((r) => r.data)
}

export function redefinirSenha(request: RedefinirSenhaRequestDTO) {
  return apiClient.post<TokenResponseDTO>('/auth/redefinir-senha', request).then((r) => r.data)
}

export function reenviarCodigo(request: ReenviarCodigoRequestDTO) {
  return apiClient.post<void>('/auth/reenviar-codigo', request).then((r) => r.data)
}
