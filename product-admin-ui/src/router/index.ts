import { createRouter, createWebHistory } from 'vue-router'
import ProductListView from '../views/product/ProductListView.vue'
import PlaceholderView from '../views/PlaceholderView.vue'

/**
 * 后台管理端路由配置。
 */
export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/products' },
    { path: '/products', component: ProductListView },
    { path: '/rates', component: PlaceholderView, props: { title: '费率中心' } },
    { path: '/rules', component: PlaceholderView, props: { title: '联动规则' } },
    { path: '/audits', component: PlaceholderView, props: { title: '审核中心' } },
    { path: '/suppliers', component: PlaceholderView, props: { title: '供应商管理' } },
    { path: '/reports', component: PlaceholderView, props: { title: '报表中心' } }
  ]
})
