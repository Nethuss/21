<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { isAxiosError } from "axios";
import { mdiDeleteOutline, mdiPencilOutline, mdiPlus } from "@mdi/js";
import type { GroupDTO } from "@/model/GroupDTO";
import { Rules } from "@/model/Rules";
import { useSnackbarService } from "@/ui/snackbar/SnackbarService";
import { createGroup, deleteGroup, getGroups, updateGroup } from "@/api/groups.api";

type GroupForm = {
  name: string;
};

const snackbarService = useSnackbarService();

const loading = ref(false);
const saving = ref(false);
const deleting = ref(false);

const groups = ref<GroupDTO[]>([]);

const isFormDialogOpen = ref(false);
const isDeleteDialogOpen = ref(false);
const editingGroup = ref<GroupDTO | null>(null);
const deletingGroup = ref<GroupDTO | null>(null);

const form = ref<GroupForm>({
  name: "",
});

const isFormValid = computed(() => !!form.value.name.trim());
const formTitle = computed(() => (editingGroup.value ? "Редактировать группу" : "Добавить группу"));

const resetForm = () => {
  form.value = { name: "" };
  editingGroup.value = null;
};

const loadGroups = async () => {
  try {
    loading.value = true;
    const response = await getGroups();
    groups.value = response.data;
  } catch (error) {
    if (isAxiosError(error)) {
      snackbarService.error(error.response?.data?.message || "Не удалось загрузить список групп");
      return;
    }
    snackbarService.error("Не удалось загрузить список групп");
  } finally {
    loading.value = false;
  }
};

const openCreateDialog = () => {
  resetForm();
  isFormDialogOpen.value = true;
};

const openEditDialog = (group: GroupDTO) => {
  editingGroup.value = group;
  form.value = { name: group.name };
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

    const payload = {
      name: form.value.name.trim(),
    };

    if (editingGroup.value) {
      await updateGroup(editingGroup.value.id, payload);
      snackbarService.success("Группа обновлена");
    } else {
      await createGroup(payload);
      snackbarService.success("Группа создана");
    }

    closeFormDialog();
    await loadGroups();
  } catch (error) {
    if (isAxiosError(error)) {
      snackbarService.error(error.response?.data?.message || "Не удалось сохранить группу");
      return;
    }
    snackbarService.error("Не удалось сохранить группу");
  } finally {
    saving.value = false;
  }
};

const openDeleteDialog = (group: GroupDTO) => {
  deletingGroup.value = group;
  isDeleteDialogOpen.value = true;
};

const closeDeleteDialog = () => {
  isDeleteDialogOpen.value = false;
  deletingGroup.value = null;
};

const confirmDelete = async () => {
  if (!deletingGroup.value) return;

  try {
    deleting.value = true;
    await deleteGroup(deletingGroup.value.id);
    snackbarService.success("Группа удалена");
    closeDeleteDialog();
    await loadGroups();
  } catch (error) {
    if (isAxiosError(error)) {
      snackbarService.error(
        error.response?.data?.message ||
          "Не удалось удалить группу. Возможно, она уже используется в системе.",
      );
      return;
    }
    snackbarService.error("Не удалось удалить группу");
  } finally {
    deleting.value = false;
  }
};

onMounted(async () => {
  await loadGroups();
});
</script>

<template>
  <div>
    <div class="d-flex align-center justify-space-between mb-6">
      <div>
        <div class="text-h5 font-weight-bold">Группы</div>
        <div class="text-body-2 text-medium-emphasis">Управление списком учебных групп</div>
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
          <tr v-else-if="groups.length === 0">
            <td colspan="3" class="text-center py-6 text-medium-emphasis">Список групп пуст</td>
          </tr>
          <template v-else>
            <tr v-for="group in groups" :key="group.id">
              <td>{{ group.id }}</td>
              <td>{{ group.name }}</td>
              <td class="text-right">
                <v-btn
                  variant="text"
                  size="small"
                  color="primary"
                  :icon="mdiPencilOutline"
                  @click="openEditDialog(group)"
                />
                <v-btn
                  variant="text"
                  size="small"
                  color="error"
                  :icon="mdiDeleteOutline"
                  @click="openDeleteDialog(group)"
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
              label="Название группы"
              placeholder="Например: 10А"
              :rules="[Rules.required(), Rules.maxLength(128)]"
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
        <v-card-title class="text-h6 font-weight-bold pt-6 px-6">Удалить группу</v-card-title>
        <v-card-text class="px-6 pt-2">
          Вы действительно хотите удалить группу <b>{{ deletingGroup?.name }}</b
          >?
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
