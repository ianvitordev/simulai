import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Alert } from '../components/ui/Alert'
import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'
import { Spinner } from '../components/ui/Spinner'
import { useProvasDisponiveis } from '../hooks/useConcursos'
import { useGerarProva } from '../hooks/useSimulados'
import { extrairMensagemErro } from '../lib/apiClient'

export function ProvasPage() {
  const { data: provas, isLoading, isError } = useProvasDisponiveis()
  const gerarProvaMutation = useGerarProva()
  const navigate = useNavigate()
  const [erro, setErro] = useState<string | null>(null)
  const [concursoEmAndamento, setConcursoEmAndamento] = useState<number | null>(null)

  async function handleFazerProva(concursoId: number) {
    setErro(null)
    setConcursoEmAndamento(concursoId)
    try {
      const simulado = await gerarProvaMutation.mutateAsync({ concursoId })
      navigate(`/simulados/${simulado.id}`)
    } catch (error) {
      setErro(extrairMensagemErro(error))
    } finally {
      setConcursoEmAndamento(null)
    }
  }

  if (isLoading) return <Spinner />
  if (isError) return <Alert tone="error">Não foi possível carregar as provas disponíveis.</Alert>

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">Provas</h1>
        <p className="mt-1 text-sm text-slate-500">
          Provas reais já aplicadas, com as questões originais na ordem em que caíram de verdade.
        </p>
      </div>

      {erro && <Alert tone="error">{erro}</Alert>}

      {!provas || provas.length === 0 ? (
        <Card className="text-center text-slate-500">Nenhuma prova real disponível no momento.</Card>
      ) : (
        <div className="flex flex-col gap-3">
          {provas.map((concurso) => (
            <Card key={concurso.id} className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <p className="font-medium text-slate-900">{concurso.nome}</p>
                <p className="mt-1 text-sm text-slate-500">
                  {concurso.orgao} · {concurso.cargo} · Banca {concurso.banca}
                </p>
              </div>

              <Button
                onClick={() => handleFazerProva(concurso.id)}
                isLoading={concursoEmAndamento === concurso.id && gerarProvaMutation.isPending}
              >
                Fazer prova
              </Button>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
