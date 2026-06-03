<template>
  <div class="grades-root">
    <!-- PERIOD -->
    <div class="period-bar">
      <div class="chips-scroll-wrapper">
        <div class="chips-row">
          <v-chip
              v-for="p in PRESETS"
              :key="p.key"
              :variant="activePreset === p.key ? 'flat' : 'outlined'"
              :color="activePreset === p.key ? 'primary' : undefined"
              size="small"
              rounded="lg"
              class="period-chip"
              @click="setPreset(p.key)"
          >{{ p.label }}</v-chip>
          <v-chip
              :variant="activePreset === 'custom' ? 'flat' : 'outlined'"
              :color="activePreset === 'custom' ? 'primary' : undefined"
              size="small"
              rounded="lg"
              class="period-chip"
              @click="calendarOpen = true"
          >
            <template #prepend>
              <v-icon :icon="mdiCalendarRange" size="14" class="mr-1" />
            </template>
            {{ customChipLabel }}
          </v-chip>
        </div>
      </div>
      <span class="range-label text-caption text-medium-emphasis">{{ rangeLabel }}</span>
    </div>

    <!-- ERROR -->
    <v-alert
        v-if="error"
        type="error"
        variant="tonal"
        density="compact"
        rounded="lg"
        closable
        class="mb-5"
        @click:close="error = null"
    >{{ error }}</v-alert>

    <!-- ─────────────────── DESKTOP ─────────────────── -->
    <template v-if="!isMobile">

      <!-- KPI -->
      <v-row class="mb-2">
        <v-col v-for="card in kpiCards" :key="card.label" cols="6" md="3">
          <div class="kpi-card" :style="{ '--accent': card.accent }">
            <div class="kpi-icon">
              <v-icon :icon="card.icon" :color="card.accent" size="18" />
            </div>
            <div class="kpi-value">
              <span v-if="loading" class="kpi-skeleton" />
              <template v-else>{{ card.value }}</template>
            </div>
            <div class="kpi-label">{{ card.label }}</div>
            <div v-if="card.sub" class="kpi-sub" :style="{ color: card.accent }">{{ card.sub }}</div>
          </div>
        </v-col>
      </v-row>

      <!-- CHARTS -->
      <v-row class="mb-2" v-if="statistics || loading">
        <v-col cols="12" md="8">
          <div class="chart-card">
            <div class="chart-card-title">Динамика среднего балла</div>
            <div class="chart-card-sub">По месяцам</div>
            <div v-if="loading" class="chart-skel" />
            <v-chart
                v-else-if="statistics?.averageTrend?.length"
                class="echart"
                :option="trendOption"
                :theme="chartTheme"
                autoresize
            />
            <div v-else class="chart-empty">Нет данных</div>
          </div>
        </v-col>
        <v-col cols="12" md="4">
          <div class="chart-card">
            <div class="chart-card-title">Распределение оценок</div>
            <div class="chart-card-sub">За период</div>
            <div v-if="loading" class="chart-skel" />
            <v-chart
                v-else-if="statistics?.distribution?.length"
                class="echart"
                :option="distOption"
                :theme="chartTheme"
                autoresize
            />
            <div v-else class="chart-empty">Нет данных</div>
          </div>
        </v-col>
      </v-row>

      <!-- TABLE -->
      <div class="grades-card">
        <div class="d-flex align-baseline ga-2 mb-4">
          <span class="chart-card-title">Журнал оценок</span>
          <span class="text-caption text-medium-emphasis">{{ grades?.lessons?.length ?? 0 }} уроков</span>
        </div>

        <template v-if="loading">
          <div v-for="i in 5" :key="i" class="mb-2 rounded-lg skeleton-row" />
        </template>

        <div v-else-if="!hasGrades" class="chart-empty py-10">
          <v-icon :icon="mdiClipboardOutline" size="36" color="grey-lighten-2" />
          <div class="mt-2">За выбранный период оценок нет</div>
        </div>

        <div v-else class="table-scroll">
          <table class="g-table">
            <thead>
            <tr>
              <th class="subj-th sticky-col">Предмет</th>
              <th v-for="lesson in grades!.lessons" :key="lesson.id" class="lesson-th">
                <v-tooltip location="bottom" :text="`${fmtDate(lesson.date)}, ${lesson.lesson_number}-й урок`">
                  <template #activator="{ props }">
                    <div v-bind="props" class="lesson-head">
                      <span>{{ shortDate(lesson.date) }}</span>
                      <span class="lesson-num-small">{{ lesson.lesson_number }}</span>
                    </div>
                  </template>
                </v-tooltip>
              </th>
              <th class="avg-th">Ср.</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="subj in grades!.subjects" :key="subj.id" class="g-row">
              <td class="subj-td sticky-col">{{ subj.name }}</td>
              <td v-for="lesson in grades!.lessons" :key="lesson.id" class="grade-td">
                <v-tooltip v-if="subj.grades[String(lesson.id)] != null" location="top">
                  <template #activator="{ props }">
                      <span v-bind="props" :class="['g-chip', gradeClass(subj.grades[String(lesson.id)])]">
                        {{ gradeLabel(subj.grades[String(lesson.id)]) }}
                      </span>
                  </template>
                  <div style="text-align:center;font-size:12px">
                    {{ fmtDate(lesson.date) }}<br />{{ lesson.lesson_number }}-й урок
                  </div>
                </v-tooltip>
                <span v-else class="g-empty">·</span>
              </td>
              <td class="avg-td">
                <span :class="['avg-badge', avgClass(subjectAvg(subj))]">{{ subjectAvg(subj) }}</span>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>

    <!-- ─────────────────── MOBILE ─────────────────── -->
    <template v-else>
      <template v-if="loading">
        <div v-for="i in 5" :key="i" class="mb-2 rounded-xl skeleton-row" style="height:72px" />
      </template>

      <div v-else-if="!hasGrades" class="chart-empty py-10">
        <v-icon :icon="mdiClipboardOutline" size="36" color="grey-lighten-2" />
        <div class="mt-2">За выбранный период оценок нет</div>
      </div>

      <!-- Days feed -->
      <div v-else class="days-feed">
        <div v-for="day in daysFeed" :key="day.date" class="day-block">
          <div class="day-header">
            <span class="day-name">{{ day.dayName }}</span>
            <span class="day-date">{{ day.dateLabel }}</span>
          </div>
          <div class="day-lessons">
            <div
                v-for="entry in day.entries"
                :key="entry.lessonId"
                class="lesson-row"
            >
              <span class="lesson-num-badge">{{ entry.lessonNum }}</span>
              <span class="lesson-subject">{{ entry.subject }}</span>
              <span :class="['g-chip', gradeClass(entry.grade)]">
                {{ gradeLabel(entry.grade) }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- CALENDAR DIALOG -->
    <v-dialog v-model="calendarOpen" max-width="340">
      <v-card rounded="xl" elevation="8">
        <v-card-title class="pt-5 px-5 text-body-1 font-weight-semibold">Выбрать период</v-card-title>
        <v-card-text class="px-3">
          <v-date-picker
              v-model="pickerRange"
              multiple="range"
              color="primary"
              hide-header
              show-adjacent-months
              style="box-shadow:none;width:100%"
          />
        </v-card-text>
        <v-card-actions class="px-5 pb-5">
          <v-btn variant="text" rounded="lg" @click="calendarOpen = false">Отмена</v-btn>
          <v-spacer />
          <v-btn
              color="primary"
              variant="flat"
              rounded="lg"
              :disabled="!pickerRange || pickerRange.length < 2"
              @click="applyCustom"
          >
            Применить
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
} from 'echarts/components'
import VChart from 'vue-echarts'

