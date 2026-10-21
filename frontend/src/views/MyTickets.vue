<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getMyTickets } from '../api/ticket.ts';
import type { TicketVO } from '../api/ticket.ts';
import { useRouter } from 'vue-router';
import TicketPriorityTag from '../component/TicketPriorityTag.vue'
import TicketStatusTag from '../component/TicketStatusTag.vue'
import { formatDateTime } from '../utils/ticketDisplay.ts'

const router = useRouter();
const tickets = ref<TicketVO[]>([]);
const loading = ref(false);

async function loadTickets() {
  loading.value = true;

  try {
    const response = await getMyTickets();
    if (response.data.code === 200) {
      tickets.value = response.data.data;
    }
  } finally {
    loading.value = false;
  }
}

function openTicket(id: number) {
  router.push(`/ticket/${id}`);
}

onMounted(() => {
  loadTickets();
});
</script>

<template>
  <div class="page">

    <div class="page-heading">
      <h2>我的工单</h2>

      <el-button
        type="primary"
        @click="router.push('/ticket/create')"
        >
        <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false"><path d="M12 5v14M5 12h14"/></svg>
        新建工单
      </el-button>
    </div>
      
    <div v-if="loading" class="empty-state">
      正在加载...
    </div>
    
    <div v-else-if="tickets.length === 0" class="empty-state">
      暂无工单
    </div>

    <div v-else>
      <div v-for="ticket in tickets" :key="ticket.id" class="ticket-item">
        <el-button @click="openTicket(ticket.id)">
          查看详情
        </el-button>
        <h3>
          {{ ticket.title }}
        </h3>
        <p>
          分类：{{ ticket.categoryName }}
        </p>

          <div class="ticket-info">
                <span>状态：</span>
                    <TicketStatusTag :status="ticket.status"/>

                <span>优先级：</span>
                    <TicketPriorityTag :priority="ticket.priority"/>
            </div>

            <p>
                创建时间：
                {{ formatDateTime(ticket.createdAt) }}
            </p>
          </div>
      </div>
    </div>

</template>

<style scoped>
.ticket-item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px 24px;
  margin-bottom: 16px;
  padding: 24px;

  background: var(--ui-surface);
  border: 1px solid var(--ui-border);
  border-radius: 12px;
  box-shadow: var(--ui-shadow);
}
.ticket-item > .el-button { grid-column: 2; grid-row: 1; }
.ticket-item h3 { grid-column: 1; grid-row: 1; margin: 0; align-self: center; font-size: 17px; }
.ticket-item p, .ticket-info { grid-column: 1 / -1; margin: 0; color: var(--ui-muted); }
@media (max-width: 600px) {
  .ticket-item { padding: 20px; gap: 12px; }
}
</style>
