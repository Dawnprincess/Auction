<template>
  <div class="card" style="width: 50%">
    <el-form ref="formRef" :rules="data.rules" :model="data.form" style="padding-right: 70px">
      <el-form-item label="头像" :label-width="'100px'">
        <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="handleAvatarChange"
        >
          <img v-if="data.form.avatar" :src="data.form.avatar" class="avatar" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
      </el-form-item>
      <el-form-item label="名字" :label-width=" '100px'" prop="name">
        <el-input v-model="data.form.name" autocomplete="off" placeholder="请输入名字"/>
      </el-form-item>
      <el-form-item label="账号" :label-width=" '100px'" prop="account" >
        <el-input disabled v-model="data.form.account" autocomplete="off" placeholder="请输入账号"/>
      </el-form-item>
      <el-form-item label="密码" :label-width=" '100px'" prop="password">
        <el-button type="info" @click="editPwd">修改</el-button>
      </el-form-item>

      <div v-if="data.user.accessId === 1">
        <el-form-item label="性别" :label-width=" '100px'">
          <el-radio-group v-model="data.form.sex">
            <el-radio value="男" label="男">男</el-radio>
            <el-radio value="女" label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
      </div>


    </el-form>
    <div style="text-align: center">
      <el-button @click="updateUser" type="primary">更新</el-button>
      <el-button @click="reset" type="warning">重置</el-button>
    </div>

    <el-dialog v-model="data.dialogVisible" title="修改密码" width="500" destroy-on-close>
      <el-form ref="formRef" :rules="data.rules" :model="data.editPsw" style="padding-right: 70px">
        <el-form-item label="原密码" :label-width=" '100px'" prop="oldPassword">
          <el-input v-model="data.editPsw.oldPassword" autocomplete="off" placeholder="请输入原密码" show-password prefix-icon="Lock"/>
        </el-form-item>
        <el-form-item label="新密码" :label-width=" '100px'" prop="newPassword">
          <el-input v-model="data.editPsw.newPassword"  autocomplete="off" placeholder="请输入新密码" show-password prefix-icon="Lock"/>
        </el-form-item>
        <el-form-item label="确认新密码" :label-width=" '100px'" prop="confirmPassword" required>
          <el-input v-model="data.editPsw.confirmPassword" autocomplete="off" placeholder="请确认新密码" show-password prefix-icon="Lock"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="save">
            确认
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive } from 'vue'
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import {Plus} from "@element-plus/icons-vue";
//与表单绑定formRef一致
const formRef = ref()
const validatePass = (rules, value, callback) => {
  if (!value) {
    callback(new Error('请再次确认密码'))
  } else if (value !== data.editPsw.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  }else {
    callback()
  }
}
const data = reactive({
  //从缓存中获取用户信息(登录时保存的)
  user: JSON.parse(localStorage.getItem('user')),
  form: {},
  editPsw:{
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  },
  rules: {
    name: [
      { required: true, message: '请输入名字', trigger: 'blur' },
      { min: 2, max: 10, message: '名字长度在 2 到 10 个字符', trigger: 'blur' }
    ],
    oldPassword: [
      { required: true, message: '请输入原密码', trigger: 'blur' },
    ],
    newPassword:[
        { required: true, message: '请输入新密码', trigger: 'blur' },
    ],
    confirmPassword: [
      {validator : validatePass, trigger:'blur'},
    ],
  },
  dialogVisible: false,
  pendingAvatarFile: null,

})

const handleAvatarChange = (file) => {
  //先判断图片大小,不能超过1MB
  if (file.size / 1024 > 1024) {
    ElMessage.error('图片大小不能超过1MB')
    return
  }
  // 创建一个临时的URL对象用于预览,以blob开头，只在当前标签页有效（刷新后失效）
  const previewURL = URL.createObjectURL(file.raw)
  data.form.avatar = previewURL
  data.pendingAvatarFile = file.raw
}

const emit = defineEmits(['updateUser'])

data.form = data.user

const reset =() => {
  //将表单重置为初始
  formRef.value.resetFields()
}

const editPwd =() =>{
  data.dialogVisible = true
}

const save =() =>{
  //验证原密码是否正确
  if(data.editPsw.oldPassword === data.form.password){
    data.form.password = data.editPsw.newPassword
    request.put('/users/update',data.form).then(res => {
      if(res.code === '200') {
        data.dialogVisible = false
        ElMessage.success('修改成功')
        localStorage.removeItem('user')
        //回到登录页面
        setTimeout(() => {
          location.href = '/login'
        }, 500)

      } else{
        ElMessage.error(res.msg)
      }
    })
  }else{
    ElMessage.error('原密码不正确')
  }
}

const updateUser =() => {
  // 如果有新的头像文件需要上传
  if(data.pendingAvatarFile) {
    // 先上传文件
    const formData = new FormData();
    formData.append('file', data.pendingAvatarFile);

    request.post('/files/upload', formData).then(uploadRes => {
      if(uploadRes.code === '200') {
        // 获取上传后的URL
        data.form.avatar = uploadRes.data;
        // 在这里执行用户信息更新，确保在文件上传成功之后
        performUserUpdate();
      } else {
        ElMessage.error('头像上传失败');
      }
    }).catch(error => {
      ElMessage.error('头像上传失败');
      console.error(error);
    });
  } else {
    // 没有新头像，直接更新用户信息
    performUserUpdate();
  }
}

// 将用户更新逻辑提取到单独函数
const performUserUpdate = () => {
  if(data.user.accessId === 1){
    //普通用户
    request.put('/users/update' , data.form).then(res => {
      if(res.code === '200'){
        ElMessage.success('更新成功')
        //更新缓存用户信息
        localStorage.setItem('user', JSON.stringify(data.form))
        //触发Manager.vue从缓存中更新数据
        emit('updateUser')
      }
      else{
        ElMessage.error(res.msg)
      }
    })
  }else{
    //管理员
    request.put('/admin/update' , data.form).then(res => {
      if(res.code === '200'){
        ElMessage.success('更新成功')
        //更新缓存用户信息
        localStorage.setItem('user', JSON.stringify(data.form))
        //触发Manager.vue从缓存中更新数据
        emit('updateUser')
      }else{
        ElMessage.error(res.msg)
      }
    })
  }
}
</script>

<style scoped>
.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}
</style>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
}
</style>