<script setup lang="ts">

import { useUserStore } from '../stores/user'

const userStore = useUserStore()

</script>

<template>

  <div class="welcome-page">

    <span class="auth-mark" aria-hidden="true"><svg class="ui-icon" viewBox="0 0 24 24" focusable="false"><rect x="4" y="3" width="16" height="18" rx="3"/><path d="m8 12 3 3 5-6"/></svg></span>

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
