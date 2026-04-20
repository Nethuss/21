export interface LocationDTO {
  name: string,
  id: number,
  createdUserId: number,
  municipalityId?: number,
  createdAt: number[],
  isDeleted: boolean
}
