import { CapabilitiesDto, CapabilityDto, CapabilityFieldDto } from '@/api/client'
import {
  CapabilityNode,
  CardTemplateConfig,
  FieldValue,
  GameConfig,
  Ownership,
  RuleConfig,
  ScalarValue,
  ZoneConfig,
  ZoneRefValue,
} from '@/types/config'

export function createEmptyConfig(): GameConfig {
  return { zones: [], cards: [], rules: [] }
}

function isCapabilityNode(value: unknown): value is CapabilityNode {
  return (
    !!value &&
    typeof value === 'object' &&
    'type' in value &&
    typeof (value as { type: unknown }).type === 'string'
  )
}

function serializeFieldValue(value: FieldValue): unknown {
  if (value === undefined) {
    return undefined
  }
  if (Array.isArray(value)) {
    return value.map((node) => toJson(node))
  }
  if (isCapabilityNode(value)) {
    return toJson(value)
  }
  return value
}

export function toJson(node: CapabilityNode): unknown {
  const result: Record<string, unknown> = { type: node.type }
  for (const [key, value] of Object.entries(node.fields)) {
    result[key] = serializeFieldValue(value)
  }
  return result
}

export function configToJson(config: GameConfig): unknown {
  return {
    zones: config.zones,
    cards: config.cards,
    rules: config.rules.map((rule) => ({
      event: rule.event,
      condition: toJson(rule.condition),
      target: toJson(rule.target),
      action: toJson(rule.action),
    })),
  }
}

function parseFieldValue(value: unknown): FieldValue {
  if (value === null || value === undefined) {
    return undefined
  }
  if (Array.isArray(value)) {
    return value.map((item) => parseCapabilityNode(item))
  }
  if (typeof value === 'object') {
    const obj = value as Record<string, unknown>
    if (typeof obj.type === 'string') {
      return parseCapabilityNode(value)
    }
    if (typeof obj.name === 'string' && typeof obj.ownership === 'string') {
      return { name: obj.name, ownership: obj.ownership as Ownership } satisfies ZoneRefValue
    }
    return undefined
  }
  if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean') {
    return value
  }
  return undefined
}

function parseCapabilityNode(raw: unknown): CapabilityNode {
  if (!raw || typeof raw !== 'object') {
    return { type: '', fields: {} }
  }
  const obj = raw as Record<string, unknown>
  const type = typeof obj.type === 'string' ? obj.type : ''
  const fields: Record<string, FieldValue> = {}
  for (const [key, value] of Object.entries(obj)) {
    if (key === 'type') continue
    fields[key] = parseFieldValue(value)
  }
  return { type, fields }
}

function parseZone(raw: unknown): ZoneConfig | null {
  if (!raw || typeof raw !== 'object') return null
  const obj = raw as Record<string, unknown>
  if (typeof obj.name !== 'string' || typeof obj.ownership !== 'string') return null
  return {
    name: obj.name,
    ownership: obj.ownership as Ownership,
    shuffle: typeof obj.shuffle === 'boolean' ? obj.shuffle : undefined,
  }
}

function parseCardTemplate(raw: unknown): CardTemplateConfig | null {
  if (!raw || typeof raw !== 'object') return null
  const obj = raw as Record<string, unknown>
  if (typeof obj.id !== 'string' || typeof obj.zone !== 'string') return null
  const attributes: Record<string, ScalarValue> = {}
  if (obj.attributes && typeof obj.attributes === 'object') {
    for (const [key, value] of Object.entries(obj.attributes as Record<string, unknown>)) {
      if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean') {
        attributes[key] = value
      }
    }
  }
  return {
    id: obj.id,
    zone: obj.zone,
    count: typeof obj.count === 'number' ? obj.count : undefined,
    attributes,
  }
}

function parseRule(raw: unknown): RuleConfig | null {
  if (!raw || typeof raw !== 'object') return null
  const obj = raw as Record<string, unknown>
  return {
    event: typeof obj.event === 'string' ? obj.event : '',
    condition: parseCapabilityNode(obj.condition),
    target: parseCapabilityNode(obj.target),
    action: parseCapabilityNode(obj.action),
  }
}

function isNotNull<T>(value: T | null): value is T {
  return value !== null
}

export function parseConfig(raw: unknown): GameConfig {
  if (!raw || typeof raw !== 'object') {
    return createEmptyConfig()
  }
  const obj = raw as Record<string, unknown>
  const zones = Array.isArray(obj.zones) ? obj.zones.map(parseZone).filter(isNotNull) : []
  const cards = Array.isArray(obj.cards) ? obj.cards.map(parseCardTemplate).filter(isNotNull) : []
  const rules = Array.isArray(obj.rules) ? obj.rules.map(parseRule).filter(isNotNull) : []
  return { zones, cards, rules }
}

const NESTED_FIELD_KINDS = new Set(['CONDITION', 'CONDITION_LIST', 'ACTION', 'ACTION_LIST'])

function isLeafCapability(capability: CapabilityDto): boolean {
  return capability.fields.every((field) => !NESTED_FIELD_KINDS.has(field.kind))
}

function findLeafCapability(capabilities: CapabilityDto[]): CapabilityDto | undefined {
  return capabilities.find(isLeafCapability) ?? capabilities[0]
}

export function createDefaultListItem(
  catalogList: CapabilityDto[],
  zones: ZoneConfig[],
  catalogs: CapabilitiesDto
): CapabilityNode {
  const leaf = findLeafCapability(catalogList)
  return leaf ? createDefaultNode(leaf, zones, catalogs) : { type: '', fields: {} }
}

function defaultFieldValue(
  field: CapabilityFieldDto,
  zones: ZoneConfig[],
  catalogs: CapabilitiesDto
): FieldValue {
  switch (field.kind) {
    case 'ZONE_REF': {
      const zone = zones[0]
      return { name: zone?.name ?? '', ownership: zone?.ownership ?? 'SHARED' }
    }
    case 'TEXT':
      return ''
    case 'INTEGER':
      return 0
    case 'BOOLEAN':
      return false
    case 'SCALAR':
      return ''
    case 'ENUM':
      return field.defaultValue ?? field.enumValues?.[0] ?? ''
    case 'CONDITION':
      return createDefaultListItem(catalogs.conditions, zones, catalogs)
    case 'CONDITION_LIST':
      return []
    case 'ACTION':
      return createDefaultListItem(catalogs.actions, zones, catalogs)
    case 'ACTION_LIST':
      return []
    default:
      return undefined
  }
}

export function createDefaultNode(
  descriptor: CapabilityDto,
  zones: ZoneConfig[],
  catalogs: CapabilitiesDto
): CapabilityNode {
  const fields: Record<string, FieldValue> = {}
  for (const field of descriptor.fields) {
    fields[field.name] = defaultFieldValue(field, zones, catalogs)
  }
  return { type: descriptor.name, fields }
}
