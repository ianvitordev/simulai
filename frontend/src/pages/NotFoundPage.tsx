import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4 text-center">
      <h1 className="text-4xl font-semibold text-slate-900">404</h1>
      <p className="text-slate-500">Página não encontrada.</p>
      <Link to="/" className="font-medium text-indigo-600 hover:underline">
        Voltar para o início
      </Link>
    </div>
  )
}
