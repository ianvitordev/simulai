import { zodResolver } from '@hookform/resolvers/zod'
import { useEffect, useMemo, useState } from 'react'
import { useFieldArray, useForm } from 'react-hook-form'
import { z } from 'zod'
import { Alert } from '../../components/ui/Alert'
import { Badge } from '../../components/ui/Badge'
import { Button } from '../../components/ui/Button'
import { Card } from '../../components/ui/Card'
import { FieldWrapper, Input, Select } from '../../components/ui/Field'
import { Modal } from '../../components/ui/Modal'
import { Spinner } from '../../components/ui/Spinner'
import { useAssuntosPorDisciplina } from '../../hooks/useAssuntos'
import { useConcursos } from '../../hooks/useConcursos'
import { useDisciplinas } from '../../hooks/useDisciplinas'
import {
  useAtualizarQuestao,
  useDeletarQuestao,
  useGerarQuestoesViaIA,
  useQuestoesModeracao,
} from '../../hooks/useQuestoes'
import { extrairMensagemErro } from '../../lib/apiClient'
import {
  DIFICULDADE_OPCOES,
  TIPO_QUESTAO_OPCOES,
  rotuloDificuldade,
  rotuloTipoQuestao,
} from '../../lib/format'
import type { Dificuldade, LetraAlternativa, QuestaoAdminResponseDTO } from '../../types/api'

const LETRAS: LetraAlternativa[] = ['A', 'B', 'C', 'D', 'E']

const alternativaSchema = z.object({
  letra: z.enum(['A', 'B', 'C', 'D', 'E']),
  descricao: z.string().min(1, 'Informe o texto da alternativa'),
})

const editSchema = z.object({
  enunciado: z.string().min(1, 'Informe o enunciado'),
  comentario: z.string().optional(),
  explicacao: z.string().optional(),
  ano: z.coerce.number().int(),
  fonte: z.string().optional(),
  dificuldade: z.enum(['FACIL', 'MEDIA', 'DIFICIL']),
  tipo: z.enum(['MULTIPLA_ESCOLHA', 'CERTO_ERRADO', 'DISCURSIVA']),
  disciplinaId: z.string().min(1, 'Selecione uma disciplina'),
  assuntoId: z.string().min(1, 'Selecione um assunto'),
  concursoId: z.string().optional(),
  alternativas: z.array(alternativaSchema),
})

type EditFormInput = z.input<typeof editSchema>
type EditFormOutput = z.output<typeof editSchema>

const gerarIaSchema = z.object({
  disciplinaId: z.string().min(1, 'Selecione uma disciplina'),
  assuntoId: z.string().min(1, 'Selecione um assunto'),
  concursoId: z.string().optional(),
  dificuldade: z.enum(['FACIL', 'MEDIA', 'DIFICIL']),
  tipo: z.enum(['MULTIPLA_ESCOLHA', 'CERTO_ERRADO', 'DISCURSIVA']),
  quantidade: z.coerce.number().int().positive('Informe um número maior que zero'),
})

type GerarIaFormInput = z.input<typeof gerarIaSchema>
type GerarIaFormOutput = z.output<typeof gerarIaSchema>

