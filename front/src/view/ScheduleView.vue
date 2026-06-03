<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { isAxiosError } from "axios";
import {
  mdiCalendarToday,
  mdiChevronLeft,
  mdiChevronRight,
  mdiCalendarWeek,
  mdiSchool,
  mdiAlertCircleOutline, mdiTrashCan,
} from "@mdi/js";
import dayjs from "dayjs";
import isoWeek from "dayjs/plugin/isoWeek";
import "dayjs/locale/ru";

import { useSnackbarService } from "@/ui/snackbar/SnackbarService";
import { getGroups } from "@/api/groups.api";
import { getSubjects } from "@/api/subjects.api";
import { getUsers } from "@/api/users.api";
import {
  createScheduleSlot,
  deleteScheduleSlot,
  getScheduleForRange,
  updateScheduleSlot,
} from "@/api/schedule.api";

import type { AdminUserDTO } from "@/model/AdminUserDTO";
import type { GroupDTO } from "@/model/GroupDTO";
import type { SubjectDTO } from "@/model/SubjectDTO";
import type { ScheduleSlotDTO, ScheduleSlotWriteBody } from "@/model/ScheduleSlotDTO";

import WeekDayCard from "@/components/WeekDayCard.vue";
import SlotFormDialog from "@/components/SlotFormDialog.vue";

// ─── Plugins / services ───────────────────────────────────────────────────────

dayjs.extend(isoWeek);
dayjs.locale("ru");

const ROLE_ADMIN   = "ROLE_ADMIN";
const ROLE_TEACHER = "ROLE_SUPERUSER";

const snackbarService = useSnackbarService();

// ─── Reference data ───────────────────────────────────────────────────────────

const groups   = ref<GroupDTO[]>([]);
const subjects = ref<SubjectDTO[]>([]);
const users    = ref<AdminUserDTO[]>([]);

// ─── Schedule data ────────────────────────────────────────────────────────────

const scheduleSlots = ref<ScheduleSlotDTO[]>([]);

// ─── UI state ─────────────────────────────────────────────────────────────────

const selectedGroupId = ref<number | null>(null);
const weekMondayStr   = ref(dayjs().startOf("isoWeek").format("YYYY-MM-DD"));

const loading  = ref(false);
const saving   = ref(false);
const deleting = ref(false);

const isFormOpen   = ref(false);
const isDeleteOpen = ref(false);
const editingSlotId = ref<number | null>(null);
const deletingSlot  = ref<ScheduleSlotDTO | null>(null);

const form = ref({
  date:          "",
  lesson_number: 1 as number,
  subject_id:    null as number | null,
  teacher_id:    null as number | null,
});

// ─── Week helpers ─────────────────────────────────────────────────────────────

const weekStart  = computed(() => dayjs(weekMondayStr.value));
const weekEndStr = computed(() => weekStart.value.add(6, "day").format("YYYY-MM-DD"));

const weekTitle = computed(() => {
  const a = weekStart.value.format("D MMMM");
  const b = weekStart.value.add(6, "day").format("D MMMM YYYY");
  return `${a} — ${b}`;
});

const isCurrentWeek = computed(() =>
    weekMondayStr.value === dayjs().startOf("isoWeek").format("YYYY-MM-DD"),
);

const weekDays = computed(() => {
  const start = dayjs(weekMondayStr.value);
  return Array.from({ length: 7 }, (_, i) => {
    const d = start.add(i, "day");
    return {
      key:       d.format("YYYY-MM-DD"),
      weekday:   d.format("dd"),
      dayNum:    d.format("D"),
      month:     d.format("MMM"),
      fullTitle: d.format("dddd, D MMMM"),
      isToday:   d.isSame(dayjs(), "day"),
    };
  });
});

// ─── Slot grouping ─────────────────────────────────────────────────────────────

const slotDayKey = (raw: ScheduleSlotDTO["date"]): string => {
  if (raw == null) return "";
  if (Array.isArray(raw as unknown[])) {
    const [y, m, d] = raw as unknown as number[];
    return dayjs(
        `${y}-${String(m).padStart(2, "0")}-${String(d).padStart(2, "0")}`,
    ).format("YYYY-MM-DD");
  }
  return dayjs(String(raw).slice(0, 10)).format("YYYY-MM-DD");
};

const slotsByDate = computed(() => {
  const map = new Map<string, ScheduleSlotDTO[]>();
  for (const slot of scheduleSlots.value) {
    const key = slotDayKey(slot.date);
    if (!map.has(key)) map.set(key, []);
    map.get(key)!.push(slot);
  }
  for (const list of map.values()) {
    list.sort((a, b) => a.lesson_number - b.lesson_number);
  }
  return map;
});

const slotsForDay = (dayKey: string) => slotsByDate.value.get(dayKey) ?? [];

