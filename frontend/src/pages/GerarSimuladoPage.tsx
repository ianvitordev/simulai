import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { Alert } from '../components/ui/Alert'
import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'
import { FieldWrapper, Input, Select } from '../components/ui/Field'
import { useConcursos } from '../hooks/useConcursos'
import { useGerarSimulado } from '../hooks/useSimulados'
import { extrairMensagemErro } from '../lib/apiClient'

const schema = z.object({
  concursoId: z.string(), // '' = nenhum concurso específico
  quantidadeQuestoes: z.coerce.number().int().positive('Informe um número maior que zero'),
  dificuldade: z.enum(['', 'FACIL', 'MEDIA', 'DIFICIL']),
  tempoLimiteMinutos: z.coerce.number().int().nonnegative().optional(),
})

// z.coerce.number() tem um tipo de entrada (string vinda do form) diferente do de
// saída (number, após a coerção) — o react-hook-form precisa dos dois separadamente.
type FormInput = z.input<typeof schema>
type FormOutput = z.output<typeof schema>

export function GerarSimuladoPage() {
  const navigate = useNavigate()
  const { data: concursos, isLoading: carregandoConcursos } = useConcursos()
  const gerarMutation = useGerarSimulado()
  const [erro, setErro] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormInput, unknown, FormOutput>({
    resolver: zodResolver(schema),
    defaultValues: { concursoId: '', quantidadeQuestoes: 10, dificuldade: '', tempoLimiteMinutos: 30 },
  })

  async function onSubmit(data: FormOutput) {
    setErro(null)
    try {
      const simulado = await gerarMutation.mutateAsync({
        concursoId: data.concursoId ? Number(data.concursoId) : undefined,
        quantidadeQuestoes: data.quantidadeQuestoes,
        dificuldade: data.dificuldade || undefined,
        tempoLimiteMinutos: data.tempoLimiteMinutos,
      })
      navigate(`/simulados/${simulado.id}`)
    } catch (error) {
      setErro(extrairMensagemErro(error))
    }
  }

  return (
    <div className="mx-auto max-w-lg">
      <h1 className="mb-6 text-2xl font-semibold text-slate-900">Gerar novo simulado</h1>

      <Card>
        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
          {erro && <Alert tone="error">{erro}</Alert>}

          <FieldWrapper label="Concurso (opcional)" htmlFor="concursoId">
            <Select id="concursoId" disabled={carregandoConcursos} {...register('concursoId')}>
              <option value="">Nenhum específico — sorteia de todo o banco</option>
              {concursos?.map((concurso) => (
                <option key={concurso.id} value={concurso.id}>
                  {concurso.nome} ({concurso.orgao})
                </option>
              ))}
            </Select>
          </FieldWrapper>

          <FieldWrapper
            label="Quantidade de questões"
            htmlFor="quantidadeQuestoes"
            error={errors.quantidadeQuestoes?.message}
          >
            <Input
              id="quantidadeQuestoes"
              type="number"
              min={1}
              {...register('quantidadeQuestoes')}
            />
          </FieldWrapper>

          <FieldWrapper label="Dificuldade (opcional)" htmlFor="dificuldade">
            <Select id="dificuldade" {...register('dificuldade')}>
              <option value="">Qualquer dificuldade</option>
              <option value="FACIL">Fácil</option>
              <option value="MEDIA">Média</option>
              <option value="DIFICIL">Difícil</option>
            </Select>
          </FieldWrapper>

          <FieldWrapper
            label="Tempo limite em minutos (opcional, 0 = sem limite)"
            htmlFor="tempoLimiteMinutos"
            error={errors.tempoLimiteMinutos?.message}
          >
            <Input id="tempoLimiteMinutos" type="number" min={0} {...register('tempoLimiteMinutos')} />
          </FieldWrapper>

          <Button type="submit" isLoading={isSubmitting} className="mt-2 w-full">
            Gerar simulado
          </Button>
        </form>
      </Card>
    </div>
  )
}
