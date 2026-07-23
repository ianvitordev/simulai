import type { Role } from '../types/api'

const TOKEN_KEY = 'simulai.token'

export interface JwtClaims {
  sub: string // email
  usuarioId: number
  role: Role
  iat: number
  exp: number
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearSession(): void {
  localStorage.removeItem(TOKEN_KEY)
}

/**
 * Decodifica o payload do JWT (base64url) sem verificar assinatura — a verificação
 * é responsabilidade exclusiva do backend. Aqui só lemos claims públicas (usuarioId,
 * role, email) para exibir na UI e montar URLs que dependem do id do usuário logado.
 */
export function decodeToken(token: string): JwtClaims | null {
  try {
    const payload = token.split('.')[1]
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
        .join(''),
    )
    return JSON.parse(json) as JwtClaims
  } catch {
    return null
  }
}

export function isTokenExpired(claims: JwtClaims): boolean {
  return claims.exp * 1000 <= Date.now()
}
