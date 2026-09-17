<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listStocks, searchStocks, collectStock, removeStock } from '../api/stock'

const router = useRouter()
const stocks = ref([])
const keyword = ref('')
const results = ref([])
const showResults = ref(false)
const collecting = ref('')

let timer = null

function load() {
  listStocks()
    .then((d) => { stocks.value = d })
    .catch((e) => console.error(e))
}

function onInput() {
  clearTimeout(timer)
  const kw = keyword.value.trim()
  if (!kw) {
    results.value = []
    showResults.value = false
    return
  }
  timer = setTimeout(() => {
    searchStocks(kw)
      .then((d) => {
        results.value = d
        showResults.value = true
      })
      .catch(() => {})
  }, 300)
}

async function collect(item) {
  showResults.value = false
  keyword.value = item.name
  collecting.value = item.code
  try {
    await collectStock(item.code, 250)
    await load()
  } catch (e) {
    alert(e.message)
  } finally {
    collecting.value = ''
  }
}

async function remove(code) {
  if (!confirm(`确定删除 ${code} 及其全部数据吗？`)) return
  try {
    await removeStock(code)
    await load()
  } catch (e) {
    alert(e.message)
  }
}

function fmt(n) {
  return n == null ? '-' : Number(n).toFixed(2)
}

onMounted(load)
</script>

<template>
  <div>
    <!-- 搜索采集区 -->
    <div class="card panel" style="margin-bottom: 20px;">
      <div style="position: relative;">
        <input
          class="input"
          v-model="keyword"
          @input="onInput"
          @keydown.esc="showResults = false"
          placeholder="输入股票代码 / 名称 / 拼音，如 002241 或 歌尔股份"
        />
        <div
          v-if="showResults && results.length"
          class="card"
          style="position: absolute; top: 52px; left: 0; right: 0; z-index: 30; max-height: 320px; overflow: auto;"
        >
          <div
            v-for="r in results"
            :key="r.code"
            @click="collect(r)"
            style="display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; cursor: pointer; border-bottom: 1px solid rgba(255,255,255,0.05);"
          >
            <div>
              <span style="font-weight: 600;">{{ r.name }}</span>
              <span class="num" style="color: var(--text-dim); margin-left: 10px;">{{ r.code }}</span>
              <span :class="`badge badge-${r.market.toLowerCase()}`" style="margin-left: 10px;">{{ r.market }}</span>
            </div>
            <span v-if="collecting === r.code" class="spinner"></span>
            <span v-else style="color: var(--cyan); font-size: 13px;">采集 →</span>
          </div>
        </div>
      </div>
      <div style="color: var(--text-dim); font-size: 12px; margin-top: 10px;">
        选中搜索结果自动采集最近 250 个交易日数据并入库
      </div>
    </div>

    <!-- 股票卡片列表 -->
    <div v-if="stocks.length" class="grid">
      <div
        v-for="s in stocks"
        :key="s.code"
        class="card hoverable"
        style="padding: 20px; cursor: pointer;"
        @click="router.push(`/stock/${s.code}`)"
      >
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span class="badge" :class="`badge-${s.market.toLowerCase()}`">{{ s.market }}</span>
          <button class="btn btn-danger" style="padding: 4px 10px; font-size: 12px;" @click.stop="remove(s.code)">删除</button>
        </div>
        <div style="margin: 14px 0 6px; font-size: 18px; font-weight: 700;">{{ s.name }}</div>
        <div class="num" style="color: var(--text-dim); font-size: 13px;">{{ s.code }}</div>
        <div style="margin-top: 14px; display: flex; align-items: baseline; gap: 12px;">
          <span class="num" style="font-size: 26px; font-weight: 700;">{{ fmt(s.latestClose) }}</span>
          <span class="num" :class="(s.latestPctChg ?? 0) >= 0 ? 'up' : 'down'" style="font-size: 15px;">
            {{ (s.latestPctChg ?? 0) >= 0 ? '+' : '' }}{{ fmt(s.latestPctChg) }}%
          </span>
        </div>
        <div style="margin-top: 8px; color: var(--text-dim); font-size: 12px;">
          {{ s.latestDate || '—' }} · 共 {{ s.rowCount }} 条
        </div>
      </div>
    </div>

    <div v-else class="card empty">
      <div class="icon">📈</div>
      <div>暂无数据，先在上方搜索并采集一只股票</div>
    </div>
  </div>
</template>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 16px;
}
</style>
