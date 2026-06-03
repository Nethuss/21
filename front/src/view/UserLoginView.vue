<template>
  <v-container fluid class="login-page pa-6 fill-height d-flex align-center justify-center">
    <div class="bg-grid" />

    <v-card ref="cardRef" class="login-card" elevation="0" rounded="xl">
      <section class="login-aside">
        <div class="aside-content">
          <GeoLoaderIcon
              class="logo"
              :color="'rgb(var(--v-theme-primary))'"
              size="6rem"
              weight="1.4"
          />

          <div class="system-title">Твой Дневник</div>

          <div class="system-subtitle">
            Электронный дневник для колледжа
          </div>

          <div class="system-status">
            <span class="status-dot" />

            <span>Все сервисы доступны</span>
          </div>

          <div class="system-tags">
            <span>Версия 4.32.9</span>

            <span>КСИПТ <v-icon class="ml-1" :icon="mdiFerry" size="small"></v-icon></span>
          </div>
        </div>
      </section>

      <!-- RIGHT -->

      <section class="login-content">
        <div class="form-container">
          <!-- MOBILE BRAND -->

          <div class="mobile-brand">
            <div class="mobile-brand-title">Твой Дневник</div>
          </div>

          <!-- HEADER -->

          <div class="login-header">
            <h1 class="header-title">Вход в систему</h1>

            <div class="header-description">Используйте служебную учётную запись</div>
          </div>

          <!-- FORM -->

          <v-form ref="formRef" v-model="formValid" @submit.prevent="login">
            <!-- LOGIN -->

            <v-text-field
                ref="usernameRef"
                v-model="loginForm.username"
                :rules="[Rules.required()]"
                variant="outlined"
                density="comfortable"
                autocomplete="username"
                name="username"
                label="Логин"
                :disabled="loading"
                hide-details="auto"
                class="login-input mb-4"
            >
              <template #prepend-inner>
                <v-icon size="18" :icon="mdiEmailOutline" />
              </template>
            </v-text-field>

            <!-- PASSWORD -->

            <v-text-field
                v-model="loginForm.password"
                :rules="[Rules.required()]"
                :type="showPassword ? 'text' : 'password'"
                variant="outlined"
                density="comfortable"
                autocomplete="current-password"
                name="password"
                label="Пароль"
                :disabled="loading"
                :error-messages="authError"
                hide-details="auto"
                class="login-input"
                @keydown.enter="login"
            >
              <template #prepend-inner>
                <v-icon size="18" :icon="mdiLockOutline" />
              </template>

              <template #append-inner>
                <v-btn icon variant="text" size="x-small" @click="showPassword = !showPassword">
                  <v-icon size="18" :icon="showPassword ? mdiEyeOff : mdiEye" />
                </v-btn>
              </template>
            </v-text-field>

            <!-- CAPS -->

            <transition name="fade">
              <div v-if="capsLock" class="caps-warning">
                <v-icon size="15" :icon="mdiAlert" />

                <span>Включён Caps Lock</span>
              </div>
            </transition>

            <!-- BUTTON -->

            <v-btn
                :disabled="!formValid || loading"
                :loading="loading"
                type="submit"
                block
                rounded="lg"
                size="x-large"
                color="primary"
                class="login-button"
            >
              Войти
            </v-btn>

            <!-- FOOTER -->

            <div class="login-footer">
              <span>TLS 1.3</span>

              <div class="footer-divider" />

              <span>ГОСТ Р 34.10-2012</span>

              <div class="footer-divider" />

              <span>Сеанс защищён</span>
            </div>
          </v-form>
        </div>
      </section>
    </v-card>
  </v-container>
  <canvas ref="canvasRef" class="fire-canvas" />
</template>

<script lang="ts" setup>
import {ref, onMounted, onBeforeUnmount, useTemplateRef} from "vue";

import { useRouter, useRoute } from "vue-router";

import { isAxiosError } from "axios";

import { Rules } from "@/model/Rules";

import GeoLoaderIcon from "@/components/GeoLoaderIcon.vue";

import {mdiAlert, mdiEmailOutline, mdiEye, mdiEyeOff, mdiFerry, mdiLockOutline} from "@mdi/js";
import {useAuthStore} from "@/store/useAuthStore";
import {useSnackbarService} from "@/ui/snackbar/SnackbarService";

const router = useRouter();
const route = useRoute();

const store = useAuthStore();
const snackbar = useSnackbarService();

const formRef = ref();
const usernameRef = ref();

const loginForm = ref({
  username: "",
  password: "",
});

