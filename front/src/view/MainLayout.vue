<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { useTheme } from "vuetify";

import router from "@/router";
import { useAuthStore } from "@/store/useAuthStore";
import { getUserRoleColor } from "@/enum/UserRole";

import {
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

const isDark = ref(
    localStorage.getItem("appTheme") === "mainThemeDark"
);

watch(isDark, (val) => {
  const themeName = val
      ? "mainThemeDark"
      : "mainThemeLight";

  theme.change(themeName);
  localStorage.setItem("appTheme", themeName);
});

/* ---------------- USER ---------------- */

const fullName = computed(() => {
  const { lastname, firstname, middlename } =
  authStore.authedUser ?? {};

  return [lastname, firstname, middlename]
      .filter(Boolean)
      .join(" ");
});

const initials = computed(() => {
  const user = authStore.authedUser;

  if (!user) return "?";

  return (
      user.firstname?.[0] ||
      user.username?.[0] ||
      "?"
  ).toUpperCase();
});

/* ---------------- MENU ---------------- */

const menu = computed(() => {
  const role = authStore.role;

  switch (role) {
    case "ROLE_ADMIN":
      return [
        {
          title: "Группы",
          icon: mdiAccountGroupOutline,
          to: "/groups",
        },
        {
          title: "Предметы",
          icon: mdiBookOpenPageVariantOutline,
          to: "/subjects",
        },
        {
          title: "Пользователи",
          icon: mdiAccountMultipleOutline,
          to: "/users",
        },
        {
          title: "Расписание",
          icon: mdiCalendarMonthOutline,
          to: "/schedule",
        },
      ];

    case "ROLE_SUPERUSER":
      return [
        {
          title: "Журнал",
          icon: mdiCalendarMonthOutline,
          to: "/teacher",
        },
      ];

    default:
      return [
        {
          title: "Ученик",
          icon: mdiCalendarMonthOutline,
          to: "/student",
        },
      ];
  }
});

/* ---------------- DEFAULT ROUTE ---------------- */

const getDefaultRoute = () => {
  switch (authStore.role) {
    case "ROLE_ADMIN":
      return "/groups";

    case "ROLE_SUPERUSER":
      return "/teacher";

    default:
      return "/student";
  }
};

const redirectFromRoot = async () => {
  if (route.path === "/") {
    await router.replace(getDefaultRoute());
  }
};

onMounted(() => {
  redirectFromRoot();
});

watch(
    () => authStore.role,
    () => {
      redirectFromRoot();
    },
    { immediate: true }
);

/* ---------------- LOGOUT ---------------- */

const logout = async () => {
  await authStore.logout();
  await router.replace("/login");
};

/* ---------------- SIDEBAR ---------------- */

const drawer = ref(true);

const navigate = async (path: string) => {
  if (route.path === path) return;

  await router.push(path);

  if (window.innerWidth < 960) {
    drawer.value = false;
  }
};
</script>
<template>
  <v-app>

    <v-navigation-drawer
        v-model="drawer"
        :permanent="$vuetify.display.mdAndUp"
        :temporary="$vuetify.display.smAndDown"
        width="260"
        class="sidebar"
    >
      <div class="logo">
        <v-icon :icon="mdiAccountSchool" size="22" />

        <span
            class="logo-text"
        >
          Дневник
        </span>
      </div>

      <v-divider />

      <!-- USER -->

      <v-list nav density="comfortable">
        <v-list-item
            class="menu-item pa-2"
            rounded="lg"
        >
          <template #prepend>

            <v-avatar
                size="42"
                :color="getUserRoleColor(authStore.role)"
            >
              <span class="text-white">
                {{ initials }}
              </span>
            </v-avatar>

          </template>

          <v-list-item-title class="user-name">
            {{ fullName || authStore.authedUser?.username }}
          </v-list-item-title>

          <v-list-item-subtitle class="user-login">
            {{ authStore.authedUser?.username }}
          </v-list-item-subtitle>

          <v-chip
              size="small"
              class="mt-2"
              variant="flat"
              :color="getUserRoleColor(authStore.role)"
          >
            {{ authStore.ruRole }}
          </v-chip>
        </v-list-item>
      </v-list>

      <v-divider />

      <!-- MENU -->

      <v-list
          nav
          density="comfortable"
          class="mt-2"
      >
        <v-list-item
            v-for="item in menu"
            :key="item.to"
            rounded="lg"
            class="menu-item"
            :class="{
              active: route.path.startsWith(item.to)
            }"
            @click="navigate(item.to)"
        >
          <template #prepend>
            <v-icon :icon="item.icon" />
          </template>

          <span
              class="menu-label"
          >
            {{ item.title }}
          </span>
        </v-list-item>
      </v-list>

      <!-- LOGOUT -->

      <div class="logout">
        <v-btn
            block
            color="error"
            variant="tonal"
            rounded="lg"
            @click="logout"
        >
          <v-icon
              start
              :icon="mdiLogout"
          />

          <span>
            Выйти
          </span>
        </v-btn>
      </div>

    </v-navigation-drawer>

    <!-- TOPBAR -->

    <v-app-bar
        elevation="0"
        class="app-bar"
    >
      <v-btn
          :icon="mdiMenu"
          variant="text"
          @click="drawer = !drawer"
      />

      <div class="page-title">
        {{ route.name || "Панель" }}
      </div>

      <v-spacer />

      <v-switch
          v-model="isDark"
          inset
          hide-details
          class="mr-3"
      />
    </v-app-bar>

    <!-- CONTENT -->

    <v-main class="main">
      <v-container fluid class="pa-6">
        <router-view />
      </v-container>
    </v-main>

  </v-app>
</template>

<style scoped>
.sidebar {
  border-right: 1px solid rgba(var(--v-theme-on-surface), 0.08);
  background: rgb(var(--v-theme-surface));
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px;
  font-size: 18px;
  font-weight: 700;
}

.logo-text {
  transition: 0.2s;
}
.user-name {
  font-size: 14px;
  font-weight: 600;
}

.user-login {
  font-size: 12px;
  opacity: 0.7;
}

.menu-item {
  margin: 4px 8px;
  transition: all .15s ease;
}

.menu-item:hover {
  background: rgba(var(--v-theme-on-surface), 0.05);
}

.menu-item.active {
  background: rgba(var(--v-theme-primary), 0.12);
  color: rgb(var(--v-theme-primary));
}

.menu-label {
  transition: .2s;
}

.logout {
  margin-top: auto;
  padding: 12px;
}

.app-bar {
  backdrop-filter: blur(10px);
  background: rgba(var(--v-theme-surface), 0.9);
  border-bottom: 1px solid rgba(var(--v-theme-on-surface), 0.08);
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  margin-left: 8px;
}

.main {
  background: rgb(var(--v-theme-background));
}
</style>