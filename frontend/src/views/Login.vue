<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/user'
import { useUserStore } from '../stores/user.ts'

const router = useRouter()
const userStore = useUserStore()

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

    if (result.code !== 0) {
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
}
</script>

<template>
  <div class="login-page">

    <el-card class="login-card">

      <h2>FixFlow</h2>

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

      </el-form>

    </el-card>

  </div>
</template>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 400px;
}
</style>