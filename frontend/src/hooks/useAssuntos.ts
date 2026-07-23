import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import * as assuntosApi from '../api/assuntos'
import type { AssuntoRequestDTO } from '../types/api'

export function useAssuntos() {
  return useQuery({ queryKey: ['assuntos'], queryFn: assuntosApi.listarTodos })
}

export function useAssuntosPorDisciplina(disciplinaId: number | undefined) {
  return useQuery({
    queryKey: ['assuntos', 'disciplina', disciplinaId],
    queryFn: () => assuntosApi.listarPorDisciplina(disciplinaId!),
    enabled: disciplinaId !== undefined,
  })
}

function useInvalidarAssuntos() {
  const queryClient = useQueryClient()
  return () => queryClient.invalidateQueries({ queryKey: ['assuntos'] })
}

export function useCadastrarAssunto() {
  const invalidar = useInvalidarAssuntos()
  return useMutation({
    mutationFn: (request: AssuntoRequestDTO) => assuntosApi.cadastrar(request),
    onSuccess: invalidar,
  })
}

export function useAtualizarAssunto() {
  const invalidar = useInvalidarAssuntos()
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: AssuntoRequestDTO }) =>
      assuntosApi.atualizar(id, request),
    onSuccess: invalidar,
  })
}

export function useDeletarAssunto() {
  const invalidar = useInvalidarAssuntos()
  return useMutation({
    mutationFn: (id: number) => assuntosApi.deletar(id),
    onSuccess: invalidar,
  })
}
