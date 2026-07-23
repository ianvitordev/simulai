import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Alert } from '../components/ui/Alert'
import { Badge } from '../components/ui/Badge'
import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'
import { Spinner } from '../components/ui/Spinner'
import { formatarTempo, useCountdown } from '../hooks/useCountdown'
import {
  useFinalizarSimulado,
  useIniciarSimulado,
  useResponderQuestao,
  useSimulado,
} from '../hooks/useSimulados'
import { extrairMensagemErro } from '../lib/apiClient'
import type { RespostaUsuarioResponseDTO } from '../types/api'

export function FazerSimuladoPage() {
  const { id } = useParams()
  const simuladoId = Number(id)
  const navigate = useNavigate()

  const { data: simulado, isLoading, isError } = useSimulado(simuladoId)
  const iniciarMutation = useIniciarSimulado(simuladoId)
  const finalizarMutation = useFinalizarSimulado(simuladoId)

  useEffect(() => {
    if (simulado?.status === 'FINALIZADO') {
      navigate(`/simulados/${simuladoId}/revisao`, { replace: true })
    }
  }, [simulado?.status, simuladoId, navigate])

  if (isLoading) return <Spinner />
  if (isError || !simulado) {
    return <Alert tone="error">Não foi possível carregar este simulado.</Alert>
  }

  if (simulado.status === 'CANCELADO') {
    return (
      <div className="mx-auto max-w-md text-center">
        <Alert tone="info">Este simulado foi cancelado.</Alert>
        <Button className="mt-4" onClick={() => navigate('/')}>
          Voltar para meus simulados
        </Button>
      </div>
    )
  }

  if (simulado.status === 'CRIADO') {
    return (
      <Card className="mx-auto max-w-md text-center">
        <h1 className="text-xl font-semibold text-slate-900">Pronto para começar?</h1>
        <p className="mt-2 text-slate-500">
          {simulado.quantidadeQuestoes} questões
          {simulado.tempoLimiteMinutos > 0 && ` · ${simulado.tempoLimiteMinutos} minutos`}
        </p>
        <Button
          className="mt-6 w-full"
          isLoading={iniciarMutation.isPending}
          onClick={() => iniciarMutation.mutate()}
        >
          Iniciar simulado
        </Button>
      </Card>
    )
  }

  // EM_ANDAMENTO
  return (
    <ProvaEmAndamento
      simulado={simulado}
      onFinalizar={async () => {
        try {
          await finalizarMutation.mutateAsync()
          navigate(`/simulados/${simuladoId}/revisao`)
        } catch (error) {
          alert(extrairMensagemErro(error))
        }
      }}
      finalizando={finalizarMutation.isPending}
    />
  )
}

