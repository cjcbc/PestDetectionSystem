import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { ElLoading } from 'element-plus'

import App from './App.vue'
import router from './router'

import '@/styles/variables.css'
import '@/styles/global.css'
import '@/styles/chat.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.directive('loading', ElLoading.directive)

app.mount('#app')
