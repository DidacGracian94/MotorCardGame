import { CapabilitiesDto, CapabilityDto, CapabilityFieldDto } from '@/api/client'
import {
  AttributeDefinition,
  AttributesConfig,
  CapabilityNode,
  CardTemplateConfig,
  FieldValue,
  GameConfig,
  Ownership,
  RuleConfig,
  ScalarType,
  ScalarValue,
  ZoneConfig,
  ZoneRefValue,
} from '@/types/config'

export function createEmptyConfig(): GameConfig {
  return { attributes: {}, playerActions: [], zones: [], cards: [], rules: [] }
}

export function defaultScalarValue(definition: AttributeDefinition | undefined): ScalarValue {
  if (!definition) return ''
  if (definition.type === 'number') return 0
  if (definition.type === 'boolean') return false
  return definition.options?.[0] ?? ''
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

function ruleToJson(rule: RuleConfig) {
  return {
    event: rule.event,
    condition: toJson(rule.condition),
    target: toJson(rule.target),
    action: toJson(rule.action),
  }
}

export function configToJson(config: GameConfig): unknown {
  return {
    attributes: config.attributes,
    playerActions: config.playerActions,
    zones: config.zones,
    cards: config.cards.map((card) => ({
      ...card,
      rules: card.rules && card.rules.length > 0 ? card.rules.map(ruleToJson) : undefined,
    })),
    rules: config.rules.map(ruleToJson),
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
  const rules = Array.isArray(obj.rules) ? obj.rules.map(parseRule).filter(isNotNull) : []
  return {
    id: obj.id,
    zone: obj.zone,
    count: typeof obj.count === 'number' ? obj.count : undefined,
    image: typeof obj.image === 'string' ? obj.image : undefined,
    attributes,
    rules: rules.length > 0 ? rules : undefined,
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

function isScalarType(value: unknown): value is ScalarType {
  return value === 'string' || value === 'number' || value === 'boolean'
}

// Acepta tanto el formato nuevo ({ "color": { "type": "string", "options": [...] } }) como el
// formato plano antiguo ({ "color": "string" }) por compatibilidad con configs ya guardadas.
function parseAttributeDefinition(raw: unknown): AttributeDefinition | null {
  if (isScalarType(raw)) {
    return { type: raw }
  }
  if (!raw || typeof raw !== 'object') return null
  const obj = raw as Record<string, unknown>
  if (!isScalarType(obj.type)) return null
  const options = Array.isArray(obj.options)
    ? obj.options.filter((item): item is string => typeof item === 'string')
    : undefined
  return { type: obj.type, options: options && options.length > 0 ? options : undefined }
}

function parseAttributes(raw: unknown): AttributesConfig {
  const attributes: AttributesConfig = {}
  if (!raw || typeof raw !== 'object') return attributes
  for (const [name, value] of Object.entries(raw as Record<string, unknown>)) {
    const definition = parseAttributeDefinition(value)
    if (definition) {
      attributes[name] = definition
    }
  }
  return attributes
}

function parsePlayerActions(raw: unknown): string[] {
  if (!Array.isArray(raw)) return []
  return raw.filter((item): item is string => typeof item === 'string')
}

export function parseConfig(raw: unknown): GameConfig {
  if (!raw || typeof raw !== 'object') {
    return createEmptyConfig()
  }
  const obj = raw as Record<string, unknown>
  const attributes = parseAttributes(obj.attributes)
  const playerActions = parsePlayerActions(obj.playerActions)
  const zones = Array.isArray(obj.zones) ? obj.zones.map(parseZone).filter(isNotNull) : []
  const cards = Array.isArray(obj.cards) ? obj.cards.map(parseCardTemplate).filter(isNotNull) : []
  const rules = Array.isArray(obj.rules) ? obj.rules.map(parseRule).filter(isNotNull) : []
  return { attributes, playerActions, zones, cards, rules }
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
