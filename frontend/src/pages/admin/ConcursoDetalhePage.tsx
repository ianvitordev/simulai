import { useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Alert } from '../../components/ui/Alert'
import { Badge } from '../../components/ui/Badge'
import { Button } from '../../components/ui/Button'
import { Card } from '../../components/ui/Card'
import { Select } from '../../components/ui/Field'
import { Spinner } from '../../components/ui/Spinner'
import { useDisciplinas } from '../../hooks/useDisciplinas'
import {
  useAdicionarDisciplinaAoConcurso,
  useConcursos,
  useIndexarEdital,
  useRemoverDisciplinaDoConcurso,
} from '../../hooks/useConcursos'
import { extrairMensagemErro } from '../../lib/apiClient'
import { rotuloStatusConcurso } from '../../lib/format'

export function ConcursoDetalhePage() {
  const { id } = useParams()
  const concursoId = Number(id)
  const navigate = useNavigate()

  const { data: concursos, isLoading } = useConcursos()
  const { data: disciplinas } = useDisciplinas()
  const adicionarMutation = useAdicionarDisciplinaAoConcurso()
  const removerMutation = useRemoverDisciplinaDoConcurso()
  const indexarMutation = useIndexarEdital()

  const [disciplinaSelecionada, setDisciplinaSelecionada] = useState('')
  const [mensagem, setMensagem] = useState<{ tipo: 'error' | 'success'; texto: string } | null>(null)

  const concurso = concursos?.find((c) => c.id === concursoId)

  const disciplinasDisponiveis = useMemo(() => {
    if (!disciplinas || !concurso) return []
    return disciplinas.filter((d) => !concurso.disciplinas.includes(d.nome))
  }, [disciplinas, concurso])

  if (isLoading) return <Spinner />
  if (!concurso) return <Alert tone="error">Concurso não encontrado.</Alert>

  async function handleAdicionar() {
    if (!disciplinaSelecionada) return
    setMensagem(null)
    try {
      await adicionarMutation.mutateAsync({ concursoId, disciplinaId: Number(disciplinaSelecionada) })
      setDisciplinaSelecionada('')
    } catch (error) {
      setMensagem({ tipo: 'error', texto: extrairMensagemErro(error) })
    }
  }

  async function handleRemover(nomeDisciplina: string) {
    const disciplina = disciplinas?.find((d) => d.nome === nomeDisciplina)
    if (!disciplina) return
    setMensagem(null)
    try {
      await removerMutation.mutateAsync({ concursoId, disciplinaId: disciplina.id })
    } catch (error) {
      setMensagem({ tipo: 'error', texto: extrairMensagemErro(error) })
    }
  }

  async function handleIndexarEdital() {
    setMensagem(null)
    try {
      const resultado = await indexarMutation.mutateAsync(concursoId)
      setMensagem({ tipo: 'success', texto: `Edital indexado: ${resultado.chunksIndexados} trecho(s).` })
    } catch (error) {
      setMensagem({ tipo: 'error', texto: extrairMensagemErro(error) })
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">{concurso.nome}</h1>
          <p className="text-sm text-slate-500">
            {concurso.orgao} · {concurso.cargo} · {concurso.banca} ·{' '}
            <Badge tone="slate">{rotuloStatusConcurso(concurso.status)}</Badge>
          </p>
        </div>
        <Button variant="secondary" onClick={() => navigate('/admin/concursos')}>
          Voltar
        </Button>
      </div>

      {mensagem && <Alert tone={mensagem.tipo}>{mensagem.texto}</Alert>}

      <Card>
        <h2 className="mb-3 font-medium text-slate-900">Edital (RAG)</h2>
        <p className="text-sm text-slate-500">
          URL: {concurso.editalUrl ? <span className="break-all">{concurso.editalUrl}</span> : 'não cadastrada'}
        </p>
        <p className="mt-1 text-sm text-slate-500">
          Status:{' '}
          {concurso.editalIndexado ? (
            <Badge tone="green">Indexado</Badge>
          ) : (
            <Badge tone="slate">Não indexado</Badge>
          )}
        </p>
        <Button
          className="mt-4"
          onClick={handleIndexarEdital}
          isLoading={indexarMutation.isPending}
          disabled={!concurso.editalUrl}
        >
          {concurso.editalIndexado ? 'Reindexar edital' : 'Indexar edital'}
        </Button>
        {!concurso.editalUrl && (
          <p className="mt-2 text-xs text-slate-400">
            Cadastre a URL do edital editando o concurso na lista para habilitar a indexação.
          </p>
        )}
      </Card>

      <Card>
        <h2 className="mb-3 font-medium text-slate-900">Disciplinas do edital</h2>

        {concurso.disciplinas.length === 0 ? (
          <p className="text-sm text-slate-500">Nenhuma disciplina associada ainda.</p>
        ) : (
          <div className="flex flex-wrap gap-2">
            {concurso.disciplinas.map((nome) => (
              <span
                key={nome}
                className="inline-flex items-center gap-2 rounded-full bg-slate-100 px-3 py-1 text-sm text-slate-700"
              >
                {nome}
                <button
                  onClick={() => handleRemover(nome)}
                  disabled={removerMutation.isPending}
                  className="text-slate-400 hover:text-red-600"
                  aria-label={`Remover ${nome}`}
                >
                  ✕
                </button>
              </span>
            ))}
          </div>
        )}

        <div className="mt-4 flex gap-2">
          <Select
            value={disciplinaSelecionada}
            onChange={(e) => setDisciplinaSelecionada(e.target.value)}
            className="flex-1"
          >
            <option value="">Selecione uma disciplina para adicionar...</option>
            {disciplinasDisponiveis.map((d) => (
              <option key={d.id} value={d.id}>
                {d.nome}
              </option>
            ))}
          </Select>
          <Button
            onClick={handleAdicionar}
            isLoading={adicionarMutation.isPending}
            disabled={!disciplinaSelecionada}
          >
            Adicionar
          </Button>
        </div>
      </Card>
    </div>
  )
}
