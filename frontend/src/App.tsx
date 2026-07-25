import { useEffect } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { AdminLayout } from './components/AdminLayout'
import { AdminRoute } from './components/AdminRoute'
import { Layout } from './components/Layout'
import { ProtectedRoute } from './components/ProtectedRoute'
import { useAuth } from './hooks/useAuth'
import { acordarBackend } from './lib/apiClient'
import { AssuntosPage } from './pages/admin/AssuntosPage'
import { BancasPage } from './pages/admin/BancasPage'
import { ConcursoDetalhePage } from './pages/admin/ConcursoDetalhePage'
import { ConcursosPage } from './pages/admin/ConcursosPage'
import { DisciplinasPage } from './pages/admin/DisciplinasPage'
import { QuestoesPage } from './pages/admin/QuestoesPage'
import { UsuariosPage } from './pages/admin/UsuariosPage'
import { CadastroPage } from './pages/CadastroPage'
import { ConfirmarCadastroPage } from './pages/ConfirmarCadastroPage'
import { CronogramaPage } from './pages/CronogramaPage'
import { DashboardPage } from './pages/DashboardPage'
import { EsqueciSenhaPage } from './pages/EsqueciSenhaPage'
import { EstatisticasPage } from './pages/EstatisticasPage'
import { FazerSimuladoPage } from './pages/FazerSimuladoPage'
import { GerarSimuladoPage } from './pages/GerarSimuladoPage'
import { LoginPage } from './pages/LoginPage'
import { NotFoundPage } from './pages/NotFoundPage'
import { RevisaoSimuladoPage } from './pages/RevisaoSimuladoPage'

function PublicOnlyRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth()
  if (isAuthenticated) return <Navigate to="/" replace />
  return <>{children}</>
}

export default function App() {
  useEffect(() => {
    acordarBackend()
  }, [])

  return (
    <Routes>
      <Route
        path="/login"
        element={
          <PublicOnlyRoute>
            <LoginPage />
          </PublicOnlyRoute>
        }
      />
      <Route
        path="/cadastro"
        element={
          <PublicOnlyRoute>
            <CadastroPage />
          </PublicOnlyRoute>
        }
      />
      <Route
        path="/confirmar-cadastro"
        element={
          <PublicOnlyRoute>
            <ConfirmarCadastroPage />
          </PublicOnlyRoute>
        }
      />
      <Route
        path="/esqueci-senha"
        element={
          <PublicOnlyRoute>
            <EsqueciSenhaPage />
          </PublicOnlyRoute>
        }
      />

      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/simulados/novo" element={<GerarSimuladoPage />} />
          <Route path="/simulados/:id" element={<FazerSimuladoPage />} />
          <Route path="/simulados/:id/revisao" element={<RevisaoSimuladoPage />} />
          <Route path="/estatisticas" element={<EstatisticasPage />} />
          <Route path="/cronograma" element={<CronogramaPage />} />

          <Route element={<AdminRoute />}>
            <Route element={<AdminLayout />}>
              <Route path="/admin/bancas" element={<BancasPage />} />
              <Route path="/admin/disciplinas" element={<DisciplinasPage />} />
              <Route path="/admin/assuntos" element={<AssuntosPage />} />
              <Route path="/admin/concursos" element={<ConcursosPage />} />
              <Route path="/admin/concursos/:id" element={<ConcursoDetalhePage />} />
              <Route path="/admin/questoes" element={<QuestoesPage />} />
              <Route path="/admin/usuarios" element={<UsuariosPage />} />
            </Route>
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}
