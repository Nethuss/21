import api from "@/api/axios";
import type { SubjectDTO } from "@/model/SubjectDTO";

const defaultUrl = "/subjects";

export const getSubjects = async () => {
  return api.get<SubjectDTO[]>(defaultUrl);
};

export const createSubject = async (payload: Pick<SubjectDTO, "name">) => {
  return api.post(defaultUrl, payload);
};

export const updateSubject = async (id: number, payload: Pick<SubjectDTO, "name">) => {
  return api.patch(`${defaultUrl}/${id}`, payload);
};

export const deleteSubject = async (id: number) => {
  return api.delete(`${defaultUrl}/${id}`);
};
