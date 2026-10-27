<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
    getManageTickets
} from '../api/ticket'

import type {
    TicketVO
} from '../api/ticket'

import {
    getCategories
} from '../api/category'

import type {
    CategoryVO
} from '../api/category'
import { getStaffUsers } from '../api/user.ts'
import type { UserVO } from '../api/user.ts'
import { assignTicket } from '../api/ticket.ts'
import TicketPriorityTag from '../component/TicketPriorityTag.vue'
import TicketStatusTag from '../component/TicketStatusTag.vue'
import { formatDateTime } from '../utils/ticketDisplay.ts'
import { getRequestError } from '../utils/requestError.ts'

const router = useRouter()
const loading = ref(false)
const ticket = ref<TicketVO[]>([])
const categories = ref<CategoryVO[]>([])
const total = ref(0)

const query = reactive(
    {
        current: 1,
        pageSize: 10,

        keyword: '',
        status: '',
        priority: '',
        categoryId: '' as number | ''
    }
)
const assignLoading = ref(false)
const assignDialogVisible = ref(false)
const staffUsers = ref<UserVO[]>([])
const selectedAssigneeId = ref<string | null>(null)
const currentTicketId = ref<string | null>(null)

async function loadManageTickets() {
    loading.value = true

    try {
        const response = await getManageTickets(
            {
                current: query.current,
                pageSize: query.pageSize,

                keyword: query.keyword.trim() || undefined,
                status: query.status || undefined,
                priority: query.priority || undefined,
                categoryId: typeof query.categoryId === 'number' ? query.categoryId : undefined
            }
        )
        
        const result = response.data

        if (result.code !== 200) {
            ElMessage.error(result.message)
            return
        }

        ticket.value = result.data.records
        total.value = result.data.total
        ElMessage.success("工单加载成功")

    } catch (error) {

        ElMessage.error(getRequestError(error,"工单加载失败"))
    } finally {

        loading.value = false
    }
}

async function loadCategory() {
    const response = await getCategories()
    const result = response.data
    if (result.code === 200) {
        categories.value = result.data
    }
}

async function handleAssign(){

    if(currentTicketId.value ===null){
        return
    }
    if(selectedAssigneeId.value === null){
        ElMessage.warning("请选择处理人员")
        return
    }

    assignLoading.value = true

    const ticketId = currentTicketId.value as string

    try{
        const response = await assignTicket(ticketId,
        {
            assigneeId:selectedAssigneeId.value as string
        })

        const result = response.data

        if(result.code!==200){
            ElMessage.error(result.message)
            return
        }

        ElMessage.success("分配成功")

        assignDialogVisible.value = false

        await loadManageTickets()
        
    }catch(error){
        ElMessage.error("分配失败")
    }finally{
        assignLoading.value = false
    }
}

async function openAssignDialog(ticketId:string){
    currentTicketId.value = ticketId
    selectedAssigneeId.value = null

    try{
        const response = await getStaffUsers()
        const result = response.data

        if(result.code!==200){
            ElMessage.error(result.message)
            return
        }

        staffUsers.value = result.data
        assignDialogVisible.value = true
            
    }catch(error){
        ElMessage.error("员工列表加载失败")
    }
}

async function handleSearch() {

    query.current = 1

    loadManageTickets()
}

async function handleReset() {
    query.current = 1
    query.keyword = ''
    query.status = ''
    query.priority = ''
    query.categoryId = ''

    loadManageTickets()
}

async function openTicket(id: string) {

    router.push(`/ticket/${id}`)
}

async function handlePageChange() {
    loadManageTickets()
}

async function handleSizeChange() {
    query.current = 1
    loadManageTickets()
}

onMounted(() => {
    loadManageTickets()
    loadCategory()
})

</script>

