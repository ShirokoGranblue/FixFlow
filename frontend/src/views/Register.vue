<script setup lang ="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {register} from '../api/user.ts';


const loading = ref(false);
const router = useRouter();
const form = ref({
    username:'',
    password:'',
    confirmPassword:'',
    email:''
})

async function handleRegister(){
    loading.value = true;
    
    if(!form.value.username.trim()){
        ElMessage.warning("请输入用户名")
    }
    if(!form.value.password.trim()){
        ElMessage.warning("请填写密码")
    }
    if(!form.value.email.trim()){
        ElMessage.warning("请输入邮箱")
    }
    if (form.value.password.length < 6) {
        ElMessage.warning('密码至少6位')
        return
    }
    if (form.value.password !== form.value.confirmPassword) {
        ElMessage.warning('两次密码输入不一致')
        return
    }

    try{

        const response = await register(form.value);

        if(response.data.code !== 200 ){
            ElMessage.error(response.data.message);
            return 
        }

        ElMessage.success("注册成功")

        router.push(`/user/login`)

    }catch(error){

        ElMessage.error("账号注册失败")
    }finally{

        loading.value = false;
    }
}
</script>

<template>

  <div class="register-page">

    <el-card class="register-card">

      <h2>注册 FixFlow</h2>

      <el-form
        :model="form"
        label-width="90px"
      >

        <el-form-item label="用户名">

          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
          />

        </el-form-item>


        <el-form-item label="邮箱">

          <el-input
            v-model="form.email"
            placeholder="请输入邮箱"
          />

        </el-form-item>


        <el-form-item label="密码">

          <el-input
            v-model="form.password"
            type="password"
            show-password
          />

        </el-form-item>


        <el-form-item label="确认密码">

          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
          />

        </el-form-item>


        <el-form-item>

          <el-button
            type="primary"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>

          <el-button
            @click="router.push('/login')"
          >
            返回登录
          </el-button>

        </el-form-item>

      </el-form>

    </el-card>

  </div>

</template>

<style scoped>
.register-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.register-card {
  width: 420px;
}
</style>