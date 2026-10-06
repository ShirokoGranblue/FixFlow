<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAssignedTickets, getOpenTickets, handleTicket } from '../api/ticket'
import type { TicketVO } from '../api/ticket'

const router = useRouter()
const loading = ref(false)
const openTickets = ref<TicketVO[]>([])
const assignedTickets = ref<TicketVO[]>([])
const handleLoadingId = ref<string | null>(null)

async function loadOpenTickets() {

    const response = await getOpenTickets()
    const result = response.data

    if (result.code === 200) {
        openTickets.value = result.data
    }
    else {
        ElMessage.error(result.message)
    }
}

async function loadAssignedTickets() {

    const response = await getAssignedTickets()
    const result = response.data

    if (result.code === 200) {
        assignedTickets.value = result.data
    }
    else {
        ElMessage.error(result.message)
    }
}

async function loadData() {

    loading.value = true

    try {
        await Promise.all([loadOpenTickets(), loadAssignedTickets()])
    } catch (error) {
        ElMessage.error("工作台加载失败")
    } finally {
        loading.value = false
    }
}

async function openDetail(id:string){
    router.push(`/ticket/${id}`)
}

async function handleClaim(id:string){
    handleLoadingId.value = id

    try{
        const response = await handleTicket(id)
        const result = response.data

        if(result.code!==200){
            ElMessage.error(result.message)
            return
        }

        ElMessage.success("接单成功")

        await loadData()
    }catch(error){
        ElMessage.error("接单失败")
    }finally{
        handleLoadingId.value = null
    }
}

onMounted(() => {
    loadData()
})
</script>

<template>

    <div class="page" v-loading="loading">

        <h2>STAFF 工作台</h2>

        <section class="section">

            <h3>
                待接工单
            </h3>

            <el-table :data="openTickets" border>

                <el-table-column 
                prop="id" label="ID" width="80" 
                />

                <el-table-column 
                prop="title" label="标题" 
                />

                <el-table-column 
                prop="categoryName" label="分类" width="120" 
                />

                <el-table-column 
                prop="priority" label="优先级" width="100" 
                />

                <el-table-column 
                label="操作" width="180"
                >

                    <template #default="scope">

                        <el-button link @click="openDetail(scope.row.id)">
                            查看
                        </el-button>

                        <el-button type="primary" link 
                            :loading="handleLoadingId === scope.row.id"
                            @click="handleClaim(scope.row.id)">
                            接单
                        </el-button>

                    </template>

                </el-table-column>

            </el-table>

        </section>
    <section class="section">

      <h3>
        我的处理中
      </h3>

      <el-table
        :data="assignedTickets"
        border
      >

        <el-table-column
          prop="id"
          label="ID"
          width="80"
        />

        <el-table-column
          prop="title"
          label="标题"
        />

        <el-table-column
          prop="categoryName"
          label="分类"
          width="120"
        />

        <el-table-column
          prop="status"
          label="状态"
          width="130"
        />

        <el-table-column
          label="操作"
          width="120"
        >

          <template #default="scope">

            <el-button
              link
              type="primary"
              @click="openDetail(scope.row.id)"
            >
              处理
            </el-button>

          </template>

        </el-table-column>

      </el-table>

    </section>

  </div>

</template>