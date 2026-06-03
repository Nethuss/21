<script setup>
import { ref, watch, onMounted, computed, nextTick } from "vue";
import { useTeacherJournalStore } from "@/store/useTeacherJournalStore.ts";
import { mdiBookOpenPageVariant, mdiMagnify, mdiTableLarge, mdiCalendarWeek, mdiClose } from "@mdi/js";
import dayjs from "dayjs";
import isoWeek from "dayjs/plugin/isoWeek";
import "dayjs/locale/ru";

dayjs.extend(isoWeek);
dayjs.locale("ru");

const store = useTeacherJournalStore();

// ─── Filters ──────────────────────────────────────────────────────────────────
const selectedGroupId   = ref(null);
const selectedSubjectId = ref(null);
const dateFrom          = ref("");
const dateTo            = ref("");

// ─── Grade menu ───────────────────────────────────────────────────────────────
const menuOpen      = ref(false);
const menuAnchor    = ref(null);
const activeStudent = ref(null);
const activeLesson  = ref(null);

const GRADE_OPTIONS = [
  { value: 5,  label: "5", color: "success" },
  { value: 4,  label: "4", color: "info"    },
  { value: 3,  label: "3", color: "warning" },
  { value: 2,  label: "2", color: "error"   },
  { value: -1, label: "Н", color: "default" },
  { value: -2, label: "Б", color: "default" },
];

// ─── Snackbar ─────────────────────────────────────────────────────────────────
const snack = ref({ show: false, text: "", color: "success" });

function notify(text, color = "success") {
  snack.value = { show: true, text, color };
}

// ─── Computed ─────────────────────────────────────────────────────────────────

const canLoad = computed(
    () => selectedGroupId.value && selectedSubjectId.value && dateFrom.value && dateTo.value,
);

const hasJournal = computed(() => store.lessons.length > 0);

// ─── Helpers ──────────────────────────────────────────────────────────────────

function formatDate(dateStr) {
  if (!dateStr) return "";
  const [, m, d] = dateStr.split("-");
  return `${d}.${m}`;
}

function gradeForCell(student, lessonId) {
  if (!student) return null;
  const raw = student.grades?.[String(lessonId)];
  return raw !== undefined ? raw : null;
}

// Числовое значение для расчёта среднего (Н/Б не считаем)
function gradeNumericValue(raw) {
  if (raw === null || raw === -1 || raw === -2) return null;
  return raw;
}

// Отображаемая строка оценки
function gradeLabel(value) {
  if (value === -1) return "Н";
  if (value === -2) return "Б";
  return String(value);
}

function gradeColor(value) {
  if (value === 5)  return "success";
  if (value === 4)  return "info";
  if (value === 3)  return "warning";
  if (value === 2)  return "error";
  return "default"; // Н, Б
}

// Средний балл по студенту (только числовые оценки 2-5)
function studentAverage(student) {
  const numeric = store.lessons
      .map((l) => gradeNumericValue(gradeForCell(student, l.id)))
      .filter((v) => v !== null);
  if (numeric.length === 0) return null;
  const avg = numeric.reduce((a, b) => a + b, 0) / numeric.length;
  return avg.toFixed(2);
}

function averageColor(avg) {
  if (avg === null) return "default";
  const n = parseFloat(avg);
  if (n >= 4.5) return "success";
  if (n >= 3.5) return "info";
  if (n >= 2.5) return "warning";
  return "error";
}

// ─── Current week shortcut ────────────────────────────────────────────────────

function setCurrentWeek() {
  dateFrom.value = dayjs().startOf("isoWeek").format("YYYY-MM-DD");
  dateTo.value   = dayjs().endOf("isoWeek").format("YYYY-MM-DD");
}

// ─── Actions ──────────────────────────────────────────────────────────────────

watch(selectedGroupId, (id) => {
  selectedSubjectId.value = null;
  if (id) store.loadSubjects(id);
});

async function loadJournal() {
  if (!canLoad.value) return;
  try {
    await store.loadJournal(
        selectedGroupId.value,
        selectedSubjectId.value,
        dateFrom.value,
        dateTo.value,
    );
  } catch {
    notify("Не удалось загрузить журнал", "error");
  }
}

