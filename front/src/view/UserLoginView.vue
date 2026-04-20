<template>
  <ShapeBackground :is-dark="useTheme().current.value.dark" />
  <v-container class="login-wrapper d-flex align-center justify-center h-100" fluid>
    <v-card
      class="pa-6 align-center justify-center rounded-xl w-100 w-sm-75 w-md-66 w-lg-50 glass-card"
    >
      <v-card
        class="glass-card rounded-xl pa-6 d-flex flex-column flex-md-row align-center justify-center"
      >
        <v-col class="d-flex flex-column align-center justify-center text-center" cols="4">
          <v-sheet class="align-center justify-center bg-transparent">
            <div class="text-h4 font-weight-bold mt-3">ТВОЙ ДНЕВНИК</div>
            <div class="text-body-1 text-medium-emphasis">Электронный дневник</div>
          </v-sheet>
        </v-col>

        <v-divider class="mx-md-6 my-4 hidden-sm-and-down" vertical />

        <v-col>
          <v-sheet class="bg-transparent">
            <v-card-title class="d-flex align-center justify-center mb-2 text-center flex-wrap">
              <span class="text-h5 font-weight-bold">Доступ к системе</span>
              <v-icon class="ml-2" color="primary" :icon="mdiShieldKey" />
            </v-card-title>

            <v-card-text>
              <v-form @submit.prevent>
                <v-text-field
                  v-model="loginForm.username"
                  :rules="[Rules.required()]"
                  class="mb-3"
                  label="Логин"
                  placeholder="Логин"
                  :prepend-icon="mdiEmailOutline"
                  autocomplete="username"
                  required
                />

                <v-text-field
                  v-model="loginForm.password"
                  :rules="[Rules.required()]"
                  :type="loginForm.showPassword ? 'text' : 'password'"
                  class="mb-4"
                  label="Пароль"
                  placeholder="Пароль"
                  :prepend-icon="mdiLockOutline"
                  autocomplete="current-password"
                  required
                >
                  <template #append-inner>
                    <v-icon
                      class="cursor-pointer"
                      @click="loginForm.showPassword = !loginForm.showPassword"
                      :icon="loginForm.showPassword ? mdiEyeOff : mdiEye"
                    />
                  </template>
                </v-text-field>

                <v-btn
                  :disabled="!isFormValid"
                  :loading="loading"
                  block
                  class="font-weight-bold"
                  color="primary"
                  rounded="lg"
                  size="large"
                  @click="login"
                  type="submit"
                >
                  Войти
                </v-btn>
              </v-form>
            </v-card-text>
          </v-sheet>
        </v-col>
      </v-card>
    </v-card>
  </v-container>
</template>

<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/store/useAuthStore";
import { useSnackbarService } from "@/ui/snackbar/SnackbarService";
import ShapeBackground from "@/ui/ShapeBackground.vue";
import { mdiEmailOutline, mdiEye, mdiEyeOff, mdiLockOutline, mdiShieldKey } from "@mdi/js";
import { Rules } from "@/model/Rules";
import { isAxiosError } from "axios";
import { useTheme } from "vuetify/framework";

const loginForm = ref({
  username: "",
  password: "",
  showPassword: false,
});
const loading = ref(false);

const isFormValid = computed(
  (): boolean => !!loginForm.value.username && !!loginForm.value.password,
);

const router = useRouter();
const route = useRoute();
const store = useAuthStore();
const snackbarService = useSnackbarService();

const login = async () => {
  if (!isFormValid.value) return;

  try {
    loading.value = true;

    await store.login(loginForm.value.username, loginForm.value.password);

    const redirectTo =
      typeof route.query.redirect === "string" ? route.query.redirect : { name: "admin" };
    await router.push(redirectTo);
  } catch (err) {
    if (isAxiosError(err)) {
      if (err.status && err.status === 401) {
        snackbarService.error(
          "Неверные аутентификационные данные. Пожалуйста, проверьте корректность и повторите вход.",
        );
      } else if (err.status && Math.floor(err.status / 100) === 5) {
        snackbarService.error("Произошла ошибка на сервере, попробуйте еще раз позже");
      } else {
        snackbarService.error("Произошла ошибка, попробуйте еще раз");
      }
    }
  } finally {
    loading.value = false;
  }
};

const handleEnterPress = (e: KeyboardEvent) => {
  if (e.key === "Enter") login();
};

onMounted(() => document.addEventListener("keydown", handleEnterPress));
onMounted(() => {

  })
onBeforeUnmount(() => document.removeEventListener("keydown", handleEnterPress));
</script>
<style scoped>
.login-wrapper {
  position: relative;
  z-index: 1;
}
</style>
