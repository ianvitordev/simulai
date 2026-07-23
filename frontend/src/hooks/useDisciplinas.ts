import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import * as disciplinasApi from '../api/disciplinas'
import type { DisciplinaRequestDTO } from '../types/api'

export function useDisciplinas() {
  return useQuery({ queryKey: ['disciplinas'], queryFn: disciplinasApi.listarTodas })
}

function useInvalidarDisciplinas() {
  const queryClient = useQueryClient()
  return () => queryClient.invalidateQueries({ queryKey: ['disciplinas'] })
}

export function useCadastrarDisciplina() {
  const invalidar = useInvalidarDisciplinas()
  return useMutation({
    mutationFn: (request: DisciplinaRequestDTO) => disciplinasApi.cadastrar(request),
    onSuccess: invalidar,
  })
}

export function useAtualizarDisciplina() {
  const invalidar = useInvalidarDisciplinas()
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: DisciplinaRequestDTO }) =>
      disciplinasApi.atualizar(id, request),
    onSuccess: invalidar,
  })
}

export function useDeletarDisciplina() {
  const invalidar = useInvalidarDisciplinas()
  return useMutation({
    mutationFn: (id: number) => disciplinasApi.deletar(id),
    onSuccess: invalidar,
  })
}
