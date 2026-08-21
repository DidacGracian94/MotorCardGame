import { useState } from 'react'
import Dialog from '@/components/ui/Dialog'
import { useAiGuide } from '@/hooks/useAiGuide'
import { useTranslation } from '@/i18n/LanguageContext'

interface Props {
  open: boolean
  onOpenChange: (open: boolean) => void
}

const PROVIDERS = [
  { labelKey: 'aiGuide.openClaude', url: 'https://claude.ai/new' },
  { labelKey: 'aiGuide.openChatGpt', url: 'https://chatgpt.com' },
  { labelKey: 'aiGuide.openGemini', url: 'https://gemini.google.com/app' },
] as const

export default function AiGenerateModal({ open, onOpenChange }: Props) {
  const { t } = useTranslation()
  const [description, setDescription] = useState('')
  const [copied, setCopied] = useState(false)
  const { data: guide, isLoading, isError } = useAiGuide(open)

  const handleGeneratePrompt = () => {
    if (!guide) return
    const instructions = t('aiGuide.promptInstructions', { description })
    const prompt = `${instructions}\n\n${guide.markdown}`
    navigator.clipboard.writeText(prompt).then(() => {
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    })
  }

  const handleOpenProvider = (url: string) => {
    window.open(url, '_blank', 'noopener,noreferrer')
  }

  return (
    <Dialog
      open={open}
      onOpenChange={onOpenChange}
      title={t('aiGuide.modalTitle')}
      widthClassName="max-w-2xl"
    >
      <div className="space-y-4">
        <p className="text-sm text-slate-600">{t('aiGuide.intro')}</p>

        <div>
          <label className="block text-sm font-medium text-slate-900 mb-1">
            {t('aiGuide.descriptionLabel')}
          </label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={4}
            placeholder={t('aiGuide.descriptionPlaceholder')}
            className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {isLoading && <p className="text-sm text-slate-500">{t('aiGuide.loadingGuide')}</p>}
        {isError && <p className="text-sm text-red-600">{t('aiGuide.guideError')}</p>}

        <button
          type="button"
          onClick={handleGeneratePrompt}
          disabled={!guide}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {copied ? t('aiGuide.promptCopied') : t('aiGuide.generatePromptButton')}
        </button>

        <div>
          <p className="text-sm font-medium text-slate-900 mb-2">{t('aiGuide.openProviderTitle')}</p>
          <div className="flex flex-wrap gap-2">
            {PROVIDERS.map((provider) => (
              <button
                key={provider.labelKey}
                type="button"
                onClick={() => handleOpenProvider(provider.url)}
                className="px-3 py-2 border border-slate-300 rounded-lg text-sm hover:bg-slate-50 transition-colors"
              >
                {t(provider.labelKey)}
              </button>
            ))}
          </div>
        </div>

        <div className="bg-slate-50 rounded-lg p-4 text-sm text-slate-700 space-y-1">
          <p className="font-medium text-slate-900">{t('aiGuide.stepsTitle')}</p>
          <p>{t('aiGuide.step1')}</p>
          <p>{t('aiGuide.step2')}</p>
          <p>{t('aiGuide.step3')}</p>
          <p>{t('aiGuide.step4')}</p>
        </div>
      </div>
    </Dialog>
  )
}
