import api from "@/api/axios";
import type { GroupDTO } from "@/model/GroupDTO";

const defaultUrl = "/groups";

export const getGroups = async () => {
  return api.get<GroupDTO[]>(defaultUrl);
};

export const createGroup = async (payload: Pick<GroupDTO, "name">) => {
  return api.post(defaultUrl, payload);
};

export const updateGroup = async (id: number, payload: Pick<GroupDTO, "name">) => {
  return api.patch(`${defaultUrl}/${id}`, payload);
};

export const deleteGroup = async (id: number) => {
  return api.delete(`${defaultUrl}/${id}`);
};
