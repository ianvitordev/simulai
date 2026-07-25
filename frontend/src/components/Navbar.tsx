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
    <header className="border-b border-slate-800 bg-slate-950">
      <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3">
        <Link to="/" className="text-lg font-bold tracking-tight text-gold-400">
          SimulaI
        </Link>

        <nav className="flex items-center gap-4">
          <Link to="/" className="text-sm text-slate-300 hover:text-gold-400">
            Meus simulados
          </Link>
          <Link
            to="/simulados/novo"
            className="text-sm text-slate-300 hover:text-gold-400"
          >
            Gerar simulado
          </Link>
          <Link to="/estatisticas" className="text-sm text-slate-300 hover:text-gold-400">
            Estatísticas
          </Link>
          <Link to="/cronograma" className="text-sm text-slate-300 hover:text-gold-400">
            Cronograma
          </Link>
          {claims?.role === 'ADMIN' && (
            <Link to="/admin/bancas" className="text-sm text-slate-300 hover:text-gold-400">
              Admin
            </Link>
          )}
          {usuario && (
            <span className="hidden text-sm text-slate-400 sm:inline">{usuario.nome}</span>
          )}
          <Button variant="ghostOnDark" onClick={handleLogout}>
            Sair
          </Button>
        </nav>
      </div>
    </header>
  )
}
