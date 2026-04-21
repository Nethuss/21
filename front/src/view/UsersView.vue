<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { isAxiosError } from "axios";
import { mdiClose, mdiDeleteOutline, mdiEyeOutline, mdiPencilOutline, mdiPlus } from "@mdi/js";
import { Rules } from "@/model/Rules";
import { useSnackbarService } from "@/ui/snackbar/SnackbarService";
import { createUser, deleteUser, getUserById, getUsers, updateUser } from "@/api/users.api";
import { getGroups } from "@/api/groups.api";
import { getSubjects } from "@/api/subjects.api";
import type { AdminUserDTO } from "@/model/AdminUserDTO";
import type { GroupDTO } from "@/model/GroupDTO";
import type { SubjectDTO } from "@/model/SubjectDTO";

const ROLE_ADMIN = "ROLE_ADMIN";
const ROLE_TEACHER = "ROLE_SUPERUSER";
const ROLE_STUDENT = "ROLE_USER";

const ROLE_OPTIONS = [
  { title: "Администратор", value: ROLE_ADMIN },
  { title: "Учитель", value: ROLE_TEACHER },
  { title: "Ученик", value: ROLE_STUDENT },
];

type UserForm = {
  username: string;
  password: string;
  firstname: string;
  middlename: string;
  lastname: string;
  isActive: boolean;
  roles: string[];
  groupId: number | null;
  subjectIds: number[];
};

const snackbarService = useSnackbarService();

const users = ref<AdminUserDTO[]>([]);
const groups = ref<GroupDTO[]>([]);
const subjects = ref<SubjectDTO[]>([]);

const loading = ref(false);
const saving = ref(false);
const deleting = ref(false);

const isFormDialogOpen = ref(false);
const isDeleteDialogOpen = ref(false);
const isViewDialogOpen = ref(false);
const editingUser = ref<AdminUserDTO | null>(null);
const deletingUser = ref<AdminUserDTO | null>(null);
const viewingUser = ref<AdminUserDTO | null>(null);
const viewLoading = ref(false);

const form = ref<UserForm>({
  username: "",
  password: "",
  firstname: "",
  middlename: "",
  lastname: "",
  isActive: true,
  roles: [ROLE_STUDENT],
  groupId: null,
  subjectIds: [],
});

const isEditMode = computed(() => !!editingUser.value);
const formTitle = computed(() => (isEditMode.value ? "Редактировать пользователя" : "Добавить пользователя"));

const rolesIncludeAdmin = computed(() => form.value.roles.includes(ROLE_ADMIN));
const rolesIncludeTeacher = computed(() => form.value.roles.includes(ROLE_TEACHER));
const rolesIncludeStudent = computed(() => form.value.roles.includes(ROLE_STUDENT));

/** Админ: не выбираем ни группу, ни предметы (по ТЗ). */
const showGroupField = computed(() => !rolesIncludeAdmin.value && rolesIncludeStudent.value);
const showSubjectsField = computed(() => !rolesIncludeAdmin.value && rolesIncludeTeacher.value);

const isFormValid = computed(() => {
  const usernameValid = !!form.value.username.trim();
  const rolesValid = form.value.roles.length > 0;
  const passwordValid = isEditMode.value ? true : !!form.value.password.trim();
  return usernameValid && rolesValid && passwordValid;
});

const normalizeGroupId = (gid: number | null | undefined): number | null => {
  if (gid == null || gid === 0) return null;
  return gid;
};

const userHasAdmin = (user: AdminUserDTO) => (user.roles || []).includes(ROLE_ADMIN);
const userHasTeacher = (user: AdminUserDTO) => (user.roles || []).includes(ROLE_TEACHER) && !userHasAdmin(user);
const userHasStudent = (user: AdminUserDTO) => (user.roles || []).includes(ROLE_STUDENT) && !userHasAdmin(user);

const resetForm = () => {
  form.value = {
    username: "",
    password: "",
    firstname: "",
    middlename: "",
    lastname: "",
    isActive: true,
    roles: [ROLE_STUDENT],
    groupId: null,
    subjectIds: [],
  };
  editingUser.value = null;
};

const buildAssignmentPayload = () => {
  if (rolesIncludeAdmin.value) {
    return { groupId: null as number | null, subjectIds: null as number[] | null };
  }
  const groupId = showGroupField.value ? form.value.groupId : null;
  const subjectIds = showSubjectsField.value ? [...form.value.subjectIds] : null;
  return { groupId, subjectIds };
};

