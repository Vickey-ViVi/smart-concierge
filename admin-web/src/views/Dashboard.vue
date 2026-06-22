<template>
  <div v-loading="loading">
    <el-row :gutter="16" class="mb-16">
      <el-col :span="8">
        <div class="stat-card">
          <div class="label">今日使用人数</div>
          <div class="value">{{ overview.todayUsers ?? 0 }}</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="label">待处理工单</div>
          <div class="value">{{ overview.pendingComplaints ?? 0 }}</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="label">超阈值品牌</div>
          <div class="value">{{ overview.overThresholdBrands ?? 0 }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <div class="page-card">
          <div class="page-title mb-12">热门提问 TOP5</div>
          <el-table :data="questions" size="small" stripe>
            <el-table-column label="意图" prop="intent">
              <template #default="{ row }">{{ intentLabel(row.intent) }}</template>
            </el-table-column>
            <el-table-column label="次数" prop="count" width="80" />
          </el-table>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="page-card">
          <div class="page-title mb-12">抱怨 TOP5</div>
          <el-table :data="complaints" size="small" stripe>
            <el-table-column label="类型" prop="type">
              <template #default="{ row }">{{ typeLabel(row.type) }}</template>
            </el-table-column>
            <el-table-column label="次数" prop="count" width="80" />
          </el-table>
        </div>
      </el-col>
    </el-row>

    <div class="page-card mt-16">
      <div class="page-title mb-12">近 7 日趋势</div>
      <v-chart class="chart" :option="chartOption" autoresize />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { getOverview, getTopQuestions, getTopComplaints, getTrend } from '@/api'
import { COMPLAINT_TYPES, INTENT_LABELS } from '@/utils/constants'

use([CanvasRenderer, LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent])

const loading = ref(false)
const overview = ref({})
const questions = ref([])
const complaints = ref([])
const trend = ref({ dates: [], complaintCounts: [], satisfactionIndex: [] })

const chartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['投诉量', '满意度指数'] },
  grid: { left: 48, right: 48, bottom: 32 },
  xAxis: { type: 'category', data: trend.value.dates || [] },
  yAxis: [
    { type: 'value', name: '投诉量' },
    { type: 'value', name: '满意度', max: 100 }
  ],
  series: [
    {
      name: '投诉量',
      type: 'line',
      smooth: true,
      data: trend.value.complaintCounts || [],
      itemStyle: { color: '#c9a96e' }
    },
    {
      name: '满意度指数',
      type: 'bar',
      yAxisIndex: 1,
      data: trend.value.satisfactionIndex || [],
      itemStyle: { color: '#1e2a3a' }
    }
  ]
}))

function intentLabel(v) {
  return INTENT_LABELS[v] || v || '—'
}

function typeLabel(v) {
  return COMPLAINT_TYPES[v] || v
}

onMounted(async () => {
  loading.value = true
  try {
    const [o, q, c, t] = await Promise.all([
      getOverview(),
      getTopQuestions(),
      getTopComplaints(),
      getTrend()
    ])
    overview.value = o
    questions.value = q
    complaints.value = c
    trend.value = t
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.mb-16 { margin-bottom: 16px; }
.mb-12 { margin-bottom: 12px; }
.mt-16 { margin-top: 16px; }
.chart { height: 360px; width: 100%; }
</style>
