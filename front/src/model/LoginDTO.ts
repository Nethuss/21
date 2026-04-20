import { UserRole } from "@/enum/UserRole";

export interface LoginDTO {
  username: string,
  id: number,
  roles: UserRole[],
  password?: string,
}
