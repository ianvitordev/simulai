import type { ReactNode } from 'react'

type Tone = 'error' | 'success' | 'info'

const TONE_CLASSES: Record<Tone, string> = {
  error: 'bg-red-50 text-red-800 border-red-200',
  success: 'bg-green-50 text-green-800 border-green-200',
  info: 'bg-blue-50 text-blue-800 border-blue-200',
}

export function Alert({ tone = 'info', children }: { tone?: Tone; children: ReactNode }) {
  return (
    <div className={`rounded-lg border px-4 py-3 text-sm ${TONE_CLASSES[tone]}`} role="alert">
      {children}
    </div>
  )
}