const loadData = async () => {
  try {
    loading.value = true;
    const [usersResponse, groupsResponse, subjectsResponse] = await Promise.all([
      getUsers(),
      getGroups(),
      getSubjects(),
    ]);
    users.value = usersResponse.data.map((u) => ({
      ...u,
      groupId: normalizeGroupId(u.groupId),
    }));
    groups.value = groupsResponse.data;
    subjects.value = subjectsResponse.data;
  } catch (error) {
    if (isAxiosError(error)) {
      snackbarService.error(error.response?.data?.message || "Не удалось загрузить данные");
      return;
    }
    snackbarService.error("Не удалось загрузить данные");
  } finally {
    loading.value = false;
  }
};

const groupNameById = (groupId: number | null) => {
  if (groupId == null) return "—";
  const group = groups.value.find((item) => item.id === groupId);
  return group?.name || `Группа #${groupId}`;
};

const subjectsLabelForUser = (user: AdminUserDTO) => {
  if (!userHasTeacher(user)) return "—";
  const names = (user.subjects || []).map((s) => s.name);
  return names.length ? names.join(", ") : "—";
};

const openCreateDialog = () => {
  resetForm();
  isFormDialogOpen.value = true;
};

const openEditDialog = (user: AdminUserDTO) => {
  editingUser.value = user;
  form.value = {
    username: user.username,
    password: "",
    firstname: user.firstname || "",
    middlename: user.middlename || "",
    lastname: user.lastname || "",
    isActive: user.isActive,
    roles: [...(user.roles || [])],
    groupId: normalizeGroupId(user.groupId),
    subjectIds: (user.subjects || []).map((s) => s.id),
  };
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
    const { groupId, subjectIds } = buildAssignmentPayload();

    if (isEditMode.value && editingUser.value) {
      await updateUser(editingUser.value.id, {
        password: form.value.password.trim() ? form.value.password.trim() : null,
        firstname: form.value.firstname.trim(),
        middlename: form.value.middlename.trim(),
        lastname: form.value.lastname.trim(),
        isActive: form.value.isActive,
        roles: form.value.roles,
        groupId,
        subjectIds,
      });
      snackbarService.success("Пользователь обновлён");
    } else {
      await createUser({
        username: form.value.username.trim(),
        password: form.value.password.trim(),
        firstname: form.value.firstname.trim(),
        middlename: form.value.middlename.trim(),
        lastname: form.value.lastname.trim(),
        roles: form.value.roles,
        groupId,
        subjectIds,
      });
      snackbarService.success("Пользователь создан");
    }

    closeFormDialog();
    await loadData();
  } catch (error) {
    if (isAxiosError(error)) {
      snackbarService.error(error.response?.data?.message || "Не удалось сохранить пользователя");
      return;
    }
    snackbarService.error("Не удалось сохранить пользователя");
  } finally {
    saving.value = false;
  }
};

const openDeleteDialog = (user: AdminUserDTO) => {
  deletingUser.value = user;
  isDeleteDialogOpen.value = true;
};

const closeDeleteDialog = () => {
  isDeleteDialogOpen.value = false;
  deletingUser.value = null;
};

const confirmDelete = async () => {
  if (!deletingUser.value) return;

  try {
    deleting.value = true;
    await deleteUser(deletingUser.value.id);
    snackbarService.success("Пользователь удалён");
    closeDeleteDialog();
    await loadData();
  } catch (error) {
    if (isAxiosError(error)) {
      snackbarService.error(error.response?.data?.message || "Не удалось удалить пользователя");
      return;
    }
    snackbarService.error("Не удалось удалить пользователя");
  } finally {
    deleting.value = false;
  }
};

const closeViewDialog = () => {
  isViewDialogOpen.value = false;
  viewingUser.value = null;
};

const continueEditFromView = () => {
  const snapshot = viewingUser.value;
  if (!snapshot) return;
  closeViewDialog();
  openEditDialog(snapshot);
};

const openViewDialog = async (user: AdminUserDTO) => {
  isViewDialogOpen.value = true;
  viewingUser.value = { ...user, groupId: normalizeGroupId(user.groupId) };
  try {
    viewLoading.value = true;
    const { data } = await getUserById(user.id);
    viewingUser.value = {
      ...data,
      groupId: normalizeGroupId(data.groupId),
    };
  } catch (error) {
    if (isAxiosError(error)) {
      snackbarService.error(error.response?.data?.message || "Не удалось загрузить пользователя");
    } else {
      snackbarService.error("Не удалось загрузить пользователя");
    }
    closeViewDialog();
  } finally {
    viewLoading.value = false;
  }
};

const roleTitle = (role: string) => ROLE_OPTIONS.find((r) => r.value === role)?.title || role;

onMounted(async () => {
  await loadData();
});
</script>

