<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { isAxiosError } from "axios";
import {
  mdiCalendarToday,
  mdiChevronLeft,
  mdiChevronRight,
  mdiDeleteOutline,
  mdiPencilOutline,
  mdiPlus,
} from "@mdi/js";
import dayjs from "dayjs";
import isoWeek from "dayjs/plugin/isoWeek";
import "dayjs/locale/ru";
import { Rules } from "@/model/Rules";
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

dayjs.extend(isoWeek);
dayjs.locale("ru");

const ROLE_ADMIN = "ROLE_ADMIN";
const ROLE_TEACHER = "ROLE_SUPERUSER";
const LESSON_NUMBERS = [1, 2, 3, 4, 5, 6, 7, 8];

const snackbarService = useSnackbarService();

const groups = ref<GroupDTO[]>([]);
const subjects = ref<SubjectDTO[]>([]);
const users = ref<AdminUserDTO[]>([]);
const scheduleSlots = ref<ScheduleSlotDTO[]>([]);

const selectedGroupId = ref<number | null>(null);
/** Понедельник выбранной недели (YYYY-MM-DD). */
const weekMondayStr = ref(dayjs().startOf("isoWeek").format("YYYY-MM-DD"));

const loading = ref(false);
const saving = ref(false);
const deleting = ref(false);

const isFormOpen = ref(false);
const isDeleteOpen = ref(false);
const editingSlotId = ref<number | null>(null);
const deletingSlot = ref<ScheduleSlotDTO | null>(null);

const form = ref({
  date: "",
  lesson_number: 1 as number,
  subject_id: null as number | null,
  teacher_id: null as number | null,
});

const weekStart = computed(() => dayjs(weekMondayStr.value));
const weekEndStr = computed(() => weekStart.value.add(6, "day").format("YYYY-MM-DD"));
const weekTitle = computed(() => {
  const a = weekStart.value.format("D MMMM");
  const b = weekStart.value.add(6, "day").format("D MMMM YYYY");
  return `${a} — ${b}`;
});

const weekDays = computed(() => {
  const start = dayjs(weekMondayStr.value);
  return Array.from({ length: 7 }, (_, i) => {
    const d = start.add(i, "day");
    return {
      key: d.format("YYYY-MM-DD"),
      weekday: d.format("dd"),
      dayNum: d.format("D"),
      month: d.format("MMM"),
      fullTitle: d.format("dddd, D MMMM"),
      isToday: d.isSame(dayjs(), "day"),
    };
  });
});

