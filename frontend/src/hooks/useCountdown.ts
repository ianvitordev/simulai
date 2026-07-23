import { useEffect, useState } from 'react'

/**
 * Conta regressiva a partir de um deadline absoluto. Retorna null quando não há
 * limite de tempo (deadline null) e 0 quando o tempo já esgotou.
 */
export function useCountdown(deadline: Date | null): number | null {
  const [segundosRestantes, setSegundosRestantes] = useState<number | null>(() =>
    deadline ? Math.max(0, Math.round((deadline.getTime() - Date.now()) / 1000)) : null,
  )

  useEffect(() => {
    if (!deadline) {
      setSegundosRestantes(null)
      return
    }

    const interval = setInterval(() => {
      const restante = Math.max(0, Math.round((deadline.getTime() - Date.now()) / 1000))
      setSegundosRestantes(restante)
    }, 1000)

    return () => clearInterval(interval)
  }, [deadline])

  return segundosRestantes
}

export function formatarTempo(segundos: number): string {
  const minutos = Math.floor(segundos / 60)
  const resto = segundos % 60
  return `${minutos}:${resto.toString().padStart(2, '0')}`
}
