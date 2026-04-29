<template>
  <!-- 后台管理端整体布局，左侧为业务导航，右侧为路由内容。 -->
  <main class="app-shell">
    <aside class="sidebar">
      <div class="brand">保险产品中心</div>
      <RouterLink to="/products">产品管理</RouterLink>
      <RouterLink to="/rates">费率中心</RouterLink>
      <RouterLink to="/rules">联动规则</RouterLink>
      <RouterLink to="/audits">审核中心</RouterLink>
      <RouterLink to="/suppliers">供应商</RouterLink>
      <RouterLink to="/reports">报表</RouterLink>
    </aside>
    <section class="content">
      <RouterView />
    </section>
  </main>
</template>
