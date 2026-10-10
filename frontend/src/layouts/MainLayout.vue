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
          我的工单
        </el-menu-item>

        <el-menu-item index="/ticket/create">
          新建工单
        </el-menu-item>


        <el-menu-item
          v-if="isStaff"
          index="/dashboard"
        >
          Dashboard
        </el-menu-item>

        <el-menu-item
          v-if="isStaff"
          index="/ticket/staff"
        >
          STAFF工作台
        </el-menu-item>


        <el-menu-item
          v-if="isAdmin"
          index="/ticket/manage"
        >
          工单管理
        </el-menu-item>

        <el-menu-item
          v-if="isAdmin"
          index="/admin/users"
        >
          用户管理
        </el-menu-item>

        <el-menu-item
          v-if="isAdmin"
          index="/audit-log"
        >
          审计日志
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
}

.el-aside {
  border-right: 1px solid #ddd;
}

.logo {
  height: 64px;

  display: flex;
  align-items: center;
  justify-content: center;

  font-size: 22px;
  font-weight: bold;
}

.menu {
  border-right: none;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  border-bottom: 1px solid #ddd;
}

.user-area {
  display: flex;
  align-items: center;

  gap: 16px;
}

.main {
  background: #f5f5f5;
}

</style>