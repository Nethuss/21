import api from "@/api/axios";

export function fetchGroups() {
    return api.get("/teacher/journal/groups");
}

export function fetchSubjects(groupId) {
    return api.get("/teacher/journal/subjects", { params: { groupId } });
}

export function fetchJournal(groupId, subjectId, dateFrom, dateTo) {
    return api.get("/teacher/journal", {
        params: { groupId, subjectId, dateFrom, dateTo },
    });
}

export function saveGrade(studentId, lessonId, value, comment = "") {
    return api.post("/teacher/journal/grade", {
        studentId,
        lessonId,
        value,
        comment,
    });
}

export function deleteGrade(studentId, lessonId) {
    return api.delete(`/teacher/journal/grade/${studentId}/${lessonId}`);
}