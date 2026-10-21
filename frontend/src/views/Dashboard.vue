<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { getDashboard } from '../api/dashboard';
import { ref } from 'vue';
import { onMounted } from 'vue';
import type { DashBoardVO } from '../api/dashboard.ts';
import { getRequestError } from '../utils/requestError.ts';

const loading = ref(false)
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

.stats {
  display: grid;

  grid-template-columns:
    repeat(3, minmax(0, 1fr));

  gap: 16px;

  margin-bottom: 24px;
}

.stat-card {
  text-align: left;
}

.stat-card :deep(.el-card__body) {
  padding: 24px;
}

.stat-title {
  font-size: 14px;
  color: var(--ui-muted);
}

.stat-value {
  margin-top: 16px;

  font-size: 36px;
  font-weight: 650;
  line-height: 1.2;
  letter-spacing: -1px;
  font-variant-numeric: tabular-nums;
  overflow-wrap: anywhere;
}

.stat-card:first-child { background: var(--el-color-primary-light-9); border-color: var(--el-color-primary-light-8); }
.stat-card:first-child .stat-value { color: var(--el-color-primary); }

.section {
  margin-top: 32px;
}

@media (max-width: 900px) {
  .stats { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 600px) {
  .stats { gap: 12px; }
  .stat-card :deep(.el-card__body) { padding: 20px 16px; }
  .stat-value { font-size: 30px; }
}

</style>
