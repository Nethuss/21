import { computed, ref } from 'vue'
import {defineStore} from "pinia";
import { getAuthedUserData, signIn, signOut } from '@/api/auth.api'
import { getRuRole } from '@/enum/UserRole'
import type { Role } from '@/enum/UserRole'
import {isAxiosError} from "axios";

/**
 * Хранение аутентификационных данных пользователя.
 * Включает в себя уникальное имя учётной записи пользователя (username), токен доступа (accessToken)
 * и токен обновления (refreshToken). Методы позволяют управлять
 * уникальным именем учётной записи пользователя, выполнять аутентификацию и обновление токенов.
 */

interface AuthedUser {
  username: string
  id: number
  firstname: string
  lastname: string
  middlename: string
  isActive: boolean
  roles: Role[]
}

export const useAuthStore = defineStore(
  "auth",
  () => {
    async function pushToLogin() {
      const { default: router } = await import("@/router");
      await router.push("/login");
    }

    const authedUser = ref<AuthedUser>({
      username: "",
      id: 0,
      isActive: true,
      firstname: '',
      lastname: '',
      middlename: '',
      roles: [],
    })

    const username = ref<string>("");
    const ruRole = computed(() => getRuRole(authedUser.value.roles[0]?.name))
    const role = computed(() => authedUser.value.roles[0]?.name);
    const firstname = ref<string>("");
    const lastname = ref<string>("");
    const middlename = ref<string>("");
    const locationIds = ref<number[]>([]);

    /**
     * Выполняет выход пользователя, очищая аутентификационные данных пользователя
     */
    const logout = async () => {
        await signOut();
    };

    /**
     * Выполняет вход пользователя, отправляя запрос на сервер с данными пользователя.
     * В случае успешного ответа сохраняет полученные данные в состояние.
     * @param usernameFromForm уникальное имя учётной записи пользователя
     * @param passwordFromForm пароль учетной записи пользователя
     * @return true при успешной аутентификации, false в случае ошибки
     */
    const login = async (
      usernameFromForm: string,
      passwordFromForm: string,
    ): Promise<boolean> => {
      try {
          await signIn(usernameFromForm, passwordFromForm)

          const response = await getAuthedUserData()

          authedUser.value = response.data

          username.value = response.data.username
          firstname.value = response.data.firstname;
          lastname.value = response.data.lastname;
          middlename.value = response.data.middlename;
          locationIds.value = response.data.locationIds;
        return true;
      } catch (error) {
        console.error("Login error: ", error);
        if (isAxiosError(error)){
          throw error;
        } else {
          throw new Error('Произошла ошибка:', {cause: error})
        }
      }
    };

    async function loadUser () {
      try {
        const userData = (await getAuthedUserData()).data
        if (!userData) {
          await pushToLogin();
        } else {
          authedUser.value = userData;
        }
      } catch (error) {
        if (isAxiosError(error) && error.status === 401) {
          await pushToLogin();
        }
      }
    }

    /**
     * Обновляет токен доступа, используя токен обновления.
     * В случае неудачи выполняет выход пользователя.
     */

    return {
        username,
        ruRole,
        role,
        logout,
        login,
        firstname,
        lastname,
        middlename,
      loadUser,
      authedUser,
    };
  },
);
