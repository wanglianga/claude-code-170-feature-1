<template>
  <div v-loading="loading">
    <el-card shadow="never" style="margin-bottom:12px">
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span class="section-title" style="margin:0">独居老人照护能力复盘</span>
        <div>
          <el-date-picker v-model="range" type="daterange" range-separator="至"
                          start-placeholder="开始" end-placeholder="结束"
                          value-format="YYYY-MM-DD" @change="load" style="margin-right:10px" />
          <el-button type="danger" @click="load">刷新</el-button>
        </div>
      </div>
      <el-row :gutter="12" style="margin-top:14px">
        <el-col :span="6" v-for="k in cards" :key="k.label">
          <div class="stat-card">
            <div class="stat-num" :style="{ color: k.color }">{{ k.value }}</div>
            <div class="muted">{{ k.label }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="12">
      <el-col :span="12">
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header><span class="section-title" style="margin:0">按风险等级（严重程度）分布</span></template>
          <div ref="sevChart" style="height:280px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header><span class="section-title" style="margin:0">按研判类型分布</span></template>
          <div ref="typeChart" style="height:280px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header><span class="section-title" style="margin:0">服务类型分布</span></template>
          <div ref="serviceChart" style="height:280px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header><span class="section-title" style="margin:0">回访结果分布</span></template>
          <div ref="followChart" style="height:280px"></div>
        </el-card>
      </el-col>
      <el-col :span="24">
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header><span class="section-title" style="margin:0">每日求助趋势</span></template>
          <div ref="trendChart" style="height:260px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header><span class="section-title" style="margin:0">响应时长</span></template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="有上门记录事件数">{{ stats.response?.count ?? 0 }} 件</el-descriptions-item>
            <el-descriptions-item label="平均响应时长">{{ stats.response?.avgMinutes ?? 0 }} 分钟</el-descriptions-item>
            <el-descriptions-item label="最快响应">{{ stats.response?.minMinutes ?? 0 }} 分钟</el-descriptions-item>
            <el-descriptions-item label="最慢响应">{{ stats.response?.maxMinutes ?? 0 }} 分钟</el-descriptions-item>
            <el-descriptions-item label="15分钟内到场率">
              <el-tag type="success">{{ stats.response?.within15Rate ?? 0 }}%</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" style="margin-bottom:12px">
          <template #header><span class="section-title" style="margin:0">特殊风险场景出现次数</span></template>
          <div ref="flagChart" style="height:230px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import api from '../api'
import {
  SEVERITY_LABEL, TRIAGE_LABEL, SERVICE_LABEL, FOLLOW_RESULT_LABEL, RISK_LABEL
} from '../api/dict'

const loading = ref(false)
const range = ref([])
const stats = ref({})

const sevChart = ref()
const typeChart = ref()
const serviceChart = ref()
const followChart = ref()
const trendChart = ref()
const flagChart = ref()
let charts = []

const cards = computed(() => [
  { label: '求助总数', value: stats.value.totalEvents ?? 0, color: '#f56c6c' },
  { label: '处置中', value: stats.value.openEvents ?? 0, color: '#e6a23c' },
  { label: '夜间求助', value: stats.value.nightEvents ?? 0, color: '#722ed1' },
  { label: '送医转介', value: stats.value.hospitalTransfer ?? 0, color: '#1890ff' }
])

async function load() {
  loading.value = true
  try {
    const params = {}
    if (range.value?.length === 2) { params.from = range.value[0]; params.to = range.value[1] }
    stats.value = await api.get('/stats/overview', { params })
    await nextTick()
    render()
  } finally {
    loading.value = false
  }
}

function pie(el, map, counts, colors) {
  const data = Object.entries(map).map(([k, v]) => ({ name: v, value: counts?.[k] || 0 }))
  const c = echarts.init(el)
  c.setOption({
    tooltip: { trigger: 'item' },
    color: colors,
    legend: { bottom: 0, textStyle: { fontSize: 12 } },
    series: [{
      type: 'pie', radius: ['40%', '68%'], center: ['50%', '45%'],
      label: { formatter: '{b}: {c}' },
      data
    }]
  })
  charts.push(c)
}

function render() {
  charts.forEach(c => c.dispose())
  charts = []

  pie(sevChart.value, SEVERITY_LABEL, stats.value.bySeverity,
    ['#909399', '#e6a23c', '#f56c6c', '#8e1f2b'])
  pie(typeChart.value, TRIAGE_LABEL, stats.value.byTriageType,
    ['#409eff', '#67c23a', '#e6a23c', '#9b59b6', '#909399'])
  pie(serviceChart.value, SERVICE_LABEL, stats.value.byServiceType,
    ['#f56c6c', '#e6a23c', '#409eff', '#67c23a', '#9b59b6', '#13c2c2', '#909399'])
  pie(followChart.value, FOLLOW_RESULT_LABEL, stats.value.byFollowUpResult,
    ['#67c23a', '#409eff', '#f56c6c', '#909399'])

  const daily = stats.value.dailyTrend || {}
  const tc = echarts.init(trendChart.value)
  tc.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: Object.keys(daily) },
    series: [{
      name: '求助数', type: 'line', smooth: true, areaStyle: { color: 'rgba(196,86,86,.2)' },
      lineStyle: { color: '#c45656', width: 3 }, itemStyle: { color: '#c45656' },
      data: Object.values(daily)
    }]
  })
  charts.push(tc)

  const flags = stats.value.riskFlags || {}
  const flagEntries = Object.entries(flags).sort((a, b) => b[1] - a[1])
  const fc = echarts.init(flagChart.value)
  fc.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 130, right: 30, top: 10, bottom: 20 },
    xAxis: { type: 'value', minInterval: 1 },
    yAxis: { type: 'category', data: flagEntries.map(([k]) => RISK_LABEL[k] || k) },
    series: [{
      type: 'bar', barWidth: 14,
      itemStyle: { color: '#f56c6c', borderRadius: [0, 6, 6, 0] },
      label: { show: true, position: 'right' },
      data: flagEntries.map(([, v]) => v)
    }]
  })
  charts.push(fc)
}

function onResize() { charts.forEach(c => c.resize()) }
onMounted(() => {
  load()
  window.addEventListener('resize', onResize)
})
</script>

<style scoped>
.stat-card {
  background: #fafafa; border-radius: 8px; padding: 14px 18px; text-align: center;
}
.stat-num { font-size: 30px; font-weight: 700; }
</style>