// Фикс: сбрасываем anchor перед следующим тиком чтобы v-menu
// всегда воспринимал новый activator как изменение
async function openGradeMenu(event, student, lesson) {
  // Закрываем если было открыто
  menuOpen.value = false;
  menuAnchor.value = null;
  await nextTick();

  menuAnchor.value   = event.currentTarget;
  activeStudent.value = student;
  activeLesson.value  = lesson;
  await nextTick();
  menuOpen.value = true;
}

async function pickGrade(gradeValue) {
  menuOpen.value = false;
  const studentSnapshot = activeStudent.value;
  const lessonSnapshot  = activeLesson.value;
  try {
    await store.submitGrade(studentSnapshot.id, lessonSnapshot.id, gradeValue);
    notify("Оценка сохранена");
  } catch {
    notify("Не удалось сохранить оценку", "error");
  }
}

async function removeGrade() {
  menuOpen.value = false;
  const studentSnapshot = activeStudent.value;
  const lessonSnapshot  = activeLesson.value;
  try {
    await store.removeGrade(studentSnapshot.id, lessonSnapshot.id);
    notify("Оценка удалена");
  } catch {
    notify("Не удалось удалить оценку", "error");
  }
}

const activeGradeValue = computed(() => {
  if (!activeStudent.value || !activeLesson.value) return null;
  return gradeForCell(activeStudent.value, activeLesson.value.id);
});

// ─── Mount ────────────────────────────────────────────────────────────────────

onMounted(async () => {
  try {
    await store.loadGroups();
  } catch {
    notify("Не удалось загрузить группы", "error");
  }
});
</script>

