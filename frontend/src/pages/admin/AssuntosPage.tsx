import { zodResolver } from '@hookform/resolvers/zod'
import { useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { Alert } from '../../components/ui/Alert'
import { Button } from '../../components/ui/Button'
import { Card } from '../../components/ui/Card'
import { FieldWrapper, Input, Select } from '../../components/ui/Field'
import { Modal } from '../../components/ui/Modal'
import { Spinner } from '../../components/ui/Spinner'
import {
  useAssuntos,
  useAtualizarAssunto,
  useCadastrarAssunto,
  useDeletarAssunto,
} from '../../hooks/useAssuntos'
import { useDisciplinas } from '../../hooks/useDisciplinas'
import { extrairMensagemErro } from '../../lib/apiClient'
import type { AssuntoResponseDTO } from '../../types/api'

const schema = z.object({
  nome: z.string().min(1, 'Informe o nome'),
  descricao: z.string().optional(),
  disciplinaId: z.string().min(1, 'Selecione uma disciplina'),
})

type FormData = z.infer<typeof schema>

export function AssuntosPage() {
  const { data: assuntos, isLoading } = useAssuntos()
  const { data: disciplinas, isLoading: carregandoDisciplinas } = useDisciplinas()
  const cadastrarMutation = useCadastrarAssunto()
  const atualizarMutation = useAtualizarAssunto()
  const deletarMutation = useDeletarAssunto()

  const [filtroDisciplina, setFiltroDisciplina] = useState('')
  const [modalAberto, setModalAberto] = useState(false)
  const [editando, setEditando] = useState<AssuntoResponseDTO | null>(null)
  const [erro, setErro] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  const assuntosFiltrados = useMemo(() => {
    if (!assuntos) return []
    if (!filtroDisciplina) return assuntos
    return assuntos.filter((assunto) => assunto.disciplina === filtroDisciplina)
  }, [assuntos, filtroDisciplina])

  function abrirCriacao() {
    setEditando(null)
    reset({ nome: '', descricao: '', disciplinaId: '' })
    setErro(null)
    setModalAberto(true)
  }

  function abrirEdicao(assunto: AssuntoResponseDTO) {
    const disciplina = disciplinas?.find((d) => d.nome === assunto.disciplina)
    setEditando(assunto)
    reset({
      nome: assunto.nome,
      descricao: assunto.descricao ?? '',
      disciplinaId: disciplina ? String(disciplina.id) : '',
    })
    setErro(null)
    setModalAberto(true)
  }

  async function onSubmit(data: FormData) {
    setErro(null)
    const request = { nome: data.nome, descricao: data.descricao, disciplinaId: Number(data.disciplinaId) }
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

  async function handleRemover(assunto: AssuntoResponseDTO) {
    if (!confirm(`Remover "${assunto.nome}"?`)) return
    try {
      await deletarMutation.mutateAsync(assunto.id)
    } catch (error) {
      alert(extrairMensagemErro(error))
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-900">Assuntos</h1>
        <Button onClick={abrirCriacao}>Novo</Button>
      </div>

      <Select
        value={filtroDisciplina}
        onChange={(e) => setFiltroDisciplina(e.target.value)}
        className="max-w-xs"
      >
        <option value="">Todas as disciplinas</option>
        {disciplinas?.map((d) => (
          <option key={d.id} value={d.nome}>
            {d.nome}
          </option>
        ))}
      </Select>

      {isLoading ? (
        <Spinner />
      ) : assuntosFiltrados.length === 0 ? (
        <Card className="text-center text-slate-500">Nenhum assunto encontrado.</Card>
      ) : (
        <div className="flex flex-col gap-2">
          {assuntosFiltrados.map((assunto) => (
            <Card key={assunto.id} className="flex items-center justify-between gap-4 py-3">
              <div className="min-w-0">
                <p className="font-medium text-slate-900">{assunto.nome}</p>
                <p className="text-sm text-slate-500">{assunto.disciplina}</p>
              </div>
              <div className="flex shrink-0 gap-2">
                <Button variant="secondary" onClick={() => abrirEdicao(assunto)}>
                  Editar
                </Button>
                <Button
                  variant="danger"
                  isLoading={deletarMutation.isPending}
                  onClick={() => handleRemover(assunto)}
                >
                  Remover
                </Button>
              </div>
            </Card>
          ))}
        </div>
      )}

      {modalAberto && (
        <Modal title={editando ? 'Editar assunto' : 'Novo assunto'} onClose={() => setModalAberto(false)}>
          <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
            {erro && <Alert tone="error">{erro}</Alert>}

            <FieldWrapper label="Nome" htmlFor="nome" error={errors.nome?.message}>
              <Input id="nome" {...register('nome')} />
            </FieldWrapper>

            <FieldWrapper label="Descrição (opcional)" htmlFor="descricao">
              <Input id="descricao" {...register('descricao')} />
            </FieldWrapper>

            <FieldWrapper label="Disciplina" htmlFor="disciplinaId" error={errors.disciplinaId?.message}>
              <Select id="disciplinaId" disabled={carregandoDisciplinas} {...register('disciplinaId')}>
                <option value="">Selecione...</option>
                {disciplinas?.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.nome}
                  </option>
                ))}
              </Select>
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
