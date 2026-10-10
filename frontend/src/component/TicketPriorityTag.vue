<script setup lang="ts">
import { getPriorityDisplay } from '../utils/ticketDisplay';
import { computed } from 'vue';

const props = defineProps<{priority:string}>()

const display = computed(()=>{
    return getPriorityDisplay(props.priority)
})

</script>

<template>
  <el-tag :type="display.type">
    {{ display.label }}
  </el-tag>
</template>