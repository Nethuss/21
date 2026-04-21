import type { SubjectDTO } from "@/model/SubjectDTO";

export interface AdminUserDTO {
  id: number;
  username: string;
  firstname: string | null;
  middlename: string | null;
  lastname: string | null;
  isActive: boolean;
  roles: string[];
  groupId: number | null;
  subjects?: SubjectDTO[];
}

export interface CreateAdminUserRequest {
  username: string;
  password: string;
  firstname: string;
  middlename: string;
  lastname: string;
  roles: string[];
  groupId: number | null;
  subjectIds: number[] | null;
}

export interface UpdateAdminUserRequest {
  password: string | null;
  firstname: string;
  middlename: string;
  lastname: string;
  isActive: boolean;
  roles: string[];
  groupId: number | null;
  subjectIds: number[] | null;
}
