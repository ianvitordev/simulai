import { SimpleCatalogPage } from '../../components/admin/SimpleCatalogPage'
import {
  useAtualizarDisciplina,
  useCadastrarDisciplina,
  useDeletarDisciplina,
  useDisciplinas,
} from '../../hooks/useDisciplinas'

export function DisciplinasPage() {
  const { data: disciplinas, isLoading } = useDisciplinas()
  const cadastrarMutation = useCadastrarDisciplina()
  const atualizarMutation = useAtualizarDisciplina()
  const deletarMutation = useDeletarDisciplina()

  return (
    <SimpleCatalogPage
      titulo="Disciplinas"
      itens={disciplinas}
      carregando={isLoading}
      criando={cadastrarMutation.isPending}
      atualizando={atualizarMutation.isPending}
      removendo={deletarMutation.isPending}
      aoCriar={(data) => cadastrarMutation.mutateAsync(data)}
      aoAtualizar={(id, data) => atualizarMutation.mutateAsync({ id, request: data })}
      aoRemover={(id) => deletarMutation.mutateAsync(id)}
    />
  )
}
