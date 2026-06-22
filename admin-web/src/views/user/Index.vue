<template>
  <div class="page-card">
    <div class="page-header">
      <div class="page-title">用户管理</div>
      <el-button type="primary" @click="openEdit()">新增用户</el-button>
    </div>

    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="用户名" prop="username" width="120" />
      <el-table-column label="姓名" prop="realName" width="100" />
      <el-table-column label="角色" prop="role" width="90">
        <template #default="{ row }">
          <el-tag size="small">{{ row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="电话" prop="phone" width="130" />
      <el-table-column label="邮箱" prop="email" min-width="160" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最后登录" width="170">
        <template #default="{ row }">{{ formatTime(row.lastLogin) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link @click="onReset(row.id)">重置密码</el-button>
          <el-popconfirm title="确认删除？" @confirm="onDelete(row.id)">
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item :label="form.id ? '新密码' : '密码'">
          <el-input v-model="form.password" type="password" :placeholder="form.id ? '留空则不修改' : '默认 123456'" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="客服" value="客服" />
            <el-option label="运营" value="运营" />
            <el-option label="招商" value="招商" />
            <el-option label="管理员" value="管理员" />
          </el-select>
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, saveUser, deleteUser, resetPassword } from '@/api'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const form = reactive({
  id: null,
  username: '',
  password: '',
  realName: '',
  role: '客服',
  phone: '',
  email: '',
  status: 1
})

function formatTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').slice(0, 19)
}

async function loadData() {
  loading.value = true
  try {
    list.value = await getUserList()
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  if (row) {
    Object.assign(form, { ...row, password: '' })
  } else {
    Object.assign(form, {
      id: null,
      username: '',
      password: '',
      realName: '',
      role: '客服',
      phone: '',
      email: '',
      status: 1
    })
  }
  dialogVisible.value = true
}

async function onSave() {
  if (!form.username?.trim()) {
    ElMessage.warning('请填写用户名')
    return
  }
  await saveUser({ ...form })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function onReset(id) {
  const res = await resetPassword(id)
  ElMessageBox.alert(`临时密码：${res.tempPassword}`, '密码已重置', { type: 'success' })
}

async function onDelete(id) {
  await deleteUser(id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>
