import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import './assets/styles/index.scss'
import wsClient from './utils/websocket'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus)

app.mount('#app')

// 如果用户已登录，自动连接WebSocket
const token = localStorage.getItem('token')
if (token) {
  wsClient.connect(token)
}
