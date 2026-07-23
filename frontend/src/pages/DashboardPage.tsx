import { useNavigate } from 'react-router-dom'
import { Alert } from '../components/ui/Alert'
import { Badge } from '../components/ui/Badge'
import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'
import { Spinner } from '../components/ui/Spinner'
import { useCancelarSimulado, useIniciarSimulado, useMeusSimulados } from '../hooks/useSimulados'
import { extrairMensagemErro } from '../lib/apiClient'
import { formatarData, rotuloStatus, tonePorStatus } from '../lib/format'
import type { SimuladoResponseDTO } from '../types/api'

export function DashboardPage() {
  const { data: simulados, isLoading, isError } = useMeusSimulados()
  const navigate = useNavigate()

  if (isLoading) return <Spinner />
  if (isError) return <Alert tone="error">Não foi possível carregar seus simulados.</Alert>

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold text-slate-900">Meus simulados</h1>
        <Button onClick={() => navigate('/simulados/novo')}>Gerar novo simulado</Button>
      </div>

      {!simulados || simulados.length === 0 ? (
        <Card className="text-center text-slate-500">
          Você ainda não tem nenhum simulado. Que tal gerar o primeiro?
        </Card>
      ) : (
        <div className="flex flex-col gap-3">
          {simulados
            .slice()
            .sort((a, b) => new Date(b.dataCriacao).getTime() - new Date(a.dataCriacao).getTime())
            .map((simulado) => (
              <SimuladoCard key={simulado.id} simulado={simulado} />
            ))}
        </div>
      )}
    </div>
  )
}

function SimuladoCard({ simulado }: { simulado: SimuladoResponseDTO }) {
  const navigate = useNavigate()
  const iniciarMutation = useIniciarSimulado(simulado.id)
  const cancelarMutation = useCancelarSimulado(simulado.id)

  async function handleIniciar() {
    try {
      await iniciarMutation.mutateAsync()
      navigate(`/simulados/${simulado.id}`)
    } catch (error) {
      alert(extrairMensagemErro(error))
    }
  }

  async function handleCancelar() {
    if (!confirm('Cancelar este simulado? Essa ação não pode ser desfeita.')) return
    try {
      await cancelarMutation.mutateAsync()
    } catch (error) {
      alert(extrairMensagemErro(error))
    }
  }

  return (
    <Card className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <div className="flex items-center gap-2">
          <span className="font-medium text-slate-900">
            {simulado.concurso ?? 'Simulado geral'}
          </span>
          <Badge tone={tonePorStatus(simulado.status)}>{rotuloStatus(simulado.status)}</Badge>
        </div>
        <p className="mt-1 text-sm text-slate-500">
          {simulado.quantidadeQuestoes} questões · criado em {formatarData(simulado.dataCriacao)}
        </p>
      </div>

      <div className="flex gap-2">
        {simulado.status === 'CRIADO' && (
          <>
            <Button variant="secondary" onClick={handleCancelar} isLoading={cancelarMutation.isPending}>
              Cancelar
            </Button>
            <Button onClick={handleIniciar} isLoading={iniciarMutation.isPending}>
              Iniciar
            </Button>
          </>
        )}
        {simulado.status === 'EM_ANDAMENTO' && (
          <>
            <Button variant="secondary" onClick={handleCancelar} isLoading={cancelarMutation.isPending}>
              Cancelar
            </Button>
            <Button onClick={() => navigate(`/simulados/${simulado.id}`)}>Continuar</Button>
          </>
        )}
        {simulado.status === 'FINALIZADO' && (
          <Button variant="secondary" onClick={() => navigate(`/simulados/${simulado.id}/revisao`)}>
            Ver revisão
          </Button>
        )}
      </div>
    </Card>
  )
}
