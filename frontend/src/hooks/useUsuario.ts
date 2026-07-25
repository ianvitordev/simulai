import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import * as usuariosApi from '../api/usuarios'
import type { AlterarRoleRequestDTO } from '../types/api'
import { useAuth } from './useAuth'

/** Dados do próprio usuário logado (nome não vem no JWT, só email/id/role). */
export function useUsuarioLogado() {
  const { claims } = useAuth()

  return useQuery({
    queryKey: ['usuarios', claims?.usuarioId],
    queryFn: () => usuariosApi.buscarPorId(claims!.usuarioId),
    enabled: claims !== null,
  })
}

export function useUsuarios() {
  return useQuery({ queryKey: ['usuarios', 'todos'], queryFn: usuariosApi.listarTodos })
}

export function useAlterarRole() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: AlterarRoleRequestDTO }) =>
      usuariosApi.alterarRole(id, request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['usuarios'] }),
  })
}

export function useDeletarUsuario() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => usuariosApi.deletar(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['usuarios'] }),
  })
}
