<script setup lang="ts">

import { useUserStore } from '../stores/user'

const userStore = useUserStore()

</script>

<template>

  <div>

    <h1>FixFlow</h1>

    <p>
      欢迎，
      {{ userStore.user?.username }}
    </p>

    <p>
      你当前身份是：
      {{ userStore.user?.role }}
    </p>

  </div>

</template>