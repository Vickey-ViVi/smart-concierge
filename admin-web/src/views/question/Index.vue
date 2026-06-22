<template>
  <div class="page-card">
    <div class="page-header">
      <div class="page-title">智能推荐记录</div>
    </div>

    <el-form :inline="true" class="mb-16">
      <el-form-item label="类型">
        <el-select v-model="query.intent" clearable placeholder="全部" style="width: 120px">
          <el-option v-for="(label, key) in INTENT_LABELS" :key="key" :label="label" :value="key" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="类型" width="90">
        <template #default="{ row }">{{ row.intentLabel }}</template>
      </el-table-column>
      <el-table-column label="推荐店铺" prop="answerText" min-width="180" show-overflow-tooltip />
      <el-table-column label="Deepseek" width="90">
        <template #default="{ row }">
          <el-tag :type="row.useDeepseek ? 'success' : 'info'" size="small">
            {{ row.useDeepseek ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="耗时" width="80">
        <template #default="{ row }">{{ row.responseTimeMs }}ms</template>
      </el-table-column>
      <el-table-column label="操作" width="80">
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

    <el-drawer v-model="drawerVisible" title="推荐详情" size="560px">
      <template v-if="detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="咨询类型">{{ detail.intentLabel }}</el-descriptions-item>
          <el-descriptions-item label="提问摘要">{{ detail.questionText }}</el-descriptions-item>
          <el-descriptions-item label="推荐店铺">{{ detail.answerText }}</el-descriptions-item>
          <el-descriptions-item label="AI推荐">{{ detail.useDeepseek ? '是' : '否' }}</el-descriptions-item>
          <el-descriptions-item label="时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
        </el-descriptions>

        <div class="section-title">推荐理由</div>
        <div v-for="(shop, i) in detail.recommendations || []" :key="i" class="rec-card">
          <div class="rec-name">{{ i + 1 }}. {{ shop.name }}</div>
          <div class="rec-reason">{{ shop.reason || '—' }}</div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getQuestionList, getQuestionDetail } from '@/api'
import { INTENT_LABELS } from '@/utils/constants'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ intent: null, pageNum: 1, pageSize: 10 })
const drawerVisible = ref(false)
const detail = ref(null)

function formatTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').slice(0, 19)
}

async function loadData() {
  loading.value = true
  try {
    const data = await getQuestionList({ ...query })
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function openDetail(id) {
  detail.value = await getQuestionDetail(id)
  drawerVisible.value = true
}

onMounted(loadData)
</script>

<style scoped>
.mb-16 { margin-bottom: 16px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
.section-title { font-weight: 600; margin: 20px 0 12px; color: #1e2a3a; }
.rec-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 10px;
}
.rec-name { font-weight: 600; color: #1e2a3a; margin-bottom: 6px; }
.rec-reason { font-size: 13px; color: #606266; line-height: 1.5; }
</style>