const totalSlotsThisWeek = computed(() =>
    weekDays.value.reduce((acc, d) => acc + slotsForDay(d.key).length, 0),
);

// ─── Teachers ─────────────────────────────────────────────────────────────────

const userIsTeacher = (u: AdminUserDTO) =>
    (u.roles ?? []).includes(ROLE_TEACHER) && !(u.roles ?? []).includes(ROLE_ADMIN);

const teacherOptions = computed(() => {
  const sid = form.value.subject_id;
  let list = users.value.filter(userIsTeacher);
  if (sid != null) {
    list = list.filter((u) => (u.subjects ?? []).some((s) => s.id === sid));
  }
  return list.map((u) => ({
    title: `${u.lastname ?? ""} ${u.firstname ?? ""}`.trim() || u.username,
    value: u.id,
  }));
});

// ─── Form validity ────────────────────────────────────────────────────────────

const isFormValid = computed(
    () =>
        !!selectedGroupId.value &&
        !!form.value.date &&
        !!form.value.subject_id &&
        form.value.teacher_id != null &&
        [1, 2, 3, 4, 5, 6, 7, 8].includes(form.value.lesson_number),
);

// ─── Data loading ─────────────────────────────────────────────────────────────

const loadRefs = async () => {
  const [g, s, u] = await Promise.all([getGroups(), getSubjects(), getUsers()]);
  groups.value   = g.data;
  subjects.value = s.data;
  users.value    = u.data.map((x) => ({ ...x, groupId: x.groupId === 0 ? null : x.groupId }));
  if (selectedGroupId.value == null && groups.value.length > 0) {
    selectedGroupId.value = groups.value[0].id;
  }
};

const loadWeek = async () => {
  if (selectedGroupId.value == null) { scheduleSlots.value = []; return; }
  try {
    loading.value = true;
    const { data } = await getScheduleForRange(
        selectedGroupId.value,
        weekMondayStr.value,
        weekEndStr.value,
    );
    scheduleSlots.value = data;
  } catch (error) {
    snackbarService.error(
        isAxiosError(error)
            ? (error.response?.data?.message ?? "Не удалось загрузить расписание")
            : "Не удалось загрузить расписание",
    );
  } finally {
    loading.value = false;
  }
};

// ─── Week navigation ──────────────────────────────────────────────────────────

const goPrevWeek = () => { weekMondayStr.value = dayjs(weekMondayStr.value).subtract(1, "week").format("YYYY-MM-DD"); };
const goNextWeek = () => { weekMondayStr.value = dayjs(weekMondayStr.value).add(1, "week").format("YYYY-MM-DD"); };
const goThisWeek = () => { weekMondayStr.value = dayjs().startOf("isoWeek").format("YYYY-MM-DD"); };

// ─── Form actions ─────────────────────────────────────────────────────────────

const resetForm = (dateStr: string) => {
  editingSlotId.value = null;
  form.value = { date: dateStr, lesson_number: 1, subject_id: subjects.value[0]?.id ?? null, teacher_id: null };
};

const openCreate = (dateKey: string) => { resetForm(dateKey); isFormOpen.value = true; };

const openEdit = (slot: ScheduleSlotDTO) => {
  editingSlotId.value = slot.id;
  form.value = {
    date:          slotDayKey(slot.date),
    lesson_number: slot.lesson_number,
    subject_id:    slot.subject_id,
    teacher_id:    slot.teacher_id,
  };
  isFormOpen.value = true;
};

const closeForm = () => { isFormOpen.value = false; editingSlotId.value = null; };

const buildBody = (): ScheduleSlotWriteBody => ({
  group_id:      selectedGroupId.value!,
  subject_id:    form.value.subject_id!,
  teacher_id:    form.value.teacher_id!,
  date:          form.value.date,
  lesson_number: form.value.lesson_number,
});

const submitForm = async () => {
  if (!isFormValid.value) return;
  try {
    saving.value = true;
    const body = buildBody();
    if (editingSlotId.value != null) {
      await updateScheduleSlot(editingSlotId.value, body);
      snackbarService.success("Занятие обновлено");
    } else {
      await createScheduleSlot(body);
      snackbarService.success("Занятие добавлено");
    }
    closeForm();
    await loadWeek();
  } catch (error) {
    snackbarService.error(
        isAxiosError(error)
            ? (error.response?.data?.message ?? "Не удалось сохранить")
            : "Не удалось сохранить",
    );
  } finally {
    saving.value = false;
  }
};

// ─── Delete actions ───────────────────────────────────────────────────────────

const openDelete  = (slot: ScheduleSlotDTO) => { deletingSlot.value = slot; isDeleteOpen.value = true; };
const closeDelete = () => { isDeleteOpen.value = false; deletingSlot.value = null; };

