<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getTicketDetail } from '../api/ticket'
import type { TicketDetailVO } from '../api/ticket'

const route = useRoute();
const ticketId = route.params.id;
const loading = ref(false);
const detail = ref<TicketDetailVO | null>(null);

async function loadTicketDetail() {
    loading.value = true;

    try {
        const response = await getTicketDetail(ticketId as string);
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

    onMounted(
        () => { loadTicketDetail() }
    )
}
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
            <section class="section">

                <h3>评论</h3>

                <p v-if="detail.ticketComments.length === 0">
                    暂无评论
                </p>

                <div v-for="comment in detail.ticketComments" :key="comment.id" class="comment">

                    <p>
                        用户 {{ comment.authorId }}
                    </p>

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

.history-item {
  display: flex;
  gap: 20px;

  padding: 8px 0;
}

</style>