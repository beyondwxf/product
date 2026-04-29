import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

/**
 * Vite 前端构建与开发服务器配置。
 */
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/admin/api': 'http://localhost:8080',
      '/open/api': 'http://localhost:8080'
    }
  }
})
