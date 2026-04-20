import axios from "axios";
import router from "@/router";
import {updateAccessToken} from "@/api/auth.api";
import { ApiError } from "@/api/error/api.error";

/**
 * Создание экземплярa axios
 */
export const api = axios.create({withCredentials: true, baseURL: `/ecd`});

/**
 * Экземпляр Axios для запросов на публичные сервера (настройка withCredentials: true блокирует запросы на сервера с Access-Control-Allow-Origin: *)
 */
export const publicApi = axios.create()

/**
 * Интерцептор для обработки ответов сервера.
 * Если ответ успешный, он возвращается без изменений.
 * В случае ошибки 401 (Unauthorized), происходит попытка обновления accessToken.
 * Если обновление успешно, запрос повторяется с новым токеном.
 * Если обновление не удалось, ошибка возвращается в цепочку промисов.
 */
let isRefreshing = false;
let refreshPromise: Promise<void> | null = null;

api.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    if (axios.isCancel(error)) {
      return Promise.resolve();
    }

    if (
      error.response?.status === 401 &&
      !originalRequest._retry
    ) {
      originalRequest._retry = true;

      // Если уже идет процесс обновления токена — ждем его завершения
      if (isRefreshing) {
        try {
          await refreshPromise;
          return api(originalRequest);
        } catch (err) {
          return Promise.reject(normalizeApiError(err));
        }
      }

      // Иначе запускаем обновление токена
      isRefreshing = true;
      refreshPromise = updateAccessToken().then(() => {})
        .finally(() => {
          isRefreshing = false;
          refreshPromise = null;
        });

      try {
        await refreshPromise;
        return api(originalRequest);
      } catch (err) {
        await router.push({ name: 'login' });
        return Promise.reject(normalizeApiError(err));
      }
    }

    return Promise.reject(normalizeApiError(error));
  }
);

function normalizeApiError(error): ApiError {
  // Axios error
  if (axios.isAxiosError(error)) {
    const status = error.response?.status;
    const data = error.response?.data;

    const message =
      data?.message ||
      data?.error ||
      error.message ||
      "Ошибка сервера";

    return new ApiError(message, {
      status,
      data,
    });
  }

  // Любая другая ошибка
  if (error instanceof Error) {
    return new ApiError(error.message);
  }

  return new ApiError("Неизвестная ошибка");
}


export default api;
