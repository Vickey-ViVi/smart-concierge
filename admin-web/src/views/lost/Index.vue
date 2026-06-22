<template>
  <div class="page-card">
    <div class="page-header">
      <div class="page-title">失物登记</div>
      <el-button type="primary" @click="dialogVisible = true">登记拾获物品</el-button>
    </div>

    <el-alert
      title="客服登记拾获物品后，查看「失物类」工单时会自动模糊匹配物品名称。"
      type="info"
      :closable="false"
      show-icon
      class="mb-16"
    />

    <el-table :data="recentList" stripe v-loading="loading">
      <el-table-column label="物品名称" prop="itemName" />
      <el-table-column label="拾获地点" prop="location" />
      <el-table-column label="保管人" prop="keeper" width="100" />
      <el-table-column label="联系方式" prop="contact" width="130" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'warning' : 'success'" size="small">
            {{ row.status === 0 ? '待认领' : '已认领' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="登记时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="登记拾获物品" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="物品名称" required>
          <el-input v-model="form.itemName" placeholder="如：黑色钱包" />
        </el-form-item>
        <el-form-item label="拾获地点">
          <el-input v-model="form.location" placeholder="如：L2 中庭" />
        </el-form-item>
        <el-form-item label="保管人">
          <el-input v-model="form.keeper" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="form.contact" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { addLostFound, getLostList } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const recentList = ref([])
const form = reactive({ itemName: '', location: '', keeper: '', contact: '' })

function formatTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').slice(0, 19)
}

async function onSubmit() {
  if (!form.itemName.trim()) {
    ElMessage.warning('请填写物品名称')
    return
  }
  submitting.value = true
  try {
    const item = await addLostFound({ ...form })
    recentList.value = await getLostList()
    ElMessage.success('登记成功')
    dialogVisible.value = false
    Object.assign(form, { itemName: '', location: '', keeper: '', contact: '' })
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    recentList.value = await getLostList()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.mb-16 { margin-bottom: 16px; }
</style>
