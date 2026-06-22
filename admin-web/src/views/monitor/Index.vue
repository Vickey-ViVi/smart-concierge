<template>
  <div v-loading="loading">
    <el-row :gutter="16">
      <el-col :span="6">
        <div class="stat-card">
          <div class="label">Deepseek 状态</div>
          <div class="value" :class="statusClass">{{ stats.statusLabel || '—' }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="label">今日成功调用</div>
          <div class="value">{{ stats.todayCalls ?? 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="label">本月成功调用</div>
          <div class="value">{{ stats.monthCalls ?? 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="label">平均耗时</div>
          <div class="value">{{ stats.avgLatencyMs ?? 0 }}ms</div>
        </div>
      </el-col>
    </el-row>

    <el-alert
      v-if="stats.statusHint"
      class="mt-16"
      :type="alertType"
      :closable="false"
      :title="stats.statusLabel"
      :description="stats.statusHint"
    />

    <div class="page-card mt-16">
      <div class="page-title mb-12">配置与调用详情</div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="配置开关">{{ stats.switchOn ? '已开启' : '已关闭' }}</el-descriptions-item>
        <el-descriptions-item label="系统配置开关">{{ stats.dbSwitchOn ? 'true' : 'false' }}</el-descriptions-item>
        <el-descriptions-item label="API Key">{{ stats.keyConfigured ? '已配置' : '未配置' }}</el-descriptions-item>
        <el-descriptions-item label="Key 来源">{{ stats.keySourceLabel || '—' }}</el-descriptions-item>
        <el-descriptions-item label="系统配置 Key">{{ stats.dbKeyConfigured ? '已填写' : '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="实际可用">{{ stats.enabled ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="今日请求总数">{{ stats.todayTotal ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="本月请求总数">{{ stats.monthTotal ?? 0 }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="page-card mt-16">
      <div class="page-title mb-12">Deepseek 调用成功率（本月）</div>
      <el-progress
        v-if="successPercent != null"
        :percentage="successPercent"
        :status="successPercent > 0 ? 'success' : 'exception'"
      />
      <div v-else class="empty-rate">暂无请求记录</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getMonitorDeepseek } from '@/api'

const loading = ref(false)
const stats = ref({})

const statusClass = computed(() => {
  const code = stats.value.status
  if (code === 'active') return 'status-active'
  if (code === 'disabled' || code === 'no_key') return 'status-off'
  return 'status-warn'
})

const alertType = computed(() => {
  const code = stats.value.status
  if (code === 'active') return 'success'
  if (code === 'idle') return 'info'
  return 'warning'
})

const successPercent = computed(() => {
  const rate = stats.value.successRate
  if (!rate || rate === '—') return null
  return parseFloat(String(rate).replace('%', ''))
})

onMounted(async () => {
  loading.value = true
  try {
    stats.value = await getMonitorDeepseek()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.mt-16 { margin-top: 16px; }
.mb-12 { margin-bottom: 12px; }
.status-active { color: #67c23a; }
.status-warn { color: #e6a23c; }
.status-off { color: #909399; }
.empty-rate { color: #909399; font-size: 14px; }
</style>
