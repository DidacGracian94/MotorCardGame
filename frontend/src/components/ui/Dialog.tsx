import * as DialogPrimitive from '@radix-ui/react-dialog'

interface Props {
  open: boolean
  onOpenChange: (open: boolean) => void
  title: string
  children: React.ReactNode
  widthClassName?: string
}

export default function Dialog({
  open,
  onOpenChange,
  title,
  children,
  widthClassName = 'max-w-md',
}: Props) {
  return (
    <DialogPrimitive.Root open={open} onOpenChange={onOpenChange}>
      <DialogPrimitive.Portal>
        <DialogPrimitive.Overlay className="fixed inset-0 bg-black/50 data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0" />
        <DialogPrimitive.Content className={`fixed left-[50%] top-[50%] w-full ${widthClassName} translate-x-[-50%] translate-y-[-50%] bg-white shadow-lg duration-200 data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95 data-[state=closed]:slide-out-to-left-1/2 data-[state=closed]:slide-out-to-top-[48%] data-[state=open]:slide-in-from-left-1/2 data-[state=open]:slide-in-from-top-[48%] rounded-lg`}>
          <div className="flex items-center justify-between p-6 border-b border-slate-200">
            <DialogPrimitive.Title className="text-lg font-semibold text-slate-900">
              {title}
            </DialogPrimitive.Title>
            <DialogPrimitive.Close className="rounded-md opacity-70 hover:opacity-100 transition-opacity">
              <span className="text-2xl text-slate-500 hover:text-slate-900">×</span>
            </DialogPrimitive.Close>
          </div>
          <div className="p-6">{children}</div>
        </DialogPrimitive.Content>
      </DialogPrimitive.Portal>
    </DialogPrimitive.Root>
  )
}
