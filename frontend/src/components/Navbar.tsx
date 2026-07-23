import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { useUsuarioLogado } from '../hooks/useUsuario'
import { Button } from './ui/Button'

export function Navbar() {
  const { logout, claims } = useAuth()
  const { data: usuario } = useUsuarioLogado()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <header className="border-b border-slate-200 bg-white">
      <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3">
        <Link to="/" className="text-lg font-semibold text-indigo-600">
          SimulaI
        </Link>

        <nav className="flex items-center gap-4">
          <Link to="/" className="text-sm text-slate-600 hover:text-slate-900">
            Meus simulados
          </Link>
          <Link
            to="/simulados/novo"
            className="text-sm text-slate-600 hover:text-slate-900"
          >
            Gerar simulado
          </Link>
          {claims?.role === 'ADMIN' && (
            <Link to="/admin/bancas" className="text-sm text-slate-600 hover:text-slate-900">
              Admin
            </Link>
          )}
          {usuario && (
            <span className="hidden text-sm text-slate-500 sm:inline">{usuario.nome}</span>
          )}
          <Button variant="ghost" onClick={handleLogout}>
            Sair
          </Button>
        </nav>
      </div>
    </header>
  )
}
