import { apiClient } from '../lib/apiClient'
import type { LoginRequestDTO, TokenResponseDTO } from '../types/api'

export function login(request: LoginRequestDTO) {
  return apiClient.post<TokenResponseDTO>('/auth/login', request).then((r) => r.data)
}
