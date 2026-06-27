<template>
  <div class="auth-page">
    <section class="auth-card glass-card">
      <div class="auth-hero">
        <h1>优选商城</h1>
        <p>新鲜水果、精选蔬菜、肉禽蛋品与日用好物统一展示，连接后端微服务完整购物链路。</p>
      </div>
      <div class="auth-form">
        <h2>登录账号</h2>
        <el-form label-position="top" @submit.prevent>
          <el-form-item label="用户名">
            <el-input v-model="form.username" size="large" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" size="large" type="password" show-password placeholder="请输入密码" />
          </el-form-item>
          <el-button type="primary" size="large" class="full-btn" :loading="loading" @click="submit">登录</el-button>
        </el-form>
        <p class="switch-line">还没有账号？<RouterLink to="/register">立即注册</RouterLink></p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const submit = async () => {
  if (!form.username || !form.password) return ElMessage.warning('请输入用户名和密码')
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    await router.push(String(route.query.redirect || '/'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
h2 {
  margin: 0 0 28px;
  font-size: 28px;
}

.full-btn {
  width: 100%;
}

.switch-line {
  margin-top: 20px;
  color: var(--muted);
}

.switch-line a {
  color: var(--primary-dark);
  font-weight: 700;
}
</style>