// ── MDI icons (импортируйте из вашего пакета иконок, например @mdi/js)
import {
  mdiChartLine,
  mdiClipboardCheckOutline,
  mdiStar,
  mdiTrophyOutline,
  mdiCalendarRange,
  mdiClipboardOutline,
} from '@mdi/js'

import { fetchGrades, fetchStatistics } from '@/api/grades'
import { PRESETS, getRange, type Preset } from '@/components/grades-period'

use([CanvasRenderer, LineChart, PieChart, BarChart, GridComponent, TooltipComponent, LegendComponent])

// ─── State ─────────────────────────────────────────────────────────────────────

const grades     = ref<any>(null)
const statistics = ref<any>(null)
const loading    = ref(false)
const error      = ref<string | null>(null)

const activePreset = ref<Preset>((localStorage.getItem('grades_preset') as Preset) ?? 'current_month')
const customRange  = ref<{ from: string; to: string } | undefined>(
    localStorage.getItem('grades_custom')
        ? JSON.parse(localStorage.getItem('grades_custom')!)
        : undefined
)
const calendarOpen = ref(false)
const pickerRange  = ref<Date[] | null>(null)
const isMobile     = ref(window.innerWidth < 768)

const onResize = () => { isMobile.value = window.innerWidth < 768 }
onMounted(() => { window.addEventListener('resize', onResize, { passive: true }); load() })
onUnmounted(() => window.removeEventListener('resize', onResize))

