import { createRouter, createWebHistory } from 'vue-router'
import StockList from '../views/StockList.vue'
import StockDetail from '../views/StockDetail.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: StockList },
    { path: '/stock/:code', name: 'detail', component: StockDetail, props: true }
  ]
})

export default router
