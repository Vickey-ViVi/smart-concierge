<template>
  <div class="page-card">
    <div class="page-header">
      <div class="page-title">活动管理</div>
      <el-button type="primary" @click="openEdit()">新增活动</el-button>
    </div>

    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="标题" prop="title" min-width="160" />
      <el-table-column label="地点" prop="location" width="120" />
      <el-table-column label="开始" width="170">
        <template #default="{ row }">{{ formatTime(row.startDate) }}</template>
      </el-table-column>
      <el-table-column label="结束" width="170">
        <template #default="{ row }">{{ formatTime(row.endDate) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '进行中' : '已结束' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确认删除？" @confirm="onDelete(row.id)">
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      class="pagination"
      layout="total, prev, pager, next"
      :total="total"
      @change="loadData"
    />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑活动' : '新增活动'" width="560px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="横幅图">
          <el-input v-model="form.bannerUrl" placeholder="图片 URL 或上传后粘贴" />
          <el-upload
            class="mt-8"
            :show-file-list="false"
            :http-request="uploadBanner"
          >
            <el-button size="small">上传图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-date-picker v-model="form.startDate" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-date-picker v-model="form.endDate" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="详情">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getActivityPage, saveActivity, deleteActivity, uploadFile } from '@/api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const form = reactive({
  id: null,
  title: '',
  bannerUrl: '',
  startDate: '',
  endDate: '',
  location: '',
  description: '',
  status: 1
})

function formatTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').slice(0, 19)
}

async function loadData() {
  loading.value = true
  try {
    const data = await getActivityPage({ pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  if (row) {
    Object.assign(form, {
      ...row,
      startDate: row.startDate ? String(row.startDate).slice(0, 19) : '',
      endDate: row.endDate ? String(row.endDate).slice(0, 19) : ''
    })
  } else {
    Object.assign(form, {
      id: null,
      title: '',
      bannerUrl: '',
      startDate: '',
      endDate: '',
      location: '',
      description: '',
      status: 1
    })
  }
  dialogVisible.value = true
}

async function uploadBanner({ file }) {
  const res = await uploadFile(file)
  form.bannerUrl = res.url
  ElMessage.success('上传成功')
}

async function onSave() {
  if (!form.title?.trim()) {
    ElMessage.warning('请填写标题')
    return
  }
  await saveActivity({ ...form })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function onDelete(id) {
  await deleteActivity(id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.pagination { margin-top: 16px; justify-content: flex-end; }
.mt-8 { margin-top: 8px; }
</style>
