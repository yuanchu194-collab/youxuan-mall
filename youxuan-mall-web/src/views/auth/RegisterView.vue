<template>
  <div class="auth-page">
    <section class="auth-card glass-card">
      <div class="auth-hero">
        <h1>加入优选</h1>
        <p>注册后可以管理收货地址、加入购物车、领取优惠券并完成模拟支付下单。</p>
      </div>
      <div class="auth-form">
        <h2>创建账号</h2>
        <el-form label-position="top" @submit.prevent>
          <el-form-item label="用户名"><el-input v-model="form.username" size="large" /></el-form-item>
          <el-form-item label="密码"><el-input v-model="form.password" size="large" type="password" show-password /></el-form-item>
          <el-form-item label="昵称"><el-input v-model="form.nickname" size="large" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="form.phone" size="large" /></el-form-item>
          <el-button type="primary" size="large" class="full-btn" :loading="loading" @click="submit">注册</el-button>
        </el-form>
        <p class="switch-line">已有账号？<RouterLink to="/login">去登录</RouterLink></p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/modules'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '', nickname: '', phone: '' })

const submit = async () => {
  if (!form.username || !form.password) return ElMessage.warning('请输入用户名和密码')
  loading.value = true
  try {
    await authApi.register(form)
    ElMessage.success('注册成功，请登录')
    await router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
h2 {
  margin: 0 0 24px;
  font-size: 28px;
}

.full-btn {
  width: 100%;
}

.switch-line {
  margin-top: 18px;
  color: var(--muted);
}

.switch-line a {
  color: var(--primary-dark);
  font-weight: 700;
}
</style>
