<script setup lang="ts">
import {computed, ref, watch} from "vue";
import {useRoute} from "vue-router";
import {useTheme} from "vuetify";
import {useAuthStore} from "@/store/useAuthStore";
import router from "@/router";
import {getUserRoleColor} from "@/enum/UserRole";

import {
  mdiAccount,
  mdiAccountGroupOutline,
  mdiAccountMultipleOutline,
  mdiAccountSchool,
  mdiBookOpenPageVariantOutline,
  mdiCalendarMonthOutline,
  mdiLogout,
  mdiMenu,
} from "@mdi/js";

const route = useRoute();
const authStore = useAuthStore();
const theme = useTheme();

/* ---------------- THEME ---------------- */
const isDark = ref(localStorage.getItem("appTheme") === "mainThemeDark");

watch(isDark, (val) => {
  const themeName = val ? "mainThemeDark" : "mainThemeLight";
  theme.change(themeName);
  localStorage.setItem("appTheme", themeName);
});

/* ---------------- USER ---------------- */
const fullName = computed(() => {
  const { lastname, firstname, middlename } = authStore.authedUser ?? {};
  return [lastname, firstname, middlename].filter(Boolean).join(" ");
});

const logout = async () => {
  await router.push("/login");
  await authStore.logout();
};

/* ---------------- SIDEBAR ---------------- */
const drawer = ref(true);
const rail = ref(false);

const menu = [
  { title: "Группы", icon: mdiAccountGroupOutline, to: "/groups" },
  { title: "Предметы", icon: mdiBookOpenPageVariantOutline, to: "/subjects" },
  { title: "Пользователи", icon: mdiAccountMultipleOutline, to: "/users" },
  { title: "Расписание", icon: mdiCalendarMonthOutline, to: "/schedule" },
];

const toggleSidebar = () => {
  if (window.innerWidth < 960) {
    drawer.value = !drawer.value;
  } else {
    rail.value = !rail.value;
  }
};
</script>

<template>
  <v-app>

    <!-- SIDEBAR -->
    <v-navigation-drawer
        v-model="drawer"
        :rail="rail"
        permanent
        class="sidebar"
        elevation="0"
    >
      <!-- LOGO -->
      <div class="logo">
        <v-icon :icon="mdiAccountSchool" size="20" />
        <span class="logo-text" :class="{ collapsed: rail }">
          Дневник
        </span>
      </div>

      <v-divider />

      <v-list nav density="comfortable">

        <!-- USER ITEM -->
        <v-list-item
            class="menu-item pa-2"
            rounded="lg"
        >
          <template #prepend>

              <v-icon :icon="mdiAccount"></v-icon>

          </template>

          <v-list-item-title class="user-name">
            {{ fullName || authStore.authedUser.username }}
          </v-list-item-title>

          <v-list-item-subtitle class="user-login">
            {{ authStore.authedUser.username }}
          </v-list-item-subtitle>

          <v-chip
              size="x-small"
              :color="getUserRoleColor(authStore.role)"
              variant="flat"
          >
            {{ authStore.ruRole }}
          </v-chip>


        </v-list-item>

      </v-list>


      <v-divider />

      <!-- MENU -->
      <v-list nav density="comfortable" class="mt-2">
        <v-list-item
            v-for="item in menu"
            :key="item.to"
            :to="item.to"
            rounded="lg"
            class="menu-item"
            :class="{ active: route.path === item.to }"
        >
          <template #prepend>
            <v-icon :icon="item.icon" />
          </template>

          <span class="menu-label" :class="{ collapsed: rail }">
            {{ item.title }}
          </span>
        </v-list-item>
      </v-list>

      <!-- LOGOUT -->
      <div class="logout">
        <v-btn
            v-if="!rail"
            size="small"
            block
            variant="tonal"
            :icon="mdiLogout"
            @click="logout"
            rounded="lg"
            color="error"
        >
          <span v-if="!rail">Выйти</span>
        </v-btn>
      </div>
    </v-navigation-drawer>

    <!-- TOP BAR -->
    <v-app-bar elevation="0" class="app-bar">
      <v-btn :icon="mdiMenu" variant="text" @click="toggleSidebar" />

      <div class="page-title">
        {{ route.name || "Панель" }}
      </div>

      <v-spacer />

      <!-- THEME SWITCH -->

      <v-switch class="align-center justify-center d-flex mr-3" inset v-model="isDark"></v-switch>

    </v-app-bar>

    <!-- CONTENT -->
    <v-main class="main">
      <v-container fluid class="pa-8">
        <router-view />
      </v-container>
    </v-main>

  </v-app>
</template>

<style scoped>
/* SIDEBAR */
.sidebar {
  border-right: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
}

/* LOGO */
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px;
  font-weight: 600;
}

.logo-text.collapsed {
  opacity: 0;
  width: 0;
  overflow: hidden;
}

/* USER */
.user-block {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
}

.user-login {
  font-size: 12px;
  color: rgba(0,0,0,0.6);
}

/* MENU */
.menu-item:hover {
  background: rgba(0, 0, 0, 0.04);
}

.menu-item.active {
  background: rgba(var(--v-theme-primary), 0.12);
  color: rgb(var(--v-theme-primary));
}

.menu-label.collapsed {
  opacity: 0;
  width: 0;
  overflow: hidden;
}

/* LOGOUT */
.logout {
  margin-top: auto;
  padding: 12px;
}

/* TOP BAR */
.app-bar {
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.7);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

/* TITLE */
.page-title {
  font-size: 18px;
  font-weight: 600;
  margin-left: 8px;
}

/* MAIN */
.main {
  background: #f7f8fa;
}
</style>