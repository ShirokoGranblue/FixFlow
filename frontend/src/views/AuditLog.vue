<script setup lang="ts">
import { onMounted, reactive, ref }from 'vue'
import { getAuditLog, type AuditLog } from '../api/audit.ts';
import { ElMessage } from 'element-plus';

const loading = ref(false)
const logs = ref<AuditLog[] | null>([])
const page = reactive({
    current:1,
    pageSize:10
})
const total = ref(0)

async function loadAuditLog(){
    loading.value = true

    try{
        const response = await getAuditLog(page)
        const result = response.data

        if(result.code!==200){
            ElMessage.error(result.message)
            return
        }

        total.value = result.data.total
        logs.value = result.data.records

    }catch(error){
        ElMessage.error("获取审计日志失败")
    }finally{
        loading.value = false
    }
}

async function handlePageChange(){

    loadAuditLog()
}

async function handleSizeChange(){
    page.current=1
    loadAuditLog()
}

onMounted(()=>{
    loadAuditLog()
})

</script>

<template>

  <div class="page">

    <h2>审计日志</h2>

    <el-table
      :data="logs"
      v-loading="loading"
      border
    >

      <el-table-column
        prop="id"
        label="ID"
        width="80"
      />

      <el-table-column
        prop="userId"
        label="用户ID"
        width="100"
      />

      <el-table-column
        prop="ip"
        label="请求ip"
        width="100"
      />

      <el-table-column
        prop="action"
        label="操作"
        width="160"
      />

      <el-table-column
        prop="method"
        label="调用方法"
        min-width="220"
      />

      <el-table-column
        label="结果"
        width="100"
      >

        <template #default="scope">

          <el-tag
            v-if="scope.row.success"
            type="success"
          >
            成功
          </el-tag>

          <el-tag
            v-else
            type="danger"
          >
            失败
          </el-tag>

        </template>

      </el-table-column>

    <el-table-column
        label="错误信息"
        min-width="200"
      >

        <template #default="scope">

            <span>
            {{ scope.row.errorMessage ?? '-' }}
            </span>

        </template>

    </el-table-column>

      <el-table-column
        prop="durationMs"
        label="耗时(ms)"
        width="110"
      />

      <el-table-column
        prop="createdAt"
        label="时间"
        width="190"
      />

    </el-table>


    <el-pagination
      v-model:current-page="page.current"
      v-model:page-size="page.pageSize"

      :total="total"

      :page-sizes="[20, 50, 100]"

      layout="total, sizes, prev, pager, next"

      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />

  </div>

</template>