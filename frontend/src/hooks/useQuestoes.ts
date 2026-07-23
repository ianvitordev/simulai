import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import * as questoesApi from '../api/questoes'
import type { GerarQuestaoIARequestDTO, QuestaoRequestDTO } from '../types/api'

export function useQuestoesModeracao() {
  return useQuery({ queryKey: ['questoes', 'moderacao'], queryFn: questoesApi.listarTodasParaModeracao })
}

function useInvalidarQuestoes() {
  const queryClient = useQueryClient()
  return () => queryClient.invalidateQueries({ queryKey: ['questoes'] })
}

export function useAtualizarQuestao() {
  const invalidar = useInvalidarQuestoes()
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: QuestaoRequestDTO }) =>
      questoesApi.atualizar(id, request),
    onSuccess: invalidar,
  })
}

export function useDeletarQuestao() {
  const invalidar = useInvalidarQuestoes()
  return useMutation({
    mutationFn: (id: number) => questoesApi.deletar(id),
    onSuccess: invalidar,
  })
}

export function useGerarQuestoesViaIA() {
  const invalidar = useInvalidarQuestoes()
  return useMutation({
    mutationFn: (request: GerarQuestaoIARequestDTO) => questoesApi.gerarViaIA(request),
    onSuccess: invalidar,
  })
}