const confirmDelete = async () => {
  if (!deletingSlot.value) return;
  try {
    deleting.value = true;
    await deleteScheduleSlot(deletingSlot.value.id);
    snackbarService.success("Удалено");
    closeDelete();
    await loadWeek();
  } catch (error) {
    snackbarService.error(
        isAxiosError(error)
            ? (error.response?.data?.message ?? "Не удалось удалить")
            : "Не удалось удалить",
    );
  } finally {
    deleting.value = false;
  }
};

// ─── Watchers ─────────────────────────────────────────────────────────────────

watch([selectedGroupId, weekMondayStr], () => { void loadWeek(); });

watch(
    () => form.value.subject_id,
    () => {
      const opts = teacherOptions.value.map((o) => o.value);
      if (form.value.teacher_id != null && !opts.includes(form.value.teacher_id)) {
        form.value = { ...form.value, teacher_id: null };
      }
    },
);

// ─── Mount ────────────────────────────────────────────────────────────────────

onMounted(async () => {
  try {
    await loadRefs();
  } catch (error) {
    snackbarService.error(
        isAxiosError(error)
            ? (error.response?.data?.message ?? "Ошибка загрузки справочников")
            : "Ошибка загрузки справочников",
    );
  }
});
</script>

<template>
  <div class="sp">

    <!-- ══════════════════════════════════════════════════════════════════════
         TOP BAR
    ══════════════════════════════════════════════════════════════════════════ -->
    <div class="sp__topbar">

      <div class="sp__topbar-left">
        <v-icon :icon="mdiCalendarWeek" size="22" color="primary" class="mr-2 flex-shrink-0" />
        <span class="sp__page-title">Расписание</span>

        <v-divider vertical class="mx-3 sp__divider" />

        <v-select
            v-model="selectedGroupId"
            :items="groups"
            item-title="name"
            item-value="id"
            label="Группа"
            density="compact"
            hide-details
            variant="outlined"
            rounded="pill"
            bg-color="surface"
            class="sp__group-select"
            :prepend-inner-icon="mdiSchool"
        />
      </div>

      <div class="sp__topbar-right">
        <div class="sp__week-nav">
          <v-btn
              :icon="mdiChevronLeft"
              variant="text"
              density="compact"
              rounded="circle"
              size="small"
              @click="goPrevWeek"
          />

          <div class="sp__week-label-wrap">
            <span class="sp__week-label">{{ weekTitle }}</span>
            <v-chip
                v-if="isCurrentWeek"
                color="primary"
                size="x-small"
                variant="tonal"
                class="sp__today-chip"
            >
              Сейчас
            </v-chip>
          </div>

          <v-btn
              :icon="mdiChevronRight"
              variant="text"
              density="compact"
              rounded="circle"
              size="small"
              @click="goNextWeek"
          />
        </div>

        <v-btn
            v-if="!isCurrentWeek"
            :prepend-icon="mdiCalendarToday"
            variant="tonal"
            color="primary"
            density="compact"
            rounded="pill"
            size="small"
            class="sp__thisweek-btn"
            @click="goThisWeek"
        >
          Эта неделя
        </v-btn>
      </div>
    </div>

    <!-- ══════════════════════════════════════════════════════════════════════
         STAT STRIP
    ══════════════════════════════════════════════════════════════════════════ -->
    <div v-if="selectedGroupId && !loading" class="sp__stat-strip">
      <span class="sp__stat-text">
        На этой неделе:
        <strong>{{ totalSlotsThisWeek }}</strong>
        {{ totalSlotsThisWeek === 1 ? 'занятие' : totalSlotsThisWeek < 5 ? 'занятия' : 'занятий' }}
      </span>
    </div>

    <!-- ══════════════════════════════════════════════════════════════════════
         EMPTY STATE
    ══════════════════════════════════════════════════════════════════════════ -->
    <div v-if="!selectedGroupId && groups.length === 0" class="sp__empty-state">
      <v-icon :icon="mdiAlertCircleOutline" size="48" color="warning" class="mb-4" />
      <div class="text-h6 font-weight-medium mb-2">Нет ни одной группы</div>
      <div class="text-body-2 text-medium-emphasis">
        Сначала создайте группу в разделе «Группы», затем вернитесь сюда.
      </div>
    </div>

    <!-- ══════════════════════════════════════════════════════════════════════
         LOADING STATE
    ══════════════════════════════════════════════════════════════════════════ -->
    <div v-else-if="loading" class="sp__loading">
      <v-progress-circular indeterminate color="primary" size="36" width="3" />
      <span class="text-body-2 text-medium-emphasis mt-3">Загружаем расписание…</span>
    </div>

    <!-- ══════════════════════════════════════════════════════════════════════
         WEEK GRID
    ══════════════════════════════════════════════════════════════════════════ -->
    <div v-else-if="selectedGroupId" class="sp__grid-wrap">
      <div class="sp__grid">
        <WeekDayCard
            v-for="day in weekDays"
            :key="day.key"
            :day="day"
            :slots="slotsForDay(day.key)"
            @add="openCreate"
            @edit="openEdit"
            @delete="openDelete"
        />
      </div>
    </div>

    <!-- ══════════════════════════════════════════════════════════════════════
         DIALOGS
    ══════════════════════════════════════════════════════════════════════════ -->

    <SlotFormDialog
        v-model="isFormOpen"
        v-model:form="form"
        :editing-slot-id="editingSlotId"
        :subjects="subjects"
        :teacher-options="teacherOptions"
        :saving="saving"
        :is-form-valid="isFormValid"
        :subject-selected="form.subject_id != null"
        @submit="submitForm"
        @close="closeForm"
    />

    <v-dialog v-model="isDeleteOpen" max-width="380" :persistent="deleting">
      <v-card rounded="xl" elevation="8">

        <div class="sp__del-header">
          <div class="sp__del-icon-wrap">
            <v-icon :icon="mdiTrashCan" size="22" color="error" />
          </div>
          <div>
            <div class="text-subtitle-1 font-weight-bold">Удалить занятие?</div>
            <div v-if="deletingSlot" class="text-body-2 text-medium-emphasis mt-0.5">
              {{ slotDayKey(deletingSlot.date) }} · урок {{ deletingSlot.lesson_number }}
              · {{ deletingSlot.subject_name || "предмет" }}
            </div>
          </div>
        </div>

        <v-card-actions class="px-5 pb-5 pt-2">
          <v-spacer />
          <v-btn variant="text" rounded="lg" :disabled="deleting" @click="closeDelete">
            Отмена
          </v-btn>
          <v-btn
              color="error"
              variant="flat"
              rounded="lg"
              :loading="deleting"
              min-width="100"
              @click="confirmDelete"
          >
            Удалить
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<style scoped>
/* ── Root ───────────────────────────────────────────────────────────────── */
.sp {
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

/* ── Top bar ────────────────────────────────────────────────────────────── */
.sp__topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 16px;
  background: rgb(var(--v-theme-surface));
  border-bottom: 1px solid rgba(var(--v-border-color), var(--v-border-opacity));
}

