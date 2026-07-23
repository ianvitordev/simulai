import { useMemo } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Alert } from '../components/ui/Alert'
import { Badge } from '../components/ui/Badge'
import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'
import { Spinner } from '../components/ui/Spinner'
import { useRevisaoSimulado } from '../hooks/useSimulados'
import type { RevisaoQuestaoDTO } from '../types/api'

export function RevisaoSimuladoPage() {
  const { id } = useParams()
  const simuladoId = Number(id)
  const navigate = useNavigate()

  const { data: revisao, isLoading, isError } = useRevisaoSimulado(simuladoId)

  const resumo = useMemo(() => {
    if (!revisao) return null
    const total = revisao.questoes.length
    const acertos = revisao.questoes.filter((q) => q.acertou).length
    const naoRespondidas = revisao.questoes.filter((q) => !q.respondida).length
    const percentual = total === 0 ? 0 : (acertos / total) * 100
    return { total, acertos, erros: total - acertos - naoRespondidas, naoRespondidas, percentual }
  }, [revisao])

  if (isLoading) return <Spinner />
  if (isError || !revisao || !resumo) {
    return <Alert tone="error">Não foi possível carregar a revisão deste simulado.</Alert>
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold text-slate-900">Revisão do simulado</h1>
        <Button variant="secondary" onClick={() => navigate('/')}>
          Voltar
        </Button>
      </div>

      <Card className="grid grid-cols-2 gap-4 sm:grid-cols-4">
        <Resumo label="Acertos" valor={resumo.acertos} tone="text-green-600" />
        <Resumo label="Erros" valor={resumo.erros} tone="text-red-600" />
        <Resumo label="Não respondidas" valor={resumo.naoRespondidas} tone="text-slate-500" />
        <Resumo label="Aproveitamento" valor={`${resumo.percentual.toFixed(0)}%`} tone="text-indigo-600" />
      </Card>

      <div className="flex flex-col gap-4">
        {revisao.questoes.map((questao, indice) => (
          <QuestaoRevisao key={questao.questaoId} numero={indice + 1} questao={questao} />
        ))}
      </div>
    </div>
  )
}

function Resumo({ label, valor, tone }: { label: string; valor: string | number; tone: string }) {
  return (
    <div className="text-center">
      <p className={`text-3xl font-semibold ${tone}`}>{valor}</p>
      <p className="text-sm text-slate-500">{label}</p>
    </div>
  )
}

function QuestaoRevisao({ numero, questao }: { numero: number; questao: RevisaoQuestaoDTO }) {
  return (
    <Card>
      <div className="mb-3 flex items-center justify-between">
        <span className="text-sm font-medium text-slate-500">Questão {numero}</span>
        {!questao.respondida ? (
          <Badge tone="slate">Não respondida</Badge>
        ) : (
          <Badge tone={questao.acertou ? 'green' : 'red'}>
            {questao.acertou ? 'Correta' : 'Incorreta'}
          </Badge>
        )}
      </div>

      <p className="whitespace-pre-wrap text-slate-900">{questao.enunciado}</p>

      <div className="mt-4 flex flex-col gap-2">
        {questao.alternativas.map((alternativa) => (
          <div
            key={alternativa.id}
            className={`rounded-lg border px-4 py-2 text-sm
              ${alternativa.correta ? 'border-green-300 bg-green-50' : 'border-slate-200'}
              ${alternativa.marcadaPeloUsuario && !alternativa.correta ? 'border-red-300 bg-red-50' : ''}`}
          >
            <strong className="mr-1">{alternativa.letra})</strong>
            {alternativa.descricao}
            {alternativa.correta && <span className="ml-2 text-xs font-medium text-green-700">✓ Gabarito</span>}
            {alternativa.marcadaPeloUsuario && (
              <span className="ml-2 text-xs font-medium text-slate-500">Sua resposta</span>
            )}
          </div>
        ))}
      </div>

      {questao.explicacao && (
        <div className="mt-4 rounded-lg bg-slate-50 p-3 text-sm text-slate-600">
          <strong className="text-slate-700">Explicação: </strong>
          {questao.explicacao}
        </div>
      )}
    </Card>
  )
}
