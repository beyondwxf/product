import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import { router } from './router'
import './styles.css'

/**
 * 创建 Vue 应用实例并挂载全局状态与路由。
 */
createApp(App).use(createPinia()).use(router).mount('#app')
