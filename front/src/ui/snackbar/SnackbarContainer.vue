<template>
  <Teleport to="body">
    <transition name="ios-spring">
      <div
        v-if="isActive"
        class="ios-snackbar-wrapper"
        :style="{ zIndex: 9999 }"
      >
        <div class="ios-snackbar-pill">
          <div v-if="currentMessage?.icon" class="ios-snackbar-icon-wrapper">
            <i :class="currentMessage.icon" class="ios-snackbar-icon"></i>
          </div>

          <span class="ios-snackbar-text">
            {{ currentMessage?.text }}
          </span>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup lang="ts">
import { watch, onUnmounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useSnackbarStore } from '@/components/snackbar/useSnackbarStore' // Путь к твоему стору

const store = useSnackbarStore()
const { isActive, currentMessage } = storeToRefs(store)
const { close } = store

let timer: ReturnType<typeof setTimeout> | null = null

// Логика автозакрытия и очистки таймеров
watch(isActive, (val) => {
  if (timer) clearTimeout(timer)

  if (val) {
    const timeout = currentMessage.value?.timeout || 3000
    timer = setTimeout(() => {
      close()
    }, timeout)
  }
})

// Очистка при уничтожении компонента
onUnmounted(() => {
  if (timer) clearTimeout(timer)
})
</script>

<style scoped>
/* ========================================
   WRAPPER & POSITIONING
========================================
*/
.ios-snackbar-wrapper {
  position: fixed;
  left: 0;
  right: 0;
  /* Отступ снизу с учетом Safe Area на iPhone */
  bottom: calc(24px + env(safe-area-inset-bottom));
  display: flex;
  justify-content: center;
  pointer-events: none; /* Пропускаем клики вокруг */
}

/* ========================================
   THE PILL (iOS 18 Style)
========================================
*/
.ios-snackbar-pill {
  /* Магия Vuetify темы:
    Используем 'on-surface' для фона (обычно черный на белом / белый на черном),
    что создает высокий контраст, как в iOS.
  */
  background: rgba(var(--v-theme-on-surface), 0.92);

  /* Цвет текста - обратный фону (цвет поверхности) */
  color: rgb(var(--v-theme-surface));

  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);

  padding: 14px 24px;
  border-radius: 50px; /* Полная капсула */

  display: flex;
  align-items: center;
  gap: 12px;

  max-width: calc(100vw - 32px);
  min-width: min-content;

  /* Глубокая, мягкая тень iOS */
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.12),
    0 4px 8px rgba(0, 0, 0, 0.08);

  pointer-events: auto; /* Сама плашка кликабельна (если нужно) */

  /* Hardware acceleration for smooth animation */
  will-change: transform, opacity;
  transform: translateZ(0);
}

/* ========================================
   CONTENT STYLES
========================================
*/
.ios-snackbar-text {
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  font-size: 15px;
  font-weight: 500;
  line-height: 1.3;
  letter-spacing: -0.01em;
  text-align: center;
}

.ios-snackbar-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
}

.ios-snackbar-icon {
  font-size: 20px;
  /* Иконка наследует цвет текста (цвет surface) */
  color: inherit;
}

/* ========================================
   SPRING ANIMATION (The "Cool" Part)
========================================
*/
.ios-spring-enter-active {
  /* Эффект пружины (bouncy) при появлении */
  transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.ios-spring-leave-active {
  /* Более быстрое и плавное исчезновение */
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.ios-spring-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.9);
}

.ios-spring-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.95);
}

/* Dark Mode Tweak (опционально)
   Если в темной теме плашка слишком яркая (чисто белая),
   можно сделать её темно-серой, как нативный iOS toast
*/
@media (prefers-color-scheme: dark) {
  /* Можно переопределить через класс vuetify .v-theme--dark, если он есть на body */
}
</style>
