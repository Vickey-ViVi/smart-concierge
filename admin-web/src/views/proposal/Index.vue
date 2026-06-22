<template>
  <div class="page-card">
    <div class="page-header">
      <div class="page-title">入驻提议管理</div>
      <el-space>
        <el-button @click="showThreshold = true">阈值设置</el-button>
        <el-button type="primary" :loading="exporting" @click="onExport">导出超阈值报告</el-button>
      </el-space>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="品牌汇总" name="summary">
        <el-table v-loading="loading" :data="list" stripe>
          <el-table-column label="排名" width="70">
            <template #default="{ $index }">{{ $index + 1 }}</template>
          </el-table-column>
          <el-table-column label="品牌名称" prop="brandName" min-width="160" />
          <el-table-column label="提议人数" prop="count" width="100" sortable />
          <el-table-column label="阈值" prop="threshold" width="80" />
          <el-table-column label="是否达阈" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.count >= row.threshold" type="danger" size="small">已达标</el-tag>
              <el-tag v-else type="info" size="small">未达标</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="已通知" width="80">
            <template #default="{ row }">{{ row.notified ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column label="最近提议" width="170">
            <template #default="{ row }">{{ formatTime(row.lastProposeTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button link type="primary" @click="editBrandThreshold(row)">设阈值</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="提议明细（含理由）" name="records">
        <el-form :inline="true" class="mb-16">
          <el-form-item label="品牌">
            <el-input v-model="recordQuery.brandName" clearable placeholder="品牌名称" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadRecords">查询</el-button>
          </el-form-item>
        </el-form>
        <el-table v-loading="recordsLoading" :data="records" stripe>
          <el-table-column label="时间" width="170">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="品牌" prop="brandName" width="140" />
          <el-table-column label="推荐理由" prop="reason" min-width="260" show-overflow-tooltip />
          <el-table-column label="用户" prop="openid" width="140" />
        </el-table>
        <el-pagination
          v-model:current-page="recordQuery.pageNum"
          v-model:page-size="recordQuery.pageSize"
          class="pagination"
          layout="total, prev, pager, next"
          :total="recordsTotal"
          @change="loadRecords"
        />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="showThreshold" title="阈值设置" width="440px">
      <el-form label-width="120px">
        <el-form-item label="全局默认阈值">
          <el-input-number v-model="thresholdForm.globalThreshold" :min="1" :max="9999" />
        </el-form-item>
        <el-form-item label="品牌名称">
          <el-input v-model="thresholdForm.brandName" placeholder="留空则只改全局" />
        </el-form-item>
        <el-form-item label="品牌单独阈值">
          <el-input-number v-model="thresholdForm.threshold" :min="1" :max="9999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showThreshold = false">取消</el-button>
        <el-button type="primary" @click="saveThreshold">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProposalList, getProposalRecords, updateProposalThreshold, exportProposal } from '@/api'

const loading = ref(false)
const recordsLoading = ref(false)
const exporting = ref(false)
const list = ref([])
const records = ref([])
const recordsTotal = ref(0)
const activeTab = ref('summary')
const showThreshold = ref(false)
const thresholdForm = reactive({ brandName: '', threshold: 100, globalThreshold: 100 })
const recordQuery = reactive({ brandName: '', pageNum: 1, pageSize: 10 })

function formatTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').slice(0, 19)
}

async function loadData() {
  loading.value = true
  try {
    list.value = await getProposalList()
  } finally {
    loading.value = false
  }
}

async function loadRecords() {
  recordsLoading.value = true
  try {
    const data = await getProposalRecords({ ...recordQuery })
    records.value = data.list || []
    recordsTotal.value = data.total || 0
  } finally {
    recordsLoading.value = false
  }
}

function editBrandThreshold(row) {
  thresholdForm.brandName = row.brandName
  thresholdForm.threshold = row.threshold
  showThreshold.value = true
}

async function saveThreshold() {
  await updateProposalThreshold({ ...thresholdForm })
  ElMessage.success('已更新')
  showThreshold.value = false
  loadData()
}

async function onExport() {
  exporting.value = true
  try {
    const blob = await exportProposal()
    const url = URL.createObjectURL(blob.data)
    const a = document.createElement('a')
    a.href = url
    a.download = '超阈值品牌建议书.xlsx'
    a.click()
    URL.revokeObjectURL(url)
  } finally {
    exporting.value = false
  }
}

onMounted(() => {
  loadData()
  loadRecords()
})
</script>

<style scoped>
.mb-16 { margin-bottom: 16px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
