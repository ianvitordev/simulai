import type { Dificuldade, StatusConcurso, StatusSimulado, TipoQuestao } from '../types/api'

export function formatarData(iso: string | null): string {
  if (!iso) return '—'
  return new Date(iso).toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function tonePorStatus(status: StatusSimulado) {
  switch (status) {
    case 'CRIADO':
      return 'slate' as const
    case 'EM_ANDAMENTO':
      return 'blue' as const
    case 'FINALIZADO':
      return 'green' as const
    case 'CANCELADO':
      return 'red' as const
  }
}

export function rotuloStatus(status: StatusSimulado): string {
  switch (status) {
    case 'CRIADO':
      return 'Não iniciado'
    case 'EM_ANDAMENTO':
      return 'Em andamento'
    case 'FINALIZADO':
      return 'Finalizado'
    case 'CANCELADO':
      return 'Cancelado'
  }
}

export function rotuloStatusConcurso(status: StatusConcurso): string {
  switch (status) {
    case 'PREVISTO':
      return 'Previsto'
    case 'AUTORIZADO':
      return 'Autorizado'
    case 'EDITAL_PUBLICADO':
      return 'Edital publicado'
    case 'INSCRICOES_ABERTAS':
      return 'Inscrições abertas'
    case 'PROVA_REALIZADA':
      return 'Prova realizada'
    case 'FINALIZADO':
      return 'Finalizado'
  }
}

export const STATUS_CONCURSO_OPCOES: StatusConcurso[] = [
  'PREVISTO',
  'AUTORIZADO',
  'EDITAL_PUBLICADO',
  'INSCRICOES_ABERTAS',
  'PROVA_REALIZADA',
  'FINALIZADO',
]

export function rotuloDificuldade(dificuldade: Dificuldade): string {
  switch (dificuldade) {
    case 'FACIL':
      return 'Fácil'
    case 'MEDIA':
      return 'Média'
    case 'DIFICIL':
      return 'Difícil'
  }
}

export const DIFICULDADE_OPCOES: Dificuldade[] = ['FACIL', 'MEDIA', 'DIFICIL']

export function rotuloTipoQuestao(tipo: TipoQuestao): string {
  switch (tipo) {
    case 'MULTIPLA_ESCOLHA':
      return 'Múltipla escolha'
    case 'CERTO_ERRADO':
      return 'Certo/Errado'
    case 'DISCURSIVA':
      return 'Discursiva'
  }
}

export const TIPO_QUESTAO_OPCOES: TipoQuestao[] = ['MULTIPLA_ESCOLHA', 'CERTO_ERRADO', 'DISCURSIVA']
