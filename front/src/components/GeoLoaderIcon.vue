<template>
  <div
    class="geo-icon"
    :class="{ 'geo-icon--inline': inline }"
    :style="{ color: resolvedColor, width: size, height: size }"
  >
    <svg viewBox="0 0 24 24" width="100%" height="100%" fill="none">
      <defs>
        <!-- мягкое свечение -->
        <radialGradient id="glow">
          <stop offset="0%" stop-color="currentColor" stop-opacity="0.6" />
          <stop offset="60%" stop-color="currentColor" stop-opacity="0.15" />
          <stop offset="100%" stop-color="currentColor" stop-opacity="0" />
        </radialGradient>

        <!-- размытие -->
        <filter id="blur">
          <feGaussianBlur stdDeviation="1.2" />
        </filter>

        <!-- лёгкий шум -->
        <filter id="noise">
          <feTurbulence type="fractalNoise" baseFrequency="0.8" numOctaves="2" />
          <feColorMatrix type="saturate" values="0" />
          <feComponentTransfer>
            <feFuncA type="linear" slope="0.03" />
          </feComponentTransfer>
        </filter>
      </defs>

      <!-- ЦЕНТР (свет) -->
      <circle cx="12" cy="12" r="3" fill="url(#glow)" class="core-glow" />

      <!-- ВНУТРЕННЕЕ КОЛЬЦО -->
      <circle
        cx="12"
        cy="12"
        r="4.5"
        stroke="currentColor"
        stroke-opacity="0.15"
        filter="url(#blur)"
        class="ring ring-1"
      />

      <!-- ВНЕШНЕЕ КОЛЬЦО -->
      <circle
        cx="12"
        cy="12"
        r="7"
        stroke="currentColor"
        stroke-opacity="0.08"
        filter="url(#blur)"
        class="ring ring-2"
      />

      <!-- ОРБИТАЛЬНЫЕ ЧАСТИЦЫ -->
      <g class="particles">
        <circle class="particle p1" cx="12" cy="5" r="0.6" fill="currentColor" />
        <circle class="particle p2" cx="19" cy="12" r="0.5" fill="currentColor" />
        <circle class="particle p3" cx="12" cy="19" r="0.4" fill="currentColor" />
      </g>

      <!-- ЛЁГКИЙ АМБИЕНТНЫЙ ФЛЭШ -->
      <circle
        cx="12"
        cy="12"
        r="9"
        fill="url(#glow)"
        class="ambient-flash"
      />

      <!-- ШУМ -->
      <rect width="24" height="24" filter="url(#noise)" opacity="0.4" />
    </svg>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useTheme } from "vuetify";

const props = defineProps<{
  color?: string;
  inline?: boolean;
  size?: string | number;
}>();

const theme = useTheme();

const resolvedColor = computed(() => {
  if (!props.color) return "currentColor";
  const themeColor = theme.current.value.colors[props.color];
  return themeColor || props.color;
});
</script>

<style scoped>
.geo-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;

  /* 💎 делает её “дороже” внутри UI */
  opacity: 0.9;
  transform: translateZ(0); /* фикс рендера */
}

.geo-icon--inline {
  vertical-align: middle;
  margin-left: 0.5rem;
}

/* 🌫️ Центральное дыхание */
.core-glow {
  animation: glowPulse 4s ease-in-out infinite;
}

/* Кольца */
.ring-1 {
  animation: drift 6s ease-in-out infinite;
}

.ring-2 {
  animation: drift 9s ease-in-out infinite reverse;
}

/* 🌀 Орбитальная система */
.particles {
  transform-origin: 12px 12px;
  animation: orbitSlow 18s linear infinite;
}

.particle {
  opacity: 0;
  filter: blur(0.3px);
}

.p1 {
  animation: particleFade 6s ease-in-out infinite;
}

.p2 {
  animation: particleFade 7s ease-in-out infinite 1.5s;
}

.p3 {
  animation: particleFade 8s ease-in-out infinite 3s;
}

/* ✨ Амбиентный всплеск */
.ambient-flash {
  opacity: 0;
  animation: flash 12s ease-in-out infinite;
}

/* --- АНИМАЦИИ --- */

@keyframes glowPulse {
  0%, 100% {
    opacity: 0.7;
    transform: scale(0.95);
  }
  50% {
    opacity: 1;
    transform: scale(1.05);
  }
}

@keyframes drift {
  0%, 100% {
    stroke-opacity: 0.08;
    transform: scale(0.98);
  }
  50% {
    stroke-opacity: 0.18;
    transform: scale(1.02);
  }
}

@keyframes orbitSlow {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes particleFade {
  0%, 100% {
    opacity: 0;
    transform: scale(0.6);
  }
  40% {
    opacity: 0.8;
    transform: scale(1);
  }
  60% {
    opacity: 0.4;
  }
}

@keyframes flash {
  0%, 85%, 100% {
    opacity: 0;
  }
  90% {
    opacity: 0.15;
  }
}
</style>