function ProvaEmAndamento({
  simulado,
  onFinalizar,
  finalizando,
}: {
  simulado: NonNullable<ReturnType<typeof useSimulado>['data']>
  onFinalizar: () => void
  finalizando: boolean
}) {
  const [indiceAtual, setIndiceAtual] = useState(0)
  const [respostas, setRespostas] = useState<Record<number, RespostaUsuarioResponseDTO>>({})
  const [erro, setErro] = useState<string | null>(null)

  const responderMutation = useResponderQuestao(simulado.id)

  const deadline = useMemo(() => {
    if (!simulado.inicio || simulado.tempoLimiteMinutos <= 0) return null
    return new Date(new Date(simulado.inicio).getTime() + simulado.tempoLimiteMinutos * 60_000)
  }, [simulado.inicio, simulado.tempoLimiteMinutos])

  const segundosRestantes = useCountdown(deadline)

  useEffect(() => {
    if (segundosRestantes === 0) {
      onFinalizar()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [segundosRestantes])

  const questaoAtual = simulado.questoes[indiceAtual]
  const respostaAtual = respostas[questaoAtual.id]
  const totalRespondidas = Object.keys(respostas).length
  const todasRespondidas = totalRespondidas === simulado.questoes.length
  const faltamResponder = simulado.questoes.length - totalRespondidas

  // Se a questão para a qual o aluno está navegando já foi respondida antes, a
  // alternativa marcada precisa aparecer pré-selecionada (convertendo letra → id,
  // já que é isso que a API guarda em RespostaUsuarioResponseDTO).
  const alternativaSelecionada = useMemo(() => {
    if (!respostaAtual) return null
    return (
      questaoAtual.alternativas.find((a) => a.letra === respostaAtual.alternativaMarcada)?.id ?? null
    )
  }, [questaoAtual, respostaAtual])

  function irParaQuestao(indice: number) {
    setIndiceAtual(indice)
    setErro(null)
  }

  async function selecionarAlternativa(alternativaId: number) {
    setErro(null)
    try {
      const resposta = await responderMutation.mutateAsync({
        questaoId: questaoAtual.id,
        alternativaMarcadaId: alternativaId,
      })
      setRespostas((prev) => ({ ...prev, [questaoAtual.id]: resposta }))

      // Avança automaticamente pra próxima questão ainda não visitada — exceto se
      // essa já era a última, onde não há pra onde avançar.
      if (indiceAtual < simulado.questoes.length - 1) {
        irParaQuestao(indiceAtual + 1)
      }
    } catch (error) {
      setErro(extrairMensagemErro(error))
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">
            Questão {indiceAtual + 1} de {simulado.questoes.length}
          </h1>
          <p className="text-sm text-slate-500">{totalRespondidas} respondida(s)</p>
        </div>
        {segundosRestantes !== null && (
          <Badge tone={segundosRestantes < 60 ? 'red' : 'blue'} className="text-sm">
            ⏱ {formatarTempo(segundosRestantes)}
          </Badge>
        )}
      </div>

      <Card>
        <p className="mb-4 text-xs font-medium uppercase tracking-wide text-slate-400">
          {questaoAtual.disciplina} · {questaoAtual.assunto}
        </p>
        <p className="whitespace-pre-wrap text-slate-900">{questaoAtual.enunciado}</p>

        <div className="mt-6 flex flex-col gap-2">
          {questaoAtual.alternativas.map((alternativa) => {
            const selecionada = alternativaSelecionada === alternativa.id
            return (
              <label
                key={alternativa.id}
                className={`flex cursor-pointer items-start gap-3 rounded-lg border px-4 py-3 text-sm transition-colors
                  ${selecionada ? 'border-indigo-500 bg-indigo-50' : 'border-slate-200 hover:bg-slate-50'}
                  ${responderMutation.isPending ? 'pointer-events-none opacity-60' : ''}`}
              >
                <input
                  type="radio"
                  name={`questao-${questaoAtual.id}`}
                  className="mt-0.5"
                  checked={selecionada}
                  disabled={responderMutation.isPending}
                  onChange={() => selecionarAlternativa(alternativa.id)}
                />
                <span>
                  <strong className="mr-1">{alternativa.letra})</strong>
                  {alternativa.descricao}
                </span>
              </label>
            )
          })}
        </div>

        {erro && (
          <div className="mt-4">
            <Alert tone="error">{erro}</Alert>
          </div>
        )}

        {respostaAtual && !responderMutation.isPending && (
          <p className="mt-4 text-sm text-slate-400">
            Resposta registrada. Você só verá o gabarito e a explicação depois de finalizar o simulado.
          </p>
        )}
      </Card>

      <div className="flex items-center justify-between">
        <Button
          variant="secondary"
          disabled={indiceAtual === 0}
          onClick={() => irParaQuestao(indiceAtual - 1)}
        >
          Anterior
        </Button>

        <div className="flex flex-wrap justify-center gap-1">
          {simulado.questoes.map((questao, indice) => (
            <button
              key={questao.id}
              onClick={() => irParaQuestao(indice)}
              className={`h-8 w-8 rounded-full text-xs font-medium transition-colors
                ${indice === indiceAtual ? 'bg-indigo-600 text-white' : ''}
                ${indice !== indiceAtual && respostas[questao.id] ? 'bg-green-100 text-green-700' : ''}
                ${indice !== indiceAtual && !respostas[questao.id] ? 'bg-slate-100 text-slate-500 hover:bg-slate-200' : ''}`}
            >
              {indice + 1}
            </button>
          ))}
        </div>

        {indiceAtual < simulado.questoes.length - 1 ? (
          <Button variant="secondary" onClick={() => irParaQuestao(indiceAtual + 1)}>
            Próxima
          </Button>
        ) : (
          <Button
            variant="danger"
            onClick={onFinalizar}
            isLoading={finalizando}
            disabled={!todasRespondidas}
            title={todasRespondidas ? undefined : `Responda todas as questões antes de finalizar`}
          >
            Finalizar simulado
          </Button>
        )}
      </div>

      {!todasRespondidas && (
        <Alert tone="info">
          Faltam {faltamResponder} questão(ões) para responder antes de finalizar o simulado.
        </Alert>
      )}
    </div>
  )
}
