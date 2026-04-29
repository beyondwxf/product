<script setup lang="ts">
import { reactive, ref } from 'vue'
import {
  approveReview,
  createProduct,
  listProducts,
  publishVersion,
  submitReview,
  validatePublish
} from '../../api/product'
import type {
  CreateProductPayload,
  ProductListItem,
  ProductNature,
  ProductStatus,
  ProductType,
  PublishValidationIssue
} from '../../types/product'

/** 产品列表加载状态。 */
const loading = ref(false)
/** 产品列表数据。 */
const products = ref<ProductListItem[]>([])
/** 产品总数。 */
const total = ref(0)
/** 操作提示。 */
const notice = ref('')
/** 发布校验问题。 */
const validationIssues = ref<PublishValidationIssue[]>([])

/** 产品列表查询条件。 */
const query = reactive({
  productCode: '',
  productName: '',
  productType: '' as ProductType | '',
  productNature: '' as ProductNature | '',
  status: '' as ProductStatus | '',
  pageNo: 1,
  pageSize: 20
})

/** 创建产品表单。 */
const form = reactive<CreateProductPayload>({
  productCode: '',
  productName: '',
  shortName: '',
  productType: 'TERM_LIFE',
  productNature: 'TRADITIONAL_LIFE',
  insuranceCompany: '',
  salesChannels: 'APP'
})

/**
 * 刷新产品列表。
 */
async function refresh() {
  loading.value = true
  notice.value = ''
  try {
    const page = await listProducts(query)
    products.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

/**
 * 重置查询条件。
 */
async function resetQuery() {
  query.productCode = ''
  query.productName = ''
  query.productType = ''
  query.productNature = ''
  query.status = ''
  query.pageNo = 1
  await refresh()
}

/**
 * 创建产品草稿。
 */
async function handleCreate() {
  await createProduct(form)
  notice.value = '产品草稿已创建'
  form.productCode = ''
  form.productName = ''
  form.shortName = ''
  form.insuranceCompany = ''
  form.salesChannels = 'APP'
  await refresh()
}

/**
 * 提交产品审核。
 *
 * @param product 产品列表项
 */
async function handleSubmit(product: ProductListItem) {
  await submitReview(product.versionId)
  notice.value = `${product.productCode} 已提交审核`
  await refresh()
}

/**
 * 审核通过产品版本。
 *
 * @param product 产品列表项
 */
async function handleApprove(product: ProductListItem) {
  await approveReview(product.versionId)
  notice.value = `${product.productCode} 已审核通过`
  await refresh()
}

/**
 * 校验产品发布条件。
 *
 * @param product 产品列表项
 */
async function handleValidate(product: ProductListItem) {
  const result = await validatePublish(product.versionId)
  validationIssues.value = result.issues
  notice.value = result.passed ? `${product.productCode} 发布校验通过` : `${product.productCode} 发布校验未通过`
}

/**
 * 发布产品版本。
 *
 * @param product 产品列表项
 */
async function handlePublish(product: ProductListItem) {
  await publishVersion(product.versionId)
  validationIssues.value = []
  notice.value = `${product.productCode} 已发布`
  await refresh()
}

refresh()
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>产品管理</h1>
        <p>传统寿险与分红寿险产品主数据、版本和发布状态。</p>
      </div>
      <button type="button" @click="refresh">刷新</button>
    </header>

    <form class="create-panel" @submit.prevent="handleCreate">
      <input v-model="form.productCode" required placeholder="产品编码" />
      <input v-model="form.productName" required placeholder="产品全称" />
      <input v-model="form.shortName" required placeholder="产品简称" />
      <select v-model="form.productType">
        <option value="TERM_LIFE">定期寿险</option>
        <option value="WHOLE_LIFE">终身寿险</option>
        <option value="ENDOWMENT">两全保险</option>
      </select>
      <select v-model="form.productNature">
        <option value="TRADITIONAL_LIFE">传统寿险</option>
        <option value="DIVIDEND_LIFE">分红寿险</option>
      </select>
      <input v-model="form.insuranceCompany" required placeholder="保险公司" />
      <input v-model="form.salesChannels" required placeholder="销售渠道" />
      <button type="submit">新建</button>
    </form>

    <div class="toolbar">
      <input v-model="query.productCode" placeholder="产品编码" />
      <input v-model="query.productName" placeholder="产品名称" />
      <select v-model="query.productType">
        <option value="">全部类型</option>
        <option value="TERM_LIFE">定期寿险</option>
        <option value="WHOLE_LIFE">终身寿险</option>
        <option value="ENDOWMENT">两全保险</option>
      </select>
      <select v-model="query.productNature">
        <option value="">全部属性</option>
        <option value="TRADITIONAL_LIFE">传统寿险</option>
        <option value="DIVIDEND_LIFE">分红寿险</option>
      </select>
      <select v-model="query.status">
        <option value="">全部状态</option>
        <option value="DRAFT">草稿</option>
        <option value="IN_REVIEW">审核中</option>
        <option value="APPROVED">已通过</option>
        <option value="PUBLISHED">已上架</option>
      </select>
      <button type="button" @click="refresh">查询</button>
      <button type="button" @click="resetQuery">重置</button>
    </div>

    <p v-if="notice" class="notice">{{ notice }}</p>

    <table class="data-table">
      <thead>
        <tr>
          <th>产品编码</th>
          <th>产品名称</th>
          <th>产品类型</th>
          <th>产品属性</th>
          <th>版本</th>
          <th>状态</th>
          <th>操作人</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="loading">
          <td colspan="8">加载中...</td>
        </tr>
        <tr v-else-if="products.length === 0">
          <td colspan="8">暂无数据</td>
        </tr>
        <tr v-for="product in products" :key="`${product.productCode}-${product.versionNo}`">
          <td>{{ product.productCode }}</td>
          <td>{{ product.productName }}</td>
          <td>{{ product.productType }}</td>
          <td>{{ product.productNature }}</td>
          <td>{{ product.versionNo }}</td>
          <td>{{ product.status }}</td>
          <td>{{ product.updatedBy }}</td>
          <td class="actions">
            <button v-if="product.status === 'DRAFT'" type="button" @click="handleSubmit(product)">提交</button>
            <button v-if="product.status === 'IN_REVIEW'" type="button" @click="handleApprove(product)">通过</button>
            <button type="button" @click="handleValidate(product)">校验</button>
            <button v-if="product.status === 'APPROVED'" type="button" @click="handlePublish(product)">发布</button>
          </td>
        </tr>
      </tbody>
    </table>

    <footer class="table-footer">共 {{ total }} 条</footer>

    <section v-if="validationIssues.length > 0" class="validation-panel">
      <h2>发布校验问题</h2>
      <ul>
        <li v-for="issue in validationIssues" :key="`${issue.module}-${issue.code}`">
          <strong>{{ issue.module }}</strong>
          <span>{{ issue.message }}</span>
        </li>
      </ul>
    </section>
  </section>
</template>