<template>
  <div class="tj">

    <!-- ══════════════════════════════════════════════════════════
         HEADER
    ══════════════════════════════════════════════════════════ -->
    <div class="tj__header">
      <v-icon :icon="mdiBookOpenPageVariant" size="22" color="primary" class="mr-2" />
      <span class="tj__title">Журнал</span>
    </div>

    <!-- ══════════════════════════════════════════════════════════
         FILTERS CARD
    ══════════════════════════════════════════════════════════ -->
    <v-card class="tj__filters" rounded="xl" elevation="0" border>
      <div class="tj__filters-row">

        <v-select
            v-model="selectedGroupId"
            :items="store.groups"
            item-title="name"
            item-value="id"
            label="Группа"
            density="compact"
            variant="outlined"
            rounded="lg"
            hide-details
            :loading="store.loadingGroups"
            class="tj__filter-field"
        />

        <v-select
            v-model="selectedSubjectId"
            :items="store.subjects"
            item-title="name"
            item-value="id"
            label="Предмет"
            density="compact"
            variant="outlined"
            rounded="lg"
            hide-details
            :disabled="!selectedGroupId"
            :loading="store.loadingSubjects"
            class="tj__filter-field"
        />

        <!-- Дата от + Дата до + кнопка текущей недели -->
        <div class="tj__date-group">
          <v-text-field
              v-model="dateFrom"
              type="date"
              label="Дата от"
              density="compact"
              variant="outlined"
              rounded="lg"
              hide-details
              class="tj__filter-field tj__filter-field--date"
          />
          <span class="tj__date-sep">—</span>
          <v-text-field
              v-model="dateTo"
              type="date"
              label="Дата до"
              density="compact"
              variant="outlined"
              rounded="lg"
              hide-details
              class="tj__filter-field tj__filter-field--date"
          />
          <v-btn
              variant="tonal"
              color="primary"
              rounded="lg"
              density="compact"
              size="small"
              :prepend-icon="mdiCalendarWeek"
              class="tj__curweek-btn"
              @click="setCurrentWeek"
          >
            Эта неделя
          </v-btn>
        </div>

        <v-btn
            color="primary"
            rounded="lg"
            :disabled="!canLoad"
            :loading="store.loadingJournal"
            :prepend-icon="mdiMagnify"
            @click="loadJournal"
        >
          Показать
        </v-btn>

      </div>
    </v-card>

    <!-- ══════════════════════════════════════════════════════════
         LOADING
    ══════════════════════════════════════════════════════════ -->
    <div v-if="store.loadingJournal" class="tj__state">
      <v-progress-circular indeterminate color="primary" size="36" width="3" />
      <span class="text-body-2 text-medium-emphasis mt-3">Загружаем журнал…</span>
    </div>

    <!-- ══════════════════════════════════════════════════════════
         EMPTY STATE
    ══════════════════════════════════════════════════════════ -->
    <div v-else-if="!hasJournal" class="tj__state">
      <v-icon :icon="mdiTableLarge" size="48" class="mb-3 tj__empty-icon" />
      <span class="text-body-1 font-weight-medium mb-1">Журнал пуст</span>
      <span class="text-body-2 text-medium-emphasis">
        Выберите группу, предмет и период — нажмите «Показать»
      </span>
    </div>

    <!-- ══════════════════════════════════════════════════════════
         JOURNAL TABLE
    ══════════════════════════════════════════════════════════ -->
    <v-card v-else class="tj__table-card" rounded="xl" elevation="0" border>
      <div class="tj__table-wrap">
        <table class="tj__table">

          <!-- THEAD -->
          <thead>
          <tr>
            <th class="tj__th tj__th--student">Ученик</th>
            <th
                v-for="lesson in store.lessons"
                :key="lesson.id"
                class="tj__th tj__th--lesson"
            >
              <div class="tj__lesson-date">{{ formatDate(lesson.date) }}</div>
              <div class="tj__lesson-num">№{{ lesson.lesson_number }}</div>
            </th>
            <th class="tj__th tj__th--avg">Средний<br>балл</th>
          </tr>
          </thead>

          <!-- TBODY -->
          <tbody>
          <tr
              v-for="student in store.students"
              :key="student.id"
              class="tj__row"
          >
            <!-- Student name — sticky left -->
            <td class="tj__td tj__td--student">
              <span class="tj__student-name">{{ student.name }}</span>
            </td>

            <!-- Grade cells -->
            <td
                v-for="lesson in store.lessons"
                :key="lesson.id"
                class="tj__td tj__td--grade"
                @click="openGradeMenu($event, student, lesson)"
            >
              <v-chip
                  v-if="gradeForCell(student, lesson.id) !== null"
                  :color="gradeColor(gradeForCell(student, lesson.id))"
                  size="small"
                  variant="tonal"
                  class="tj__grade-chip font-weight-bold"
              >
                {{ gradeLabel(gradeForCell(student, lesson.id)) }}
              </v-chip>
              <span v-else class="tj__add-hint">+</span>
            </td>

            <!-- Average — sticky right -->
            <td class="tj__td tj__td--avg">
              <v-chip
                  v-if="studentAverage(student) !== null"
                  :color="averageColor(studentAverage(student))"
                  size="small"
                  variant="flat"
                  class="tj__avg-chip font-weight-bold"
              >
                {{ studentAverage(student) }}
              </v-chip>
              <span v-else class="text-medium-emphasis text-caption">—</span>
            </td>
          </tr>
          </tbody>

        </table>
      </div>
    </v-card>

    <!-- ══════════════════════════════════════════════════════════
         GRADE MENU
    ══════════════════════════════════════════════════════════ -->
    <v-menu
        v-model="menuOpen"
        :activator="menuAnchor"
        location="bottom center"
        :close-on-content-click="false"
        offset="6"
    >
      <v-card rounded="xl" elevation="8" width="220" class="tj__grade-menu">

        <!-- Menu header -->
        <div class="tj__menu-header">
          <div>
            <div class="text-caption text-medium-emphasis font-weight-medium" style="text-transform:uppercase;letter-spacing:.05em">
              Оценка
            </div>
            <div class="text-body-2 font-weight-semibold mt-0.5" style="max-width:140px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">
              {{ activeStudent?.name }}
            </div>
          </div>
          <v-btn
              :icon="mdiClose"
              size="x-small"
              variant="text"
              density="compact"
              rounded="md"
              @click="menuOpen = false"
          />
        </div>

        <v-divider />

        <!-- Grade buttons -->
        <div class="tj__grade-options pa-3">
          <button
              v-for="opt in GRADE_OPTIONS"
              :key="opt.value"
              class="tj__grade-btn"
              :class="[
              `tj__grade-btn--${opt.color}`,
              { 'tj__grade-btn--active': activeGradeValue === opt.value }
            ]"
              :disabled="store.savingGrade"
              @click="pickGrade(opt.value)"
          >
            {{ opt.label }}
          </button>
        </div>

        <v-divider class="mx-3" />

        <!-- Delete -->
        <div class="pa-2">
          <v-btn
              block
              variant="text"
              color="error"
              size="small"
              rounded="lg"
              :disabled="activeGradeValue === null || store.savingGrade"
              :loading="store.savingGrade"
              @click="removeGrade"
          >
            Удалить оценку
          </v-btn>
        </div>

      </v-card>
    </v-menu>

    <!-- ══════════════════════════════════════════════════════════
         SNACKBAR
    ══════════════════════════════════════════════════════════ -->
    <v-snackbar
        v-model="snack.show"
        :color="snack.color"
        location="bottom right"
        :timeout="2500"
        rounded="lg"
    >
      {{ snack.text }}
    </v-snackbar>

  </div>
