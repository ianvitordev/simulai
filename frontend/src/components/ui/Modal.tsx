import type { ReactNode } from 'react'

type MaxWidth = 'md' | 'xl'

const MAX_WIDTH_CLASSES: Record<MaxWidth, string> = {
  md: 'max-w-md',
  xl: 'max-w-2xl',
}

interface ModalProps {
  title: string
  onClose: () => void
  children: ReactNode
  maxWidth?: MaxWidth
}

export function Modal({ title, onClose, children, maxWidth = 'md' }: ModalProps) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
      <div
        className={`w-full ${MAX_WIDTH_CLASSES[maxWidth]} rounded-xl bg-white p-6 shadow-xl`}
        role="dialog"
        aria-modal="true"
        aria-label={title}
      >
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-lg font-semibold text-slate-900">{title}</h2>
          <button
            onClick={onClose}
            aria-label="Fechar"
            className="rounded-full p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
          >
            ✕
          </button>
        </div>
        {children}
      </div>
    </div>
  )
}
