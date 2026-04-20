export interface FeatureEditableProps {
  address: string
  cadastralNumber: string
  ks: number

  entityId?: number
  categoryName?: string
  area?: number
  buildRecordRegistrationDate?: string
  purpose?: string
  yearBuilt?: number
  floors?: number
  [key: string]: unknown
}
