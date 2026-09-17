import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 统一错误提示
http.interceptors.response.use(
  (res) => res,
  (err) => {
    const msg = err?.response?.data?.detail || err?.response?.data?.message || err.message || '请求失败'
    return Promise.reject(new Error(msg))
  }
)

export const listStocks = () => http.get('/stocks').then((r) => r.data)
export const searchStocks = (keyword) => http.get('/stocks/search', { params: { keyword } }).then((r) => r.data)
export const collectStock = (code, days) => http.post('/stocks/collect', { code, days }).then((r) => r.data)
export const getStock = (code) => http.get(`/stocks/${code}`).then((r) => r.data)
export const getDaily = (code, limit) => http.get(`/stocks/${code}/daily`, { params: { limit } }).then((r) => r.data)
export const removeStock = (code) => http.delete(`/stocks/${code}`).then((r) => r.data)
