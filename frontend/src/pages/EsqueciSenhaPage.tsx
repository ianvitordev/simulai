import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { z } from 'zod'
import * as authApi from '../api/auth'
import { AuthLayout } from '../components/AuthLayout'
import { Alert } from '../components/ui/Alert'
import { Button } from '../components/ui/Button'
import { FieldWrapper, Input } from '../components/ui/Field'
import { useAuth } from '../hooks/useAuth'
import { extrairMensagemErro } from '../lib/apiClient'

const schemaEmail = z.object({
  email: z.string().min(1, 'Informe o email').email('Email inválido').max(100, 'Máximo de 100 caracteres'),
})

const schemaRedefinicao = z.object({
  codigo: z.string().length(6, 'O código tem 6 dígitos'),
  novaSenha: z.string().min(6, 'A senha precisa ter pelo menos 6 caracteres').max(50, 'Máximo de 50 caracteres'),
})

type FormEmail = z.infer<typeof schemaEmail>
type FormRedefinicao = z.infer<typeof schemaRedefinicao>

export function EsqueciSenhaPage() {
  const { autenticarComToken } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState<string | null>(null)
  const [erro, setErro] = useState<string | null>(null)

  const formEmail = useForm<FormEmail>({ resolver: zodResolver(schemaEmail) })
  const formRedefinicao = useForm<FormRedefinicao>({ resolver: zodResolver(schemaRedefinicao) })

  async function onSubmitEmail(data: FormEmail) {
    setErro(null)
    try {
      await authApi.esqueciSenha({ email: data.email })
      setEmail(data.email)
    } catch (error) {
      setErro(extrairMensagemErro(error))
    }
  }

  async function onSubmitRedefinicao(data: FormRedefinicao) {
    setErro(null)
    try {
      const response = await authApi.redefinirSenha({ email: email!, codigo: data.codigo, novaSenha: data.novaSenha })
      autenticarComToken(response.accessToken)
      navigate('/', { replace: true })
    } catch (error) {
      setErro(extrairMensagemErro(error))
    }
  }

  return (
    <AuthLayout>
      <h1 className="mb-1 text-2xl font-semibold text-slate-900">Esqueci minha senha</h1>

      {!email ? (
        <>
          <p className="mb-6 text-sm text-slate-500">
            Informe seu email — se ele estiver cadastrado, você recebe um código para redefinir a senha.
          </p>
          <form onSubmit={formEmail.handleSubmit(onSubmitEmail)} className="flex flex-col gap-4">
            {erro && <Alert tone="error">{erro}</Alert>}

            <FieldWrapper label="Email" htmlFor="email" error={formEmail.formState.errors.email?.message}>
              <Input id="email" type="email" autoComplete="email" maxLength={100} {...formEmail.register('email')} />
            </FieldWrapper>

            <Button type="submit" isLoading={formEmail.formState.isSubmitting} className="mt-2 w-full">
              Enviar código
            </Button>
          </form>
        </>
      ) : (
        <>
          <p className="mb-6 text-sm text-slate-500">
            Se <strong>{email}</strong> estiver cadastrado, você recebeu um código de 6 dígitos. Digite-o abaixo
            junto com a nova senha.
          </p>
          <form onSubmit={formRedefinicao.handleSubmit(onSubmitRedefinicao)} className="flex flex-col gap-4">
            {erro && <Alert tone="error">{erro}</Alert>}

            <FieldWrapper label="Código" htmlFor="codigo" error={formRedefinicao.formState.errors.codigo?.message}>
              <Input id="codigo" inputMode="numeric" maxLength={6} autoComplete="one-time-code"
                {...formRedefinicao.register('codigo')} />
            </FieldWrapper>

            <FieldWrapper label="Nova senha" htmlFor="novaSenha" error={formRedefinicao.formState.errors.novaSenha?.message}>
              <Input id="novaSenha" type="password" autoComplete="new-password" maxLength={50}
                {...formRedefinicao.register('novaSenha')} />
            </FieldWrapper>

            <Button type="submit" isLoading={formRedefinicao.formState.isSubmitting} className="mt-2 w-full">
              Redefinir senha
            </Button>
          </form>
        </>
      )}

      <p className="mt-6 text-center text-sm text-slate-500">
        <Link to="/login" className="font-medium text-gold-600 hover:underline">
          Voltar para o login
        </Link>
      </p>
    </AuthLayout>
  )
}
