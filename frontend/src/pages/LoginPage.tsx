import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { Alert } from '../components/ui/Alert'
import { Button } from '../components/ui/Button'
import { FieldWrapper, Input } from '../components/ui/Field'
import { useAuth } from '../hooks/useAuth'
import { extrairMensagemErro } from '../lib/apiClient'

const schema = z.object({
  email: z.string().min(1, 'Informe o email').email('Email inválido'),
  senha: z.string().min(1, 'Informe a senha'),
})

type FormData = z.infer<typeof schema>

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const cadastroSucesso = Boolean((location.state as { cadastroSucesso?: boolean } | null)?.cadastroSucesso)
  const [erro, setErro] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  async function onSubmit(data: FormData) {
    setErro(null)
    try {
      await login(data)
      navigate('/', { replace: true })
    } catch (error) {
      setErro(extrairMensagemErro(error))
    }
  }

  return (
    <div className="mx-auto flex min-h-screen max-w-sm flex-col justify-center px-4">
      <h1 className="mb-1 text-2xl font-semibold text-slate-900">Entrar</h1>
      <p className="mb-6 text-sm text-slate-500">Acesse sua conta SimulaI</p>

      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
        {cadastroSucesso && !erro && (
          <Alert tone="success">Conta criada com sucesso! Faça login para continuar.</Alert>
        )}
        {erro && <Alert tone="error">{erro}</Alert>}

        <FieldWrapper label="Email" htmlFor="email" error={errors.email?.message}>
          <Input id="email" type="email" autoComplete="email" {...register('email')} />
        </FieldWrapper>

        <FieldWrapper label="Senha" htmlFor="senha" error={errors.senha?.message}>
          <Input id="senha" type="password" autoComplete="current-password" {...register('senha')} />
        </FieldWrapper>

        <Button type="submit" isLoading={isSubmitting} className="mt-2 w-full">
          Entrar
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-slate-500">
        Ainda não tem conta?{' '}
        <Link to="/cadastro" className="font-medium text-indigo-600 hover:underline">
          Cadastre-se
        </Link>
      </p>
    </div>
  )
}
