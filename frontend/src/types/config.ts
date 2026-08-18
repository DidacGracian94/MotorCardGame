// Único evento de ciclo de vida disparado por el propio motor/app (ver
// GameInstanceService.create en el backend) — no requiere estar declarado en playerActions.
export const GAME_STARTED_EVENT = 'GAME_STARTED'

export type Ownership = 'SHARED' | 'PER_PLAYER'

export type ScalarType = 'string' | 'number' | 'boolean'

export type ScalarValue = string | number | boolean

// "options" solo tiene sentido cuando type === 'string': una lista cerrada de valores permitidos
// para este atributo (p.ej. color -> RED/BLUE/GREEN), para que el resto del editor pueda ofrecer
// un selector en vez de texto libre y así evitar errores de escritura al asignar valores.
export interface AttributeDefinition {
  type: ScalarType
  options?: string[]
}

export type AttributesConfig = Record<string, AttributeDefinition>

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
  image?: string
  attributes: Record<string, ScalarValue>
  // Reglas propias de esta carta (p.ej. "chupa 2" define aquí qué hace al jugarla). El motor no
  // distingue estas reglas de las de la lista global — solo viven en otro sitio del JSON para que
  // el editor las muestre pegadas a "su" carta en vez de en una lista plana.
  rules?: RuleConfig[]
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
  attributes: AttributesConfig
  playerActions: string[]
  zones: ZoneConfig[]
  cards: CardTemplateConfig[]
  rules: RuleConfig[]
}
