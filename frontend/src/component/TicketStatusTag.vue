<script setup lang="ts">
import { getStatusDisplay } from '../utils/ticketDisplay.ts';
import { computed } from 'vue';

const props = defineProps<{status:string}>()
const display = computed(()=>{
    return getStatusDisplay(props.status)
})

</script>

<template>
  <el-tag :type="display.type">
    {{ display.label }}
  </el-tag>
</template>