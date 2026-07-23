import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import * as simuladosApi from '../api/simulados'
import { useAuth } from './useAuth'
import type { GerarSimuladoRequestDTO, RespostaUsuarioRequestDTO } from '../types/api'

export function useMeusSimulados() {
  const { claims } = useAuth()

  return useQuery({
    queryKey: ['simulados', 'usuario', claims?.usuarioId],
    queryFn: () => simuladosApi.listarPorUsuario(claims!.usuarioId),
    enabled: claims !== null,
  })
}

export function useSimulado(id: number) {
  return useQuery({
    queryKey: ['simulados', id],
    queryFn: () => simuladosApi.buscarPorId(id),
    enabled: Number.isFinite(id),
  })
}

export function useRevisaoSimulado(id: number) {
  return useQuery({
    queryKey: ['simulados', id, 'revisao'],
    queryFn: () => simuladosApi.revisar(id),
    enabled: Number.isFinite(id),
  })
}

export function useGerarSimulado() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: GerarSimuladoRequestDTO) => simuladosApi.gerar(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['simulados', 'usuario'] })
    },
  })
}

export function useIniciarSimulado(id: number) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: () => simuladosApi.iniciar(id),
    onSuccess: (data) => {
      queryClient.setQueryData(['simulados', id], data)
    },
  })
}

export function useResponderQuestao(simuladoId: number) {
  return useMutation({
    mutationFn: (request: RespostaUsuarioRequestDTO) =>
      simuladosApi.responderQuestao(simuladoId, request),
  })
}

export function useFinalizarSimulado(id: number) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: () => simuladosApi.finalizar(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['simulados', id] })
      queryClient.invalidateQueries({ queryKey: ['simulados', 'usuario'] })
    },
  })
}

export function useCancelarSimulado(id: number) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: () => simuladosApi.cancelar(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['simulados', id] })
      queryClient.invalidateQueries({ queryKey: ['simulados', 'usuario'] })
    },
  })
}
