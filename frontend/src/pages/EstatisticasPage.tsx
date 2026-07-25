import { useState } from 'react'
import {
  Bar,
  BarChart,
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { Alert } from '../components/ui/Alert'
import { Card } from '../components/ui/Card'
import { Spinner } from '../components/ui/Spinner'
import { useEstatisticas } from '../hooks/useEstatisticas'
import { formatarData, formatarDuracaoSegundos } from '../lib/format'
import type { EstatisticaDisciplinaDTO } from '../types/api'

const COR_GRAFICO = '#a97f1f'

export function EstatisticasPage() {
  const { data: estatisticas, isLoading, isError } = useEstatisticas()

  if (isLoading) return <Spinner />
  if (isError) return <Alert tone="error">Não foi possível carregar suas estatísticas.</Alert>
  if (!estatisticas) return null

  if (estatisticas.totalRespondidas === 0) {
    return (
      <div className="flex flex-col gap-6">
        <h1 className="text-2xl font-semibold text-slate-900">Estatísticas</h1>
        <Alert tone="info">
          Você ainda não respondeu nenhuma questão. Faça um simulado para começar a ver suas
          estatísticas de desempenho aqui.
        </Alert>
      </div>
    )
  }

  const dadosPorDisciplina = estatisticas.porDisciplina.map((disciplina) => ({
    disciplina: disciplina.disciplina,
    percentual: Number(disciplina.percentual.toFixed(1)),
  }))

  const dadosEvolucao = estatisticas.evolucao.map((ponto) => ({
    data: formatarData(ponto.data),
    percentual: Number(ponto.percentual.toFixed(1)),
  }))

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-semibold text-slate-900">Estatísticas</h1>

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
        <Tile label="Aproveitamento geral" valor={`${estatisticas.percentualGeral.toFixed(0)}%`} />
        <Tile label="Questões respondidas" valor={String(estatisticas.totalRespondidas)} />
        <Tile label="Simulados finalizados" valor={String(estatisticas.totalSimuladosFinalizados)} />
        <Tile label="Tempo total estudado" valor={formatarDuracaoSegundos(estatisticas.tempoTotalSegundos)} />
      </div>

      <Card>
        <h2 className="mb-4 text-lg font-medium text-slate-900">Percentual de acerto por disciplina</h2>
        <div className="h-72 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={dadosPorDisciplina}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
              <XAxis dataKey="disciplina" tick={{ fontSize: 12 }} interval={0} angle={-15} textAnchor="end" height={60} />
              <YAxis domain={[0, 100]} tickFormatter={(v: number) => `${v}%`} width={45} />
              <Tooltip formatter={(valor) => [`${valor}%`, 'Acerto']} />
              <Bar dataKey="percentual" fill={COR_GRAFICO} radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </Card>

      {dadosEvolucao.length > 1 && (
        <Card>
          <h2 className="mb-4 text-lg font-medium text-slate-900">Evolução por simulado</h2>
          <div className="h-72 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={dadosEvolucao}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                <XAxis dataKey="data" tick={{ fontSize: 12 }} />
                <YAxis domain={[0, 100]} tickFormatter={(v: number) => `${v}%`} width={45} />
                <Tooltip formatter={(valor) => [`${valor}%`, 'Acerto']} />
                <Line type="monotone" dataKey="percentual" stroke={COR_GRAFICO} strokeWidth={2} dot={{ r: 4 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </Card>
      )}

      <Card>
        <h2 className="mb-4 text-lg font-medium text-slate-900">Detalhamento por assunto</h2>
        <div className="flex flex-col gap-4">
          {estatisticas.porDisciplina.map((disciplina) => (
            <DetalheDisciplina key={disciplina.disciplina} disciplina={disciplina} />
          ))}
        </div>
      </Card>
    </div>
  )
}

function Tile({ label, valor }: { label: string; valor: string }) {
  return (
    <Card className="text-center">
      <p className="text-2xl font-semibold text-slate-900">{valor}</p>
      <p className="mt-1 text-xs text-slate-500">{label}</p>
    </Card>
  )
}

function DetalheDisciplina({ disciplina }: { disciplina: EstatisticaDisciplinaDTO }) {
  const [aberta, setAberta] = useState(false)

  return (
    <div className="rounded-lg border border-slate-200">
      <button
        type="button"
        onClick={() => setAberta((atual) => !atual)}
        className="flex w-full items-center justify-between px-4 py-3 text-left text-sm font-medium text-slate-900"
      >
        <span>{disciplina.disciplina}</span>
        <span className="text-slate-500">
          {disciplina.percentual.toFixed(0)}% ({disciplina.acertos}/{disciplina.totalRespondidas})
        </span>
      </button>

      {aberta && (
        <div className="overflow-x-auto border-t border-slate-200 px-4 py-3">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="text-slate-500">
                <th className="pb-2 font-medium">Assunto</th>
                <th className="pb-2 font-medium">Respondidas</th>
                <th className="pb-2 font-medium">Acertos</th>
                <th className="pb-2 font-medium">Percentual</th>
              </tr>
            </thead>
            <tbody>
              {disciplina.porAssunto.map((assunto) => (
                <tr key={assunto.assunto} className="border-t border-slate-100 text-slate-700">
                  <td className="py-2">{assunto.assunto}</td>
                  <td className="py-2">{assunto.totalRespondidas}</td>
                  <td className="py-2">{assunto.acertos}</td>
                  <td className="py-2">{assunto.percentual.toFixed(0)}%</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