</template>

<style scoped>
/* ── Root ───────────────────────────────────────────────────────────────── */
.tj {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  min-height: 100%;
}

/* ── Header ─────────────────────────────────────────────────────────────── */
.tj__header {
  display: flex;
  align-items: center;
}

.tj__title {
  font-size: 1.1rem;
  font-weight: 700;
}

/* ── Filters ─────────────────────────────────────────────────────────────── */
.tj__filters {
  padding: 12px 16px;
}

.tj__filters-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.tj__filter-field {
  flex: 1 1 160px;
  max-width: 240px;
}

.tj__filter-field--date {
  max-width: 160px;
}

.tj__date-group {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
}

.tj__date-sep {
  font-size: 0.9rem;
  color: rgba(var(--v-theme-on-surface), 0.4);
  flex-shrink: 0;
}

.tj__curweek-btn {
  white-space: nowrap;
  flex-shrink: 0;
}

/* ── State ───────────────────────────────────────────────────────────────── */
.tj__state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 16px;
  flex: 1;
}

.tj__empty-icon {
  opacity: 0.2;
}

/* ── Table card ──────────────────────────────────────────────────────────── */
.tj__table-card {
  overflow: hidden;
}

.tj__table-wrap {
  overflow-x: auto;
  overflow-y: auto;
  max-height: calc(100vh - 260px);
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
  scrollbar-color: rgba(var(--v-border-color), 0.3) transparent;
}

.tj__table-wrap::-webkit-scrollbar       { width: 6px; height: 6px; }
.tj__table-wrap::-webkit-scrollbar-track { background: transparent; }
.tj__table-wrap::-webkit-scrollbar-thumb { background: rgba(var(--v-border-color), 0.3); border-radius: 4px; }

/* ── Table ───────────────────────────────────────────────────────────────── */
.tj__table {
  width: 100%;
  border-collapse: collapse;
  table-layout: auto;
}

/* ── Headers ─────────────────────────────────────────────────────────────── */
.tj__th {
  position: sticky;
  top: 0;
  z-index: 3;
  background: rgb(var(--v-theme-surface-variant));
  padding: 10px 12px;
  white-space: nowrap;
  border-bottom: 2px solid rgba(var(--v-border-color), var(--v-border-opacity));
  text-align: center;
  font-size: 0.75rem;
  font-weight: 600;
}

.tj__th--student {
  text-align: left;
  position: sticky;
  left: 0;
  z-index: 4;
  min-width: 190px;
  padding-left: 16px;
}

.tj__th--lesson {
  min-width: 62px;
}

.tj__th--avg {
  position: sticky;
  right: 0;
  z-index: 4;
  min-width: 72px;
  background: rgb(var(--v-theme-surface-variant));
  border-left: 2px solid rgba(var(--v-border-color), var(--v-border-opacity));
  line-height: 1.3;
}

.tj__lesson-date {
  font-weight: 700;
  font-size: 0.8rem;
}

.tj__lesson-num {
  font-weight: 400;
  color: rgba(var(--v-theme-on-surface), 0.5);
  font-size: 0.7rem;
}

/* ── Rows ────────────────────────────────────────────────────────────────── */
.tj__row:hover .tj__td {
  background: rgba(var(--v-theme-primary), 0.04);
}

