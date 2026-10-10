<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { getDashboard } from '../api/dashboard';
import { reactive, ref } from 'vue';
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import type { DashBoardVO } from '../api/dashboard.ts';
import { getRequestError } from '../utils/requestError.ts';

const loading = ref(false)
const router = useRouter()
const dashboard = ref<DashBoardVO|null>(null)

async function loadDashboard(){

    loading.value = true

    try{
        const response = await getDashboard()
        const result = response.data

        if(result.code!==200){
            ElMessage.error(result.message)
            return
        }

        dashboard.value = result.data
        
    }catch(error){

        ElMessage.error(getRequestError(error,"Dashboard加载失败"))
    }finally{
      
        loading.value = false
    }
}

onMounted(()=>{
    loadDashboard()
})

</script>

<template>

  <div
    class="page"
    v-loading="loading"
  >

    <h2>Dashboard</h2>

    <div
      v-if="dashboard"
      class="stats"
    >

      <el-card class="stat-card">
        <div class="stat-title">
          总工单
        </div>

        <div class="stat-value">
          {{ dashboard.totalCount }}
        </div>
      </el-card>


      <el-card class="stat-card">
        <div class="stat-title">
          待处理
        </div>

        <div class="stat-value">
          {{ dashboard.openCount }}
        </div>
      </el-card>


      <el-card class="stat-card">
        <div class="stat-title">
          处理中
        </div>

        <div class="stat-value">
          {{ dashboard.inProgressCount }}
        </div>
      </el-card>


      <el-card class="stat-card">
        <div class="stat-title">
          已解决
        </div>

        <div class="stat-value">
          {{ dashboard.resolvedCount }}
        </div>
      </el-card>


      <el-card class="stat-card">
        <div class="stat-title">
          已关闭
        </div>

        <div class="stat-value">
          {{ dashboard.closedCount }}
        </div>
      </el-card>


      <el-card class="stat-card">
        <div class="stat-title">
          今日新增
        </div>

        <div class="stat-value">
          {{ dashboard.todayCount }}
        </div>
      </el-card>

    </div>

    <section
        v-if="dashboard"
        class="section"
        >

        <h3>工单分类统计</h3>

        <el-table
            :data="dashboard.categoryCount"
            border
        >

            <el-table-column
                prop="categoryName"
                label="分类"
            />

            <el-table-column
                prop="count"
                label="工单数量"
                width="150"
            />

        </el-table>

    </section>

    </div>

</template>

<style scoped>

.page {
  padding: 24px;
}

.stats {
  display: grid;

  grid-template-columns:
    repeat(3, 1fr);

  gap: 16px;

  margin-bottom: 24px;
}

.stat-card {
  text-align: center;
}

.stat-title {
  font-size: 14px;
  color: #666;
}

.stat-value {
  margin-top: 12px;

  font-size: 32px;
  font-weight: bold;
}

.section {
  margin-top: 24px;
}

</style>