<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getStock, getDaily, collectStock } from '../api/stock'
import KlineChart from '../components/KlineChart.vue'

const props = defineProps({ code: String })
const router = useRouter()

const info = ref(null)
const daily = ref([])
const loading = ref(false)

async function load() {
  try {
    const [i, d] = await Promise.all([getStock(props.code), getDaily(props.code)])
    info.value = i
    daily.value = d
  } catch (e) {
    alert(e.message)
    router.push('/')
  }
}

async function refresh() {
  loading.value = true
  try {
    await collectStock(props.code, 250)
    await load()
  } catch (e) {
    alert(e.message)
  } finally {
    loading.value = false
  }
}

function fmt(n) {
  return n == null ? '-' : Number(n).toFixed(2)
}

const latest = computed(() =>
  info.value
    ? { close: info.value.latestClose, pctChg: info.value.latestPctChg, date: info.value.latestDate }
    : null
)

const rows = computed(() => daily.value.slice().reverse())

onMounted(load)
</script>

<template>
  <div v-if="info">
    <div style="display: flex; align-items: center; gap: 14px; margin-bottom: 20px; flex-wrap: wrap;">
      <button class="btn btn-ghost" @click="router.push('/')">← 返回</button>
      <h1 style="font-size: 24px; font-weight: 700;">{{ info.name }}</h1>
      <span class="badge" :class="`badge-${info.market.toLowerCase()}`">{{ info.market }}</span>
      <span class="num" style="color: var(--text-dim);">{{ info.code }}</span>
      <div style="margin-left: auto; display: flex; align-items: center; gap: 14px;">
        <span class="num" style="font-size: 26px; font-weight: 700;">{{ fmt(latest?.close) }}</span>
        <span class="num" :class="(latest?.pctChg ?? 0) >= 0 ? 'up' : 'down'" style="font-size: 16px;">
          {{ (latest?.pctChg ?? 0) >= 0 ? '+' : '' }}{{ fmt(latest?.pctChg) }}%
        </span>
        <button class="btn btn-primary" :disabled="loading" @click="refresh">
          <span v-if="loading" class="spinner"></span>
          <span>{{ loading ? '采集中' : '更新数据' }}</span>
        </button>
      </div>
    </div>

    <div class="card" style="padding: 16px; margin-bottom: 20px;">
      <KlineChart :data="daily" />
    </div>

    <div class="card" style="padding: 16px;">
      <div style="margin-bottom: 12px; color: var(--text-dim); font-size: 13px;">日 K 数据（最新在前）</div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>日期</th>
              <th>开盘</th>
              <th>最高</th>
              <th>最低</th>
              <th>收盘</th>
              <th>涨跌幅</th>
              <th>成交量(手)</th>
              <th>成交额</th>
              <th>换手率</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in rows" :key="r.id">
              <td class="num">{{ r.tradeDate }}</td>
              <td class="num">{{ fmt(r.open) }}</td>
              <td class="num">{{ fmt(r.high) }}</td>
              <td class="num">{{ fmt(r.low) }}</td>
              <td class="num" :class="(r.pctChg ?? 0) >= 0 ? 'up' : 'down'">{{ fmt(r.close) }}</td>
              <td class="num" :class="(r.pctChg ?? 0) >= 0 ? 'up' : 'down'">
                {{ (r.pctChg ?? 0) >= 0 ? '+' : '' }}{{ fmt(r.pctChg) }}%
              </td>
              <td class="num">{{ fmt(r.volume) }}</td>
              <td class="num">{{ fmt(r.amount) }}</td>
              <td class="num">{{ r.turnoverRate == null ? '-' : Number(r.turnoverRate).toFixed(2) + '%' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
