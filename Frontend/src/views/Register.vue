<template>
  <div class="login-container">
    <div class="login-box">
      <div style="padding: 20px; background-color: white; margin-bottom: 20px; border-radius: 20px;">
        <el-form ref="formRef" :model="data.form" :rules="data.rules" style="width: 330px">
          <div style="text-align: center; font-size: 24px;color: #0777e6; font-weight: bold">注册</div>
          <el-form-item prop="account">
            <el-input size="large" v-model="data.form.account" autocomplete="off" placeholder="请输入账号" prefix-icon="User"></el-input>
          </el-form-item>
          <el-form-item prop="name">
            <el-input size="large" v-model="data.form.name" autocomplete="off" placeholder="请输入名称" prefix-icon="Stamp"></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input size="large" v-model="data.form.password" autocomplete="off" placeholder="请输入密码" prefix-icon="Lock" show-password></el-input>
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input size="large" v-model="data.form.confirmPassword" autocomplete="off" placeholder="请确认密码" prefix-icon="Lock" show-password></el-input>
          </el-form-item>
          <div>
            <el-button @click="register" size="large" style="width: 100%" type="primary">注册</el-button>
          </div >
          <div style="margin-top: 10px; text-align: right;">已有账号？<a href="/login">登陆</a></div>
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

const validatePass = (rules, value, callback) => {
  if (!value) {
    callback(new Error('请再次确认密码'))
  } else if (value !== data.form.password) {
      callback(new Error('两次输入的密码不一致'))
    }else {
    callback()
  }
}

const data = reactive({
  form: {},
  rules:{
    account: [
      { required: true, message: '请输入账号', trigger: 'blur' },
      { pattern: /^[01]\d{2}$/, message: '账号必须以0或1开头,且长度为3位', trigger: ['blur', 'submit'] }
    ],
    //名称不能超过10个字符
    name:[
      { max: 10, message: '名称不能超过10个字符', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
    ],
    confirmPassword: [
        {validator: validatePass, trigger: 'blur'},
        ],
  }
})

const formRef = ref()

const register = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      request.post('/register', data.form).then(res => {
        if (res.code === '200') {
          ElMessage.success('注册成功')
          setTimeout(() => {
            location.href = '/login'
          }, 2000)

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