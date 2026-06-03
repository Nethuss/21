<script setup lang="ts">
import { computed } from "vue";
import { mdiDeleteOutline, mdiPencilOutline, mdiPlus } from "@mdi/js";
import type { ScheduleSlotDTO } from "@/model/ScheduleSlotDTO";

// ─── Props ────────────────────────────────────────────────────────────────────

interface DayMeta {
  key: string;
  weekday: string;
  dayNum: string;
  month: string;
  fullTitle: string;
  isToday: boolean;
}

interface Props {
  day: DayMeta;
  slots: ScheduleSlotDTO[];
}

const props = defineProps<Props>();

// ─── Emits ────────────────────────────────────────────────────────────────────

const emit = defineEmits<{
  (e: "add", dayKey: string): void;
  (e: "edit", slot: ScheduleSlotDTO): void;
  (e: "delete", slot: ScheduleSlotDTO): void;
}>();

// ─── Helpers ──────────────────────────────────────────────────────────────────

function teacherLine(slot: ScheduleSlotDTO): string {
  const fio = `${slot.teacher_lastname ?? ""} ${slot.teacher_firstname ?? ""}`.trim();
  return fio || slot.teacher_username || "—";
}

const hasSlots = computed(() => props.slots.length > 0);
</script>

<template>
  <v-card
      class="week-day-card d-flex flex-column rounded-xl"
      :class="{ 'week-day-card--today': day.isToday }"
      elevation="0"
      border
  >
    <!-- ── Day header ──────────────────────────────────────────────── -->
    <div
        class="week-day-card__header d-flex align-center justify-space-between px-4 py-3 rounded-t-xl"
        :class="day.isToday ? 'bg-primary' : 'bg-surface-variant'"
    >
      <div>
        <div
            class="text-caption text-uppercase font-weight-medium"
            :class="day.isToday ? 'text-on-primary' : 'text-medium-emphasis'"
            style="letter-spacing: 0.06em"
        >
          {{ day.weekday }}
        </div>
        <div
            class="text-h6 font-weight-bold lh-1 mt-0"
            :class="day.isToday ? 'text-on-primary' : 'text-high-emphasis'"
        >
          {{ day.dayNum }}
          <span
              class="text-body-2 font-weight-regular"
              :class="day.isToday ? 'text-on-primary opacity-80' : 'text-medium-emphasis'"
          >
            {{ day.month }}
          </span>
        </div>
      </div>

      <!-- Бейдж с количеством уроков -->
      <v-chip
          v-if="hasSlots"
          :color="day.isToday ? 'on-primary' : 'primary'"
          :variant="day.isToday ? 'outlined' : 'tonal'"
          size="small"
          class="font-weight-medium"
      >
        {{ slots.length }}
      </v-chip>
    </div>

    <!-- ── Slots list ──────────────────────────────────────────────── -->
    <div class="week-day-card__body d-flex flex-column ga-2 pa-3 flex-grow-1">
      <template v-if="hasSlots">
        <div
            v-for="slot in slots"
            :key="slot.id"
            class="slot-item d-flex align-start ga-2 pa-3 rounded-lg"
        >
          <!-- Lesson number pill -->
          <div class="slot-item__num d-flex align-center justify-center rounded-md flex-shrink-0">
            <span class="text-caption font-weight-bold">{{ slot.lesson_number }}</span>
          </div>

          <!-- Content -->
          <div class="min-w-0 flex-grow-1">
            <div
                class="text-body-2 font-weight-semibold text-truncate text-high-emphasis"
                :title="slot.subject_name"
            >
              {{ slot.subject_name || "Предмет" }}
            </div>
            <div class="text-caption text-medium-emphasis text-truncate mt-0.5" :title="teacherLine(slot)">
              {{ teacherLine(slot) }}
            </div>
          </div>

          <!-- Actions -->
          <div class="d-flex flex-shrink-0 ga-0 ml-auto">
            <v-btn
                :icon="mdiPencilOutline"
                size="x-small"
                variant="text"
                color="primary"
                density="compact"
                rounded="md"
                @click.stop="emit('edit', slot)"
            />
            <v-btn
                :icon="mdiDeleteOutline"
                size="x-small"
                variant="text"
                color="error"
                density="compact"
                rounded="md"
                @click.stop="emit('delete', slot)"
            />
          </div>
        </div>
      </template>

      <!-- Empty state -->
      <div
          v-else
          class="d-flex flex-column align-center justify-center text-center flex-grow-1 py-4 empty-state"
      >
        <div class="text-caption text-medium-emphasis">Нет занятий</div>
      </div>
    </div>

    <!-- ── Add button ──────────────────────────────────────────────── -->
    <div class="px-3 pb-3">
      <v-btn
          block
          variant="tonal"
          color="primary"
          rounded="lg"
          size="small"
          :prepend-icon="mdiPlus"
          @click="emit('add', day.key)"
      >
        Добавить
      </v-btn>
    </div>
  </v-card>
</template>

<style scoped>
/* ── Card shell ────────────────────────────────────────────────────────────── */
.week-day-card {
  min-width: 180px;
  background: rgb(var(--v-theme-surface));
  transition: box-shadow 0.2s ease;
}

.week-day-card:hover {
  box-shadow: 0 4px 20px rgba(var(--v-theme-on-surface), 0.08) !important;
}

.week-day-card--today {
  outline: 2px solid rgb(var(--v-theme-primary));
  outline-offset: 0px;
}

/* ── Slot item ─────────────────────────────────────────────────────────────── */
.slot-item {
  background: rgb(var(--v-theme-surface-variant), 0.5);
  border: 1px solid rgba(var(--v-theme-outline), 0.12);
  transition: background 0.15s ease;
}

.slot-item:hover {
  background: rgba(var(--v-theme-primary), 0.06);
  border-color: rgba(var(--v-theme-primary), 0.25);
}

.slot-item__num {
  width: 26px;
  height: 26px;
  background: rgba(var(--v-theme-primary), 0.12);
  color: rgb(var(--v-theme-primary));
  min-width: 26px;
  font-size: 0.72rem;
}

/* ── Empty state ───────────────────────────────────────────────────────────── */
.empty-state {
  min-height: 80px;
}

/* ── Misc ──────────────────────────────────────────────────────────────────── */
.lh-1 {
  line-height: 1.2;
}

.opacity-80 {
  opacity: 0.8;
}
</style>