// ─── Period ────────────────────────────────────────────────────────────────────

const range = computed(() => getRange(activePreset.value, customRange.value))

const rangeLabel = computed(() => {
  const fmt = (s: string) =>
      new Date(s).toLocaleDateString('ru-RU', { day: 'numeric', month: 'long', year: 'numeric' })
  return range.value.from === range.value.to
      ? fmt(range.value.from)
      : `${fmt(range.value.from)} — ${fmt(range.value.to)}`
})

const customChipLabel = computed(() => {
  if (activePreset.value === 'custom' && customRange.value) {
    const f = customRange.value.from.slice(5).replace('-', '.')
    const t = customRange.value.to.slice(5).replace('-', '.')
    return `${f} – ${t}`
  }
  return 'Период'
})

function setPreset(key: Preset) {
  activePreset.value = key
  localStorage.setItem('grades_preset', key)
  load()
}

function applyCustom() {
  if (!pickerRange.value || pickerRange.value.length < 2) return
  const sorted = [...pickerRange.value].sort((a, b) => a.getTime() - b.getTime())
  customRange.value = {
    from: sorted[0].toISOString().slice(0, 10),
    to:   sorted[sorted.length - 1].toISOString().slice(0, 10),
  }
  localStorage.setItem('grades_custom', JSON.stringify(customRange.value))
  activePreset.value = 'custom'
  localStorage.setItem('grades_preset', 'custom')
  calendarOpen.value = false
  load()
}

// ─── Load ──────────────────────────────────────────────────────────────────────

async function load() {
  loading.value = true
  error.value = null
  try {
    const { from, to } = range.value
    ;[grades.value, statistics.value] = await Promise.all([
      fetchGrades(from, to),
      fetchStatistics(from, to),
    ])
  } catch {
    error.value = 'Не удалось загрузить данные. Проверьте подключение.'
  } finally {
    loading.value = false
  }
}

// ─── Grade helpers ─────────────────────────────────────────────────────────────

function gradeLabel(g: number): string {
  if (g === -1) return 'Н'
  if (g === -2) return 'Б'
  return String(g)
}

function gradeClass(g: number): string {
  if (g === -1) return 'gn'
  if (g === -2) return 'gb'
  if (g === 5)  return 'g5'
  if (g === 4)  return 'g4'
  if (g === 3)  return 'g3'
  return 'g2'
}

function realGrades(subj: any): number[] {
  return Object.values<number>(subj.grades).filter(g => g > 0)
}

function subjectAvg(subj: any): string {
  const vals = realGrades(subj)
  if (!vals.length) return '—'
  return (vals.reduce((a, b) => a + b, 0) / vals.length).toFixed(1)
}

function avgClass(avg: string) {
  const n = parseFloat(avg)
  if (n >= 4.5) return 'avg5'
  if (n >= 3.5) return 'avg4'
  if (n >= 2.5) return 'avg3'
  if (n > 0)    return 'avg2'
  return ''
}

// ─── KPI ───────────────────────────────────────────────────────────────────────

