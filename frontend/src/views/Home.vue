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
      当前角色：
      {{ userStore.user?.role }}
    </p>

  </div>

</template>