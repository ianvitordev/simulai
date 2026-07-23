import { NavLink, Outlet } from 'react-router-dom'

const LINKS = [
  { to: '/admin/bancas', label: 'Bancas' },
  { to: '/admin/disciplinas', label: 'Disciplinas' },
  { to: '/admin/assuntos', label: 'Assuntos' },
  { to: '/admin/concursos', label: 'Concursos' },
  { to: '/admin/questoes', label: 'Questões' },
  { to: '/admin/usuarios', label: 'Usuários' },
]

export function AdminLayout() {
  return (
    <div className="flex flex-col gap-6 sm:flex-row">
      <aside className="shrink-0 sm:w-48">
        <nav className="flex flex-row flex-wrap gap-1 sm:flex-col">
          {LINKS.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              className={({ isActive }) =>
                `rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                  isActive ? 'bg-indigo-100 text-indigo-700' : 'text-slate-600 hover:bg-slate-100'
                }`
              }
            >
              {link.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="min-w-0 flex-1">
        <Outlet />
      </div>
    </div>
  )
}
