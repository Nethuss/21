<script setup lang="ts">
import { computed, mergeProps, ref } from "vue";
import { useAuthStore } from "@/store/useAuthStore";
import router from "@/router";
import { getUserRoleColor } from "@/enum/UserRole";
import { mdiAccount, mdiBriefcase, mdiLogout } from "@mdi/js";

const { buttonProps = {}, menuProps = {} } = defineProps<{
  buttonProps?: Record<string, unknown>;
  menuProps?: Record<string, unknown>;
}>();

const authStore = useAuthStore();
const profileMenu = ref(false);

const fullName = computed(() => {
  const { lastname, firstname, middlename } = authStore.authedUser ?? {};
  return [lastname, firstname, middlename].filter(Boolean).join(" ");
});

const logout = async () => {
  profileMenu.value = false;
  await router.push({ path: "/login" });
  await authStore.logout();
};
</script>

<template>
  <v-menu
      v-model="profileMenu"
      :close-on-content-click="false"
      transition="scale-transition"
      location="bottom end"
      :open-on-hover="$vuetify.display.lgAndUp"
      offset="8"
      v-bind="menuProps"
  >
    <template #activator="{ props }">
      <v-btn
          v-bind="mergeProps(buttonProps, props)"
          :icon="mdiAccount"
          variant="text"
          size="small"
          class="top-btn"
      />
    </template>

    <v-card class="menu-card rounded-lg">

      <!-- USER -->
      <div class="user-block">
        <v-avatar size="36" color="primary">
          <v-icon size="18" :icon="mdiAccount" />
        </v-avatar>

        <div>
          <div class="user-name">
            {{ fullName || authStore.authedUser.username }}
          </div>
          <div class="user-login">
            {{ authStore.authedUser.username }}
          </div>
        </div>
      </div>

      <!-- INFO -->
      <div class="info-block">
        <div class="info-row">
          <span>Роль</span>

          <v-chip
              size="small"
              :color="getUserRoleColor(authStore.role)"
              variant="flat"
          >
            <v-icon start size="14" :icon="mdiBriefcase" />
            {{ authStore.ruRole }}
          </v-chip>
        </div>
      </div>

      <!-- ACTION -->
      <v-btn
          block
          :icon="mdiLogout"
          @click="logout"
          class="rounded-lg"
      >
        Выйти
      </v-btn>

    </v-card>
  </v-menu>
</template>

<style scoped>
.top-btn {
  border-radius: 10px;
  transition: all 0.2s ease;
}

.top-btn:hover {
  background: rgba(0, 0, 0, 0.05);
}

/* CARD */
.menu-card {
  padding: 16px;
  border-radius: 16px;
  min-width: 240px;
  backdrop-filter: blur(12px);
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid rgba(0,0,0,0.06);
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* USER */
.user-block {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-name {
  font-weight: 600;
  font-size: 14px;
}

.user-login {
  font-size: 12px;
  color: rgba(0,0,0,0.6);
}

/* INFO */
.info-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}
</style>