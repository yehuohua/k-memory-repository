<script setup>
</script>

<template>
  <header class="nav">
    <div class="container nav-inner">
      <router-link to="/" class="logo">
        <span class="logo-dot"></span>
        <em>股票数据中心</em>
      </router-link>
      <span class="num" style="color: var(--text-dim); font-size: 13px;">A 股行情 · 本地记录</span>
    </div>
  </header>
  <main class="container" style="padding: 28px 0 60px;">
    <router-view />
  </main>
</template>
