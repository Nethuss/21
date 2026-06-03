import api from "@/api/axios";

export async function fetchGrades(dateFrom: string, dateTo: string) {
    const { data } = await api.get('/student/gradess', { params: { dateFrom, dateTo } })
    return data
}

export async function fetchStatistics(dateFrom: string, dateTo: string) {
    const { data } = await api.get('/student/statistics', { params: { dateFrom, dateTo } })
    return data
}