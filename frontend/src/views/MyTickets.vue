<script setup lang ="ts">
import { ref, onMounted } from 'vue';
import { getMyTickets } from '../api/ticket.ts';
import type { TicketVO } from '../api/ticket.ts';
import { useRouter } from 'vue-router';

const router = useRouter();
const tickets = ref<TicketVO[]>([]);
const loading = ref(false);

async function loadTickets() {
    loading.value = true;

    try {
        const response = await getMyTickets();
        if(response.data.code === 200 ){
            tickets.value = response.data.data;
        }
    } finally {
        loading.value = false;
    }
}

function openTicket(id:number){
    router.push(`/ticket/${id}`);
}

onMounted(() => {
    loadTickets();
});
</script>

<template>
  <div class="page">
    <h2>我的工单</h2>

    <div v-if="loading">
      正在加载...
    </div>

    <div v-else-if="tickets.length === 0">
      暂无工单
    </div>

    <div v-else>
      <div
        v-for="ticket in tickets"
        :key="ticket.id"
        class="ticket-item"
      >
        <el-button @click="openTicket(ticket.id)">
          查看详情
        </el-button>
        <h3>
          {{ ticket.title }}
        </h3>
        <p>
          分类：{{ ticket.categoryName }}
        </p>
        <p>
          状态：{{ ticket.status }}
        </p>
        <p>
          优先级：{{ ticket.priority }}
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  padding: 24px;
}

.ticket-item {
  margin-bottom: 16px;
  padding: 16px;

  border: 1px solid #ddd;
  border-radius: 8px;
}
</style>