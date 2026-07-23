import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'

export function AdminRoute() {
  const { claims } = useAuth()

  if (!claims) {
    return <Navigate to="/login" replace />
  }

  if (claims.role !== 'ADMIN') {
    return <Navigate to="/" replace />
  }

  return <Outlet />
}
