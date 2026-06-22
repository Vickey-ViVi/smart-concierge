<template>
  <div class="page-card">
    <div class="page-header">
      <div class="page-title">店铺管理</div>
      <el-button type="primary" @click="openEdit()">新增店铺</el-button>
    </div>

    <el-form :inline="true" class="mb-16">
      <el-form-item label="搜索">
        <el-input v-model="keyword" placeholder="店铺名称" clearable @keyup.enter="loadData" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="名称" prop="name" min-width="120" />
      <el-table-column label="楼层" prop="floor" width="80" />
      <el-table-column label="大类" prop="category" width="90" />
      <el-table-column label="子类" prop="subCategory" width="90" />
      <el-table-column label="人均" prop="avgPrice" width="80">
        <template #default="{ row }">¥{{ row.avgPrice ?? '—' }}</template>
      </el-table-column>
      <el-table-column label="标签" prop="tags" min-width="140" show-overflow-tooltip />
      <el-table-column label="点赞" prop="likeCount" width="70" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑店铺' : '新增店铺'" width="520px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="楼层">
          <el-input v-model="form.floor" placeholder="L1" />
        </el-form-item>
        <el-form-item label="大类">
          <el-input v-model="form.category" placeholder="美食/娱乐/亲子" />
        </el-form-item>
        <el-form-item label="子类">
          <el-input v-model="form.subCategory" />
        </el-form-item>
        <el-form-item label="人均">
          <el-input-number v-model="form.avgPrice" :min="0" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder='JSON 如 ["网红","不辣"]' />
        </el-form-item>
        <el-form-item label="优惠">
          <el-input v-model="form.discountInfo" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="点赞数">
          <el-input-number v-model="form.likeCount" :min="0" />
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
import { getShopPage, saveShop, deleteShop } from '@/api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const keyword = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const form = reactive({
  id: null,
  name: '',
  floor: '',
  category: '',
  subCategory: '',
  tags: '[]',
  avgPrice: 0,
  discountInfo: '',
  likeCount: 0,
  status: 1
})

async function loadData() {
  loading.value = true
  try {
    const data = await getShopPage({ keyword: keyword.value, pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, {
      id: null,
      name: '',
      floor: '',
      category: '',
      subCategory: '',
      tags: '[]',
      avgPrice: 0,
      discountInfo: '',
      likeCount: 0,
      status: 1
    })
  }
  dialogVisible.value = true
}

async function onSave() {
  if (!form.name?.trim()) {
    ElMessage.warning('请填写店铺名称')
    return
  }
  await saveShop({ ...form })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function onDelete(id) {
  await deleteShop(id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.mb-16 { margin-bottom: 16px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
