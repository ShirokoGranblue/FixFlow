<script setup lang="ts">
import { ref, onMounted , computed } from 'vue';
import { useUserStore } from '../stores/user.ts';
import { useRoute } from 'vue-router';
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

const route = useRoute();
const ticketId = route.params.id;
const loading = ref(false);
const actionLoading = ref(false);
const detail = ref<TicketDetailVO | null>(null);
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

    try {
        const response = await getTicketById(ticketId as string);
        if (response.data.code !== 200) {
            ElMessage.error(response.data.message);
            return;
        }
        detail.value = response.data.data;
    }
    catch (error) {
        ElMessage.error('获取工单详情失败');
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
        const response = await addComment(comment.value, ticketId as string);
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
        const response = await cancelTicket(ticketId as string)
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
        const response = await handleTicket(ticketId as string)
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
        const response = await resolveTicket(ticketId as string )
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
        const response = await reOpenTicket(ticketId as string)
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
        const response = await closeTicket(ticketId as string)
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

onMounted(
    () => { loadTicketById() }
)
</script>

<template>

    <div class="page">

        <div v-if="loading">
            正在加载...
        </div>


        <div v-else-if="detail">

            <h2>
                {{ detail.ticket.title }}
            </h2>

            <p>
                状态：
                {{ detail.ticket.status }}
            </p>

            <p>
                优先级：
                {{ detail.ticket.priority }}
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
                        {{ item.createdAt }}
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
.page {
    max-width: 900px;
    margin: 0 auto;
    padding: 24px;
}

.section {
    margin-bottom: 24px;
    padding: 20px;

    border: 1px solid #ddd;
    border-radius: 8px;
}

.comment {
    padding: 12px 0;
    border-bottom: 1px solid #eee;
}

.comment-form {
  margin-bottom: 24px;
}

.comment-form .el-button {
  margin-top: 12px;
}

.history-item {
    display: flex;
    gap: 20px;

    padding: 8px 0;
}
</style>