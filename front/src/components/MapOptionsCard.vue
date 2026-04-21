<script setup lang="ts">
import { ref, watch } from "vue";
import { useTheme } from "vuetify";
import {
  mdiCog,
  mdiWeatherNight,
  mdiWhiteBalanceSunny,
} from "@mdi/js";

const theme = useTheme();

const currentTheme = ref(localStorage.getItem("appTheme") ?? "mainThemeLight");

watch(currentTheme, (value) => {
  theme.change(value);
  localStorage.setItem("appTheme", value);
});
</script>

<template>
  <v-menu
      :close-on-content-click="false"
      transition="scale-transition"
      location="bottom end"
      :open-on-hover="$vuetify.display.lgAndUp"
      offset="8"
  >
    <template #activator="{ props }">
      <v-btn
          v-bind="props"
          :icon="mdiCog"
          variant="text"
          size="small"
          class="top-btn"
      />
    </template>

    <v-card class="menu-card rounded-lg">
      <!-- HEADER -->
      <div class="menu-header">
        <v-icon size="18" :icon="mdiCog" />
        <span>Настройки</span>
      </div>

      <!-- THEME -->
      <div class="menu-section">
        <div class="menu-label">Тема</div>

        <v-btn-toggle
            v-model="currentTheme"
            mandatory
            divided
            class="theme-toggle"
        >
          <v-btn value="mainThemeLight">
            <v-icon start :icon="mdiWhiteBalanceSunny" />
            Светлая
          </v-btn>

          <v-btn value="mainThemeDark">
            <v-icon start :icon="mdiWeatherNight" />
            Тёмная
          </v-btn>
        </v-btn-toggle>
      </div>
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
  min-width: 220px;
  backdrop-filter: blur(12px);
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid rgba(0,0,0,0.06);
}

/* HEADER */
.menu-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  margin-bottom: 12px;
}

/* SECTION */
.menu-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.menu-label {
  font-size: 12px;
  color: rgba(0,0,0,0.6);
}

/* TOGGLE */
.theme-toggle .v-btn {
  text-transform: none;
  font-size: 13px;
}
</style>