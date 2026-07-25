import { Badge } from '../../components/ui/Badge'
import { Button } from '../../components/ui/Button'
import { Card } from '../../components/ui/Card'
import { Spinner } from '../../components/ui/Spinner'
import { useAuth } from '../../hooks/useAuth'
import { useAlterarRole, useDeletarUsuario, useUsuarios } from '../../hooks/useUsuario'
import { extrairMensagemErro } from '../../lib/apiClient'
import type { Role, UsuarioResponseDTO } from '../../types/api'

export function UsuariosPage() {
  const { claims } = useAuth()
  const { data: usuarios, isLoading } = useUsuarios()
  const alterarRoleMutation = useAlterarRole()
  const deletarMutation = useDeletarUsuario()

  async function handleAlterarRole(usuario: UsuarioResponseDTO, novaRole: Role) {
    if (!confirm(`Alterar a role de "${usuario.nome}" para ${novaRole}?`)) return
    try {
      await alterarRoleMutation.mutateAsync({ id: usuario.id, request: { role: novaRole } })
    } catch (error) {
      alert(extrairMensagemErro(error))
    }
  }

  async function handleRemover(usuario: UsuarioResponseDTO) {
    if (!confirm(`Remover o usuário "${usuario.nome}"? Essa ação não pode ser desfeita.`)) return
    try {
      await deletarMutation.mutateAsync(usuario.id)
    } catch (error) {
      alert(extrairMensagemErro(error))
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <h1 className="text-xl font-semibold text-slate-900">Usuários</h1>

      {isLoading ? (
        <Spinner />
      ) : !usuarios || usuarios.length === 0 ? (
        <Card className="text-center text-slate-500">Nenhum usuário cadastrado ainda.</Card>
      ) : (
        <div className="flex flex-col gap-2">
          {usuarios.map((usuario) => {
            const ehVocêMesmo = usuario.id === claims?.usuarioId
            return (
              <Card key={usuario.id} className="flex items-center justify-between gap-4 py-3">
                <div className="min-w-0">
                  <div className="flex items-center gap-2">
                    <p className="font-medium text-slate-900">{usuario.nome}</p>
                    <Badge tone={usuario.role === 'ADMIN' ? 'green' : 'slate'}>{usuario.role}</Badge>
                    {ehVocêMesmo && <Badge tone="blue">Você</Badge>}
                  </div>
                  <p className="truncate text-sm text-slate-500">{usuario.email}</p>
                </div>
                <div className="flex shrink-0 gap-2">
                  {usuario.role === 'ALUNO' ? (
                    <Button
                      variant="secondary"
                      isLoading={alterarRoleMutation.isPending}
                      onClick={() => handleAlterarRole(usuario, 'ADMIN')}
                    >
                      Promover a ADMIN
                    </Button>
                  ) : (
                    <Button
                      variant="secondary"
                      isLoading={alterarRoleMutation.isPending}
                      disabled={ehVocêMesmo}
                      title={ehVocêMesmo ? 'Você não pode remover sua própria permissão de admin' : undefined}
                      onClick={() => handleAlterarRole(usuario, 'ALUNO')}
                    >
                      Rebaixar a ALUNO
                    </Button>
                  )}
                  <Button
                    variant="danger"
                    isLoading={deletarMutation.isPending}
                    disabled={ehVocêMesmo}
                    title={ehVocêMesmo ? 'Você não pode remover sua própria conta' : undefined}
                    onClick={() => handleRemover(usuario)}
                  >
                    Remover
                  </Button>
                </div>
              </Card>
            )
          })}
        </div>
      )}
    </div>
  )
}
