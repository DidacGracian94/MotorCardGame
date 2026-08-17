export type Ownership = 'SHARED' | 'PER_PLAYER'

export type ScalarValue = string | number | boolean

export interface ZoneRefValue {
  name: string
  ownership: Ownership
}

export interface ZoneConfig {
  name: string
  ownership: Ownership
  shuffle?: boolean
}

export interface CardTemplateConfig {
  id: string
  zone: string
  count?: number
  attributes: Record<string, ScalarValue>
}

// Nodo genérico y data-driven — NO una interfaz TS por capacidad, porque el catálogo
// en sí es dinámico (viene de /api/capabilities). Es literalmente la forma del JSON:
// { type: node.type, ...node.fields } === {"type": "AND", "conditions": [...]}
export interface CapabilityNode {
  type: string
  fields: Record<string, FieldValue>
}

export type FieldValue =
  | string
  | number
  | boolean
  | ZoneRefValue
  | CapabilityNode
  | CapabilityNode[]
  | undefined

export interface RuleConfig {
  event: string
  condition: CapabilityNode
  target: CapabilityNode
  action: CapabilityNode
}

export interface GameConfig {
  zones: ZoneConfig[]
  cards: CardTemplateConfig[]
  rules: RuleConfig[]
}
