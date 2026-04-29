<script setup lang="ts">
/**
 * 占位页面入参，用于标识待实现业务模块名称。
 */
defineProps<{
  /** 页面标题。 */
  title: string
}>()
</script>

<template>
  <!-- 暂未完整实现的业务模块占位页。 -->
  <section class="page">
    <header class="page-header">
      <div>
        <h1>{{ title }}</h1>
        <p>模块骨架已建立，后续按 V1.5 实施方案补齐用例和页面。</p>
      </div>
    </header>
  </section>
</template>