const showPassword = ref(false);
const loading = ref(false);
const formValid = ref(false);
const authError = ref("");
const capsLock = ref(false);

const handleCapsLock = (e: KeyboardEvent) => {
  capsLock.value = e.getModifierState("CapsLock");
};

// баловство
const cardRef = useTemplateRef<InstanceType<typeof import("vuetify/components").VCard>>("cardRef");
const canvasRef = useTemplateRef<HTMLCanvasElement>("canvasRef");

onMounted(() => {
  document.addEventListener("keydown", handleCapsLock);

  const saved = localStorage.getItem("lastLogin");

  if (saved) {
    loginForm.value.username = saved;
  }

  setTimeout(() => {
    usernameRef.value?.focus();
  }, 250);
});

onBeforeUnmount(() => {
  document.removeEventListener("keydown", handleCapsLock);
});

const login = async () => {
  authError.value = "";

  if (!formValid.value) {
    return;
  }

  try {
    loading.value = true;

    await store.login(loginForm.value.username, loginForm.value.password);

    localStorage.setItem("lastLogin", loginForm.value.username);

    snackbar.success("Вход выполнен");

    await new Promise((r) => setTimeout(r, 300));

    const redirectTo =
        typeof route.query.redirect === "string" ? route.query.redirect : { name: "main" };


      await router.push(redirectTo);
  } catch (err) {
    if (isAxiosError(err)) {
      if (err.response?.status === 401) {
        authError.value = "Неверный логин или пароль";
      } else {
        snackbar.error("Ошибка авторизации");
      }
    } else {
      snackbar.error("Ошибка входа");
    }
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-page {
  position: fixed; /* было relative */
  inset: 0;
  overflow: hidden; /* уже есть */
  background: rgb(var(--v-theme-background));
}

/* BACKGROUND */

.bg-grid {
  position: absolute;
  inset: 0;

  opacity: 0.035;

  background-image:
      linear-gradient(rgba(var(--v-theme-on-surface), 0.08) 1px, transparent 1px),
      linear-gradient(90deg, rgba(var(--v-theme-on-surface), 0.08) 1px, transparent 1px);

  background-size: 40px 40px;

  mask-image: radial-gradient(circle at center, black 35%, transparent 100%);
}

/* CARD */

.login-card {
  position: relative;
  z-index: 2;

  width: min(1040px, 92vw);
  min-height: 640px;

  display: grid;
  grid-template-columns: 1fr 430px;

  overflow: hidden;

  border-radius: 28px !important;

  background: rgb(var(--v-theme-surface));

  border: 1px solid rgba(var(--v-theme-on-surface), 0.08);

  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
}

/* LEFT */

.login-aside {
  display: flex;
  align-items: center;

  padding: 72px;

  border-right: 1px solid rgba(var(--v-theme-on-surface), 0.06);
}

.aside-content {
  max-width: 380px;
}

.logo {
  margin-bottom: 42px;
}

.system-title {
  font-size: 64px;
  line-height: 0.92;

  letter-spacing: -3px;
  font-weight: 800;

  color: rgb(var(--v-theme-on-surface));
}

.system-subtitle {
  margin-top: 22px;

  max-width: 360px;

  font-size: 15px;
  line-height: 1.8;

  color: rgba(var(--v-theme-on-surface), 0.58);
}

/* STATUS */

.system-status {
  margin-top: 34px;

  display: inline-flex;
  align-items: center;
  gap: 10px;

  padding: 10px 14px;

  border-radius: 999px;

  background: rgba(var(--v-theme-success), 0.06);

  border: 1px solid rgba(var(--v-theme-success), 0.12);

  font-size: 13px;
  font-weight: 650;

  color: rgba(var(--v-theme-on-surface), 0.72);
}

.status-dot {
  width: 8px;
  height: 8px;

  border-radius: 50%;

  background: rgb(var(--v-theme-success));

  box-shadow: 0 0 0 0 rgba(var(--v-theme-success), 0.35);

  animation: pulse 4s infinite;
}

/* TAGS */

.system-tags {
  margin-top: 28px;

  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.system-tags span {
  height: 32px;

  display: inline-flex;
  align-items: center;

  padding: 0 14px;

  border-radius: 999px;

  border: 1px solid rgba(var(--v-theme-on-surface), 0.08);

  background: rgba(var(--v-theme-on-surface), 0.025);

  font-size: 12px;
  font-weight: 600;

  color: rgba(var(--v-theme-on-surface), 0.58);
}

/* RIGHT */

.login-content {
  display: flex;
  align-items: center;
  justify-content: center;

  padding: 72px 64px;
}

.form-container {
  width: 100%;
  max-width: 340px;
}

/* MOBILE BRAND */

.mobile-brand {
  display: none;
}

/* HEADER */

.login-header {
  margin-bottom: 40px;
}

.header-title {
  font-size: 38px;
  line-height: 1.05;

  letter-spacing: -1.6px;
  font-weight: 780;

  color: rgb(var(--v-theme-on-surface));
}

.header-description {
  margin-top: 12px;

  color: rgba(var(--v-theme-on-surface), 0.56);

  line-height: 1.7;
}

/* INPUTS */

:deep(.login-input .v-field) {
  border-radius: 16px !important;

  background: rgba(var(--v-theme-on-surface), 0.018) !important;

  transition:
      border-color 0.15s ease,
      background 0.15s ease;
}

:deep(.login-input .v-field:hover) {
  background: rgba(var(--v-theme-on-surface), 0.025) !important;
}

:deep(.login-input .v-field--focused) {
  background: rgba(var(--v-theme-surface), 1) !important;

  box-shadow: none;
}

:deep(.login-input input) {
  font-weight: 600;
}

/* BUTTON */

.login-button {
  margin-top: 28px;

  height: 56px !important;

  font-weight: 700;

  letter-spacing: 0.2px;

  box-shadow: none !important;
}

/* FOOTER */

.login-footer {
  margin-top: 28px;

  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 10px;

  font-size: 11px;

  letter-spacing: 0.06em;
  text-transform: uppercase;

  color: rgba(var(--v-theme-on-surface), 0.42);
}

.footer-divider {
  width: 4px;
  height: 4px;

  border-radius: 50%;

  background: rgba(var(--v-theme-on-surface), 0.18);
}

/* CAPS */

.caps-warning {
  margin-top: 14px;

  display: inline-flex;
  align-items: center;
  gap: 8px;

  padding: 10px 12px;

  border-radius: 12px;

  background: rgba(var(--v-theme-warning), 0.1);

  border: 1px solid rgba(var(--v-theme-warning), 0.18);

  color: rgb(var(--v-theme-warning));

  font-size: 13px;
  font-weight: 600;
}

/* ANIMATIONS */

.fade-enter-active,
.fade-leave-active {
  transition:
      opacity 0.15s ease,
      transform 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(var(--v-theme-success), 0.35);
  }

  70% {
    box-shadow: 0 0 0 5px rgba(var(--v-theme-success), 0);
  }

  100% {
    box-shadow: 0 0 0 0 rgba(var(--v-theme-success), 0);
  }
}

/* MOBILE */

@media (max-width: 960px) {
  .login-page {
    padding: 0 !important;
    position: fixed; /* добавляем */
    overflow-y: auto; /* если контент не помещается */
    -webkit-overflow-scrolling: touch;
  }

  .login-card {
    grid-template-columns: 1fr;
    min-height: 100%; /* было 100dvh */
    width: 100%;
    border: none;
    border-radius: 0 !important;
    background: transparent;
    box-shadow: none;
  }

  .login-aside {
    display: none;
  }

  .login-content {
    padding: 40px 28px;

    width: 100%;
  }

  .mobile-brand {
    display: flex;
    flex-direction: column;
    align-items: center;

    margin-bottom: 52px;
  }

  .mobile-brand-title {
    margin-top: 18px;

    font-size: 42px;
    font-weight: 800;

    letter-spacing: -2px;

    color: rgb(var(--v-theme-on-surface));
  }

  /* УБИРАЕМ HEADER */

  .login-header {
    display: none;
  }

  .login-footer {
    gap: 8px;

    line-height: 1.6;
  }

  .fade-enter-active,
  .fade-leave-active {
    transition:
        opacity 0.15s ease,
        transform 0.15s ease;
  }
}

@media (max-width: 600px) {
  .login-page {
    padding: 16px !important;
  }

  .login-content {
    padding: 32px 22px;
  }

  .login-footer {
    gap: 8px;

    line-height: 1.6;
  }

  .header-title {
    font-size: 28px;
  }
}

.fire-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 3;
}

:deep(.login-input .v-field__outline) {
  color: rgba(var(--v-theme-on-surface), 0.12);
}

:deep(.login-input .v-field--focused .v-field__outline) {
  color: rgb(var(--v-theme-primary));
}

/* у .login-card уже есть z-index: 2, поднять до 4 */
.login-card {
  z-index: 4;
  /* остальные стили без изменений */
}
</style>