const slotDayKey = (raw: ScheduleSlotDTO["date"]): string => {
  if (raw == null) return "";
  if (Array.isArray(raw as unknown[])) {
    const arr = raw as unknown as number[];
    const [y, m, d] = arr;
    return dayjs(`${y}-${String(m).padStart(2, "0")}-${String(d).padStart(2, "0")}`).format("YYYY-MM-DD");
  }
  const s = String(raw);
  return dayjs(s.slice(0, 10)).format("YYYY-MM-DD");
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

const userIsTeacher = (u: AdminUserDTO) =>
  (u.roles || []).includes(ROLE_TEACHER) && !(u.roles || []).includes(ROLE_ADMIN);

const teacherCandidates = computed(() => users.value.filter(userIsTeacher));

const teacherOptions = computed(() => {
  const sid = form.value.subject_id;
  let list = teacherCandidates.value;
  if (sid != null) {
    list = list.filter((u) => (u.subjects || []).some((s) => s.id === sid));
  }
  return list.map((u) => ({
    title: `${u.lastname || ""} ${u.firstname || ""}`.trim() || u.username,
    value: u.id,
  }));
});

const teacherLine = (slot: ScheduleSlotDTO) => {
  const fio = `${slot.teacher_lastname || ""} ${slot.teacher_firstname || ""}`.trim();
  return fio || slot.teacher_username || "—";
};

const isFormValid = computed(
  () =>
    !!selectedGroupId.value &&
    !!form.value.date &&
    !!form.value.subject_id &&
    form.value.teacher_id != null &&
    LESSON_NUMBERS.includes(form.value.lesson_number),
);

const loadRefs = async () => {
  const [g, s, u] = await Promise.all([getGroups(), getSubjects(), getUsers()]);
  groups.value = g.data;
  subjects.value = s.data;
  users.value = u.data.map((x) => ({
    ...x,
    groupId: x.groupId === 0 ? null : x.groupId,
  }));
  if (selectedGroupId.value == null && groups.value.length > 0) {
    selectedGroupId.value = groups.value[0].id;
  }
};

const loadWeek = async () => {
  if (selectedGroupId.value == null) {
    scheduleSlots.value = [];
    return;
  }
  try {
    loading.value = true;
    const { data } = await getScheduleForRange(
      selectedGroupId.value,
      weekMondayStr.value,
      weekEndStr.value,
    );
    scheduleSlots.value = data;
  } catch (error) {
    if (isAxiosError(error)) {
      snackbarService.error(error.response?.data?.message || "Не удалось загрузить расписание");
      return;
    }
    snackbarService.error("Не удалось загрузить расписание");
  } finally {
    loading.value = false;
  }
};

const goPrevWeek = () => {
  weekMondayStr.value = dayjs(weekMondayStr.value).subtract(1, "week").format("YYYY-MM-DD");
};

const goNextWeek = () => {
  weekMondayStr.value = dayjs(weekMondayStr.value).add(1, "week").format("YYYY-MM-DD");
};

const goThisWeek = () => {
  weekMondayStr.value = dayjs().startOf("isoWeek").format("YYYY-MM-DD");
};

const resetForm = (dateStr: string) => {
  editingSlotId.value = null;
  form.value = {
    date: dateStr,
    lesson_number: 1,
    subject_id: subjects.value[0]?.id ?? null,
    teacher_id: null,
  };
};

const openCreate = (dateKey: string) => {
  resetForm(dateKey);
  isFormOpen.value = true;
};

const openEdit = (slot: ScheduleSlotDTO) => {
  editingSlotId.value = slot.id;
  form.value = {
    date: slotDayKey(slot.date),
    lesson_number: slot.lesson_number,
    subject_id: slot.subject_id,
    teacher_id: slot.teacher_id,
  };
  isFormOpen.value = true;
};

const closeForm = () => {
  isFormOpen.value = false;
  editingSlotId.value = null;
};

const buildBody = (): ScheduleSlotWriteBody => ({
  group_id: selectedGroupId.value!,
  subject_id: form.value.subject_id!,
  teacher_id: form.value.teacher_id!,
  date: form.value.date,
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
    if (isAxiosError(error)) {
      snackbarService.error(error.response?.data?.message || "Не удалось сохранить");
      return;
    }
    snackbarService.error("Не удалось сохранить");
  } finally {
    saving.value = false;
  }
};

const openDelete = (slot: ScheduleSlotDTO) => {
  deletingSlot.value = slot;
  isDeleteOpen.value = true;
};

const closeDelete = () => {
  isDeleteOpen.value = false;
  deletingSlot.value = null;
};

const confirmDelete = async () => {
  if (!deletingSlot.value) return;
  try {
    deleting.value = true;
    await deleteScheduleSlot(deletingSlot.value.id);
    snackbarService.success("Удалено");
    closeDelete();
    await loadWeek();
  } catch (error) {
    if (isAxiosError(error)) {
      snackbarService.error(error.response?.data?.message || "Не удалось удалить");
      return;
    }
    snackbarService.error("Не удалось удалить");
  } finally {
    deleting.value = false;
  }
};

watch([selectedGroupId, weekMondayStr], () => {
  void loadWeek();
});

watch(
  () => form.value.subject_id,
  () => {
    const opts = teacherOptions.value.map((o) => o.value);
    if (form.value.teacher_id != null && !opts.includes(form.value.teacher_id)) {
      form.value.teacher_id = null;
    }
  },
);

onMounted(async () => {
  try {
    await loadRefs();
  } catch (error) {
    if (isAxiosError(error)) {
      snackbarService.error(error.response?.data?.message || "Ошибка загрузки справочников");
    }
  }
});
</script>

<template>
  <div class="schedule-page">
    <div class="d-flex flex-column flex-md-row align-md-center justify-space-between gap-4 mb-6">
      <div>
        <div class="text-h5 font-weight-bold">Расписание</div>
        <div class="text-body-2 text-medium-emphasis">Неделя, группа, уроки — добавление и правка в пару кликов</div>
      </div>

      <div class="d-flex flex-wrap align-center ga-3">
        <v-select
          v-model="selectedGroupId"
          :items="groups"
          item-title="name"
          item-value="id"
          label="Группа"
          density="comfortable"
          hide-details
          min-width="220"
          max-width="320"
          variant="outlined"
          rounded="lg"
          bg-color="surface"
        />
      </div>
    </div>

    <v-alert v-if="!selectedGroupId && groups.length === 0" type="warning" variant="tonal" rounded="lg" class="mb-6">
      Сначала создайте хотя бы одну группу в разделе «Группы».
    </v-alert>

    <template v-else-if="selectedGroupId">
      <v-card class="mb-6 pa-4 rounded-lg" elevation="1">
        <div class="d-flex flex-column flex-sm-row align-center justify-space-between ga-4">
          <div class="d-flex align-center ga-2">
            <v-btn :icon="mdiChevronLeft" variant="tonal" rounded="lg" @click="goPrevWeek" />
            <v-btn :icon="mdiChevronRight" variant="tonal" rounded="lg" @click="goNextWeek" />
            <div class="text-subtitle-1 font-weight-medium text-center text-sm-start px-2">
              {{ weekTitle }}
            </div>
          </div>
          <v-btn
            color="primary"
            variant="tonal"
            rounded="lg"
            :prepend-icon="mdiCalendarToday"
            @click="goThisWeek"
          >
            Эта неделя
          </v-btn>
        </div>
      </v-card>

      <div v-if="loading" class="text-center py-12">
        <v-progress-circular indeterminate color="primary" size="40" />
      </div>

      <div v-else class="schedule-week-scroll d-flex ga-3 pb-2">
        <v-card
          v-for="day in weekDays"
          :key="day.key"
          min-width="200"
          max-width="280"
          class="schedule-day flex-grow-1 rounded-lg"
          :class="{ 'schedule-day--today': day.isToday }"
          elevation="1"
        >
          <v-card-title class="py-3 px-4 d-flex flex-column align-start border-b bg-surface-variant text-surface-variant">
            <span class="text-caption text-uppercase opacity-90">{{ day.weekday }}</span>
            <span class="text-h6 font-weight-bold text-high-emphasis">
              {{ day.dayNum }}
              <span class="text-body-2 font-weight-regular text-medium-emphasis">{{ day.month }}</span>
            </span>
          </v-card-title>

          <v-card-text class="pa-3 d-flex flex-column ga-2" style="min-height: 200px">
            <template v-if="slotsForDay(day.key).length">
              <v-sheet
                v-for="slot in slotsForDay(day.key)"
                :key="slot.id"
                class="pa-3 rounded-lg border schedule-slot-sheet"
                border
              >
                <div class="d-flex align-start justify-space-between ga-2">
                  <div class="min-w-0">
                    <div class="text-caption text-medium-emphasis">Урок {{ slot.lesson_number }}</div>
                    <div class="text-body-1 font-weight-medium text-truncate">
                      {{ slot.subject_name || "Предмет" }}
                    </div>
                    <div class="text-body-2 text-medium-emphasis text-truncate">
                      {{ teacherLine(slot) }}
                    </div>
                  </div>
                  <div class="d-flex flex-shrink-0">
                    <v-btn size="x-small" variant="text" color="primary" :icon="mdiPencilOutline" @click="openEdit(slot)" />
                    <v-btn size="x-small" variant="text" color="error" :icon="mdiDeleteOutline" @click="openDelete(slot)" />
                  </div>
                </div>
              </v-sheet>
            </template>
            <div v-else class="text-body-2 text-medium-emphasis py-2">Нет занятий</div>

            <v-btn
              block
              variant="tonal"
              color="primary"
              rounded="lg"
              class="mt-auto"
              :prepend-icon="mdiPlus"
              @click="openCreate(day.key)"
            >
              Добавить
            </v-btn>
          </v-card-text>
        </v-card>
      </div>
    </template>

    <v-dialog v-model="isFormOpen" max-width="520" scrollable>
      <v-card rounded="lg">
        <v-card-title class="text-h6 font-weight-bold pt-6 px-6">
          {{ editingSlotId != null ? "Редактировать занятие" : "Новое занятие" }}
        </v-card-title>
        <v-card-text class="px-6 pt-4">
          <v-text-field
            v-model="form.date"
            type="date"
            label="Дата"
            variant="outlined"
            rounded="lg"
            density="comfortable"
            :rules="[Rules.required()]"
          />
          <v-select
            v-model="form.lesson_number"
            :items="LESSON_NUMBERS"
            label="Номер урока"
            variant="outlined"
            rounded="lg"
            class="mt-3"
            density="comfortable"
          />
          <v-select
            v-model="form.subject_id"
            :items="subjects"
            item-title="name"
            item-value="id"
            label="Предмет"
            variant="outlined"
            rounded="lg"
            class="mt-3"
            density="comfortable"
            :rules="[Rules.required()]"
          />
          <v-select
            v-model="form.teacher_id"
            :items="teacherOptions"
            item-title="title"
            item-value="value"
            label="Учитель"
            variant="outlined"
            rounded="lg"
            class="mt-3"
            density="comfortable"
            :rules="[Rules.required()]"
            :hint="form.subject_id ? 'Список по предмету (teacher_subjects)' : 'Сначала выберите предмет'"
            persistent-hint
          />
        </v-card-text>
        <v-card-actions class="px-6 pb-6">
          <v-spacer />
          <v-btn variant="text" @click="closeForm">Отмена</v-btn>
          <v-btn color="primary" :loading="saving" :disabled="!isFormValid" @click="submitForm">Сохранить</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="isDeleteOpen" max-width="420">
      <v-card rounded="lg">
        <v-card-title class="text-h6 font-weight-bold pt-6 px-6">Удалить занятие?</v-card-title>
        <v-card-text v-if="deletingSlot" class="px-6 pt-2">
          {{ slotDayKey(deletingSlot.date) }}, урок {{ deletingSlot.lesson_number }} —
          {{ deletingSlot.subject_name || "предмет" }}
        </v-card-text>
        <v-card-actions class="px-6 pb-6">
          <v-spacer />
          <v-btn variant="text" @click="closeDelete">Отмена</v-btn>
          <v-btn color="error" :loading="deleting" @click="confirmDelete">Удалить</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<style scoped>
.schedule-week-scroll {
  overflow-x: auto;
  padding-bottom: 4px;
}

.schedule-day--today {
  outline: 2px solid rgb(var(--v-theme-primary));
  outline-offset: 2px;
}

.schedule-slot-sheet {
  background: rgba(var(--v-theme-surface-variant), 0.35);
}
</style>
