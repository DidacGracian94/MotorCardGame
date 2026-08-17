import { CapabilitiesDto, CapabilityDto } from '@/api/client'
import { useTranslation } from '@/i18n/LanguageContext'
import { createDefaultListItem, createDefaultNode } from '@/lib/configTransforms'
import { CapabilityNode, FieldValue, ZoneConfig, ZoneRefValue } from '@/types/config'
import ScalarValueField from '@/components/editor/ScalarValueField'
import ZoneRefField from '@/components/editor/ZoneRefField'

type NodeKind = 'ACTION' | 'CONDITION' | 'TARGET'

interface Props {
  kind: NodeKind
  node: CapabilityNode
  onChange: (node: CapabilityNode) => void
  catalogs: CapabilitiesDto
  zones: ZoneConfig[]
  depth: number
}

function catalogFor(kind: NodeKind, catalogs: CapabilitiesDto): CapabilityDto[] {
  if (kind === 'ACTION') return catalogs.actions
  if (kind === 'CONDITION') return catalogs.conditions
  return catalogs.targets
}

export default function CapabilityNodeEditor({ kind, node, onChange, catalogs, zones, depth }: Props) {
  const { t, tf } = useTranslation()
  const catalog = catalogFor(kind, catalogs)
  const descriptor = catalog.find((c) => c.name === node.type)

  const handleTypeChange = (name: string) => {
    const nextDescriptor = catalog.find((c) => c.name === name)
    if (!nextDescriptor) return
    onChange(createDefaultNode(nextDescriptor, zones, catalogs))
  }

  const updateField = (fieldName: string, value: FieldValue) => {
    onChange({ ...node, fields: { ...node.fields, [fieldName]: value } })
  }

  return (
    <div
      className="border border-slate-200 rounded-lg p-3 bg-slate-50 space-y-3"
      style={{ marginLeft: depth > 0 ? 16 : 0 }}
    >
      <select
        value={node.type}
        onChange={(e) => handleTypeChange(e.target.value)}
        className="px-2 py-1 border border-slate-300 rounded text-sm bg-white"
      >
        <option value="">{t('editor.selectCapabilityPlaceholder')}</option>
        {catalog.map((capability) => (
          <option key={capability.name} value={capability.name}>
            {tf(`capability.${capability.name}`, capability.name)}
          </option>
        ))}
      </select>

      {descriptor?.fields.map((field) => (
        <div key={field.name} className="space-y-1">
          <label className="block text-xs font-medium text-slate-600">
            {tf(`field.${field.name}`, field.name)}
          </label>

          {field.kind === 'ZONE_REF' && (
            <ZoneRefField
              value={(node.fields[field.name] as ZoneRefValue | undefined) ?? { name: '', ownership: 'SHARED' }}
              onChange={(value) => updateField(field.name, value)}
              zones={zones}
            />
          )}

          {field.kind === 'TEXT' && (
            <input
              type="text"
              value={(node.fields[field.name] as string | undefined) ?? ''}
              onChange={(e) => updateField(field.name, e.target.value)}
              className="w-full px-2 py-1 border border-slate-300 rounded text-sm"
            />
          )}

          {field.kind === 'INTEGER' && (
            <input
              type="number"
              value={(node.fields[field.name] as number | undefined) ?? 0}
              onChange={(e) => updateField(field.name, Number(e.target.value))}
              className="w-full px-2 py-1 border border-slate-300 rounded text-sm"
            />
          )}

          {field.kind === 'BOOLEAN' && (
            <input
              type="checkbox"
              checked={(node.fields[field.name] as boolean | undefined) ?? false}
              onChange={(e) => updateField(field.name, e.target.checked)}
              className="h-4 w-4"
            />
          )}

          {field.kind === 'SCALAR' && (
            <ScalarValueField
              value={(node.fields[field.name] as string | number | boolean | undefined) ?? ''}
              onChange={(value) => updateField(field.name, value)}
            />
          )}

          {field.kind === 'ENUM' && (
            <select
              value={(node.fields[field.name] as string | undefined) ?? field.defaultValue ?? ''}
              onChange={(e) => updateField(field.name, e.target.value)}
              className="px-2 py-1 border border-slate-300 rounded text-sm"
            >
              {(field.enumValues ?? []).map((option) => (
                <option key={option} value={option}>
                  {tf(`enum.${option}`, option)}
                </option>
              ))}
            </select>
          )}

          {(field.kind === 'CONDITION' || field.kind === 'ACTION') && (
            <CapabilityNodeEditor
              kind={field.kind}
              node={(node.fields[field.name] as CapabilityNode | undefined) ?? { type: '', fields: {} }}
              onChange={(value) => updateField(field.name, value)}
              catalogs={catalogs}
              zones={zones}
              depth={depth + 1}
            />
          )}

          {(field.kind === 'CONDITION_LIST' || field.kind === 'ACTION_LIST') && (
            <NodeListField
              kind={field.kind === 'CONDITION_LIST' ? 'CONDITION' : 'ACTION'}
              nodes={(node.fields[field.name] as CapabilityNode[] | undefined) ?? []}
              onChange={(nodes) => updateField(field.name, nodes)}
              catalogs={catalogs}
              zones={zones}
              depth={depth + 1}
            />
          )}
        </div>
      ))}
    </div>
  )
}

interface NodeListFieldProps {
  kind: 'CONDITION' | 'ACTION'
  nodes: CapabilityNode[]
  onChange: (nodes: CapabilityNode[]) => void
  catalogs: CapabilitiesDto
  zones: ZoneConfig[]
  depth: number
}

function NodeListField({ kind, nodes, onChange, catalogs, zones, depth }: NodeListFieldProps) {
  const { t } = useTranslation()
  const catalogList = kind === 'CONDITION' ? catalogs.conditions : catalogs.actions

  const updateAt = (index: number, node: CapabilityNode) => {
    onChange(nodes.map((n, i) => (i === index ? node : n)))
  }

  const removeAt = (index: number) => {
    onChange(nodes.filter((_, i) => i !== index))
  }

  const addItem = () => {
    onChange([...nodes, createDefaultListItem(catalogList, zones, catalogs)])
  }

  return (
    <div className="space-y-2">
      {nodes.map((node, index) => (
        <div key={index} className="flex gap-2 items-start">
          <div className="flex-1">
            <CapabilityNodeEditor
              kind={kind}
              node={node}
              onChange={(value) => updateAt(index, value)}
              catalogs={catalogs}
              zones={zones}
              depth={depth}
            />
          </div>
          <button onClick={() => removeAt(index)} className="text-red-600 hover:text-red-700 text-sm">
            {t('common.remove')}
          </button>
        </div>
      ))}
      <button onClick={addItem} className="text-blue-600 hover:text-blue-700 font-medium text-xs">
        {kind === 'CONDITION' ? t('editor.addCondition') : t('editor.addAction')}
      </button>
    </div>
  )
}
