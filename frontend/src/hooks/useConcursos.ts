import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import * as concursosApi from '../api/concursos'
import type { ConcursoRequestDTO } from '../types/api'

export function useConcursos() {
  return useQuery({
    queryKey: ['concursos'],
    queryFn: concursosApi.listarTodos,
  })
}

function useInvalidarConcursos() {
  const queryClient = useQueryClient()
  return () => queryClient.invalidateQueries({ queryKey: ['concursos'] })
}

export function useCadastrarConcurso() {
  const invalidar = useInvalidarConcursos()
  return useMutation({
    mutationFn: (request: ConcursoRequestDTO) => concursosApi.cadastrar(request),
    onSuccess: invalidar,
  })
}

export function useAtualizarConcurso() {
  const invalidar = useInvalidarConcursos()
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: ConcursoRequestDTO }) =>
      concursosApi.atualizar(id, request),
    onSuccess: invalidar,
  })
}

export function useDeletarConcurso() {
  const invalidar = useInvalidarConcursos()
  return useMutation({
    mutationFn: (id: number) => concursosApi.deletar(id),
    onSuccess: invalidar,
  })
}

export function useAdicionarDisciplinaAoConcurso() {
  const invalidar = useInvalidarConcursos()
  return useMutation({
    mutationFn: ({ concursoId, disciplinaId }: { concursoId: number; disciplinaId: number }) =>
      concursosApi.adicionarDisciplina(concursoId, disciplinaId),
    onSuccess: invalidar,
  })
}

export function useRemoverDisciplinaDoConcurso() {
  const invalidar = useInvalidarConcursos()
  return useMutation({
    mutationFn: ({ concursoId, disciplinaId }: { concursoId: number; disciplinaId: number }) =>
      concursosApi.removerDisciplina(concursoId, disciplinaId),
    onSuccess: invalidar,
  })
}

export function useIndexarEdital() {
  const invalidar = useInvalidarConcursos()
  return useMutation({
    mutationFn: (id: number) => concursosApi.indexarEdital(id),
    onSuccess: invalidar,
  })
}
