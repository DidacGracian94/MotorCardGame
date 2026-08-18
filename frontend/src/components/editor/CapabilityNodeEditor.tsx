import { CapabilitiesDto, CapabilityDto } from '@/api/client'
import { useTranslation } from '@/i18n/LanguageContext'
import { createDefaultListItem, createDefaultNode } from '@/lib/configTransforms'
import { AttributesConfig, CapabilityNode, FieldValue, ZoneConfig, ZoneRefValue } from '@/types/config'
import ScalarValueField from '@/components/editor/ScalarValueField'
import ZoneRefField from '@/components/editor/ZoneRefField'

type NodeKind = 'ACTION' | 'CONDITION' | 'TARGET'

interface Props {
  kind: NodeKind
  node: CapabilityNode
  onChange: (node: CapabilityNode) => void
  catalogs: CapabilitiesDto
  zones: ZoneConfig[]
  attributes: AttributesConfig
  depth: number
  readOnly?: boolean
}

function catalogFor(kind: NodeKind, catalogs: CapabilitiesDto): CapabilityDto[] {
  if (kind === 'ACTION') return catalogs.actions
  if (kind === 'CONDITION') return catalogs.conditions
  return catalogs.targets
}

export default function CapabilityNodeEditor({
  kind,
  node,
  onChange,
  catalogs,
  zones,
  attributes,
  depth,
  readOnly = false,
}: Props) {
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
        disabled={readOnly}
        className="px-2 py-1 border border-slate-300 rounded text-sm bg-white disabled:bg-slate-50 disabled:text-slate-600"
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
              readOnly={readOnly}
            />
          )}

          {field.kind === 'TEXT' && field.name === 'attribute' && (
            <AttributeFieldSelect
              value={(node.fields[field.name] as string | undefined) ?? ''}
              onChange={(value) => updateField(field.name, value)}
              attributes={attributes}
              readOnly={readOnly}
            />
          )}

          {field.kind === 'TEXT' && field.name !== 'attribute' && (
            <input
              type="text"
              value={(node.fields[field.name] as string | undefined) ?? ''}
              onChange={(e) => updateField(field.name, e.target.value)}
              disabled={readOnly}
              className="w-full px-2 py-1 border border-slate-300 rounded text-sm disabled:bg-slate-50 disabled:text-slate-600"
            />
          )}

          {field.kind === 'INTEGER' && (
            <input
              type="number"
              value={(node.fields[field.name] as number | undefined) ?? 0}
              onChange={(e) => updateField(field.name, Number(e.target.value))}
              disabled={readOnly}
              className="w-full px-2 py-1 border border-slate-300 rounded text-sm disabled:bg-slate-50 disabled:text-slate-600"
            />
          )}

          {field.kind === 'BOOLEAN' && (
            <input
              type="checkbox"
              checked={(node.fields[field.name] as boolean | undefined) ?? false}
              onChange={(e) => updateField(field.name, e.target.checked)}
              disabled={readOnly}
              className="h-4 w-4"
            />
          )}

          {field.kind === 'SCALAR' && (() => {
            // Si este nodo tiene un campo "attribute" hermano (p.ej. CARD_ATTRIBUTE_EQUALS), el
            // valor de este campo debe encajar con lo que ese atributo declara — se fuerza su
            // tipo/opciones en vez de dejar elegir tipo y texto libres por separado.
            const referencedAttributeName = node.fields.attribute as string | undefined
            const referencedAttribute = referencedAttributeName ? attributes[referencedAttributeName] : undefined
            return (
              <ScalarValueField
                value={(node.fields[field.name] as string | number | boolean | undefined) ?? ''}
                onChange={(value) => updateField(field.name, value)}
                type={referencedAttribute?.type}
                options={referencedAttribute?.options}
                readOnly={readOnly}
              />
            )
          })()}

          {field.kind === 'ENUM' && (
            <select
              value={(node.fields[field.name] as string | undefined) ?? field.defaultValue ?? ''}
              onChange={(e) => updateField(field.name, e.target.value)}
              disabled={readOnly}
              className="px-2 py-1 border border-slate-300 rounded text-sm disabled:bg-slate-50 disabled:text-slate-600"
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
              attributes={attributes}
              depth={depth + 1}
              readOnly={readOnly}
            />
          )}

          {(field.kind === 'CONDITION_LIST' || field.kind === 'ACTION_LIST') && (
            <NodeListField
              kind={field.kind === 'CONDITION_LIST' ? 'CONDITION' : 'ACTION'}
              nodes={(node.fields[field.name] as CapabilityNode[] | undefined) ?? []}
              onChange={(nodes) => updateField(field.name, nodes)}
              catalogs={catalogs}
              zones={zones}
              attributes={attributes}
              depth={depth + 1}
              readOnly={readOnly}
            />
          )}
        </div>
      ))}
    </div>
  )
}

interface AttributeFieldSelectProps {
  value: string
  onChange: (value: string) => void
  attributes: AttributesConfig
  readOnly?: boolean
}

function AttributeFieldSelect({ value, onChange, attributes, readOnly = false }: AttributeFieldSelectProps) {
  const { t, tf } = useTranslation()
  const declared = Object.keys(attributes)
  const options = value && !declared.includes(value) ? [value, ...declared] : declared

  return (
    <select
      value={value}
      onChange={(e) => onChange(e.target.value)}
      disabled={readOnly}
      className="px-2 py-1 border border-slate-300 rounded text-sm bg-white disabled:bg-slate-50 disabled:text-slate-600"
    >
      <option value="">{t('editor.selectAttributePlaceholder')}</option>
      {options.map((name) => (
        <option key={name} value={name}>
          {tf(`attribute.${name}`, name)}
          {!declared.includes(name) ? ` (${t('editor.attributeUndeclaredOption')})` : ''}
        </option>
      ))}
    </select>
  )
}

interface NodeListFieldProps {
  kind: 'CONDITION' | 'ACTION'
  nodes: CapabilityNode[]
  onChange: (nodes: CapabilityNode[]) => void
  catalogs: CapabilitiesDto
  zones: ZoneConfig[]
  attributes: AttributesConfig
  depth: number
  readOnly?: boolean
}

function NodeListField({ kind, nodes, onChange, catalogs, zones, attributes, depth, readOnly = false }: NodeListFieldProps) {
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
              attributes={attributes}
              depth={depth}
              readOnly={readOnly}
            />
          </div>
          {!readOnly && (
            <button onClick={() => removeAt(index)} className="text-red-600 hover:text-red-700 text-sm">
              {t('common.remove')}
            </button>
          )}
        </div>
      ))}
      {!readOnly && (
        <button onClick={addItem} className="text-blue-600 hover:text-blue-700 font-medium text-xs">
          {kind === 'CONDITION' ? t('editor.addCondition') : t('editor.addAction')}
        </button>
      )}
    </div>
  )
}
