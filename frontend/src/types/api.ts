// Espelha 1:1 os DTOs do backend (SimulaI.dto.*). Manter em sincronia manualmente —
// o backend não gera um cliente TS automaticamente nesta fase do projeto.

export type Role = 'ALUNO' | 'ADMIN'

export type Dificuldade = 'FACIL' | 'MEDIA' | 'DIFICIL'

export type TipoQuestao = 'MULTIPLA_ESCOLHA' | 'CERTO_ERRADO' | 'DISCURSIVA'

export type LetraAlternativa = 'A' | 'B' | 'C' | 'D' | 'E'

export type StatusConcurso =
  | 'PREVISTO'
  | 'AUTORIZADO'
  | 'EDITAL_PUBLICADO'
  | 'INSCRICOES_ABERTAS'
  | 'PROVA_REALIZADA'
  | 'FINALIZADO'

export type StatusSimulado = 'CRIADO' | 'EM_ANDAMENTO' | 'FINALIZADO' | 'CANCELADO'

export type DiaSemana = 'SEGUNDA' | 'TERCA' | 'QUARTA' | 'QUINTA' | 'SEXTA' | 'SABADO' | 'DOMINGO'

export type TipoCodigoVerificacao = 'CONFIRMACAO_CADASTRO' | 'REDEFINICAO_SENHA'

// ---------- Auth ----------

export interface LoginRequestDTO {
  email: string
  senha: string
}

export interface TokenResponseDTO {
  accessToken: string
  tipo: string
  expiraEmSegundos: number
}

export interface ConfirmarCadastroRequestDTO {
  email: string
  codigo: string
}

export interface EsqueciSenhaRequestDTO {
  email: string
}

export interface RedefinirSenhaRequestDTO {
  email: string
  codigo: string
  novaSenha: string
}

export interface ReenviarCodigoRequestDTO {
  email: string
  tipo: TipoCodigoVerificacao
}

// ---------- Usuario ----------

export interface UsuarioRequestDTO {
  nome: string
  email: string
  senha: string
  telefone: string
}

export interface UsuarioResponseDTO {
  id: number
  nome: string
  email: string
  telefone: string | null
  emailVerificado: boolean
  role: Role
}

export interface AlterarRoleRequestDTO {
  role: Role
}

// ---------- Banca ----------

export interface BancaRequestDTO {
  nome: string
  descricao: string
}

export interface BancaResponseDTO {
  id: number
  nome: string
  descricao: string
}

// ---------- Disciplina ----------

export interface DisciplinaRequestDTO {
  nome: string
  descricao: string
}

export interface DisciplinaResponseDTO {
  id: number
  nome: string
  descricao: string
}

// ---------- Assunto ----------

export interface AssuntoRequestDTO {
  nome: string
  descricao?: string
  disciplinaId: number
}

export interface AssuntoResponseDTO {
  id: number
  nome: string
  descricao: string | null
  disciplina: string
}

// ---------- Concurso ----------

export interface ConcursoRequestDTO {
  nome: string
  orgao: string
  cargo: string
  ano?: number
  bancaId: number
  status: StatusConcurso
  editalUrl?: string
}

export interface ConcursoResponseDTO {
  id: number
  nome: string
  orgao: string
  cargo: string
  ano: number | null
  banca: string
  status: StatusConcurso
  editalUrl: string | null
  editalIndexado: boolean
  disciplinas: string[]
}

// ---------- Questao / Alternativa ----------

export interface AlternativaRequestDTO {
  letra: LetraAlternativa
  descricao: string
  correta: boolean
}

export interface AlternativaResponseDTO {
  id: number
  letra: LetraAlternativa
  descricao: string
}

// Variante de moderação (ADMIN): expõe qual alternativa é a correta. Nunca usada no
// fluxo do aluno — apenas em GET /questoes/moderacao.
export interface AlternativaAdminResponseDTO extends AlternativaResponseDTO {
  correta: boolean
}

export interface QuestaoRequestDTO {
  enunciado: string
  comentario?: string
  explicacao?: string
  ano: number
  fonte?: string
  dificuldade: Dificuldade
  tipo: TipoQuestao
  geradaPorIA?: boolean
  concursoId?: number
  disciplinaId: number
  assuntoId: number
  usuarioId?: number
  alternativas: AlternativaRequestDTO[]
}

