import { createContext, useCallback, useMemo, useState, type ReactNode } from 'react'
import * as authApi from '../api/auth'
import { clearSession, decodeToken, getToken, isTokenExpired, setToken, type JwtClaims } from '../lib/auth'
import type { LoginRequestDTO } from '../types/api'

interface AuthContextValue {
  claims: JwtClaims | null
  isAuthenticated: boolean
  login: (request: LoginRequestDTO) => Promise<void>
  logout: () => void
}

// eslint-disable-next-line react-refresh/only-export-components
export const AuthContext = createContext<AuthContextValue | undefined>(undefined)

function lerClaimsValidas(): JwtClaims | null {
  const token = getToken()
  if (!token) return null

  const claims = decodeToken(token)
  if (!claims || isTokenExpired(claims)) {
    clearSession()
    return null
  }
  return claims
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [claims, setClaims] = useState<JwtClaims | null>(() => lerClaimsValidas())

  const login = useCallback(async (request: LoginRequestDTO) => {
    const response = await authApi.login(request)
    setToken(response.accessToken)
    setClaims(decodeToken(response.accessToken))
  }, [])

  const logout = useCallback(() => {
    clearSession()
    setClaims(null)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({ claims, isAuthenticated: claims !== null, login, logout }),
    [claims, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
