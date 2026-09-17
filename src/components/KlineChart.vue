<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: { type: Array, default: () => [] }
})

const el = ref(null)
let chart = null

function ma(n, closes) {
  return closes.map((_, i) => {
    if (i < n - 1) return null
    let sum = 0
    for (let j = i - n + 1; j <= i; j++) sum += closes[j]
    return +(sum / n).toFixed(2)
  })
}

function buildOption(data) {
  const dates = data.map((d) => d.tradeDate)
  const ohlc = data.map((d) => [d.open, d.close, d.low, d.high])
  const closes = data.map((d) => d.close)
  const volumes = data.map((d) => ({
    value: d.volume,
    itemStyle: { color: d.close >= d.open ? 'rgba(255,77,94,0.5)' : 'rgba(0,197,142,0.5)' }
  }))

  return {
    backgroundColor: 'transparent',
    animation: false,
    legend: {
      data: ['MA5', 'MA10', 'MA20'],
      textStyle: { color: '#7c8aa5' },
      top: 0
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
      backgroundColor: 'rgba(13,20,36,0.95)',
      borderColor: 'rgba(56,189,248,0.3)',
      textStyle: { color: '#e2e8f0', fontSize: 12 }
    },
    axisPointer: { link: [{ xAxisIndex: 'all' }] },
    grid: [
      { left: 60, right: 20, top: 36, height: '58%' },
      { left: 60, right: 20, top: '74%', height: '16%' }
    ],
    xAxis: [
      {
        type: 'category',
        data: dates,
        boundaryGap: true,
        axisLine: { lineStyle: { color: 'rgba(255,255,255,0.15)' } },
        axisLabel: { color: '#7c8aa5' }
      },
      {
        type: 'category',
        gridIndex: 1,
        data: dates,
        boundaryGap: true,
        axisLine: { lineStyle: { color: 'rgba(255,255,255,0.15)' } },
        axisLabel: { show: false }
      }
    ],
    yAxis: [
      {
        scale: true,
        axisLabel: { color: '#7c8aa5' },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } }
      },
      {
        gridIndex: 1,
        scale: true,
        axisLabel: { color: '#7c8aa5' },
        splitLine: { show: false }
      }
    ],
    dataZoom: [
      { type: 'inside', xAxisIndex: [0, 1], start: 0, end: 100 },
      {
        type: 'slider',
        xAxisIndex: [0, 1],
        start: 0,
        end: 100,
        bottom: 6,
        height: 18,
        borderColor: 'rgba(56,189,248,0.2)',
        textStyle: { color: '#7c8aa5' }
      }
    ],
    series: [
      {
        name: 'K线',
        type: 'candlestick',
        data: ohlc,
        itemStyle: {
          color: '#ff4d5e',
          color0: '#00c58e',
          borderColor: '#ff4d5e',
          borderColor0: '#00c58e'
        }
      },
      { name: 'MA5', type: 'line', data: ma(5, closes), smooth: true, showSymbol: false, lineStyle: { width: 1, color: '#fbbf24' } },
      { name: 'MA10', type: 'line', data: ma(10, closes), smooth: true, showSymbol: false, lineStyle: { width: 1, color: '#38bdf8' } },
      { name: 'MA20', type: 'line', data: ma(20, closes), smooth: true, showSymbol: false, lineStyle: { width: 1, color: '#a78bfa' } },
      { name: '成交量', type: 'bar', xAxisIndex: 1, yAxisIndex: 1, data: volumes }
    ]
  }
}

function render() {
  if (!chart) return
  chart.setOption(buildOption(props.data), true)
}

function onResize() {
  chart && chart.resize()
}

onMounted(() => {
  chart = echarts.init(el.value)
  render()
  window.addEventListener('resize', onResize)
})

watch(() => props.data, render, { deep: true })

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  chart && chart.dispose()
  chart = null
})
</script>

<template>
  <div ref="el" style="width: 100%; height: 460px;"></div>
</template>
