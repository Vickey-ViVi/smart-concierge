<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <span class="logo-icon">M</span>
        <div>
          <div class="logo-title">万象智慧助手</div>
          <div class="logo-sub">企业后台</div>
        </div>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#1e2a3a"
        text-color="#b8c0cc"
        active-text-color="#c9a96e"
      >
        <el-menu-item v-if="show('dashboard')" index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>会员之声</span>
        </el-menu-item>
        <el-menu-item v-if="show('question')" index="/question">
          <el-icon><ChatLineRound /></el-icon>
          <span>推荐记录</span>
        </el-menu-item>
        <el-menu-item v-if="show('complaint')" index="/complaint">
          <el-icon><ChatDotSquare /></el-icon>
          <span>工单管理</span>
        </el-menu-item>
        <el-menu-item v-if="show('lost')" index="/lost">
          <el-icon><Box /></el-icon>
          <span>失物登记</span>
        </el-menu-item>
        <el-menu-item v-if="show('proposal')" index="/proposal">
          <el-icon><ShoppingBag /></el-icon>
          <span>入驻提议</span>
        </el-menu-item>
        <el-menu-item v-if="show('shop')" index="/shop">
          <el-icon><Shop /></el-icon>
          <span>店铺管理</span>
        </el-menu-item>
        <el-menu-item v-if="show('activity')" index="/activity">
          <el-icon><Calendar /></el-icon>
          <span>活动管理</span>
        </el-menu-item>
        <el-menu-item v-if="show('user')" index="/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item v-if="show('config')" index="/config">
          <el-icon><Setting /></el-icon>
          <span>系统配置</span>
        </el-menu-item>
        <el-menu-item v-if="show('audit')" index="/audit">
          <el-icon><Document /></el-icon>
          <span>审计日志</span>
        </el-menu-item>
        <el-menu-item v-if="show('monitor')" index="/monitor">
          <el-icon><Monitor /></el-icon>
          <span>系统监控</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">{{ currentTitle }}</div>
        <div class="header-right">
          <el-tag type="warning" effect="dark">{{ auth.role }}</el-tag>
          <span class="user-name">{{ auth.realName }}</span>
          <el-button link type="primary" @click="onLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { hasMenu } from '@/utils/role'

const route = useRoute()
const auth = useAuthStore()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '万象后台')

function show(menu) {
  return hasMenu(auth.role, menu)
}

function onLogout() {
  auth.logout()
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.aside {
  background: #1e2a3a;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 16px;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #c9a96e;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #1e2a3a;
}

.logo-title {
  font-size: 15px;
  font-weight: 600;
}

.logo-sub {
  font-size: 12px;
  opacity: 0.65;
  margin-top: 2px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  height: 56px;
  padding: 0 24px;
}

.header-left {
  font-size: 16px;
  font-weight: 600;
  color: #1e2a3a;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  color: #606266;
  font-size: 14px;
}

.main {
  padding: 20px;
  background: var(--mixc-bg);
}
</style>