const kpi = computed(() => {
  if (!grades.value) return null
  const all: number[] = []
  const subAvgs: { name: string; avg: number }[] = []
  for (const s of grades.value.subjects ?? []) {
    const vals = realGrades(s)
    all.push(...vals)
    if (vals.length)
      subAvgs.push({ name: s.name, avg: vals.reduce((a: number, b: number) => a + b, 0) / vals.length })
  }
  const avg = all.length ? all.reduce((a, b) => a + b, 0) / all.length : 0
  const best = [...subAvgs].sort((a, b) => b.avg - a.avg)[0]
  return {
    avg: Math.round(avg * 10) / 10,
    total: all.length,
    fives: all.filter(g => g === 5).length,
    best: best?.name ?? '—',
  }
})

const kpiCards = computed(() => {
  const k = kpi.value
  const avgLabel = (n: number) =>
      n >= 4.5 ? 'Отлично' : n >= 3.5 ? 'Хорошо' : n >= 2.5 ? 'Удовлетв.' : n > 0 ? 'Слабо' : ''
  return [
    { label: 'Средний балл',   value: k ? k.avg.toFixed(1) : '—', icon: mdiChartLine,             accent: '#4F8EF7', sub: k ? avgLabel(k.avg) : null },
    { label: 'Всего оценок',   value: k ? String(k.total) : '—',  icon: mdiClipboardCheckOutline, accent: '#7C6FCD', sub: null },
    { label: 'Пятёрок',        value: k ? String(k.fives) : '—',  icon: mdiStar,                  accent: '#3DBE7A', sub: k && k.total ? `${Math.round(k.fives / k.total * 100)}%` : null },
    { label: 'Лучший предмет', value: k?.best ?? '—',             icon: mdiTrophyOutline,          accent: '#F0A500', sub: null },
  ]
})

// ─── Charts ────────────────────────────────────────────────────────────────────

const MONTH_SHORT: Record<string, string> = {
  '01':'Янв','02':'Фев','03':'Мар','04':'Апр','05':'Май','06':'Июн',
  '07':'Июл','08':'Авг','09':'Сен','10':'Окт','11':'Ноя','12':'Дек',
}
const GRADE_COLOR: Record<number, string> = {
  2: '#F44B6E',
  3: '#FF8C42',
  4: '#4F8EF7',
  5: '#3DBE7A',
}

// Определяем тему (light/dark) из Vuetify
const chartTheme = computed(() =>
    document.documentElement.classList.contains('v-theme--dark') ? 'dark' : undefined
)

const trendOption = computed(() => {
  const trend: Array<{ period: string; average: number }> = statistics.value?.averageTrend ?? []

  return {
    animation: true,
    animationDuration: 600,
    grid: { top: 16, right: 16, bottom: 28, left: 44, containLabel: false },
    xAxis: {
      type: 'category',
      data: trend.map(p => MONTH_SHORT[p.period.slice(5, 7)] ?? p.period.slice(5)),
      boundaryGap: false,
      axisLine:  { show: false },
      axisTick:  { show: false },
      axisLabel: { color: 'rgba(120,120,120,.7)', fontSize: 11 },
    },
    yAxis: {
      type: 'value',
      min: 2,
      max: 5,
      interval: 1,
      axisLabel: { color: 'rgba(120,120,120,.7)', fontSize: 11 },
      splitLine: { lineStyle: { color: 'rgba(0,0,0,0.06)', type: 'dashed' } },
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: 'rgba(0,0,0,.08)',
      borderWidth: 1,
      padding: [8, 12],
      textStyle: { color: '#333', fontSize: 12 },
      formatter: (params: any) => {
        const p = params[0]
        return `<b>${p.name}</b>: ${Number(p.value).toFixed(2)}`
      },
    },
    series: [{
      type: 'line',
      data: trend.map(p => p.average),
      smooth: 0.4,
      symbol: 'circle',
      symbolSize: 7,
      lineStyle: { width: 3, color: '#4F8EF7' },
      itemStyle: { color: '#4F8EF7', borderWidth: 2, borderColor: '#fff' },
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(79,142,247,.20)' },
            { offset: 1, color: 'rgba(79,142,247,0)' },
          ],
        },
      },
    }],
  }
})

