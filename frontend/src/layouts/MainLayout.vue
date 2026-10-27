<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const isStaff = computed(()=>{
    const role = userStore.user?.role
    return role==='STAFF'||role==='ADMIN'
})
const isAdmin = computed(()=>{
    const role = userStore.user?.role
    return role =='ADMIN'
})

async function handleLogout(){

  await userStore.setlogout()
  return router.push('/user/login')
}

</script>

<template>

  <el-container class="layout">

    <!-- 左侧导航 -->
    <el-aside width="220px">

      <div class="logo">
        FixFlow
      </div>

      <el-menu
        router
        class="menu"
        :default-active="route.path"
      >

        <el-menu-item index="/ticket/my-tickets">
          <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false"><rect x="4" y="3" width="16" height="18" rx="3"/><path d="M8 8h8M8 12h8M8 16h5"/></svg>
          我的工单
        </el-menu-item>

        <el-menu-item index="/ticket/create">
          <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false"><rect x="3" y="3" width="18" height="18" rx="4"/><path d="M12 8v8M8 12h8"/></svg>
          新建工单
        </el-menu-item>


        <el-menu-item
          v-if="isStaff"
          index="/dashboard"
        >
          Dashboard
          <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false"><path d="M4 4h6v6H4zM14 4h6v6h-6zM4 14h6v6H4zM14 14h6v6h-6z"/></svg>
        </el-menu-item>

        <el-menu-item
          v-if="isStaff"
          index="/ticket/staff"
        >
          STAFF工作台
          <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false"><rect x="3" y="7" width="18" height="14" rx="3"/><path d="M8 7V4h8v3M3 12a21 21 0 0 0 18 0M12 12v4"/></svg>
        </el-menu-item>


        <el-menu-item
          v-if="isAdmin"
          index="/ticket/manage"
        >
          工单管理
          <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false"><rect x="3" y="4" width="18" height="16" rx="3"/><path d="M3 9h18M9 9v11M13 13h4M13 16h4"/></svg>
        </el-menu-item>

        <el-menu-item
          v-if="isAdmin"
          index="/admin/users"
        >
          用户管理
          <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false"><circle cx="9" cy="8" r="3"/><path d="M3 21v-2a6 6 0 0 1 12 0v2M16 5a3 3 0 0 1 0 6M18 14a5 5 0 0 1 3 5v2"/></svg>
        </el-menu-item>

        <el-menu-item
          v-if="isAdmin"
          index="/audit-log"
        >
          审计日志
          <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false"><path d="M12 3 4 6v6c0 5 8 9 8 9s8-4 8-9V6zM8 12l3 3 5-6"/></svg>
        </el-menu-item>

      </el-menu>

    </el-aside>


    <!-- 右边整体 -->
    <el-container>

      <el-header class="header">

        <div></div>

        <div class="user-area">

          <span v-if="userStore.user">
            {{ userStore.user.username }}
            （{{ userStore.user.role }}）
          </span>

          <el-button
            link
            type="danger"
            @click="handleLogout"
          >
            <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false"><path d="M9 5H5v14h4M9 12h12M17 8l4 4-4 4"/></svg>
            退出登录
          </el-button>

        </div>

      </el-header>

      <!-- 业务界面 -->
      <el-main class="main">

        <router-view />

      </el-main>

    </el-container>

  </el-container>

</template>

<style scoped>

.layout {
  min-height: 100vh;
  --el-menu-text-color: var(--ui-muted);
  --el-menu-active-color: var(--el-color-primary);
  --el-menu-hover-bg-color: var(--ui-soft);
}

.el-aside {
  position: sticky;
  top: 0;
  height: 100vh;
  flex-shrink: 0;
  padding: 0 12px;
  background: var(--ui-surface);
  border-right: 1px solid var(--ui-border);
}

.logo {
  height: 80px;

  display: flex;
  align-items: center;
  padding-left: 20px;

  font-size: 25px;
  font-weight: 700;
  letter-spacing: -1px;
  color: var(--el-text-color-primary);
}

.menu {
  border-right: none;
  padding-top: 16px;
}

.menu :deep(.el-menu-item) {
  height: 46px;
  margin-bottom: 8px;
  padding: 0 16px;
  gap: 12px;
  border-radius: 8px;
  font-weight: 500;
}

.menu :deep(.el-menu-item.is-active) {
  background: var(--el-color-primary-light-9);
  font-weight: 600;
}

.menu .ui-icon { order: -1; }

.layout > .el-container {
  min-width: 0;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  min-height: 72px;
  height: auto;
  padding: 16px 32px;
  background: var(--ui-surface);
  border-bottom: 1px solid var(--ui-border);
}

.user-area {
  display: flex;
  align-items: center;

  gap: 16px;
  flex-wrap: wrap;
  justify-content: flex-end;
  color: var(--ui-muted);
}

.main {
  padding: 32px;
  min-width: 0;
  background: var(--el-bg-color-page);
}

@media (max-width: 900px) {
  .el-aside { width: 190px; padding: 0 8px; }
  .header { padding: 16px 24px; }
  .main { padding: 24px; }
}

@media (max-width: 600px) {
  .layout { flex-direction: column; }
  .el-aside { position: static; width: 100%; height: auto; border-right: 0; border-bottom: 1px solid var(--ui-border); }
  .logo { height: 60px; padding-left: 12px; font-size: 22px; }
  .menu { display: flex; overflow-x: auto; padding: 0 4px 8px; gap: 8px; }
  .menu :deep(.el-menu-item) { flex-shrink: 0; height: 42px; margin: 0; padding: 0 12px; gap: 8px; }
  .header { min-height: 60px; padding: 12px 16px; }
  .user-area { gap: 8px 16px; font-size: 13px; overflow-wrap: anywhere; }
  .main { padding: 24px 16px; }
}

</style>