export interface QuestaoResponseDTO {
  id: number
  enunciado: string
  comentario: string | null
  explicacao: string | null
  ano: number
  fonte: string | null
  dificuldade: Dificuldade
  tipo: TipoQuestao
  geradaPorIA: boolean
  concurso: string | null
  disciplina: string
  assunto: string
  alternativas: AlternativaResponseDTO[]
}

// Variante de moderação (ADMIN) de QuestaoResponseDTO — retornada por GET /questoes/moderacao.
export interface QuestaoAdminResponseDTO extends Omit<QuestaoResponseDTO, 'alternativas'> {
  alternativas: AlternativaAdminResponseDTO[]
}

export interface GerarQuestaoIARequestDTO {
  disciplinaId: number
  assuntoId: number
  concursoId?: number
  dificuldade: Dificuldade
  tipo: TipoQuestao
  quantidade: number
}

// ---------- Simulado ----------

export interface GerarSimuladoRequestDTO {
  concursoId?: number
  quantidadeQuestoes: number
  dificuldade?: Dificuldade
  tempoLimiteMinutos?: number
}

export interface SimuladoResponseDTO {
  id: number
  dataCriacao: string
  inicio: string | null
  fim: string | null
  quantidadeQuestoes: number
  tempoLimiteMinutos: number
  status: StatusSimulado
  usuario: string
  concurso: string | null
  questoes: QuestaoResponseDTO[]
}

export interface RespostaUsuarioRequestDTO {
  questaoId: number
  alternativaMarcadaId: number
  tempoRespostaSegundos?: number
}

export interface RespostaUsuarioResponseDTO {
  id: number
  questaoId: number
  alternativaMarcada: LetraAlternativa
  acertou: boolean
  tempoRespostaSegundos: number
}

export interface SimuladoResultadoDTO {
  simuladoId: number
  totalQuestoes: number
  acertos: number
  erros: number
  percentualAcerto: number
  tempoTotalSegundos: number
}

export interface RevisaoAlternativaDTO {
  id: number
  letra: LetraAlternativa
  descricao: string
  correta: boolean
  marcadaPeloUsuario: boolean
}

export interface RevisaoQuestaoDTO {
  questaoId: number
  enunciado: string
  explicacao: string | null
  respondida: boolean
  acertou: boolean
  alternativas: RevisaoAlternativaDTO[]
}

export interface SimuladoRevisaoDTO {
  simuladoId: number
  questoes: RevisaoQuestaoDTO[]
}

// ---------- Estatísticas ----------

export interface EstatisticaAssuntoDTO {
  assunto: string
  totalRespondidas: number
  acertos: number
  percentual: number
}

export interface EstatisticaDisciplinaDTO {
  disciplina: string
  totalRespondidas: number
  acertos: number
  percentual: number
  porAssunto: EstatisticaAssuntoDTO[]
}

export interface EstatisticaEvolucaoDTO {
  simuladoId: number
  data: string
  percentual: number
}

export interface EstatisticaResponseDTO {
  totalRespondidas: number
  totalAcertos: number
  percentualGeral: number
  totalSimuladosFinalizados: number
  tempoTotalSegundos: number
  porDisciplina: EstatisticaDisciplinaDTO[]
  evolucao: EstatisticaEvolucaoDTO[]
}

// ---------- Cronograma ----------

export interface GerarCronogramaRequestDTO {
  diasPorSemana: number
  horasPorDia: number
}

export interface ItemCronogramaResponseDTO {
  id: number
  diaSemana: DiaSemana
  disciplina: string
  assunto: string
  duracaoMinutos: number
  foco: string
  justificativa: string | null
}

export interface CronogramaResponseDTO {
  id: number
  geradoEm: string
  diasPorSemana: number
  horasPorDia: number
  observacaoGeral: string | null
  itens: ItemCronogramaResponseDTO[]
}

// ---------- Erros ----------

export interface ErroResponseDTO {
  timestamp: string
  status: number
  erro: string
  mensagem: string
  path: string
}

export interface ErroValidacaoDTO {
  erro: ErroResponseDTO
  camposInvalidos: { campo: string; mensagem: string }[]
}
