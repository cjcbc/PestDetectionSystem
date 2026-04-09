import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    port: 3000,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8888',
        changeOrigin: true,
      }
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) {
            return
          }

          if (id.includes('markdown-it') || id.includes('highlight.js') || id.includes('dompurify')) {
            return 'chat-renderer'
          }

          if (id.includes('@element-plus/icons-vue')) {
            return 'element-plus-icons'
          }

          if (id.includes('element-plus')) {
            return 'element-plus'
          }

          if (id.includes('vue')) {
            return 'vue-vendor'
          }
        }
      }
    }
  }
})
