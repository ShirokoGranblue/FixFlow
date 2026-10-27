<script setup lang="ts">
import { ref , computed, watch } from 'vue';
import { useUserStore } from '../stores/user.ts';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getTicketById } from '../api/ticket.ts'
import type { TicketDetailVO } from '../api/ticket.ts'
import { addComment } from '../api/ticket.ts';
import {
  cancelTicket,
  closeTicket,
  resolveTicket,
  handleTicket,
  reOpenTicket
} from '../api/ticket'
import TicketPriorityTag from '../component/TicketPriorityTag.vue';
import TicketStatusTag from '../component/TicketStatusTag.vue';
import { formatDateTime } from '../utils/ticketDisplay.ts';
import { getRequestError } from '../utils/requestError.ts';

const router = useRouter()
const route = useRoute();
const ticketId = computed(()=>route.params.id as string)
const loading = ref(false);
const actionLoading = ref(false);
const detail = ref<TicketDetailVO | null>(null);
const detailError = ref('')
const commentLoading = ref(false);
const comment = ref({ content: '' });
const userStore = useUserStore()

const isAssignee = computed(()=>{
    if(!detail.value?.ticket||!userStore.user){
        return false;
    }
    return detail.value.ticket.assigneeId===userStore.user.id
})
const isStaff = computed(()=>{
    if(!userStore.user){
        return false
    }
    return userStore.user.role==='ADMIN' || userStore.user.role==='STAFF'
})
const canCancel = computed(()=>{
    if(!detail.value?.ticket||!userStore?.user){
        return false
    }
    return detail.value.ticket.status === 'OPEN' && isCreator.value
})
const isCreator = computed(()=>{
    if(!detail.value?.ticket||!userStore?.user){
        return false
    }
    return detail.value.ticket.creatorId === userStore.user?.id;
})
const canHandle = computed(()=>{
    if(!detail.value?.ticket||!userStore?.user){
        return false
    }
    return detail.value.ticket.status === 'OPEN' && isStaff.value
})
const canResolve = computed(()=>{
    if(!detail.value?.ticket||!userStore.user){
        return false
    }
    return isStaff.value && detail.value.ticket.status==='IN_PROGRESS' && isAssignee.value
})
const canClose = computed(()=>{
    if(!detail.value?.ticket||!userStore.user){
        return false
    }
    return isAssignee.value && detail.value.ticket.status === 'RESOLVED'
})
const canReopen = computed(()=>{
    if(!detail.value?.ticket||!userStore.user){
        return false
    }
    return isCreator.value && detail.value.ticket.status==='RESOLVED'
})

async function loadTicketById() {

    loading.value = true;

    detail.value = null
    detailError.value = ''

    try {
        const ticketId = route.params.id as string
        if(!ticketId){
            detailError.value = '工单不存在'
            return
        }

        const response = await getTicketById(ticketId);
        const result = response.data

        if (result.code !== 200) {
            detailError.value = result.message || "无法获取工单详情"
            return;
        }

        detail.value = result.data;
    }
    catch (error) {
        detailError.value = getRequestError(error,"工单加载失败")
        
    } finally {
        loading.value = false;
    }
}

async function handleAddComment() {

    if (!comment.value.content.trim()) {
        ElMessage.warning("请输入评论内容");
        return;
    }

    commentLoading.value = true;

    try {
        const response = await addComment(comment.value, ticketId.value);
        if (response.data.code !== 200) {
            ElMessage.error(response.data.message);
            return;
        }

        if (detail.value) {
            detail.value.ticketComments.push(response.data.data)
        }

        comment.value.content = '';

        ElMessage.success("评论已发布");

    } catch (error) {

        ElMessage.error("评论失败")

    } finally {

        commentLoading.value = false
    }
}

async function handleCancel(){
    actionLoading.value = true;

    try{
        const response = await cancelTicket(ticketId.value)
        const result = response.data
        if(result.code!==200){
            ElMessage.error(result.message)
            return
        }
        ElMessage.success("工单取消成功")

        await loadTicketById()
    }catch(error){
        ElMessage.error("取消失败")
    }finally{
        actionLoading.value = false;
    }
}

async function handleClaim(){
    actionLoading.value = true;

    try{
        const response = await handleTicket(ticketId.value)
        const result = response.data

        if(result.code !== 200){
            ElMessage.error(result.message)
            return
        }

        ElMessage.success("接单成功")
        await loadTicketById()

    }catch(error){
        ElMessage.error("接单失败")
    }finally{
        actionLoading.value=false
    }
}

async function handleResolve(){
    actionLoading.value = true;

    try{
        const response = await resolveTicket(ticketId.value)
        const result = response.data
        
        if(result.code!== 200){
            ElMessage.error(result.message)
            return
        }

        ElMessage.success("工单已完成")
        await loadTicketById()

    }catch(error){
        ElMessage.error("工单处理失败")
    }finally{
        actionLoading.value = false;
    }
}

async function handleReopen(){
    actionLoading.value = true

    try{
        const response = await reOpenTicket(ticketId.value)
        const result = response.data

        if(result.code!==200){
            ElMessage.error(result.message)
            return
        }

        ElMessage.success("工单已重新开放")
        await loadTicketById()

    }catch(error){
        ElMessage.error("重新开放失败")
    }finally{
        actionLoading.value = false
    }
}  