const distOption = computed(() => {
  const dist = [...(statistics.value?.distribution ?? [])]
      .filter((d: any) => d.grade > 0)
      .sort((a: any, b: any) => a.grade - b.grade)

  const total = dist.reduce((s: number, d: any) => s + d.count, 0)

  return {
    animation: true,
    animationDuration: 600,
    tooltip: {
      trigger: 'item',
      backgroundColor: '#fff',
      borderColor: 'rgba(0,0,0,.08)',
      borderWidth: 1,
      padding: [8, 12],
      textStyle: { color: '#333', fontSize: 12 },
      formatter: (p: any) =>
          `Оценка <b>${p.name}</b>: ${p.value} (${p.percent.toFixed(0)}%)`,
    },
    legend: {
      show: false,
    },
    series: [{
      type: 'pie',
      radius: ['44%', '68%'],
      center: ['50%', '50%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        position: 'center',
        formatter: () => `{total|${total}}\n{sub|оценок}`,
        rich: {
          total: { fontSize: 20, fontWeight: 700, color: 'rgba(0,0,0,.78)', lineHeight: 28 },
          sub:   { fontSize: 11, color: 'rgba(0,0,0,.4)', lineHeight: 18 },
        },
      },
      emphasis: {
        scale: true,
        scaleSize: 6,
        label: {
          show: true,
          formatter: (p: any) => `{grade|${p.name}}\n{cnt|${p.value}}`,
          rich: {
            grade: { fontSize: 20, fontWeight: 700, color: 'rgba(0,0,0,.78)', lineHeight: 28 },
            cnt:   { fontSize: 11, color: 'rgba(0,0,0,.4)', lineHeight: 18 },
          },
        },
      },
      labelLine: { show: false },
      data: dist.map((d: any) => ({
        name:      String(d.grade),
        value:     d.count,
        itemStyle: { color: GRADE_COLOR[d.grade] ?? '#ccc', borderWidth: 0 },
      })),
    }],
  }
})

// ─── Table helpers ─────────────────────────────────────────────────────────────

const hasGrades = computed(() =>
    (grades.value?.subjects?.length ?? 0) > 0 && (grades.value?.lessons?.length ?? 0) > 0
)

function shortDate(date: string) {
  const d = new Date(date)
  return `${d.getDate()}.${String(d.getMonth() + 1).padStart(2, '0')}`
}

function fmtDate(date: string) {
  return new Date(date).toLocaleDateString('ru-RU', { day: 'numeric', month: 'long' })
}

// ─── Mobile days feed ──────────────────────────────────────────────────────────

const DAY_NAMES = ['Вс', 'Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб']

const daysFeed = computed(() => {
  if (!grades.value) return []

  const byDate = new Map<string, typeof grades.value.lessons>()
  for (const lesson of grades.value.lessons) {
    if (!byDate.has(lesson.date)) byDate.set(lesson.date, [])
    byDate.get(lesson.date)!.push(lesson)
  }

  const result = []
  for (const [date, lessons] of [...byDate.entries()].sort((a, b) => b[0].localeCompare(a[0]))) {
    const entries: { lessonId: number; lessonNum: number; subject: string; grade: number }[] = []

    for (const lesson of lessons.sort((a: any, b: any) => a.lesson_number - b.lesson_number)) {
      for (const subj of grades.value.subjects) {
        const g = subj.grades[String(lesson.id)]
        if (g != null)
          entries.push({ lessonId: lesson.id, lessonNum: lesson.lesson_number, subject: subj.name, grade: g })
      }
    }

    if (!entries.length) continue

    const d = new Date(date)
    result.push({
      date,
      dayName:   DAY_NAMES[d.getDay()],
      dateLabel: d.toLocaleDateString('ru-RU', { day: 'numeric', month: 'long' }),
      entries,
    })
  }

  return result
})
</script>

<style scoped>
/* ── Root ── */
.grades-root {
  /* Запрещаем горизонтальное переполнение на уровне компонента */
  overflow-x: hidden;
  width: 100%;
  box-sizing: border-box;
}

/* ── Period bar ── */
.period-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  min-width: 0;        /* важно для flex-дочерних overflow */
}