.tj__row:hover .tj__td--student {
  background: rgba(var(--v-theme-primary), 0.07);
}

.tj__row:hover .tj__td--avg {
  background: rgba(var(--v-theme-primary), 0.04);
}

/* ── Cells ───────────────────────────────────────────────────────────────── */
.tj__td {
  padding: 6px 12px;
  border-bottom: 1px solid rgba(var(--v-border-color), var(--v-border-opacity));
  text-align: center;
  vertical-align: middle;
  transition: background 0.12s ease;
}

.tj__td--student {
  position: sticky;
  left: 0;
  z-index: 2;
  background: rgb(var(--v-theme-surface));
  text-align: left;
  padding-left: 16px;
  border-right: 1px solid rgba(var(--v-border-color), var(--v-border-opacity));
}

.tj__td--grade {
  cursor: pointer;
  min-width: 62px;
}

.tj__td--avg {
  position: sticky;
  right: 0;
  z-index: 2;
  background: rgb(var(--v-theme-surface));
  border-left: 2px solid rgba(var(--v-border-color), var(--v-border-opacity));
  min-width: 72px;
}

/* ── Student name ─────────────────────────────────────────────────────────── */
.tj__student-name {
  font-size: 0.875rem;
  font-weight: 500;
  white-space: nowrap;
}

/* ── Grade chip ──────────────────────────────────────────────────────────── */
.tj__grade-chip {
  font-size: 0.8rem;
  min-width: 28px;
  justify-content: center;
}

/* ── Average chip ────────────────────────────────────────────────────────── */
.tj__avg-chip {
  font-size: 0.78rem;
  min-width: 52px;
  justify-content: center;
}

/* ── Add hint ────────────────────────────────────────────────────────────── */
.tj__add-hint {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  border-radius: 6px;
  font-size: 1rem;
  color: transparent;
  transition: color 0.15s ease, background 0.15s ease;
}

.tj__td--grade:hover .tj__add-hint {
  color: rgba(var(--v-theme-on-surface), 0.4);
  background: rgba(var(--v-theme-on-surface), 0.06);
}

/* ── Grade menu ──────────────────────────────────────────────────────────── */
.tj__menu-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 12px 12px 10px 16px;
}

.tj__grade-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
}

/* Custom grade buttons — не используем v-btn чтобы избежать цветовых
   коллизий с Vuetify при значении "default" */
.tj__grade-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 700;
  cursor: pointer;
  border: 2px solid transparent;
  transition: transform 0.1s ease, box-shadow 0.1s ease, border-color 0.1s ease;
  outline: none;
}

.tj__grade-btn:hover {
  transform: scale(1.06);
  box-shadow: 0 2px 8px rgba(0,0,0,.15);
}

.tj__grade-btn:active {
  transform: scale(0.97);
}

.tj__grade-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.tj__grade-btn--active {
  border-color: currentColor !important;
  box-shadow: 0 0 0 2px rgba(var(--v-theme-primary), 0.3);
}

.tj__grade-btn--success {
  background: rgba(var(--v-theme-success), 0.15);
  color: rgb(var(--v-theme-success));
}

.tj__grade-btn--info {
  background: rgba(var(--v-theme-info), 0.15);
  color: rgb(var(--v-theme-info));
}

.tj__grade-btn--warning {
  background: rgba(var(--v-theme-warning), 0.15);
  color: rgb(var(--v-theme-warning));
}

.tj__grade-btn--error {
  background: rgba(var(--v-theme-error), 0.15);
  color: rgb(var(--v-theme-error));
}

.tj__grade-btn--default {
  background: rgba(var(--v-theme-on-surface), 0.08);
  color: rgba(var(--v-theme-on-surface), 0.7);
}

/* ── Mobile ──────────────────────────────────────────────────────────────── */
@media (max-width: 599px) {
  .tj__filters-row {
    flex-direction: column;
    align-items: stretch;
  }

  .tj__filter-field,
  .tj__filter-field--date {
    max-width: 100%;
  }

  .tj__date-group {
    flex-wrap: wrap;
  }

  .tj__table-wrap {
    max-height: calc(100vh - 340px);
  }
}
</style>