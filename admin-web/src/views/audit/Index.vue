<template>
  <div class="page-card">
    <div class="page-header">
      <div class="page-title">审计日志</div>
      <el-button @click="onExport">导出 CSV</el-button>
    </div>

    <el-form :inline="true" class="mb-16">
      <el-form-item label="用户名">
        <el-input v-model="query.username" clearable placeholder="操作人" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="用户名" prop="username" width="100" />
      <el-table-column label="姓名" prop="realName" width="90" />
      <el-table-column label="操作" prop="action" min-width="200" />
      <el-table-column label="模块" prop="module" width="80" />
      <el-table-column label="IP" prop="ipAddress" width="130" />
    </el-table>

    <el-pagination
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      class="pagination"
      layout="total, prev, pager, next"
      :total="total"
      @change="loadData"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAuditLogs, exportAudit } from '@/api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ username: '', pageNum: 1, pageSize: 20 })

function formatTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').slice(0, 19)
}

async function loadData() {
  loading.value = true
  try {
    const data = await getAuditLogs({ ...query })
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function onExport() {
  const res = await exportAudit({ username: query.username })
  const url = URL.createObjectURL(res.data)
  const a = document.createElement('a')
  a.href = url
  a.download = 'audit_logs.csv'
  a.click()
  URL.revokeObjectURL(url)
}

onMounted(loadData)
</script>

<style scoped>
.mb-16 { margin-bottom: 16px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
