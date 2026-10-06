<script setup lang="ts">
import { useRoute } from 'vue-router'
import { computed } from 'vue'
import AppNavigation from './views/AppNavigation.vue'

const route = useRoute()
const showNavigation = computed(()=>{
  return route.path!=='/user/login' && route.path!=='/user/register'
})
</script>
<template>
  <AppNavigation v-if="showNavigation"></AppNavigation>
  <router-view />
</template>
