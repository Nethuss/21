
export type RecognizedObjectType = "CAPITAL_STRUCTURE" | "FARMLAND" | "HOGWEED" | "DUMP" | "DEFORESTATION";

export type RecognizedObjectTypeName = Record<RecognizedObjectType, string>

export const OBJECT_TYPE_NAME: RecognizedObjectTypeName = {
  CAPITAL_STRUCTURE: 'Объект капитального строительства',
  FARMLAND: '',
  HOGWEED: '',
  DUMP: '',
  DEFORESTATION: '',
} // TODO Дописать