<template>

    <div class="page">

        <h2>工单管理</h2>


        <el-form :inline="true" :model="query">

            <el-form-item label="关键词">

                <el-input v-model="query.keyword" placeholder="标题关键词" clearable />

            </el-form-item>


            <el-form-item label="状态">

                <el-select v-model="query.status" placeholder="全部状态" clearable>

                    <el-option label="全部" value=""/>

                    <el-option label="待处理" value="OPEN" />

                    <el-option label="处理中" value="IN_PROGRESS" />

                    <el-option label="已解决" value="RESOLVED" />

                    <el-option label="已关闭" value="CLOSED" />

                    <el-option label="已取消" value="CANCELLED" />

                </el-select>

            </el-form-item>


            <el-form-item label="优先级">

                <el-select v-model="query.priority" placeholder="全部优先级" clearable>

                    <el-option label="全部" value=""/>

                    <el-option label="低" value="LOW" />

                    <el-option label="普通" value="MEDIUM" />

                    <el-option label="高" value="HIGH" />

                    <el-option label="紧急" value="URGENT" />

                </el-select>

            </el-form-item>


            <el-form-item label="分类">

                <el-select v-model="query.categoryId" placeholder="全部分类" clearable>
                    
                    <el-option
                        label="全部"
                        value=""
                    />

                    <el-option v-for="category in categories" 
                        :key="category.id" 
                        :label="category.name"
                        :value="category.id" 
                    />

                </el-select>

            </el-form-item>


            <el-form-item>

                <el-button type="primary" @click="handleSearch">
                    搜索
                </el-button>

                <el-button @click="handleReset">
                    重置
                </el-button>

            </el-form-item>

        </el-form>

        <el-table :data="ticket" v-loading="loading" border>

            <el-table-column prop="title" label="标题" min-width="220" />
                  
            <el-table-column
                prop="creatorName"
                label="用户名"
                width="100"
            />
            
            <el-table-column prop="categoryName" label="分类" width="120" />

            <el-table-column label="优先级" width="110">
                <template #default="{ row }">
                    <TicketPriorityTag :priority="row.priority" />
                </template>
            </el-table-column>

            <el-table-column label="状态" width="120">
                <template #default="{ row }">
                    <TicketStatusTag :status="row.status" />
                </template>
            </el-table-column>

            <el-table-column label="创建时间" width="180">
                <template #default="{ row }">
                    {{ formatDateTime(row.createdAt) }}
                </template>
            </el-table-column>

            <el-table-column label="操作" width="120">

                <template #default="scope">

                    <el-button link type="primary" @click="openTicket(scope.row.id)">
                        查看
                    </el-button>

                    <el-button
                        v-if="
                            scope.row.status === 'OPEN'
                            && scope.row.assigneeId === null
                        "
                        link
                        type="success"
                        @click="openAssignDialog(scope.row.id)"
                        >
                        分配
                    </el-button>

                </template>

            </el-table-column>

        </el-table>

        <el-pagination 
            v-model:current-page="query.current" 
            v-model:page-size="query.pageSize" 
            :total="total"
            
            :page-sizes="[10, 20, 50]" 
            layout="total, sizes, prev, pager, next" 

            @current-change="handlePageChange"
            @size-change="handleSizeChange" 
        />

        <el-dialog
            v-model="assignDialogVisible"
            title="分配工单"
            width="420px"
            >

            <el-form label-width="90px">

                <el-form-item label="处理人员">

                    <el-select
                        v-model="selectedAssigneeId"
                        placeholder="请选择STAFF"
                        style="width: 100%"
                    >

                        <el-option
                            v-for="staff in staffUsers"
                            :key="staff.id"
                            :label="staff.username"
                            :value="staff.id"
                        />

                    </el-select>

                </el-form-item>

            </el-form>

            <template #footer>

                <el-button
                @click="assignDialogVisible = false"
                >
                取消
                </el-button>

                <el-button
                    type="primary"
                    :loading="assignLoading"
                    @click="handleAssign"
                >
                确认分配
                </el-button>

            </template>

        </el-dialog>

    </div>

</template>