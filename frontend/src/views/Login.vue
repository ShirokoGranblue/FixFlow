<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/user'
import { useUserStore } from '../stores/user.ts'

const router = useRouter()
const userStore = useUserStore()
const route = useRoute()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

async function handleLogin() {
  
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true

  try {
    const response = await login(form)

    const result = response.data

    if (result.code !== 200) {
      ElMessage.error(result.message)
      return
    }

    const loginData = result.data

    userStore.setLogin(
        loginData.tokenName,
        loginData.tokenValue,
        loginData.user
    )

    ElMessage.success('登录成功')

    router.push('/')

  } catch (error) {
    ElMessage.error('登录失败')
  } finally {
    loading.value = false
  }

onMounted(() => {
    if (route.query.reason === 'session-expired') {
        ElMessage.warning('登录状态已失效，请重新登录')

        const query = { ...route.query }
        delete query.reason
        void router.replace({ query })
    }
})
}
</script>

<template>
  <div class="login-page">

    <el-card class="login-card">

      <div class="auth-heading">
        <span class="auth-mark" aria-hidden="true"><svg class="ui-icon" viewBox="0 0 24 24" focusable="false"><rect x="4" y="3" width="16" height="18" rx="3"/><path d="m8 12 3 3 5-6"/></svg></span>
        <h2>FixFlow</h2>
      </div>

      <el-form :model="form">

        <el-form-item label="用户名">
          <el-input
              v-model="form.username"
              placeholder="请输入用户名"
          />
        </el-form-item>

        <el-form-item label="密码">
          <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="请输入密码"
          />
        </el-form-item>

        <el-button
            type="primary"
            :loading="loading"
            @click="handleLogin"
        >
          登录
        </el-button>

        <el-button
            @click="router.push('/user/register')"
          >
            注册账号
          </el-button>

      </el-form>

    </el-card>

  </div>
</template>
