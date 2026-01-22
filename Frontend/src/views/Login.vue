<template>
  <div class="login-container">
    <div class="login-box">
      <div style="padding: 20px; background-color: white; margin-bottom: 20px; border-radius: 20px;">
        <el-form ref="formRef" :model="data.form" :rules="data.rules" style="width: 330px">
          <div style="text-align: center; font-size: 24px;color: #0777e6; font-weight: bold">登录</div>
          <el-form-item prop="account">
            <el-input size="large" v-model="data.form.account" autocomplete="off" placeholder="请输入账号" prefix-icon="User"></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input size="large" v-model="data.form.password" autocomplete="off" placeholder="请输入密码" prefix-icon="Lock" show-password></el-input>
          </el-form-item>
          <el-form-item style="width: 100%" size=large>
            <el-select v-model="data.form.accessId">
              <el-option :value = "0" label = "管理员"></el-option>
              <el-option :value = "1" label = "用户"></el-option>
            </el-select>
          </el-form-item>
          <div>
            <el-button @click="login" size="large" style="width: 100%" type="primary">登录</el-button>
          </div >
          <div style="margin-top: 10px; text-align: right;">没有账号？<a href="/register">注册</a></div>
        </el-form>
      </div>

    </div>

  </div>
</template>

<script setup>
import {reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import request from "@/utils/request.js";
import router from "@/router/index.js";

const data = reactive({
  form: {
    account: '',
    password: '',
    accessId: 1,
  },
  rules:{
    account: [
      { required: true, message: '请输入账号', trigger: 'blur' },
      { pattern: /^[01]\d{2}$/, message: '账号必须以0或1开头,且长度为3位', trigger: ['blur', 'submit'] }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' }
    ]
  }
})

const formRef = ref()

const login = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      request.post('/login', data.form).then(res => {
        if (res.code === '200') {
          // 存储用户信息到本地存储,在跳转后可以使用localStorage.getItem('user')获取
          ElMessage.success('登录成功')
          localStorage.setItem('user', JSON.stringify(res.data))
          setTimeout(() => {
            location.href = '/manager/home'
          }, 1000)

        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}

</script>

<style scoped>
.login-container {
  height: 100vh;
  overflow: hidden;
  background-image: url("@/assets/background.png");
  background-size: 80% 120%;
  background-position: 0 -50px;
}

.login-box{
  width: 25%;
  height: 30%;
  right: 1%;
  top: 25%;
  display: flex;
  align-items: center;
  position: absolute;
}
</style>