async function handleClose(){
    actionLoading.value = true;
    try{
        const response = await closeTicket(ticketId.value)
        const result = response.data

        if(result.code!== 200){
            ElMessage.error(result.message)
            return
        }
        
        ElMessage.success("工单已关闭")
        await loadTicketById()

    }catch(error){
        ElMessage.error("工单关闭失败")
    }finally{
        actionLoading.value = false
    }
}

watch(
    ticketId,
    ()=>{
        loadTicketById()
    },
    {
        immediate:true
    }
)
</script>

<template>

    <div class="page ticket-detail-page">

        <div v-if="loading" class="empty-state">
            正在加载...
        </div>

        <el-result
            v-else-if="detailError"
            icon="warning"
            title="无法查看工单"
            :sub-title="detailError"
        >
            <template #extra>
                <el-button @click="router.push('/ticket/my-tickets')">
                返回工单列表
                </el-button>
            </template>
        </el-result>

        <div v-else-if="detail">

            <h2>
                {{ detail.ticket.title }}
            </h2>

            <div class="ticket-info">
                <span>状态：</span>
                    <TicketStatusTag :status="detail.ticket.status"/>

                <span>优先级：</span>
                    <TicketPriorityTag :priority="detail.ticket.priority"/>
            </div>

            <p>
                创建时间：
                {{ formatDateTime(detail.ticket.createdAt) }}
            </p>

            <p>
                分类：
                {{ detail.ticket.categoryName }}
            </p>

            <p>
                描述：
                {{ detail.ticket.description }}
            </p>

            <section v-if="detail" class="section">

                <h3>评论</h3>

                <div class="comment-form">

                    <el-input v-model="comment.content" type="textarea" :rows="3" placeholder="请输入评论内容" />

                    <el-button type="primary" :loading="commentLoading" @click="handleAddComment">
                        发表评论
                    </el-button>

                </div>


                <p v-if="detail.ticketComments.length === 0">
                    暂无评论
                </p>


                <div v-for="comment in detail.ticketComments" :key="comment.id" class="comment">

                    <strong>
                        用户 {{ comment.authorId }}
                    </strong>

                    <p>
                        {{ comment.content }}
                    </p>

                    <small>
                        {{ comment.createdAt }}
                    </small>

                </div>

            </section>

            <section class="section">

                <h3>操作历史</h3>

                <div v-for="item in detail.ticketHistory" :key="item.id" class="history-item">

                    <strong>
                        {{ item.action }}
                    </strong>

                    <span>
                        {{ item.fromStatus ?? '-' }}
                        →
                        {{ item.toStatus ?? '-' }}
                    </span>

                    <span>
                        {{formatDateTime(item.createdAt)}}
                    </span>

                </div>

            </section>
            <div class="actions">

            <el-button
                v-if="canCancel"
                type="danger"
                @click="handleCancel"
            >
                取消工单
            </el-button>


            <el-button
                v-if="canHandle"
                type="primary"
                @click="handleClaim"
            >
                接单
            </el-button>


            <el-button
                v-if="canResolve"
                type="success"
                @click="handleResolve"
            >
                标记已解决
            </el-button>


            <el-button
                v-if="canClose"
                type="success"
                @click="handleClose"
            >
                确认关闭
            </el-button>


            <el-button
                v-if="canReopen"
                @click="handleReopen"
            >
                问题仍未解决
            </el-button>

            </div>
        </div>

    </div>

</template>

<style scoped>
.page.ticket-detail-page {
    max-width: 1000px;
    margin: 0 auto;
    padding: 0;
}

.ticket-detail-page > div > p { color: var(--ui-muted); }
.ticket-detail-page > div > p:last-of-type { padding: 24px; color: var(--el-text-color-primary); background: var(--ui-surface); border: 1px solid var(--ui-border); border-radius: 12px; white-space: pre-wrap; }

.section {
    margin-bottom: 24px;
    padding: 24px;

    background: var(--ui-surface);
    border: 1px solid var(--ui-border);
    border-radius: 12px;
    box-shadow: var(--ui-shadow);
}

.comment {
    padding: 20px 0;
    border-bottom: 1px solid var(--ui-border);
}
.comment:last-child { border-bottom: 0; padding-bottom: 0; }
.comment p { margin: 8px 0; white-space: pre-wrap; }
.comment small { color: var(--ui-muted); font-variant-numeric: tabular-nums; }

.comment-form {
  margin-bottom: 24px;
}

.comment-form .el-button {
  margin-top: 12px;
}

.history-item {
    display: flex;
    flex-wrap: wrap;
    gap: 8px 20px;
    color: var(--ui-muted);
    overflow-wrap: anywhere;

    padding: 14px 0;
    border-bottom: 1px solid var(--ui-border);
}
.history-item strong { color: var(--el-text-color-primary); }
.history-item:last-child { border-bottom: 0; }
.actions { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 24px; }
.actions .el-button { margin-left: 0; }
@media (max-width: 600px) {
    .section { padding: 20px 16px; }
}
</style>
