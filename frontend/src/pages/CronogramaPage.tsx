import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { Alert } from '../components/ui/Alert'
import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'
import { FieldWrapper, Input } from '../components/ui/Field'
import { Spinner } from '../components/ui/Spinner'
import { useCronogramaAtual, useGerarCronograma } from '../hooks/useCronograma'
import { extrairMensagemErro } from '../lib/apiClient'
import { DIA_SEMANA_ORDEM, rotuloDiaSemana } from '../lib/format'
import type { CronogramaResponseDTO, ItemCronogramaResponseDTO } from '../types/api'

const schema = z.object({
  diasPorSemana: z.coerce.number().int().min(1, 'Mínimo de 1 dia').max(7, 'Máximo de 7 dias'),
  horasPorDia: z.coerce.number().int().min(1, 'Mínimo de 1 hora').max(8, 'Máximo de 8 horas'),
})

type FormInput = z.input<typeof schema>
type FormOutput = z.output<typeof schema>

export function CronogramaPage() {
  const { data: cronograma, isLoading, isError } = useCronogramaAtual()
  const [mostrarFormulario, setMostrarFormulario] = useState(false)

  if (isLoading) return <Spinner />
  if (isError) return <Alert tone="error">Não foi possível carregar seu cronograma.</Alert>

  const exibirFormulario = mostrarFormulario || !cronograma

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold text-slate-900">Cronograma de estudos</h1>
        {cronograma && !exibirFormulario && (
          <Button variant="secondary" onClick={() => setMostrarFormulario(true)}>
            Gerar novo
          </Button>
        )}
      </div>

      {exibirFormulario ? (
        <FormularioCronograma
          temCronogramaAtual={Boolean(cronograma)}
          onGerado={() => setMostrarFormulario(false)}
          onCancelar={cronograma ? () => setMostrarFormulario(false) : undefined}
        />
      ) : (
        cronograma && <VisualizacaoCronograma cronograma={cronograma} />
      )}
    </div>
  )
}

function FormularioCronograma({
  temCronogramaAtual,
  onGerado,
  onCancelar,
}: {
  temCronogramaAtual: boolean
  onGerado: () => void
  onCancelar?: () => void
}) {
  const gerarMutation = useGerarCronograma()
  const [erro, setErro] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormInput, unknown, FormOutput>({
    resolver: zodResolver(schema),
    defaultValues: { diasPorSemana: 5, horasPorDia: 2 },
  })

  async function onSubmit(data: FormOutput) {
    setErro(null)
    try {
      await gerarMutation.mutateAsync(data)
      onGerado()
    } catch (error) {
      setErro(extrairMensagemErro(error))
    }
  }

  return (
    <Card>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
        {!temCronogramaAtual && (
          <p className="text-sm text-slate-500">
            Diga quanto tempo você tem disponível e a IA monta um cronograma priorizando as
            disciplinas/assuntos onde seu desempenho está mais fraco.
          </p>
        )}
        {erro && <Alert tone="error">{erro}</Alert>}

        <FieldWrapper label="Dias por semana disponíveis" htmlFor="diasPorSemana" error={errors.diasPorSemana?.message}>
          <Input id="diasPorSemana" type="number" min={1} max={7} {...register('diasPorSemana')} />
        </FieldWrapper>

        <FieldWrapper label="Horas de estudo por dia" htmlFor="horasPorDia" error={errors.horasPorDia?.message}>
          <Input id="horasPorDia" type="number" min={1} max={8} {...register('horasPorDia')} />
        </FieldWrapper>

        <div className="mt-2 flex gap-2">
          {onCancelar && (
            <Button type="button" variant="secondary" onClick={onCancelar} className="flex-1">
              Cancelar
            </Button>
          )}
          <Button type="submit" isLoading={isSubmitting} className="flex-1">
            {temCronogramaAtual ? 'Gerar novo cronograma' : 'Gerar cronograma'}
          </Button>
        </div>
      </form>
    </Card>
  )
}

function VisualizacaoCronograma({ cronograma }: { cronograma: CronogramaResponseDTO }) {
  const itensPorDia = new Map<string, ItemCronogramaResponseDTO[]>()
  cronograma.itens.forEach((item) => {
    const lista = itensPorDia.get(item.diaSemana) ?? []
    lista.push(item)
    itensPorDia.set(item.diaSemana, lista)
  })

  return (
    <div className="flex flex-col gap-4">
      {cronograma.observacaoGeral && <Alert tone="info">{cronograma.observacaoGeral}</Alert>}

      {DIA_SEMANA_ORDEM.filter((dia) => itensPorDia.has(dia)).map((dia) => (
        <Card key={dia}>
          <h2 className="mb-3 font-medium text-slate-900">{rotuloDiaSemana(dia)}</h2>
          <div className="flex flex-col gap-3">
            {itensPorDia.get(dia)!.map((item) => (
              <div key={item.id} className="rounded-lg border border-slate-200 px-4 py-3">
                <div className="flex items-center justify-between">
                  <span className="font-medium text-slate-900">
                    {item.disciplina} — {item.assunto}
                  </span>
                  <span className="text-sm text-slate-500">{item.duracaoMinutos}min</span>
                </div>
                <p className="mt-1 text-sm text-slate-600">{item.foco}</p>
                {item.justificativa && (
                  <p className="mt-1 text-xs text-slate-400">{item.justificativa}</p>
                )}
              </div>
            ))}
          </div>
        </Card>
      ))}
    </div>
  )
}
