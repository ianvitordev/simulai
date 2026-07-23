import { SimpleCatalogPage } from '../../components/admin/SimpleCatalogPage'
import { useAtualizarBanca, useBancas, useCadastrarBanca, useDeletarBanca } from '../../hooks/useBancas'

export function BancasPage() {
  const { data: bancas, isLoading } = useBancas()
  const cadastrarMutation = useCadastrarBanca()
  const atualizarMutation = useAtualizarBanca()
  const deletarMutation = useDeletarBanca()

  return (
    <SimpleCatalogPage
      titulo="Bancas"
      itens={bancas}
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
