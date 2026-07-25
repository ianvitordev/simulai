import type { ReactNode } from 'react'

const DESTAQUES = [
  'Questões geradas por IA com base no edital real',
  'Estatísticas completas de desempenho',
  'Cronograma de estudos personalizado',
]

export function AuthLayout({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-screen flex-col lg:flex-row">
      <div className="relative hidden overflow-hidden bg-slate-950 lg:flex lg:w-[45%] lg:flex-col lg:justify-between lg:p-12 xl:p-16">
        <div
          aria-hidden
          className="pointer-events-none absolute inset-0"
          style={{
            backgroundImage:
              'radial-gradient(circle at 15% 15%, rgba(201,154,46,0.28), transparent 45%), ' +
              'radial-gradient(circle at 85% 75%, rgba(220,174,63,0.18), transparent 40%)',
          }}
        />

        <span className="relative text-2xl font-bold tracking-tight text-gold-400">SimulaI</span>

        <div className="relative flex flex-col gap-6">
          <h2 className="text-3xl font-semibold leading-tight text-white xl:text-4xl">
            Simulados de concursos gerados por IA
          </h2>
          <ul className="flex flex-col gap-3 text-sm text-slate-300">
            {DESTAQUES.map((item) => (
              <li key={item} className="flex items-center gap-3">
                <span className="h-1.5 w-1.5 shrink-0 rounded-full bg-gold-400" />
                {item}
              </li>
            ))}
          </ul>
        </div>

        <p className="relative text-xs text-slate-500">© {new Date().getFullYear()} SimulaI</p>
      </div>

      <div className="flex items-center justify-center bg-slate-950 py-6 lg:hidden">
        <span className="text-xl font-bold tracking-tight text-gold-400">SimulaI</span>
      </div>

      <div className="flex flex-1 items-center justify-center bg-slate-50 px-4 py-10 sm:py-16">
        <div className="w-full max-w-sm rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8 sm:shadow-xl">
          {children}
        </div>
      </div>
    </div>
  )
}
