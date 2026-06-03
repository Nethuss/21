<script setup lang="ts">
import { computed, watch } from "vue";
import { mdiClose } from "@mdi/js";
import { Rules } from "@/model/Rules";
import type { AdminUserDTO } from "@/model/AdminUserDTO";
import type { SubjectDTO } from "@/model/SubjectDTO";

// ─── Props ────────────────────────────────────────────────────────────────────

interface Props {
  modelValue: boolean;
  editingSlotId: number | null;
  subjects: SubjectDTO[];
  teacherOptions: { title: string; value: number }[];
  saving: boolean;
  isFormValid: boolean;
  /** Управляется снаружи через v-model:form */
  form: {
    date: string;
    lesson_number: number;
    subject_id: number | null;
    teacher_id: number | null;
  };
  subjectSelected: boolean;
}

const props = defineProps<Props>();

// ─── Emits ────────────────────────────────────────────────────────────────────

const emit = defineEmits<{
  (e: "update:modelValue", v: boolean): void;
  (e: "update:form", v: Props["form"]): void;
  (e: "submit"): void;
  (e: "close"): void;
}>();

// ─── Helpers ──────────────────────────────────────────────────────────────────

const LESSON_NUMBERS = [1, 2, 3, 4, 5, 6, 7, 8];

const isOpen = computed({
  get: () => props.modelValue,
  set: (v) => emit("update:modelValue", v),
});

// Patch отдельных полей формы без мутации пропса
function patchForm(patch: Partial<Props["form"]>) {
  emit("update:form", { ...props.form, ...patch });
}

const dialogTitle = computed(() =>
    props.editingSlotId != null ? "Редактировать занятие" : "Новое занятие",
);

const teacherHint = computed(() =>
    props.subjectSelected
        ? "Показаны учителя по выбранному предмету"
        : "Сначала выберите предмет",
);

function close() {
  emit("close");
}

function submit() {
  if (props.isFormValid) emit("submit");
}
</script>

<template>
  <v-dialog v-model="isOpen" max-width="500" scrollable :persistent="saving">
    <v-card rounded="xl" class="slot-dialog">
      <!-- ── Header ─────────────────────────────────────────────────── -->
      <div class="slot-dialog__header d-flex align-center justify-space-between px-6 pt-5 pb-2">
        <div>
          <div class="text-h6 font-weight-bold">{{ dialogTitle }}</div>
          <div v-if="editingSlotId == null" class="text-body-2 text-medium-emphasis mt-0.5">
            Заполните параметры занятия
          </div>
        </div>
        <v-btn
            :icon="mdiClose"
            variant="text"
            density="comfortable"
            rounded="lg"
            :disabled="saving"
            @click="close"
        />
      </div>

      <v-divider class="mx-6" />

      <!-- ── Form body ──────────────────────────────────────────────── -->
      <v-card-text class="px-6 pt-5 pb-2">
        <div class="d-flex flex-column ga-4">
          <!-- Дата + Номер урока — в ряд на широком экране -->
          <div class="d-flex flex-column flex-sm-row ga-4">
            <v-text-field
                :model-value="form.date"
                type="date"
                label="Дата"
                variant="outlined"
                rounded="lg"
                density="comfortable"
                hide-details="auto"
                :rules="[Rules.required()]"
                class="flex-grow-1"
                @update:model-value="patchForm({ date: $event as string })"
            />
            <v-select
                :model-value="form.lesson_number"
                :items="LESSON_NUMBERS"
                label="№ урока"
                variant="outlined"
                rounded="lg"
                density="comfortable"
                hide-details="auto"
                style="max-width: 140px; min-width: 120px"
                @update:model-value="patchForm({ lesson_number: $event as number })"
            />
          </div>

          <!-- Предмет -->
          <v-select
              :model-value="form.subject_id"
              :items="subjects"
              item-title="name"
              item-value="id"
              label="Предмет"
              variant="outlined"
              rounded="lg"
              density="comfortable"
              hide-details="auto"
              :rules="[Rules.required()]"
              @update:model-value="patchForm({ subject_id: $event as number })"
          />

          <!-- Учитель -->
          <v-select
              :model-value="form.teacher_id"
              :items="teacherOptions"
              item-title="title"
              item-value="value"
              label="Учитель"
              variant="outlined"
              rounded="lg"
              density="comfortable"
              :hint="teacherHint"
              persistent-hint
              :rules="[Rules.required()]"
              :disabled="!subjectSelected"
              @update:model-value="patchForm({ teacher_id: $event as number })"
          />
        </div>
      </v-card-text>

      <!-- ── Actions ────────────────────────────────────────────────── -->
      <v-card-actions class="px-6 pb-6 pt-4">
        <v-spacer />
        <v-btn variant="text" rounded="lg" :disabled="saving" @click="close">
          Отмена
        </v-btn>
        <v-btn
            color="primary"
            variant="flat"
            rounded="lg"
            :loading="saving"
            :disabled="!isFormValid"
            min-width="120"
            @click="submit"
        >
          {{ editingSlotId != null ? "Сохранить" : "Добавить" }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<style scoped>
.slot-dialog {
  /* Inherit surface color from Vuetify theme automatically */
  background: rgb(var(--v-theme-surface));
}

.slot-dialog__header {
  background: rgb(var(--v-theme-surface));
}
</style>