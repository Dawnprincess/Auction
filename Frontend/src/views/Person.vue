<template>
  <div style="padding: 20px; max-width: 1000px; margin: 0 auto;">

    <!-- 【修改点1】给 Tabs 容器一个相对定位的父级 -->
    <div style="position: relative; margin-bottom: 20px;">
      <el-tabs v-model="activeTab">

        <el-tab-pane label="基本资料" name="info">
          <el-form :model="data.form" label-width="100px">
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
          </el-form>
        </el-tab-pane>

        <el-tab-pane v-if="data.user.accessId === 1" label="我的发布" name="myGoods">
          <el-table :data="myGoodsList" border stripe style="margin-top: 10px;">
            <el-table-column prop="name" label="商品名称"></el-table-column>
            <el-table-column prop="currentPrice" label="当前价" width="100"></el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag v-if="scope.row.status === 0" type="warning">待审核</el-tag>
                <el-tag v-else-if="scope.row.status === 4" type="primary">即将上架</el-tag>
                <el-tag v-else-if="scope.row.status === 1" type="success">拍卖中</el-tag>
                <el-tag v-else-if="scope.row.status === 2" type="info">已成交</el-tag>
                <el-tag v-else type="danger">流拍</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="商品图" width="100">
               <template #default="scope">
                 <img :src="scope.row.imageUrl" style="width: 50px; height: 50px; object-fit: cover;" />
               </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button size="small" @click="openEditDialog(scope.row)">详情/编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
          <!-- 商品详情/编辑弹窗 -->
          <el-dialog v-model="editDialogVisible" title="商品详情与修改" width="600px">
            <el-form :model="editForm" label-width="100px">
              <el-form-item label="商品名称">
                <el-input v-model="editForm.name" />
              </el-form-item>
              <el-form-item label="商品介绍">
                <el-input v-model="editForm.intro" type="textarea" :rows="3" />
              </el-form-item>
              <el-form-item label="开始时间">
                <el-date-picker v-model="editForm.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%" />
              </el-form-item>
              <el-form-item label="结束时间">
                <el-date-picker v-model="editForm.endTime" type="datetime" placeholder="选择结束时间" style="width: 100%" />
              </el-form-item>

              <!-- 展示一些不可修改的信息 -->
              <el-descriptions :column="2" border size="small" style="margin-top: 20px;">
                <el-descriptions-item label="起拍价">¥{{ editForm.startPrice }}</el-descriptions-item>
                <el-descriptions-item label="当前价">¥{{ editForm.currentPrice }}</el-descriptions-item>
                <el-descriptions-item label="拍卖类型">
                  {{ editForm.auctionType === 1 ? '英式' : (editForm.auctionType === 2 ? '荷兰式' : '密封式') }}
                </el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag v-if="editForm.status === 0" type="warning">待审核</el-tag>
                  <el-tag v-else-if="editForm.status === 4" type="primary">即将上架</el-tag>
                  <el-tag v-else type="info">{{ editForm.status === 1 ? '拍卖中' : '已结束' }}</el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </el-form>
            <template #footer>
              <el-button @click="editDialogVisible = false">关闭</el-button>
              <el-button type="primary" @click="handleSaveEdit" :disabled="editForm.status !== 0 && editForm.status !== 4">
                保存修改
              </el-button>
            </template>
          </el-dialog>
        </el-tab-pane>

        <el-tab-pane v-if="data.user.accessId === 1" label="我的订单" name="myOrders">
          <el-table :data="myOrderList" border stripe style="margin-top: 10px;">
            <el-table-column prop="orderNo" label="订单号" width="180"></el-table-column>
            <el-table-column prop="goodsId" label="商品ID" width="100"></el-table-column>
            <el-table-column prop="price" label="成交价格" width="120">
              <template #default="scope">
                <span style="color: #f56c6c; font-weight: bold;">¥{{ scope.row.price }}</span>
              </template>
            </el-table-column>
            <el-table-column label="订单状态" width="120">
              <template #default="scope">
                <el-tag v-if="scope.row.status === 0" type="warning">待支付</el-tag>
                <el-tag v-else-if="scope.row.status === 1" type="success">已支付</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="成交时间">
              <template #default="scope">
                {{ formatTime(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button
                  v-if="scope.row.status === 0"
                  size="small"
                  type="primary"
                  @click="handlePay(scope.row.id)"
                >
                  模拟支付
                </el-button>
                <span v-else style="color: #999; font-size: 12px;">已完成</span>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="myOrderList.length === 0" description="暂无订单记录" />
        </el-tab-pane>

      </el-tabs>

      <!-- 【修改点2】按钮使用绝对定位固定在右上角 -->
      <el-button
        v-if="data.user.accessId === 1"
        type="primary"
        @click="openPublishDialog"
        style="position: absolute; right: 0; top: 5px; z-index: 10;"
      >
        <el-icon><Plus /></el-icon> 发布商品
      </el-button>
    </div>


  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'; // 【修改点1】导入 watch
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus"; // 引入 ElMessageBox
import { Plus } from '@element-plus/icons-vue';
import router from "@/router/index.js";

const openPublishDialog = () => {
  router.push('/manager/publish')
};

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
    formData.append('type', 'avatar');

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

const activeTab = ref('info');
const myGoodsList = ref([]);
const myOrderList = ref([]); // 新增订单列表

// 格式化时间工具函数
const formatTime = (time) => {
  if (!time) return '';
  return time.replace('T', ' ').substring(0, 19);
};

// 当切换到“我的发布”时加载数据
const loadMyGoods = () => {
  const user = JSON.parse(localStorage.getItem('user'));
  request.get("/goods/myList", { params: { userAccount: user.account } }).then(res => {
    if (res.code === '200') myGoodsList.value = res.data;
  });
};

// 加载我的订单
const loadMyOrders = () => {
  const user = JSON.parse(localStorage.getItem('user'));
  request.get(`/order/buyer/${user.account}`).then(res => {
    if (res.code === '200') myOrderList.value = res.data;
  });
};

const handlePay = (orderId) => {
  ElMessageBox.confirm('确认支付该笔订单吗？', '提示').then(() => {
    request.put(`/order/pay/${orderId}`).then(res => {
      if (res.code === '200') {
        ElMessage.success('支付成功！');
        loadMyOrders(); // 重新加载列表以更新状态
      } else {
        ElMessage.error(res.msg);
      }
    });
  });
};

// 监听标签切换
watch(activeTab, (newVal) => {
  if (newVal === 'myGoods') {
    loadMyGoods();
  } else if (newVal === 'myOrders') {
    loadMyOrders();
  }
});

const editDialogVisible = ref(false);
const editForm = ref({});

const openEditDialog = (row) => {
  editForm.value = { ...row };
  editDialogVisible.value = true;
};

const handleSaveEdit = () => {
  // 1. 时间格式化处理（仿照 Publish.vue）
  const formData = { ...editForm.value };

  if (formData.startTime) {
    const start = new Date(formData.startTime);
    formData.startTime = start.getFullYear() + '-' +
      String(start.getMonth() + 1).padStart(2, '0') + '-' +
      String(start.getDate()).padStart(2, '0') + ' ' +
      String(start.getHours()).padStart(2, '0') + ':' +
      String(start.getMinutes()).padStart(2, '0') + ':' +
      String(start.getSeconds()).padStart(2, '0');
  }

  if (formData.endTime) {
    const end = new Date(formData.endTime);
    formData.endTime = end.getFullYear() + '-' +
      String(end.getMonth() + 1).padStart(2, '0') + '-' +
      String(end.getDate()).padStart(2, '0') + ' ' +
      String(end.getHours()).padStart(2, '0') + ':' +
      String(end.getMinutes()).padStart(2, '0') + ':' +
      String(end.getSeconds()).padStart(2, '0');

    // 简单校验
    if (formData.startTime && new Date(formData.endTime) <= new Date(formData.startTime)) {
      ElMessage.error("结束时间必须晚于开始时间");
      return;
    }
  }

  // 2. 直接复用 update 接口，后端会自动处理权限、状态回退和字段过滤
  request.put("/goods/update", formData).then(res => {
    if (res.code === '200') {
      ElMessage.success("修改成功");
      if (editForm.value.status === 4) {
        ElMessage.info("注意：商品已重新进入待审核状态");
      }
      editDialogVisible.value = false;
      loadMyGoods();
    } else {
      ElMessage.error(res.msg);
    }
  });
};

</script>

<style scoped>
.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}

/* 确保 Tabs 的背景色和容器一致，看起来更融合 */
:deep(.el-tabs__nav-wrap::after) {
  height: 0; /* 去掉 Tabs 自带的下划线，因为我们已经在外面包了一层 border */
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