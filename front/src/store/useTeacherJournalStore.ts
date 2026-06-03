import { defineStore } from "pinia";
import { ref } from "vue";
import {
    fetchGroups,
    fetchSubjects,
    fetchJournal,
    saveGrade,
    deleteGrade,
} from "@/api/teacher.api";

export const useTeacherJournalStore = defineStore("teacherJournal", () => {
    // ─── Reference data ──────────────────────────────────────────────────────
    const groups = ref([]);
    const subjects = ref([]);

    // ─── Journal data ─────────────────────────────────────────────────────────
    const lessons = ref([]);
    const students = ref([]);

    // ─── Loading flags ────────────────────────────────────────────────────────
    const loadingGroups = ref(false);
    const loadingSubjects = ref(false);
    const loadingJournal = ref(false);
    const savingGrade = ref(false);

    // ─── Actions ──────────────────────────────────────────────────────────────

    async function loadGroups() {
        loadingGroups.value = true;
        try {
            const { data } = await fetchGroups();
            groups.value = data;
        } finally {
            loadingGroups.value = false;
        }
    }

    async function loadSubjects(groupId) {
        subjects.value = [];
        loadingSubjects.value = true;
        try {
            const { data } = await fetchSubjects(groupId);
            subjects.value = data;
        } finally {
            loadingSubjects.value = false;
        }
    }

    async function loadJournal(groupId, subjectId, dateFrom, dateTo) {
        loadingJournal.value = true;
        try {
            const { data } = await fetchJournal(groupId, subjectId, dateFrom, dateTo);
            lessons.value = data.lessons;
            students.value = data.students;
        } finally {
            loadingJournal.value = false;
        }
    }

    async function submitGrade(studentId, lessonId, value) {
        savingGrade.value = true;
        try {
            await saveGrade(studentId, lessonId, value);
            // обновляем локально
            const student = students.value.find((s) => s.id === studentId);
            if (student) {
                student.grades = { ...student.grades, [lessonId]: value };
            }
        } finally {
            savingGrade.value = false;
        }
    }

    async function removeGrade(studentId, lessonId) {
        savingGrade.value = true;
        try {
            await deleteGrade(studentId, lessonId);
            // обновляем локально
            const student = students.value.find((s) => s.id === studentId);
            if (student) {
                const updated = { ...student.grades };
                delete updated[String(lessonId)];
                student.grades = updated;
            }
        } finally {
            savingGrade.value = false;
        }
    }

    return {
        groups,
        subjects,
        lessons,
        students,
        loadingGroups,
        loadingSubjects,
        loadingJournal,
        savingGrade,
        loadGroups,
        loadSubjects,
        loadJournal,
        submitGrade,
        removeGrade,
    };
});