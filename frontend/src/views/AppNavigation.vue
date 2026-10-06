<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'


const router = useRouter()
const userStore = useUserStore()
const isStaff = computed(()=>
{
    const role = userStore.user?.role
    return role==='STAFF'||role==='ADMIN'
})

</script>

<template>

  <el-menu
    mode="horizontal"
    router
  >

    <el-menu-item index="/ticket/my-tickets">
      我的工单
    </el-menu-item>

    <el-menu-item index="/ticket/create">
      新建工单
    </el-menu-item>


    <el-menu-item
      v-if="isStaff"
      index="/ticket/staff"
    >
      STAFF工作台
    </el-menu-item>


    <el-menu-item
      v-if="isStaff"
      index="/ticket/manage"
    >
      工单管理
    </el-menu-item>

  </el-menu>

</template>