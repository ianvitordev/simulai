import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { z } from 'zod'
import * as authApi from '../api/auth'
import { AuthLayout } from '../components/AuthLayout'
import { Alert } from '../components/ui/Alert'
import { Button } from '../components/ui/Button'
import { FieldWrapper, Input } from '../components/ui/Field'
import { useAuth } from '../hooks/useAuth'
import { extrairMensagemErro } from '../lib/apiClient'

const schema = z.object({
  codigo: z.string().length(6, 'O código tem 6 dígitos'),
})

type FormData = z.infer<typeof schema>

export function ConfirmarCadastroPage() {
  const { autenticarComToken } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const email = (location.state as { email?: string } | null)?.email
  const [erro, setErro] = useState<string | null>(null)
  const [reenviado, setReenviado] = useState(false)
  const [reenviando, setReenviando] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  if (!email) {
    return <Navigate to="/cadastro" replace />
  }

  async function onSubmit(data: FormData) {
    setErro(null)
    try {
      const response = await authApi.confirmarCadastro({ email: email!, codigo: data.codigo })
      autenticarComToken(response.accessToken)
      navigate('/', { replace: true })
    } catch (error) {
      setErro(extrairMensagemErro(error))
    }
  }

  async function handleReenviar() {
    setErro(null)
    setReenviado(false)
    setReenviando(true)
    try {
      await authApi.reenviarCodigo({ email: email!, tipo: 'CONFIRMACAO_CADASTRO' })
      setReenviado(true)
    } catch (error) {
      setErro(extrairMensagemErro(error))
    } finally {
      setReenviando(false)
    }
  }

  return (
    <AuthLayout>
      <h1 className="mb-1 text-2xl font-semibold text-slate-900">Confirme seu cadastro</h1>
      <p className="mb-6 text-sm text-slate-500">
        Enviamos um código de 6 dígitos para <strong>{email}</strong>. Digite abaixo para ativar sua conta.
      </p>

      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
        {erro && <Alert tone="error">{erro}</Alert>}
        {reenviado && !erro && <Alert tone="success">Novo código enviado.</Alert>}

        <FieldWrapper label="Código" htmlFor="codigo" error={errors.codigo?.message}>
          <Input id="codigo" inputMode="numeric" maxLength={6} autoComplete="one-time-code" {...register('codigo')} />
        </FieldWrapper>

        <Button type="submit" isLoading={isSubmitting} className="w-full">
          Confirmar
        </Button>
        <Button type="button" variant="secondary" isLoading={reenviando} onClick={handleReenviar} className="w-full">
          Reenviar código
        </Button>
      </form>
    </AuthLayout>
  )
}