/* Обёртка прокрутки чипов — занимает свободное место, скролл внутри */
.chips-scroll-wrapper {
  flex: 1 1 0;
  min-width: 0;
  overflow-x: auto;
  overflow-y: visible;
  scrollbar-width: none;
  /* Отступ снизу, чтобы скролл не обрезал чипы */
  padding-bottom: 4px;
  margin-bottom: -4px;
}
.chips-scroll-wrapper::-webkit-scrollbar { display: none }

.chips-row {
  display: flex;
  gap: 6px;
  width: max-content; /* строка не переносится — только внутренний скролл */
}

.period-chip {
  cursor: pointer;
  white-space: nowrap;
  font-size: 12px !important;
  flex-shrink: 0;
}

/* Метка диапазона — не сжимается */
.range-label {
  flex-shrink: 0;
  white-space: nowrap;
  font-size: 12px;
}

/* ── KPI cards ── */
.kpi-card {
  background: rgb(var(--v-theme-surface));
  border-radius: 14px;
  border: 1.5px solid rgba(var(--v-theme-on-surface), .07);
  padding: 18px;
  position: relative;
  overflow: hidden;
  transition: transform .2s, box-shadow .2s;
}
.kpi-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 3px;
  background: var(--accent);
  opacity: 0;
  transition: opacity .2s;
}
.kpi-card:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(0,0,0,.07) }
.kpi-card:hover::before { opacity: 1 }
.kpi-icon {
  width: 34px; height: 34px; border-radius: 9px;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 12px;
  background: color-mix(in srgb, var(--accent) 12%, transparent);
}
.kpi-value {
  font-size: 26px; font-weight: 700; letter-spacing: -.5px;
  color: rgb(var(--v-theme-on-surface));
  margin-bottom: 3px;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.kpi-label { font-size: 11px; font-weight: 500; color: rgba(var(--v-theme-on-surface), .5); margin-bottom: 2px }
.kpi-sub   { font-size: 10px; font-weight: 600 }
.kpi-skeleton {
  display: inline-block; width: 60px; height: 26px;
  background: rgba(var(--v-theme-on-surface), .08);
  border-radius: 6px; animation: pulse 1.4s infinite;
}

/* ── Chart cards ── */
.chart-card {
  background: rgb(var(--v-theme-surface));
  border-radius: 14px;
  border: 1.5px solid rgba(var(--v-theme-on-surface), .07);
  padding: 18px;
  height: 100%;
  /* Фиксируем box-sizing, чтобы padding не порождал overflow */
  box-sizing: border-box;
}
.chart-card-title { font-size: 13px; font-weight: 600; color: rgb(var(--v-theme-on-surface)) }
.chart-card-sub   { font-size: 11px; color: rgba(var(--v-theme-on-surface), .45); margin-bottom: 10px }

/* echart занимает фиксированную высоту */
.echart    { height: 200px; width: 100% }
.chart-skel {
  height: 200px;
  background: rgba(var(--v-theme-on-surface), .06);
  border-radius: 10px;
  animation: pulse 1.4s infinite;
}
.chart-empty {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  height: 200px;
  color: rgba(var(--v-theme-on-surface), .3); font-size: 13px; gap: 8px;
}

/* ── Grades table card ── */
.grades-card {
  background: rgb(var(--v-theme-surface));
  border-radius: 14px;
  border: 1.5px solid rgba(var(--v-theme-on-surface), .07);
  padding: 20px;
  /* Таблица может быть шире — скролл только внутри */
  overflow: hidden;
}

.skeleton-row {
  height: 44px;
  background: rgba(var(--v-theme-on-surface), .06);
  animation: pulse 1.4s infinite;
}

/* ── Table ── */
.table-scroll { overflow-x: auto; overflow-y: visible; scrollbar-width: thin }
.g-table {
  width: max-content; min-width: 100%;
  border-collapse: collapse; font-size: 12px;
}
.g-table thead th {
  padding: 8px 10px;
  text-align: center;
  font-size: 10px; font-weight: 600;
  color: rgba(var(--v-theme-on-surface), .45);
  text-transform: uppercase; letter-spacing: .04em;
  border-bottom: 1.5px solid rgba(var(--v-theme-on-surface), .07);
  background: rgb(var(--v-theme-surface));
  white-space: nowrap;
}
.subj-th { text-align: left !important; min-width: 150px }
.lesson-th { min-width: 50px; padding: 6px 4px !important }
.avg-th { min-width: 54px }

.sticky-col {
  position: sticky !important; left: 0; z-index: 2;
  background: rgb(var(--v-theme-surface)) !important;
  box-shadow: 2px 0 6px rgba(0,0,0,.04);
}

.g-row:hover td { background: rgba(var(--v-theme-on-surface), .025) }
.g-row:not(:last-child) td { border-bottom: 1px solid rgba(var(--v-theme-on-surface), .05) }

.subj-td {
  padding: 10px 14px; font-size: 13px; font-weight: 500;
  max-width: 200px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.grade-td { text-align: center; padding: 6px 3px }
.avg-td   { text-align: center; padding: 6px 12px }

.lesson-head {
  display: flex; flex-direction: column; align-items: center; gap: 1px; cursor: default;
}
.lesson-num-small { font-size: 9px; opacity: .5 }
.g-empty { color: rgba(var(--v-theme-on-surface), .2); font-size: 18px }

/* ── Grade chips — desktop + mobile ── */
.g-chip {
  display: inline-flex; align-items: center; justify-content: center;
  width: 27px; height: 27px; border-radius: 7px;
  font-size: 12px; font-weight: 700;
  cursor: default; user-select: none;
  transition: transform .15s;
}
.g-chip:hover { transform: scale(1.12) }
.g5 { background: rgba(61,190,122,.15);  color: #2a9d5c }
.g4 { background: rgba(79,142,247,.15);  color: #2c6fd4 }
.g3 { background: rgba(255,140,66,.15);  color: #d97222 }
.g2 { background: rgba(244,75,110,.15);  color: #d02a55 }
.gn { background: rgba(120,120,120,.12); color: #666 }
.gb { background: rgba(124,111,205,.15); color: #5b4fa8 }

/* ── Avg badge ── */
.avg-badge {
  display: inline-block; padding: 2px 8px;
  border-radius: 20px; font-size: 11px; font-weight: 700;
}
.avg5 { background: rgba(61,190,122,.12);  color: #2a9d5c }
.avg4 { background: rgba(79,142,247,.12);  color: #2c6fd4 }
.avg3 { background: rgba(255,140,66,.12);  color: #d97222 }
.avg2 { background: rgba(244,75,110,.12);  color: #d02a55 }

/* ── Mobile days feed ── */
.days-feed { display: flex; flex-direction: column; gap: 4px }

.day-block {
  background: rgb(var(--v-theme-surface));
  border-radius: 16px;
  border: 1.5px solid rgba(var(--v-theme-on-surface), .07);
  overflow: hidden;
}
.day-header {
  display: flex; align-items: baseline; gap: 8px;
  padding: 12px 16px 10px;
  border-bottom: 1px solid rgba(var(--v-theme-on-surface), .06);
}
.day-name  { font-size: 13px; font-weight: 700; color: rgb(var(--v-theme-primary)); min-width: 22px }
.day-date  { font-size: 12px; color: rgba(var(--v-theme-on-surface), .45) }
.day-lessons { padding: 4px 0 6px }
.lesson-row {
  display: flex; align-items: center; gap: 10px;
  padding: 7px 16px;
  transition: background .12s;
}
.lesson-row:active { background: rgba(var(--v-theme-on-surface), .04) }
.lesson-num-badge {
  width: 20px; height: 20px; border-radius: 6px;
  background: rgba(var(--v-theme-on-surface), .06);
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 600; color: rgba(var(--v-theme-on-surface), .4);
  flex-shrink: 0;
}
.lesson-subject {
  flex: 1; font-size: 14px; font-weight: 500;
  color: rgb(var(--v-theme-on-surface));
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}

/* ── Animation ── */
@keyframes pulse { 0%, 100% { opacity: 1 } 50% { opacity: .4 } }
</style>