export function QuestoesPage() {
  const { data: questoes, isLoading } = useQuestoesModeracao()
  const { data: disciplinas, isLoading: carregandoDisciplinas } = useDisciplinas()
  const { data: concursos } = useConcursos()
  const atualizarMutation = useAtualizarQuestao()
  const deletarMutation = useDeletarQuestao()
  const gerarIaMutation = useGerarQuestoesViaIA()

  const [filtroDisciplina, setFiltroDisciplina] = useState('')
  const [filtroDificuldade, setFiltroDificuldade] = useState<Dificuldade | ''>('')

  const [editando, setEditando] = useState<QuestaoAdminResponseDTO | null>(null)
  const [erroEdicao, setErroEdicao] = useState<string | null>(null)

  const [modalGerarIaAberto, setModalGerarIaAberto] = useState(false)
  const [erroGerarIa, setErroGerarIa] = useState<string | null>(null)
  const [resultadoGerarIa, setResultadoGerarIa] = useState<string | null>(null)

  const questoesFiltradas = useMemo(() => {
    if (!questoes) return []
    return questoes.filter((questao) => {
      if (filtroDisciplina && questao.disciplina !== filtroDisciplina) return false
      if (filtroDificuldade && questao.dificuldade !== filtroDificuldade) return false
      return true
    })
  }, [questoes, filtroDisciplina, filtroDificuldade])

  const {
    register: registerEdit,
    handleSubmit: handleSubmitEdit,
    reset: resetEdit,
    watch: watchEdit,
    setValue: setValueEdit,
    control: controlEdit,
    formState: { errors: errorsEdit },
  } = useForm<EditFormInput, unknown, EditFormOutput>({ resolver: zodResolver(editSchema) })

  const { fields, append, remove } = useFieldArray({ control: controlEdit, name: 'alternativas' })

  const disciplinaIdEdit = watchEdit('disciplinaId')
  const tipoEdit = watchEdit('tipo')
  const alternativasEdit = watchEdit('alternativas')
  const { data: assuntosDisciplinaEdit } = useAssuntosPorDisciplina(
    disciplinaIdEdit ? Number(disciplinaIdEdit) : undefined,
  )

  function abrirEdicao(questao: QuestaoAdminResponseDTO) {
    const disciplina = disciplinas?.find((d) => d.nome === questao.disciplina)
    const concurso = questao.concurso ? concursos?.find((c) => c.nome === questao.concurso) : undefined
    setEditando(questao)
    setErroEdicao(null)
    resetEdit({
      enunciado: questao.enunciado,
      comentario: questao.comentario ?? '',
      explicacao: questao.explicacao ?? '',
      ano: questao.ano,
      fonte: questao.fonte ?? '',
      dificuldade: questao.dificuldade,
      tipo: questao.tipo,
      disciplinaId: disciplina ? String(disciplina.id) : '',
      assuntoId: '',
      concursoId: concurso ? String(concurso.id) : '',
      alternativas: questao.alternativas.map((a) => ({ letra: a.letra, descricao: a.descricao })),
    })
  }

  // O assunto só pode ser resolvido depois que a lista de assuntos da disciplina carregar,
  // já que o nome do assunto não é necessariamente único fora do escopo da disciplina.
  useEffect(() => {
    if (!editando || !assuntosDisciplinaEdit) return
    const assunto = assuntosDisciplinaEdit.find((a) => a.nome === editando.assunto)
    if (assunto) {
      setValueEdit('assuntoId', String(assunto.id))
    }
  }, [editando, assuntosDisciplinaEdit, setValueEdit])

  const [corretaIndex, setCorretaIndex] = useState<number | null>(null)

  function abrirEdicaoComGabarito(questao: QuestaoAdminResponseDTO) {
    abrirEdicao(questao)
    const indiceCorreta = questao.alternativas.findIndex((a) => a.correta)
    setCorretaIndex(indiceCorreta >= 0 ? indiceCorreta : null)
  }

  async function onSubmitEdicao(data: EditFormOutput) {
    if (!editando) return
    setErroEdicao(null)

    const alternativas =
      data.tipo === 'DISCURSIVA'
        ? []
        : data.alternativas.map((alternativa, index) => ({
            ...alternativa,
            correta: index === corretaIndex,
          }))

    if (data.tipo !== 'DISCURSIVA' && !alternativas.some((a) => a.correta)) {
      setErroEdicao('Marque uma alternativa como correta.')
      return
    }

    try {
      await atualizarMutation.mutateAsync({
        id: editando.id,
        request: {
          enunciado: data.enunciado,
          comentario: data.comentario || undefined,
          explicacao: data.explicacao || undefined,
          ano: data.ano,
          fonte: data.fonte || undefined,
          dificuldade: data.dificuldade,
          tipo: data.tipo,
          disciplinaId: Number(data.disciplinaId),
          assuntoId: Number(data.assuntoId),
          concursoId: data.concursoId ? Number(data.concursoId) : undefined,
          alternativas,
        },
      })
      setEditando(null)
    } catch (error) {
      setErroEdicao(extrairMensagemErro(error))
    }
  }

  async function handleRemover(questao: QuestaoAdminResponseDTO) {
    if (!confirm('Remover esta questão?')) return
    try {
      await deletarMutation.mutateAsync(questao.id)
    } catch (error) {
      alert(extrairMensagemErro(error))
    }
  }

  const {
    register: registerGerarIa,
    handleSubmit: handleSubmitGerarIa,
    reset: resetGerarIa,
    watch: watchGerarIa,
    formState: { errors: errorsGerarIa },
  } = useForm<GerarIaFormInput, unknown, GerarIaFormOutput>({
    resolver: zodResolver(gerarIaSchema),
    defaultValues: { disciplinaId: '', assuntoId: '', concursoId: '', dificuldade: 'MEDIA', tipo: 'MULTIPLA_ESCOLHA', quantidade: 5 },
  })

  const disciplinaIdGerarIa = watchGerarIa('disciplinaId')
  const { data: assuntosDisciplinaGerarIa, isLoading: carregandoAssuntosGerarIa } = useAssuntosPorDisciplina(
    disciplinaIdGerarIa ? Number(disciplinaIdGerarIa) : undefined,
  )

  function abrirGerarIa() {
    resetGerarIa({ disciplinaId: '', assuntoId: '', concursoId: '', dificuldade: 'MEDIA', tipo: 'MULTIPLA_ESCOLHA', quantidade: 5 })
    setErroGerarIa(null)
    setResultadoGerarIa(null)
    setModalGerarIaAberto(true)
  }

  async function onSubmitGerarIa(data: GerarIaFormOutput) {
    setErroGerarIa(null)
    setResultadoGerarIa(null)
    try {
      const geradas = await gerarIaMutation.mutateAsync({
        disciplinaId: Number(data.disciplinaId),
        assuntoId: Number(data.assuntoId),
        concursoId: data.concursoId ? Number(data.concursoId) : undefined,
        dificuldade: data.dificuldade,
        tipo: data.tipo,
        quantidade: data.quantidade,
      })
      setResultadoGerarIa(`${geradas.length} questão(ões) gerada(s) com sucesso.`)
    } catch (error) {
      setErroGerarIa(extrairMensagemErro(error))
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-900">Questões</h1>
        <Button onClick={abrirGerarIa}>Gerar via IA</Button>
      </div>

      <div className="flex gap-2">
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

        <Select
          value={filtroDificuldade}
          onChange={(e) => setFiltroDificuldade(e.target.value as Dificuldade | '')}
          className="max-w-xs"
        >
          <option value="">Todas as dificuldades</option>
          {DIFICULDADE_OPCOES.map((d) => (
            <option key={d} value={d}>
              {rotuloDificuldade(d)}
            </option>
          ))}
        </Select>
      </div>

      {isLoading ? (
        <Spinner />
      ) : questoesFiltradas.length === 0 ? (
        <Card className="text-center text-slate-500">Nenhuma questão encontrada.</Card>
      ) : (
        <div className="flex flex-col gap-2">
          {questoesFiltradas.map((questao) => (
            <Card key={questao.id} className="flex items-start justify-between gap-4 py-3">
              <div className="min-w-0">
                <p className="line-clamp-2 font-medium text-slate-900">{questao.enunciado}</p>
                <div className="mt-1 flex flex-wrap items-center gap-2">
                  <Badge tone="slate">{questao.disciplina}</Badge>
                  <Badge tone="slate">{questao.assunto}</Badge>
                  <Badge tone="blue">{rotuloDificuldade(questao.dificuldade)}</Badge>
                  <Badge tone="amber">{rotuloTipoQuestao(questao.tipo)}</Badge>
                  {questao.geradaPorIA && <Badge tone="green">IA</Badge>}
                </div>
              </div>
              <div className="flex shrink-0 gap-2">
                <Button variant="secondary" onClick={() => abrirEdicaoComGabarito(questao)}>
                  Editar
                </Button>
                <Button variant="danger" isLoading={deletarMutation.isPending} onClick={() => handleRemover(questao)}>
                  Remover
                </Button>
              </div>
            </Card>
          ))}
        </div>
      )}

      {editando && (
        <Modal title="Editar questão" onClose={() => setEditando(null)} maxWidth="xl">
          <form
            onSubmit={handleSubmitEdit(onSubmitEdicao)}
            className="flex max-h-[70vh] flex-col gap-4 overflow-y-auto pr-1"
          >
            {erroEdicao && <Alert tone="error">{erroEdicao}</Alert>}

            <FieldWrapper label="Enunciado" htmlFor="enunciado" error={errorsEdit.enunciado?.message}>
              <textarea
                id="enunciado"
                rows={4}
                className="rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 shadow-sm focus:border-gold-500 focus:outline-none focus:ring-1 focus:ring-gold-500"
                {...registerEdit('enunciado')}
              />
            </FieldWrapper>

            <FieldWrapper label="Comentário (opcional)" htmlFor="comentario">
              <Input id="comentario" {...registerEdit('comentario')} />
            </FieldWrapper>

            <FieldWrapper label="Explicação (opcional)" htmlFor="explicacao">
              <textarea
                id="explicacao"
                rows={3}
                className="rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 shadow-sm focus:border-gold-500 focus:outline-none focus:ring-1 focus:ring-gold-500"
                {...registerEdit('explicacao')}
              />
            </FieldWrapper>

            <div className="grid grid-cols-2 gap-4">
              <FieldWrapper label="Ano" htmlFor="ano" error={errorsEdit.ano?.message}>
                <Input id="ano" type="number" {...registerEdit('ano')} />
              </FieldWrapper>

              <FieldWrapper label="Fonte (opcional)" htmlFor="fonte">
                <Input id="fonte" {...registerEdit('fonte')} />
              </FieldWrapper>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <FieldWrapper label="Dificuldade" htmlFor="dificuldade">
                <Select id="dificuldade" {...registerEdit('dificuldade')}>
                  {DIFICULDADE_OPCOES.map((d) => (
                    <option key={d} value={d}>
                      {rotuloDificuldade(d)}
                    </option>
                  ))}
                </Select>
              </FieldWrapper>

              <FieldWrapper label="Tipo" htmlFor="tipo">
                <Select id="tipo" {...registerEdit('tipo')}>
                  {TIPO_QUESTAO_OPCOES.map((t) => (
                    <option key={t} value={t}>
                      {rotuloTipoQuestao(t)}
                    </option>
                  ))}
                </Select>
              </FieldWrapper>
            </div>

            <FieldWrapper label="Disciplina" htmlFor="disciplinaId" error={errorsEdit.disciplinaId?.message}>
              <Select id="disciplinaId" disabled={carregandoDisciplinas} {...registerEdit('disciplinaId')}>
                <option value="">Selecione...</option>
                {disciplinas?.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.nome}
                  </option>
                ))}
              </Select>
            </FieldWrapper>

            <FieldWrapper label="Assunto" htmlFor="assuntoId" error={errorsEdit.assuntoId?.message}>
              <Select id="assuntoId" disabled={!disciplinaIdEdit} {...registerEdit('assuntoId')}>
                <option value="">Selecione...</option>
                {assuntosDisciplinaEdit?.map((a) => (
                  <option key={a.id} value={a.id}>
                    {a.nome}
                  </option>
                ))}
              </Select>
            </FieldWrapper>

            <FieldWrapper label="Concurso (opcional)" htmlFor="concursoId">
              <Select id="concursoId" {...registerEdit('concursoId')}>
                <option value="">Nenhum específico</option>
                {concursos?.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.nome}
                  </option>
                ))}
              </Select>
            </FieldWrapper>

            {tipoEdit !== 'DISCURSIVA' && (
              <div className="flex flex-col gap-2">
                <p className="text-sm font-medium text-slate-700">Alternativas</p>
                {fields.map((field, index) => (
                  <div key={field.id} className="flex items-center gap-2">
                    <input
                      type="radio"
                      name="correta"
                      checked={corretaIndex === index}
                      onChange={() => setCorretaIndex(index)}
                      aria-label={`Marcar alternativa ${index + 1} como correta`}
                    />
                    <Select className="w-20" {...registerEdit(`alternativas.${index}.letra` as const)}>
                      {LETRAS.map((letra) => (
                        <option key={letra} value={letra}>
                          {letra}
                        </option>
                      ))}
                    </Select>
                    <Input
                      className="flex-1"
                      {...registerEdit(`alternativas.${index}.descricao` as const)}
                    />
                    <Button
                      type="button"
                      variant="ghost"
                      onClick={() => {
                        remove(index)
                        if (corretaIndex === index) setCorretaIndex(null)
                        else if (corretaIndex !== null && corretaIndex > index) setCorretaIndex(corretaIndex - 1)
                      }}
                    >
                      ✕
                    </Button>
                  </div>
                ))}
                {errorsEdit.alternativas && (
                  <p className="text-sm text-red-600">Revise as alternativas.</p>
                )}
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() =>
                    append({ letra: LETRAS[(alternativasEdit?.length ?? 0) % LETRAS.length], descricao: '' })
                  }
                >
                  Adicionar alternativa
                </Button>
              </div>
            )}

            <Button
              type="submit"
              isLoading={atualizarMutation.isPending}
              className="w-full"
            >
              Salvar
            </Button>
          </form>
        </Modal>
      )}

      {modalGerarIaAberto && (
        <Modal title="Gerar questões via IA" onClose={() => setModalGerarIaAberto(false)}>
          <form onSubmit={handleSubmitGerarIa(onSubmitGerarIa)} className="flex flex-col gap-4">
            {erroGerarIa && <Alert tone="error">{erroGerarIa}</Alert>}
            {resultadoGerarIa && <Alert tone="success">{resultadoGerarIa}</Alert>}

            <FieldWrapper label="Disciplina" htmlFor="ia-disciplinaId" error={errorsGerarIa.disciplinaId?.message}>
              <Select id="ia-disciplinaId" disabled={carregandoDisciplinas} {...registerGerarIa('disciplinaId')}>
                <option value="">Selecione...</option>
                {disciplinas?.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.nome}
                  </option>
                ))}
              </Select>
            </FieldWrapper>

            <FieldWrapper label="Assunto" htmlFor="ia-assuntoId" error={errorsGerarIa.assuntoId?.message}>
              <Select
                id="ia-assuntoId"
                disabled={!disciplinaIdGerarIa || carregandoAssuntosGerarIa}
                {...registerGerarIa('assuntoId')}
              >
                <option value="">Selecione...</option>
                {assuntosDisciplinaGerarIa?.map((a) => (
                  <option key={a.id} value={a.id}>
                    {a.nome}
                  </option>
                ))}
              </Select>
            </FieldWrapper>

            <FieldWrapper label="Concurso (opcional, contexto do edital)" htmlFor="ia-concursoId">
              <Select id="ia-concursoId" {...registerGerarIa('concursoId')}>
                <option value="">Nenhum específico</option>
                {concursos?.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.nome}
                  </option>
                ))}
              </Select>
            </FieldWrapper>

            <div className="grid grid-cols-2 gap-4">
              <FieldWrapper label="Dificuldade" htmlFor="ia-dificuldade">
                <Select id="ia-dificuldade" {...registerGerarIa('dificuldade')}>
                  {DIFICULDADE_OPCOES.map((d) => (
                    <option key={d} value={d}>
                      {rotuloDificuldade(d)}
                    </option>
                  ))}
                </Select>
              </FieldWrapper>

              <FieldWrapper label="Tipo" htmlFor="ia-tipo">
                <Select id="ia-tipo" {...registerGerarIa('tipo')}>
                  {TIPO_QUESTAO_OPCOES.map((t) => (
                    <option key={t} value={t}>
                      {rotuloTipoQuestao(t)}
                    </option>
                  ))}
                </Select>
              </FieldWrapper>
            </div>

            <FieldWrapper label="Quantidade" htmlFor="ia-quantidade" error={errorsGerarIa.quantidade?.message}>
              <Input id="ia-quantidade" type="number" min={1} {...registerGerarIa('quantidade')} />
            </FieldWrapper>

            <Button type="submit" isLoading={gerarIaMutation.isPending} className="w-full">
              Gerar
            </Button>
          </form>
        </Modal>
      )}
    </div>
  )
}
