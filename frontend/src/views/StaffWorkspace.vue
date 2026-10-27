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
        throw new Error(result.message)
    }
}

async function loadAssignedTickets() {

    const response = await getAssignedTickets()
    const result = response.data

    if (result.code === 200) {
        assignedTickets.value = result.data
    }
    else {
        throw new Error(result.message)
    }
}

async function loadData(showMessage = false) {

    loading.value = true

    try {
        await Promise.all([loadOpenTickets(), loadAssignedTickets()])
        if (showMessage) {
            if (openTickets.value.length === 0 && assignedTickets.value.length === 0) {
                ElMessage.info("暂无工单")
            } else {
                ElMessage.success("刷新成功")
            }
        }
    } catch (error) {
        if (showMessage) {
            ElMessage.error(error instanceof Error ? error.message : "工作台加载失败")
        }
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

        const claimedTicket = result.data
        openTickets.value = openTickets.value.filter(ticket => String(ticket.id) !== String(claimedTicket.id))
        assignedTickets.value = [
            claimedTicket,
            ...assignedTickets.value.filter(ticket => String(ticket.id) !== String(claimedTicket.id))
        ]

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

        <el-button type="primary" :loading="loading" @click="loadData(true)">
            刷新
        </el-button>

        <section class="section">

            <h3>
                待接工单
            </h3>

            <el-table :data="openTickets" border>

                <el-table-column 
                prop="title" label="标题" 
                />

                <el-table-column
                prop="creatorName" label="创建者"
                />

                <el-table-column 
                prop="categoryName" label="分类" width="120" 
                />

                <el-table-column label="优先级" width="110">
                    <template #default="{ row }">
                        <TicketPriorityTag :priority="row.priority" />
                    </template>
                </el-table-column>

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
          prop="title"
          label="标题"
        />

        <el-table-column
          prop="categoryName"
          label="分类"
          width="120"
        />

        <el-table-column label="状态" width="120">
            <template #default="{ row }">
                <TicketStatusTag :status="row.status" />
            </template>
        </el-table-column>

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
