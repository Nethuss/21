export type Preset =
    | 'today' | 'current_week' | 'last_week'
    | 'current_month' | 'last_month' | 'current_quarter' | 'academic_year'
    | 'custom'

export const PRESETS: { key: Preset; label: string }[] = [
    { key: 'today',          label: 'Сегодня' },
    { key: 'current_week',   label: 'Эта неделя' },
    { key: 'last_week',      label: 'Прошлая неделя' },
    { key: 'current_month',  label: 'Этот месяц' },
    { key: 'last_month',     label: 'Прошлый месяц' },
    { key: 'current_quarter',label: 'Четверть' },
    { key: 'academic_year',  label: 'Учебный год' },
]

function fmt(d: Date) {
    return d.toISOString().slice(0, 10)
}

export function getRange(preset: Preset, custom?: { from: string; to: string }) {
    if (preset === 'custom' && custom) return custom

    const now = new Date()
    const y = now.getFullYear()
    const m = now.getMonth()
    const d = now.getDate()
    const dow = now.getDay()

    if (preset === 'today') {
        const t = fmt(now)
        return { from: t, to: t }
    }
    if (preset === 'current_week') {
        const offset = dow === 0 ? -6 : 1 - dow
        return { from: fmt(new Date(y, m, d + offset)), to: fmt(new Date(y, m, d + offset + 6)) }
    }
    if (preset === 'last_week') {
        const offset = dow === 0 ? -6 : 1 - dow
        return { from: fmt(new Date(y, m, d + offset - 7)), to: fmt(new Date(y, m, d + offset - 1)) }
    }
    if (preset === 'current_month') {
        return { from: fmt(new Date(y, m, 1)), to: fmt(new Date(y, m + 1, 0)) }
    }
    if (preset === 'last_month') {
        return { from: fmt(new Date(y, m - 1, 1)), to: fmt(new Date(y, m, 0)) }
    }
    if (preset === 'current_quarter') {
        const quarters: [number, number][] = [[8,9],[10,11],[0,2],[3,4]]
        const q = quarters.find(([s, e]) => m >= s && m <= e) ?? [m, m]
        return { from: fmt(new Date(y, q[0], 1)), to: fmt(new Date(y, q[1] + 1, 0)) }
    }
    // academic_year
    const sy = m >= 8 ? y : y - 1
    return { from: fmt(new Date(sy, 8, 1)), to: fmt(new Date(sy + 1, 4, 31)) }
}