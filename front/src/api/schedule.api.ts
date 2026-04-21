import api from "@/api/axios";
import type { ScheduleSlotDTO, ScheduleSlotWriteBody } from "@/model/ScheduleSlotDTO";

const defaultUrl = "/schedule";

export const getScheduleForDay = async (groupId: number, date: string) => {
  return api.get<ScheduleSlotDTO[]>(defaultUrl, {
    params: { group_id: groupId, date },
  });
};

export const getScheduleForRange = async (groupId: number, dateFrom: string, dateTo: string) => {
  return api.get<ScheduleSlotDTO[]>(defaultUrl, {
    params: { group_id: groupId, date_from: dateFrom, date_to: dateTo },
  });
};

export const createScheduleSlot = async (body: ScheduleSlotWriteBody) => {
  return api.post(defaultUrl, body);
};

export const updateScheduleSlot = async (id: number, body: ScheduleSlotWriteBody) => {
  return api.patch(`${defaultUrl}/${id}`, body);
};

export const deleteScheduleSlot = async (id: number) => {
  return api.delete(`${defaultUrl}/${id}`);
};