.sp__topbar-left {
  display: flex;
  align-items: center;
  gap: 0;
  min-width: 0;
  flex: 1 1 auto;
}

.sp__page-title {
  font-size: 1rem;
  font-weight: 600;
  white-space: nowrap;
}

.sp__divider {
  opacity: 0.25;
}

.sp__group-select {
  max-width: 220px;
  min-width: 140px;
}

.sp__topbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.sp__week-nav {
  display: flex;
  align-items: center;
  gap: 4px;
}

.sp__week-label-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 6px;
}

.sp__week-label {
  font-size: 0.875rem;
  font-weight: 500;
  white-space: nowrap;
}

.sp__today-chip {
  font-size: 0.68rem;
}

.sp__thisweek-btn {
  white-space: nowrap;
}

/* ── Stat strip ─────────────────────────────────────────────────────────── */
.sp__stat-strip {
  padding: 6px 20px;
  border-bottom: 1px solid rgba(var(--v-border-color), var(--v-border-opacity));
}

.sp__stat-text {
  font-size: 0.8rem;
}

/* ── Empty state ────────────────────────────────────────────────────────── */
.sp__empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 80px 32px;
  flex: 1;
}

/* ── Loading ────────────────────────────────────────────────────────────── */
.sp__loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  flex: 1;
}

/* ── Grid ───────────────────────────────────────────────────────────────── */
.sp__grid-wrap {
  padding: 16px;
  flex: 1;
}

.sp__grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 10px;
}

/* ── Delete dialog header ────────────────────────────────────────────────── */
.sp__del-header {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 20px 20px 12px;
}

.sp__del-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: rgba(var(--v-theme-error), 0.1);
  flex-shrink: 0;
}

/* ── Mobile ──────────────────────────────────────────────────────────────── */
@media (max-width: 599px) {
  .sp__topbar {
    flex-direction: column;
    align-items: stretch;
  }

  .sp__topbar-left {
    flex-wrap: wrap;
    gap: 8px;
  }

  .sp__group-select {
    max-width: 100%;
    width: 100%;
  }

  .sp__topbar-right {
    justify-content: space-between;
  }

  .sp__week-label {
    font-size: 0.8rem;
  }

  .sp__grid {
    grid-template-columns: 1fr;
  }
}

/* ── Tablet ──────────────────────────────────────────────────────────────── */
@media (min-width: 600px) and (max-width: 959px) {
  .sp__grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
</style>