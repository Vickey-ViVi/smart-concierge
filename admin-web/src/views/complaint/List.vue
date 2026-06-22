<template>
  <div class="page-card">
    <div class="page-header">
      <div class="page-title">工单管理</div>
    </div>

    <el-form :inline="true" class="filter-form">
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
          <el-option label="待处理" :value="0" />
          <el-option label="处理中" :value="1" />
          <el-option label="已完成" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="query.type" clearable placeholder="全部" style="width: 120px">
          <el-option v-for="(label, key) in COMPLAINT_TYPES" :key="key" :label="label" :value="key" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="工单号" prop="ticketNo" width="160" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">{{ COMPLAINT_TYPES[row.type] || row.type }}</template>
      </el-table-column>
      <el-table-column label="描述" prop="descriptionSummary" min-width="200" show-overflow-tooltip />
      <el-table-column label="手机号" prop="phoneMasked" width="130" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ COMPLAINT_STATUS[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理人" prop="assignee" width="90" />
      <el-table-column label="提交时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      class="pagination"
      layout="total, prev, pager, next"
      :total="total"
      @change="loadData"
    />

    <el-drawer v-model="drawerVisible" title="工单详情" size="520px">
      <template v-if="detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="工单号">{{ detail.ticketNo }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ COMPLAINT_TYPES[detail.type] }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detail.status)">{{ COMPLAINT_STATUS[detail.status] }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            {{ phoneDisplay }}
            <el-button link type="primary" size="small" @click="viewPhone">查看完整号码</el-button>
          </el-descriptions-item>
          <el-descriptions-item label="描述">{{ detail.description }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="detail.type === 'lost'" class="section">
          <div class="section-title">失物匹配</div>
          <el-table :data="lostMatches" size="small" stripe>
            <el-table-column label="物品" prop="itemName" />
            <el-table-column label="地点" prop="location" />
            <el-table-column label="保管人" prop="keeper" />
          </el-table>
        </div>

        <div class="section">
          <div class="section-title">处理记录</div>
          <el-timeline>
            <el-timeline-item v-for="(log, i) in detail.logs || []" :key="i" :timestamp="formatTime(log.createTime)">
              {{ log.operator }} · {{ log.action }}<span v-if="log.remark">：{{ log.remark }}</span>
            </el-timeline-item>
          </el-timeline>
        </div>

        <div v-if="detail.status !== 2" class="section">
          <div class="section-title">处理操作</div>
          <el-input v-model="remark" type="textarea" placeholder="处理备注" :rows="2" class="mb-8" />
          <el-space>
            <el-button v-if="detail.status === 0" type="primary" @click="doHandle('accept')">接单</el-button>
            <el-button v-if="detail.status === 1" type="success" @click="doHandle('complete')">已完成</el-button>
            <el-button v-if="detail.status === 1" @click="doHandle('transfer')">转交物业</el-button>
          </el-space>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getComplaintList,
  getComplaintDetail,
  getComplaintPhone,
  handleComplaint,
  matchLost
} from '@/api'
import { COMPLAINT_TYPES, COMPLAINT_STATUS } from '@/utils/constants'
import { statusTagType } from '@/utils/role'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ status: null, type: null, pageNum: 1, pageSize: 10 })
const drawerVisible = ref(false)
const detail = ref(null)
const phoneDisplay = ref('')
const remark = ref('')
const lostMatches = ref([])

function formatTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').slice(0, 19)
}

async function loadData() {
  loading.value = true
  try {
    const data = await getComplaintList({ ...query })
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function openDetail(id) {
  detail.value = await getComplaintDetail(id)
  phoneDisplay.value = detail.value.phoneMasked || '—'
  remark.value = ''
  lostMatches.value = []
  if (detail.value.type === 'lost') {
    lostMatches.value = await matchLost(id)
  }
  drawerVisible.value = true
}

async function viewPhone() {
  const phone = await getComplaintPhone(detail.value.id)
  phoneDisplay.value = phone
  ElMessage.success('已记录审计日志')
}

async function doHandle(action) {
  await handleComplaint({ id: detail.value.id, action, remark: remark.value })
  ElMessage.success('操作成功')
  await openDetail(detail.value.id)
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.filter-form { margin-bottom: 16px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
.section { margin-top: 20px; }
.section-title { font-weight: 600; margin-bottom: 8px; color: #1e2a3a; }
.mb-8 { margin-bottom: 8px; }
</style>
