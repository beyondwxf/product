<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listProducts } from '../../api/product'
import type { ProductListItem } from '../../types/product'

/** 产品列表加载状态。 */
const loading = ref(false)
/** 产品列表数据。 */
const products = ref<ProductListItem[]>([])

/**
 * 刷新产品列表数据。
 */
async function refresh() {
  loading.value = true
  try {
    products.value = await listProducts()
  } finally {
    loading.value = false
  }
}

onMounted(refresh)
</script>

<template>
  <!-- 产品管理列表页，承载产品查询和生命周期入口。 -->
  <section class="page">
    <header class="page-header">
      <div>
        <h1>产品管理</h1>
        <p>传统寿险与分红寿险产品主数据、版本和发布状态。</p>
      </div>
      <button type="button" @click="refresh">刷新</button>
    </header>

    <div class="toolbar">
      <input placeholder="产品代码" />
      <input placeholder="产品名称" />
      <select>
        <option value="">全部类型</option>
        <option value="TERM_LIFE">定期寿险</option>
        <option value="WHOLE_LIFE">终身寿险</option>
        <option value="ENDOWMENT">两全保险</option>
      </select>
      <select>
        <option value="">全部状态</option>
        <option value="DRAFT">草稿</option>
        <option value="IN_REVIEW">审核中</option>
        <option value="PUBLISHED">已上架</option>
      </select>
    </div>

    <table class="data-table">
      <thead>
        <tr>
          <th>产品代码</th>
          <th>产品名称</th>
          <th>产品类型</th>
          <th>产品属性</th>
          <th>版本</th>
          <th>状态</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="loading">
          <td colspan="6">加载中...</td>
        </tr>
        <tr v-else-if="products.length === 0">
          <td colspan="6">暂无数据</td>
        </tr>
        <tr v-for="product in products" :key="`${product.productCode}-${product.versionNo}`">
          <td>{{ product.productCode }}</td>
          <td>{{ product.productName }}</td>
          <td>{{ product.productType }}</td>
          <td>{{ product.productNature }}</td>
          <td>{{ product.versionNo }}</td>
          <td>{{ product.status }}</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>
