import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import * as bancasApi from '../api/bancas'
import type { BancaRequestDTO } from '../types/api'

export function useBancas() {
  return useQuery({ queryKey: ['bancas'], queryFn: bancasApi.listarTodas })
}

function useInvalidarBancas() {
  const queryClient = useQueryClient()
  return () => queryClient.invalidateQueries({ queryKey: ['bancas'] })
}

export function useCadastrarBanca() {
  const invalidar = useInvalidarBancas()
  return useMutation({
    mutationFn: (request: BancaRequestDTO) => bancasApi.cadastrar(request),
    onSuccess: invalidar,
  })
}

export function useAtualizarBanca() {
  const invalidar = useInvalidarBancas()
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: BancaRequestDTO }) =>
      bancasApi.atualizar(id, request),
    onSuccess: invalidar,
  })
}

export function useDeletarBanca() {
  const invalidar = useInvalidarBancas()
  return useMutation({
    mutationFn: (id: number) => bancasApi.deletar(id),
    onSuccess: invalidar,
  })
}
