import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { useUsuarioLogado } from '../hooks/useUsuario'
import { Button } from './ui/Button'

const LINKS = [
  { to: '/', label: 'Meus simulados' },
  { to: '/simulados/novo', label: 'Gerar simulado' },
  { to: '/estatisticas', label: 'Estatísticas' },
  { to: '/cronograma', label: 'Cronograma' },
]

export function Navbar() {
  const { logout, claims } = useAuth()
  const { data: usuario } = useUsuarioLogado()
  const navigate = useNavigate()
  const [menuAberto, setMenuAberto] = useState(false)

  const links = claims?.role === 'ADMIN' ? [...LINKS, { to: '/admin/bancas', label: 'Admin' }] : LINKS

  function handleLogout() {
    setMenuAberto(false)
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <header className="border-b border-slate-800 bg-slate-950">
      <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3">
        <Link to="/" className="text-lg font-bold tracking-tight text-gold-400" onClick={() => setMenuAberto(false)}>
          SimulaI
        </Link>

        <nav className="hidden items-center gap-4 md:flex">
          {links.map((link) => (
            <Link key={link.to} to={link.to} className="text-sm text-slate-300 hover:text-gold-400">
              {link.label}
            </Link>
          ))}
          {usuario && <span className="text-sm text-slate-400">{usuario.nome}</span>}
          <Button variant="ghostOnDark" onClick={handleLogout}>
            Sair
          </Button>
        </nav>

        <button
          type="button"
          onClick={() => setMenuAberto((aberto) => !aberto)}
          aria-label={menuAberto ? 'Fechar menu' : 'Abrir menu'}
          aria-expanded={menuAberto}
          className="rounded-lg p-2 text-slate-300 hover:bg-slate-800 hover:text-gold-400 md:hidden"
        >
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"
            strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" className="h-6 w-6">
            {menuAberto ? <path d="M6 6l12 12M18 6L6 18" /> : <path d="M4 6h16M4 12h16M4 18h16" />}
          </svg>
        </button>
      </div>

      {menuAberto && (
        <nav className="flex flex-col gap-1 border-t border-slate-800 px-4 py-3 md:hidden">
          {links.map((link) => (
            <Link
              key={link.to}
              to={link.to}
              onClick={() => setMenuAberto(false)}
              className="rounded-lg px-3 py-2 text-sm text-slate-300 hover:bg-slate-800 hover:text-gold-400"
            >
              {link.label}
            </Link>
          ))}
          {usuario && <span className="px-3 py-1 text-sm text-slate-400">{usuario.nome}</span>}
          <Button variant="ghostOnDark" onClick={handleLogout} className="justify-start">
            Sair
          </Button>
        </nav>
      )}
    </header>
  )
}
