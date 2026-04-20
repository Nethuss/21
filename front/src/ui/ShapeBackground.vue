<template>
  <div class="glass-background">
    <svg
      viewBox="0 0 1440 900"
      preserveAspectRatio="xMidYMid slice"
      xmlns="http://www.w3.org/2000/svg"
    >
      <!-- BASE -->
      <rect width="1440" height="900" :fill="themeColors.base" />

      <!-- AIRLIGHT LAYER -->
      <rect width="1440" height="900" fill="url(#airlight)" opacity="0.9" />

      <!-- DEPTH SEPARATION -->
      <rect y="400" width="1440" height="500" fill="url(#depth)" opacity="0.85" />

      <!-- MICRO GRAIN -->
      <rect width="1440" height="900" filter="url(#grain)" opacity="0.08" />

      <defs>
        <!-- AIRLIGHT -->
        <radialGradient id="airlight" cx="50%" cy="20%" r="80%">
          <stop offset="0%" :stop-color="themeColors.accent1" stop-opacity="0.35" />
          <stop offset="60%" :stop-color="themeColors.base" stop-opacity="0.15" />
          <stop offset="100%" :stop-color="themeColors.base" stop-opacity="0" />
        </radialGradient>

        <!-- DEPTH -->
        <linearGradient id="depth" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" :stop-color="themeColors.accent2" stop-opacity="0.12" />
          <stop offset="100%" :stop-color="themeColors.base" stop-opacity="0" />
        </linearGradient>

        <!-- GRAIN -->
        <filter id="grain">
          <feTurbulence type="fractalNoise" baseFrequency="0.35" numOctaves="1" />
          <feColorMatrix
            type="matrix"
            values="
              0 0 0 0 0
              0 0 0 0 0
              0 0 0 0 0
              0 0 0 0.02 0
            "
          />
        </filter>
      </defs>
    </svg>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = defineProps({
  isDark: { type: Boolean, default: false },
});

const themeColors = computed(() =>
  props.isDark
    ? {
        base: "#0B0E14",
        accent1: "#1E2636",
        accent2: "#141A24",
      }
    : {
        base: "#F4F6F8",
        accent1: "#E1E6ED",
        accent2: "#E8EDF3",
      },
);
</script>

<style scoped>
.glass-background {
  position: fixed;
  inset: 0;
  background: v-bind("themeColors.base");
}

svg {
  width: 100%;
  height: 100%;
  shape-rendering: geometricPrecision;
}
</style>