<template>
  <div>
    <div class="d-flex align-center justify-space-between mb-6">
      <div>
        <div class="text-h5 font-weight-bold">Пользователи</div>
        <div class="text-body-2 text-medium-emphasis">
          Учителям назначаются предметы, ученикам — группа; у администратора без привязок
        </div>
      </div>

      <v-btn color="primary" rounded="lg" :icon="mdiPlus" @click="openCreateDialog">

      </v-btn>
    </div>

    <v-card rounded="lg" elevation="1">
      <v-table>
        <thead>
          <tr>
            <th class="text-left">ID</th>
            <th class="text-left">Логин</th>
            <th class="text-left">ФИО</th>
            <th class="text-left">Группа</th>
            <th class="text-left">Предметы</th>
            <th class="text-left">Роли</th>
            <th class="text-left">Статус</th>
            <th class="text-right">Действия</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="8" class="text-center py-6">
              <v-progress-circular indeterminate color="primary" size="24" />
            </td>
          </tr>
          <tr v-else-if="users.length === 0">
            <td colspan="8" class="text-center py-6 text-medium-emphasis">Список пользователей пуст</td>
          </tr>
          <template v-else>
            <tr v-for="user in users" :key="user.id">
              <td>{{ user.id }}</td>
              <td>{{ user.username }}</td>
              <td>{{ `${user.lastname || ""} ${user.firstname || ""} ${user.middlename || ""}`.trim() || "—" }}</td>
              <td>{{ userHasStudent(user) ? groupNameById(normalizeGroupId(user.groupId)) : "—" }}</td>
              <td>{{ subjectsLabelForUser(user) }}</td>
              <td>{{ (user.roles || []).map(roleTitle).join(", ") || "—" }}</td>
              <td>
                <v-chip :color="user.isActive ? 'success' : 'default'" size="small" variant="tonal">
                  {{ user.isActive ? "Активен" : "Отключен" }}
                </v-chip>
              </td>
              <td class="text-right">
                <v-btn
                  variant="text"
                  size="small"
                  color="primary"
                  :icon="mdiEyeOutline"
                  @click="openViewDialog(user)"
                />
                <v-btn
                  variant="text"
                  size="small"
                  color="primary"
                  :icon="mdiPencilOutline"
                  @click="openEditDialog(user)"
                />
                <v-btn
                  variant="text"
                  size="small"
                  color="error"
                  :icon="mdiDeleteOutline"
                  @click="openDeleteDialog(user)"
                />
              </td>
            </tr>
          </template>
        </tbody>
      </v-table>
    </v-card>

    <v-dialog v-model="isFormDialogOpen" class="w-50" scrollable>
      <v-card rounded="lg">
        <v-card-title class="text-h6 font-weight-bold pt-6 px-6">
          {{ formTitle }}
        </v-card-title>

        <v-card-text class="px-6 pt-2">
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="form.username"
                label="Логин"
                :disabled="isEditMode"
                :rules="[Rules.required(), Rules.withoutSpaces]"
                required
              />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="form.password"
                :label="isEditMode ? 'Новый пароль (опционально)' : 'Пароль'"
                type="password"
                :rules="isEditMode ? [] : [Rules.required()]"
                :required="!isEditMode"
              />
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field v-model="form.lastname" label="Фамилия" />
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field v-model="form.firstname" label="Имя" />
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field v-model="form.middlename" label="Отчество" />
            </v-col>
            <v-col cols="12">
              <v-select
                v-model="form.roles"
                :items="ROLE_OPTIONS"
                item-title="title"
                item-value="value"
                label="Роли"
                multiple
                chips
                closable-chips
                :rules="[Rules.required()]"
              />
            </v-col>
            <v-col v-if="showGroupField" cols="12" md="6">
              <v-select
                v-model="form.groupId"
                :items="groups"
                item-title="name"
                item-value="id"
                label="Группа (для ученика)"
                clearable
              />
            </v-col>
            <v-col v-if="showSubjectsField" cols="12" md="6">
              <v-select
                v-model="form.subjectIds"
                :items="subjects"
                item-title="name"
                item-value="id"
                label="Предметы (для учителя)"
                multiple
                chips
                closable-chips
              />
            </v-col>
            <v-col v-if="rolesIncludeAdmin" cols="12">
              <v-alert type="info" variant="tonal" density="comfortable" rounded="lg">
                У администратора не задаются группа и предметы.
              </v-alert>
            </v-col>
            <v-col cols="12" v-if="isEditMode">
              <v-switch v-model="form.isActive" label="Пользователь активен" color="primary" />
            </v-col>
          </v-row>
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

    <v-dialog v-model="isViewDialogOpen" class="w-50">
      <v-card rounded="lg">
        <v-card-title class="text-h6 font-weight-bold pt-6 px-6 d-flex align-center justify-space-between">
          <span>Просмотр пользователя</span>
          <v-btn icon variant="text" size="small" @click="closeViewDialog">
            <v-icon :icon="mdiClose" />
          </v-btn>
        </v-card-title>

        <v-divider />

        <v-card-text class="px-6 py-6">
          <div v-if="viewLoading" class="text-center py-8">
            <v-progress-circular indeterminate color="primary" size="32" />
          </div>
          <template v-else-if="viewingUser">
            <v-list density="comfortable" class="bg-transparent pa-0">
              <v-list-item class="px-0">
                <v-list-item-title class="text-caption text-medium-emphasis">ID</v-list-item-title>
                <v-list-item-subtitle class="text-body-1 text-high-emphasis opacity-100">
                  {{ viewingUser.id }}
                </v-list-item-subtitle>
              </v-list-item>
              <v-list-item class="px-0">
                <v-list-item-title class="text-caption text-medium-emphasis">Логин</v-list-item-title>
                <v-list-item-subtitle class="text-body-1 text-high-emphasis opacity-100">
                  {{ viewingUser.username }}
                </v-list-item-subtitle>
              </v-list-item>
              <v-list-item class="px-0">
                <v-list-item-title class="text-caption text-medium-emphasis">ФИО</v-list-item-title>
                <v-list-item-subtitle class="text-body-1 text-high-emphasis opacity-100">
                  {{
                    `${viewingUser.lastname || ""} ${viewingUser.firstname || ""} ${viewingUser.middlename || ""}`.trim() ||
                    "—"
                  }}
                </v-list-item-subtitle>
              </v-list-item>
              <v-list-item class="px-0">
                <v-list-item-title class="text-caption text-medium-emphasis">Роли</v-list-item-title>
                <v-list-item-subtitle class="opacity-100">
                  <v-chip
                    v-for="r in viewingUser.roles || []"
                    :key="r"
                    class="mr-1 mb-1"
                    size="small"
                    variant="tonal"
                    color="primary"
                  >
                    {{ roleTitle(r) }}
                  </v-chip>
                </v-list-item-subtitle>
              </v-list-item>
              <v-list-item class="px-0">
                <v-list-item-title class="text-caption text-medium-emphasis">Статус</v-list-item-title>
                <v-list-item-subtitle class="opacity-100">
                  <v-chip :color="viewingUser.isActive ? 'success' : 'default'" size="small" variant="tonal">
                    {{ viewingUser.isActive ? "Активен" : "Отключен" }}
                  </v-chip>
                </v-list-item-subtitle>
              </v-list-item>
              <v-list-item v-if="userHasStudent(viewingUser)" class="px-0">
                <v-list-item-title class="text-caption text-medium-emphasis">Группа</v-list-item-title>
                <v-list-item-subtitle class="text-body-1 text-high-emphasis opacity-100">
                  {{ groupNameById(normalizeGroupId(viewingUser.groupId)) }}
                </v-list-item-subtitle>
              </v-list-item>
              <v-list-item v-if="userHasTeacher(viewingUser)" class="px-0">
                <v-list-item-title class="text-caption text-medium-emphasis">Предметы</v-list-item-title>
                <v-list-item-subtitle class="opacity-100">
                  <template v-if="(viewingUser.subjects || []).length">
                    <v-chip
                      v-for="s in viewingUser.subjects"
                      :key="s.id"
                      class="mr-1 mb-1"
                      size="small"
                      variant="tonal"
                    >
                      {{ s.name }}
                    </v-chip>
                  </template>
                  <span v-else class="text-body-2 text-medium-emphasis">Не назначены</span>
                </v-list-item-subtitle>
              </v-list-item>
              <v-list-item v-if="userHasAdmin(viewingUser)" class="px-0">
                <v-list-item-title class="text-caption text-medium-emphasis">Привязки</v-list-item-title>
                <v-list-item-subtitle class="text-body-2 text-medium-emphasis opacity-100">
                  У администратора нет группы и предметов
                </v-list-item-subtitle>
              </v-list-item>
            </v-list>
          </template>
        </v-card-text>

        <v-card-actions class="px-6 pb-6">
          <v-spacer />
          <v-btn variant="text" @click="closeViewDialog">Закрыть</v-btn>
          <v-btn v-if="viewingUser" color="primary" variant="flat" rounded="lg" @click="continueEditFromView">
            Редактировать
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="isDeleteDialogOpen" class="w-33">
      <v-card rounded="lg">
        <v-card-title class="text-h6 font-weight-bold pt-6 px-6">Удалить пользователя</v-card-title>
        <v-card-text class="px-6 pt-2">
          Вы действительно хотите удалить пользователя <b>{{ deletingUser?.username }}</b
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
