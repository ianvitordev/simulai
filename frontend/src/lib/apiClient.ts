import axios from 'axios'
import type { ErroResponseDTO, ErroValidacaoDTO } from '../types/api'
import { getToken, clearSession } from './auth'

/**
 * Em dev, o Vite faz proxy de /api para o backend (vite.config.ts) — sem isso
 * precisaríamos configurar CORS no Spring Security. Em produção, VITE_API_URL deve
 * apontar para a URL real da API (ou o frontend ser servido do mesmo domínio).
 */
export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '/api',
})

apiClient.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      // Token ausente/expirado/inválido: o backend já garante isso via
      // GlobalExceptionHandler/AuthenticationEntryPoint — aqui só limpamos a sessão
      // local e mandamos de volta pro login.
      clearSession()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  },
)

/**
 * Extrai a mensagem de erro do formato padrão do backend (ErroResponseDTO/ErroValidacaoDTO).
 * Usa 'camposInvalidos' como discriminador — não dá pra usar 'erro', já que
 * ErroResponseDTO também tem um campo chamado "erro" (string com o nome do status HTTP).
 */
export function extrairMensagemErro(error: unknown): string {
  if (axios.isAxiosError(error) && error.response?.data) {
    const data = error.response.data as ErroResponseDTO | ErroValidacaoDTO
    if ('camposInvalidos' in data) {
      return data.erro.mensagem
    }
    return data.mensagem
  }
  return 'Erro inesperado. Tente novamente.'
}
