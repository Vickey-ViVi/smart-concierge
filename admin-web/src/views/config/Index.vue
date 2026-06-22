<template>
  <div class="page-card">
    <div class="page-header">
      <div class="page-title">系统配置</div>
    </div>

    <el-alert
      class="mb-16"
      type="info"
      :closable="false"
      title="Deepseek 配置说明"
      description="启用后，智能提问的自由对话与店铺推荐将调用 Deepseek。请编辑 deepseek_api_key 填入密钥（platform.deepseek.com 申请），保存后无需重启即可生效。也可在项目根目录创建 application-local.properties 配置 app.deepseek.api-key。"
    />

    <el-table v-loading="loading" :data="sortedList" stripe>
      <el-table-column label="配置键" prop="configKey" width="220" />
      <el-table-column label="当前值" min-width="160">
        <template #default="{ row }">
          <el-input
            v-if="editingKey === row.configKey"
            v-model="editValue"
            size="small"
            :type="isSecretKey(row.configKey) ? 'password' : 'text'"
            show-password
          />
          <span v-else>{{ displayValue(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="说明" prop="description" min-width="200" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <template v-if="editingKey === row.configKey">
            <el-button link type="primary" @click="saveRow(row)">保存</el-button>
            <el-button link @click="editingKey = ''">取消</el-button>
          </template>
          <el-button v-else link type="primary" @click="startEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getConfigList, updateConfig } from '@/api'

const loading = ref(false)
const list = ref([])
const editingKey = ref('')
const editValue = ref('')

const sortedList = computed(() => {
  const deepseekKeys = ['deepseek_enabled', 'deepseek_api_key', 'deepseek_base_url', 'deepseek_timeout']
  return [...list.value].sort((a, b) => {
    const ai = deepseekKeys.indexOf(a.configKey)
    const bi = deepseekKeys.indexOf(b.configKey)
    if (ai >= 0 && bi >= 0) return ai - bi
    if (ai >= 0) return -1
    if (bi >= 0) return 1
    return a.configKey.localeCompare(b.configKey)
  })
})

function isSecretKey(key) {
  return key === 'deepseek_api_key'
}

function displayValue(row) {
  if (isSecretKey(row.configKey) && row.configValue) {
    return '******（已配置）'
  }
  return row.configValue || '—'
}

async function loadData() {
  loading.value = true
  try {
    list.value = await getConfigList()
  } finally {
    loading.value = false
  }
}

function startEdit(row) {
  editingKey.value = row.configKey
  editValue.value = row.configValue
}

async function saveRow(row) {
  await updateConfig({ configKey: row.configKey, configValue: editValue.value })
  ElMessage.success('已更新')
  editingKey.value = ''
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.mb-16 { margin-bottom: 16px; }
</style>
