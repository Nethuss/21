<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { isAxiosError } from "axios";
import { mdiDeleteOutline, mdiPencilOutline, mdiPlus } from "@mdi/js";
import type { SubjectDTO } from "@/model/SubjectDTO";
import { Rules } from "@/model/Rules";
import { useSnackbarService } from "@/ui/snackbar/SnackbarService";
import { createSubject, deleteSubject, getSubjects, updateSubject } from "@/api/subjects.api";

type SubjectForm = { name: string };

const snackbarService = useSnackbarService();

const loading = ref(false);
const saving = ref(false);
const deleting = ref(false);

const subjects = ref<SubjectDTO[]>([]);

const isFormDialogOpen = ref(false);
const isDeleteDialogOpen = ref(false);
const editingSubject = ref<SubjectDTO | null>(null);
const deletingSubject = ref<SubjectDTO | null>(null);

const form = ref<SubjectForm>({ name: "" });

const isFormValid = computed(() => !!form.value.name.trim());
const formTitle = computed(() =>
  editingSubject.value ? "Редактировать предмет" : "Добавить предмет",
);

const resetForm = () => {
  form.value = { name: "" };
  editingSubject.value = null;
};

const loadSubjects = async () => {
  try {
    loading.value = true;
    const response = await getSubjects();
    subjects.value = response.data;
  } catch (error) {
    if (isAxiosError(error)) {
      snackbarService.error(error.response?.data?.message || "Не удалось загрузить предметы");
      return;
    }
    snackbarService.error("Не удалось загрузить предметы");
  } finally {
    loading.value = false;
  }
};

const openCreateDialog = () => {
  resetForm();
  isFormDialogOpen.value = true;
};

const openEditDialog = (subject: SubjectDTO) => {
  editingSubject.value = subject;
  form.value = { name: subject.name };
  isFormDialogOpen.value = true;
};

const closeFormDialog = () => {
  isFormDialogOpen.value = false;
  resetForm();
};

const submitForm = async () => {
  if (!isFormValid.value) return;

  try {
    saving.value = true;
    const payload = { name: form.value.name.trim() };

    if (editingSubject.value) {
      await updateSubject(editingSubject.value.id, payload);
      snackbarService.success("Предмет обновлён");
    } else {
      await createSubject(payload);
      snackbarService.success("Предмет создан");
    }

    closeFormDialog();
    await loadSubjects();
  } catch (error) {
    if (isAxiosError(error)) {
      snackbarService.error(error.response?.data?.message || "Не удалось сохранить предмет");
      return;
    }
    snackbarService.error("Не удалось сохранить предмет");
  } finally {
    saving.value = false;
  }
};

const openDeleteDialog = (subject: SubjectDTO) => {
  deletingSubject.value = subject;
  isDeleteDialogOpen.value = true;
};

const closeDeleteDialog = () => {
  isDeleteDialogOpen.value = false;
  deletingSubject.value = null;
};

const confirmDelete = async () => {
  if (!deletingSubject.value) return;

  try {
    deleting.value = true;
    await deleteSubject(deletingSubject.value.id);
    snackbarService.success("Предмет удалён");
    closeDeleteDialog();
    await loadSubjects();
  } catch (error) {
    if (isAxiosError(error)) {
      snackbarService.error(
        error.response?.data?.message ||
          "Не удалось удалить предмет. Возможно, он привязан к расписанию или учителям.",
      );
      return;
    }
    snackbarService.error("Не удалось удалить предмет");
  } finally {
    deleting.value = false;
  }
};

onMounted(async () => {
  await loadSubjects();
});
</script>

<template>
  <div>
    <div class="d-flex align-center justify-space-between mb-6">
      <div>
        <div class="text-h5 font-weight-bold">Предметы</div>
        <div class="text-body-2 text-medium-emphasis">Справочник предметов (связь с учителями — в карточке пользователя)</div>
      </div>

      <v-btn color="primary" rounded="lg" :icon="mdiPlus" @click="openCreateDialog">

      </v-btn>
    </div>

    <v-card rounded="lg" elevation="1">
      <v-table>
        <thead>
          <tr>
            <th class="text-left">ID</th>
            <th class="text-left">Название</th>
            <th class="text-right">Действия</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="3" class="text-center py-6">
              <v-progress-circular indeterminate color="primary" size="24" />
            </td>
          </tr>
          <tr v-else-if="subjects.length === 0">
            <td colspan="3" class="text-center py-6 text-medium-emphasis">Список предметов пуст</td>
          </tr>
          <template v-else>
            <tr v-for="subject in subjects" :key="subject.id">
              <td>{{ subject.id }}</td>
              <td>{{ subject.name }}</td>
              <td class="text-right">
                <v-btn
                  variant="text"
                  size="small"
                  color="primary"
                  :icon="mdiPencilOutline"
                  @click="openEditDialog(subject)"
                />
                <v-btn
                  variant="text"
                  size="small"
                  color="error"
                  :icon="mdiDeleteOutline"
                  @click="openDeleteDialog(subject)"
                />
              </td>
            </tr>
          </template>
        </tbody>
      </v-table>
    </v-card>

    <v-dialog v-model="isFormDialogOpen" max-width="500">
      <v-card rounded="lg">
        <v-card-title class="text-h6 font-weight-bold pt-6 px-6">
          {{ formTitle }}
        </v-card-title>

        <v-card-text class="px-6 pt-2">
          <v-form @submit.prevent="submitForm">
            <v-text-field
              v-model="form.name"
              label="Название предмета"
              placeholder="Например: Математика"
              :rules="[Rules.required(), Rules.maxLength(255)]"
              required
            />
          </v-form>
        </v-card-text>

        <v-card-actions class="px-6 pb-6">
          <v-spacer />
          <v-btn variant="text" @click="closeFormDialog">Отмена</v-btn>
          <v-btn color="primary" :loading="saving" :disabled="!isFormValid" @click="submitForm">
            Сохранить
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="isDeleteDialogOpen" max-width="420">
      <v-card rounded="lg">
        <v-card-title class="text-h6 font-weight-bold pt-6 px-6">Удалить предмет</v-card-title>
        <v-card-text class="px-6 pt-2">
          Удалить предмет <b>{{ deletingSubject?.name }}</b>?
        </v-card-text>
        <v-card-actions class="px-6 pb-6">
          <v-spacer />
          <v-btn variant="text" @click="closeDeleteDialog">Отмена</v-btn>
          <v-btn color="error" :loading="deleting" @click="confirmDelete">Удалить</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>
