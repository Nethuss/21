import api from "@/api/axios";
import type {
  AdminUserDTO,
  CreateAdminUserRequest,
  UpdateAdminUserRequest,
} from "@/model/AdminUserDTO";

const defaultUrl = "/admin/users";

export const getUsers = async () => {
  return api.get<AdminUserDTO[]>(defaultUrl);
};

export const getUserById = async (id: number) => {
  return api.get<AdminUserDTO>(`${defaultUrl}/${id}`);
};

export const createUser = async (payload: CreateAdminUserRequest) => {
  return api.post(defaultUrl, payload);
};

export const updateUser = async (id: number, payload: UpdateAdminUserRequest) => {
  return api.put(`${defaultUrl}/${id}`, payload);
};

export const deleteUser = async (id: number) => {
  return api.delete(`${defaultUrl}/${id}`);
};
