import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { Alert } from '../ui/Alert'
import { Button } from '../ui/Button'
import { Card } from '../ui/Card'
import { FieldWrapper, Input } from '../ui/Field'
import { Modal } from '../ui/Modal'
import { Spinner } from '../ui/Spinner'
import { extrairMensagemErro } from '../../lib/apiClient'

const schema = z.object({
  nome: z.string().min(1, 'Informe o nome'),
  descricao: z.string().min(1, 'Informe a descrição'),
})

type FormData = z.infer<typeof schema>

interface ItemBase {
  id: number
  nome: string
  descricao: string
}

interface SimpleCatalogPageProps<T extends ItemBase> {
  titulo: string
  itens: T[] | undefined
  carregando: boolean
  criando: boolean
  atualizando: boolean
  removendo: boolean
  aoCriar: (data: FormData) => Promise<unknown>
  aoAtualizar: (id: number, data: FormData) => Promise<unknown>
  aoRemover: (id: number) => Promise<unknown>
}

export function SimpleCatalogPage<T extends ItemBase>({
  titulo,
  itens,
  carregando,
  criando,
  atualizando,
  removendo,
  aoCriar,
  aoAtualizar,
  aoRemover,
}: SimpleCatalogPageProps<T>) {
  const [modalAberto, setModalAberto] = useState(false)
  const [editando, setEditando] = useState<T | null>(null)
  const [erro, setErro] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  function abrirCriacao() {
    setEditando(null)
    reset({ nome: '', descricao: '' })
    setErro(null)
    setModalAberto(true)
  }

  function abrirEdicao(item: T) {
    setEditando(item)
    reset({ nome: item.nome, descricao: item.descricao })
    setErro(null)
    setModalAberto(true)
  }

  async function onSubmit(data: FormData) {
    setErro(null)
    try {
      if (editando) {
        await aoAtualizar(editando.id, data)
      } else {
        await aoCriar(data)
      }
      setModalAberto(false)
    } catch (error) {
      setErro(extrairMensagemErro(error))
    }
  }

  async function handleRemover(item: T) {
    if (!confirm(`Remover "${item.nome}"? Essa ação não pode ser desfeita.`)) return
    try {
      await aoRemover(item.id)
    } catch (error) {
      alert(extrairMensagemErro(error))
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-900">{titulo}</h1>
        <Button onClick={abrirCriacao}>Novo</Button>
      </div>

      {carregando ? (
        <Spinner />
      ) : !itens || itens.length === 0 ? (
        <Card className="text-center text-slate-500">Nenhum registro ainda.</Card>
      ) : (
        <div className="flex flex-col gap-2">
          {itens.map((item) => (
            <Card key={item.id} className="flex items-center justify-between gap-4 py-3">
              <div className="min-w-0">
                <p className="font-medium text-slate-900">{item.nome}</p>
                <p className="truncate text-sm text-slate-500">{item.descricao}</p>
              </div>
              <div className="flex shrink-0 gap-2">
                <Button variant="secondary" onClick={() => abrirEdicao(item)}>
                  Editar
                </Button>
                <Button variant="danger" isLoading={removendo} onClick={() => handleRemover(item)}>
                  Remover
                </Button>
              </div>
            </Card>
          ))}
        </div>
      )}

      {modalAberto && (
        <Modal title={editando ? 'Editar' : 'Novo registro'} onClose={() => setModalAberto(false)}>
          <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
            {erro && <Alert tone="error">{erro}</Alert>}

            <FieldWrapper label="Nome" htmlFor="nome" error={errors.nome?.message}>
              <Input id="nome" {...register('nome')} />
            </FieldWrapper>

            <FieldWrapper label="Descrição" htmlFor="descricao" error={errors.descricao?.message}>
              <Input id="descricao" {...register('descricao')} />
            </FieldWrapper>

            <Button type="submit" isLoading={criando || atualizando} className="w-full">
              {editando ? 'Salvar' : 'Criar'}
            </Button>
          </form>
        </Modal>
      )}
    </div>
  )
}
