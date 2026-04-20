import { ApiError } from "@/api/error/api.error";

export function checkErrorOrElseDefaultMsg(error) : string {
  if (error instanceof ApiError) {
    return error.message;
  } else {
    return "Ошибка"
  }
}
