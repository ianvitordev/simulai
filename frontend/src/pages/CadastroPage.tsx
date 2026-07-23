import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { z } from 'zod'
import * as usuariosApi from '../api/usuarios'
import { Alert } from '../components/ui/Alert'
import { Button } from '../components/ui/Button'
import { FieldWrapper, Input } from '../components/ui/Field'
import { extrairMensagemErro } from '../lib/apiClient'

const schema = z.object({
  nome: z.string().min(1, 'Informe seu nome'),
  email: z.string().min(1, 'Informe o email').email('Email inválido'),
  senha: z.string().min(6, 'A senha precisa ter pelo menos 6 caracteres'),
})

type FormData = z.infer<typeof schema>

export function CadastroPage() {
  const navigate = useNavigate()
  const [erro, setErro] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  async function onSubmit(data: FormData) {
    setErro(null)
    try {
      await usuariosApi.cadastrar(data)
      navigate('/login', { replace: true, state: { cadastroSucesso: true } })
    } catch (error) {
      setErro(extrairMensagemErro(error))
    }
  }

  return (
    <div className="mx-auto flex min-h-screen max-w-sm flex-col justify-center px-4">
      <h1 className="mb-1 text-2xl font-semibold text-slate-900">Criar conta</h1>
      <p className="mb-6 text-sm text-slate-500">Comece a gerar seus simulados</p>

      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
        {erro && <Alert tone="error">{erro}</Alert>}

        <FieldWrapper label="Nome" htmlFor="nome" error={errors.nome?.message}>
          <Input id="nome" autoComplete="name" {...register('nome')} />
        </FieldWrapper>

        <FieldWrapper label="Email" htmlFor="email" error={errors.email?.message}>
          <Input id="email" type="email" autoComplete="email" {...register('email')} />
        </FieldWrapper>

        <FieldWrapper label="Senha" htmlFor="senha" error={errors.senha?.message}>
          <Input id="senha" type="password" autoComplete="new-password" {...register('senha')} />
        </FieldWrapper>

        <Button type="submit" isLoading={isSubmitting} className="mt-2 w-full">
          Criar conta
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-slate-500">
        Já tem conta?{' '}
        <Link to="/login" className="font-medium text-indigo-600 hover:underline">
          Entrar
        </Link>
      </p>
    </div>
  )
}
