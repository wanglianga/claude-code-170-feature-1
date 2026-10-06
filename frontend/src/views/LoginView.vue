<template>
  <div class="login-wrap">
    <div class="login-card">
      <div class="brand">
        <el-icon size="34" color="#c45656"><AlarmClock /></el-icon>
        <h2>幸福社区</h2>
        <p>独居老人一键求助 · 上门核实转介回访平台</p>
      </div>
      <el-form :model="form" @submit.prevent="login">
        <el-form-item>
          <el-input v-model="form.username" size="large" placeholder="账号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" size="large" type="password" show-password
                    placeholder="密码" :prefix-icon="Lock" @keyup.enter="login" />
        </el-form-item>
        <el-button type="danger" size="large" style="width:100%" :loading="loading" @click="login">登 录</el-button>
      </el-form>
      <el-divider>测试账号（密码均为 123456）</el-divider>
      <div class="accounts">
        <el-tag v-for="a in accounts" :key="a.u" class="acc-tag"
                @click="form.username = a.u"
                :type="a.type" effect="plain">
          {{ a.label }}：{{ a.u }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: 'worker', password: '123456' })

const accounts = [
  { u: 'worker', label: '值班社工', type: 'danger' },
  { u: 'grid', label: '网格员', type: 'warning' },
  { u: 'doctor', label: '家庭医生', type: 'success' },
  { u: 'volunteer', label: '邻里志愿者', type: 'info' },
  { u: 'admin', label: '社区管理者', type: 'primary' }
]

async function login() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号密码')
    return
  }
  loading.value = true
  try {
    const data = await api.post('/auth/login', form)
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data.user))
    ElMessage.success(`欢迎，${data.user.name}`)
    router.push('/workbench')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #8e1f2b 0%, #c45656 60%, #e8908d 100%);
}
.login-card {
  width: 420px;
  background: #fff;
  border-radius: 14px;
  padding: 36px 40px 28px;
  box-shadow: 0 18px 60px rgba(0, 0, 0, .25);
}
.brand { text-align: center; margin-bottom: 22px; }
.brand h2 { margin: 8px 0 4px; color: #8e1f2b; letter-spacing: 4px; }
.brand p { margin: 0; color: #909399; font-size: 13px; }
.accounts { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }
.acc-tag { cursor: pointer; }
</style>
