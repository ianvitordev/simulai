import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { Alert } from '../../components/ui/Alert'
import { Badge } from '../../components/ui/Badge'
import { Button } from '../../components/ui/Button'
import { Card } from '../../components/ui/Card'
import { FieldWrapper, Input, Select } from '../../components/ui/Field'
import { Modal } from '../../components/ui/Modal'
import { Spinner } from '../../components/ui/Spinner'
import { useBancas } from '../../hooks/useBancas'
import {
  useAtualizarConcurso,
  useCadastrarConcurso,
  useConcursos,
  useDeletarConcurso,
} from '../../hooks/useConcursos'
import { extrairMensagemErro } from '../../lib/apiClient'
import { STATUS_CONCURSO_OPCOES, rotuloStatusConcurso } from '../../lib/format'
import type { ConcursoResponseDTO, StatusConcurso } from '../../types/api'

const schema = z.object({
  nome: z.string().min(1, 'Informe o nome'),
  orgao: z.string().min(1, 'Informe o órgão'),
  cargo: z.string().min(1, 'Informe o cargo'),
  ano: z.coerce.number().int().optional(),
  bancaId: z.string().min(1, 'Selecione uma banca'),
  status: z.enum(STATUS_CONCURSO_OPCOES as [StatusConcurso, ...StatusConcurso[]]),
  editalUrl: z.string().optional(),
})

type FormInput = z.input<typeof schema>
type FormOutput = z.output<typeof schema>

export function ConcursosPage() {
  const navigate = useNavigate()
  const { data: concursos, isLoading } = useConcursos()
  const { data: bancas, isLoading: carregandoBancas } = useBancas()
  const cadastrarMutation = useCadastrarConcurso()
  const atualizarMutation = useAtualizarConcurso()
  const deletarMutation = useDeletarConcurso()

  const [modalAberto, setModalAberto] = useState(false)
  const [editando, setEditando] = useState<ConcursoResponseDTO | null>(null)
  const [erro, setErro] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormInput, unknown, FormOutput>({ resolver: zodResolver(schema) })

  function abrirCriacao() {
    setEditando(null)
    reset({ nome: '', orgao: '', cargo: '', bancaId: '', status: 'PREVISTO', editalUrl: '' })
    setErro(null)
    setModalAberto(true)
  }

  function abrirEdicao(concurso: ConcursoResponseDTO) {
    const banca = bancas?.find((b) => b.nome === concurso.banca)
    setEditando(concurso)
    reset({
      nome: concurso.nome,
      orgao: concurso.orgao,
      cargo: concurso.cargo,
      ano: concurso.ano ?? undefined,
      bancaId: banca ? String(banca.id) : '',
      status: concurso.status,
      editalUrl: concurso.editalUrl ?? '',
    })
    setErro(null)
    setModalAberto(true)
  }

  async function onSubmit(data: FormOutput) {
    setErro(null)
    const request = {
      nome: data.nome,
      orgao: data.orgao,
      cargo: data.cargo,
      ano: data.ano,
      bancaId: Number(data.bancaId),
      status: data.status,
      editalUrl: data.editalUrl || undefined,
    }
    try {
      if (editando) {
        await atualizarMutation.mutateAsync({ id: editando.id, request })
      } else {
        await cadastrarMutation.mutateAsync(request)
      }
      setModalAberto(false)
    } catch (error) {
      setErro(extrairMensagemErro(error))
    }
  }

  async function handleRemover(concurso: ConcursoResponseDTO) {
    if (!confirm(`Remover "${concurso.nome}"?`)) return
    try {
      await deletarMutation.mutateAsync(concurso.id)
    } catch (error) {
      alert(extrairMensagemErro(error))
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-900">Concursos</h1>
        <Button onClick={abrirCriacao}>Novo</Button>
      </div>

      {isLoading ? (
        <Spinner />
      ) : !concursos || concursos.length === 0 ? (
        <Card className="text-center text-slate-500">Nenhum concurso cadastrado ainda.</Card>
      ) : (
        <div className="flex flex-col gap-2">
          {concursos.map((concurso) => (
            <Card key={concurso.id} className="flex items-center justify-between gap-4 py-3">
              <div className="min-w-0">
                <div className="flex items-center gap-2">
                  <p className="font-medium text-slate-900">{concurso.nome}</p>
                  <Badge tone="slate">{rotuloStatusConcurso(concurso.status)}</Badge>
                  {concurso.editalIndexado && <Badge tone="green">Edital indexado</Badge>}
                </div>
                <p className="truncate text-sm text-slate-500">
                  {concurso.orgao} · {concurso.cargo} · {concurso.banca}
                  {concurso.disciplinas.length > 0 && ` · ${concurso.disciplinas.length} disciplina(s)`}
                </p>
              </div>
              <div className="flex shrink-0 gap-2">
                <Button variant="secondary" onClick={() => navigate(`/admin/concursos/${concurso.id}`)}>
                  Gerenciar
                </Button>
                <Button variant="secondary" onClick={() => abrirEdicao(concurso)}>
                  Editar
                </Button>
                <Button variant="danger" isLoading={deletarMutation.isPending} onClick={() => handleRemover(concurso)}>
                  Remover
                </Button>
              </div>
            </Card>
          ))}
        </div>
      )}

      {modalAberto && (
        <Modal title={editando ? 'Editar concurso' : 'Novo concurso'} onClose={() => setModalAberto(false)}>
          <form onSubmit={handleSubmit(onSubmit)} className="flex max-h-[70vh] flex-col gap-4 overflow-y-auto pr-1">
            {erro && <Alert tone="error">{erro}</Alert>}

            <FieldWrapper label="Nome" htmlFor="nome" error={errors.nome?.message}>
              <Input id="nome" {...register('nome')} />
            </FieldWrapper>

            <FieldWrapper label="Órgão" htmlFor="orgao" error={errors.orgao?.message}>
              <Input id="orgao" {...register('orgao')} />
            </FieldWrapper>

            <FieldWrapper label="Cargo" htmlFor="cargo" error={errors.cargo?.message}>
              <Input id="cargo" {...register('cargo')} />
            </FieldWrapper>

            <FieldWrapper label="Ano (opcional)" htmlFor="ano">
              <Input id="ano" type="number" {...register('ano')} />
            </FieldWrapper>

            <FieldWrapper label="Banca" htmlFor="bancaId" error={errors.bancaId?.message}>
              <Select id="bancaId" disabled={carregandoBancas} {...register('bancaId')}>
                <option value="">Selecione...</option>
                {bancas?.map((b) => (
                  <option key={b.id} value={b.id}>
                    {b.nome}
                  </option>
                ))}
              </Select>
            </FieldWrapper>

            <FieldWrapper label="Status" htmlFor="status" error={errors.status?.message}>
              <Select id="status" {...register('status')}>
                {STATUS_CONCURSO_OPCOES.map((status) => (
                  <option key={status} value={status}>
                    {rotuloStatusConcurso(status)}
                  </option>
                ))}
              </Select>
            </FieldWrapper>

            <FieldWrapper label="URL do edital (opcional, para RAG)" htmlFor="editalUrl">
              <Input id="editalUrl" placeholder="https://..." {...register('editalUrl')} />
            </FieldWrapper>

            <Button
              type="submit"
              isLoading={cadastrarMutation.isPending || atualizarMutation.isPending}
              className="w-full"
            >
              {editando ? 'Salvar' : 'Criar'}
            </Button>
          </form>
        </Modal>
      )}
    </div>
  )
}
