<template>
  <div style="padding: 40px; max-width: 800px; margin: 0 auto; background-color: white; border-radius: 8px;">
    <h2 style="margin-bottom: 30px; text-align: center;">发布拍卖商品</h2>

    <el-form :model="form" label-width="120px" ref="formRef">
      <el-form-item label="商品名称" required>
        <el-input v-model="form.name" placeholder="请输入商品名称"></el-input>
      </el-form-item>

      <el-form-item label="商品介绍">
        <el-input v-model="form.intro" type="textarea" :rows="4" placeholder="请详细描述您的商品"></el-input>
      </el-form-item>

      <div style="display: flex; gap: 20px;">
        <el-form-item label="起拍价" required>
          <el-input-number v-model="form.startPrice" :min="0" :precision="2"></el-input-number>
        </el-form-item>
        <el-form-item label="保留价" required>
          <el-input-number v-model="form.reservePrice" :min="0" :precision="2"></el-input-number>
        </el-form-item>
      </div>

      <el-form-item label="拍卖开始时间" required>
        <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择拍卖开始时间"
            style="width: 100%;"
        />
      </el-form-item>
      <el-form-item label="拍卖结束时间" required>
        <el-date-picker
          v-model="form.endTime"
          type="datetime"
          placeholder="选择拍卖结束时间"
          style="width: 100%;"
        />
      </el-form-item>

      <el-form-item label="商品图片" required>
        <el-upload
          class="goods-uploader"
          action="http://localhost:8080/files/upload"
          :data="{ type: 'goods' }"
          :show-file-list="false"
          :on-success="handleUploadSuccess"
        >
          <img v-if="form.imageUrl" :src="form.imageUrl" class="goods-img" />
          <el-icon v-else class="uploader-icon"><Plus /></el-icon>
        </el-upload>
        <div style="color: #999; font-size: 12px; margin-top: 5px;">建议尺寸：800x800，支持 jpg/png</div>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm" :loading="loading" style="width: 100%;">立即发布</el-button>
        <el-button @click="cancel" style="width: 100%; margin-left: 0;">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";
import { Plus } from '@element-plus/icons-vue';
import router from "@/router/index.js";

const loading = ref(false);
const formRef = ref();
const form = reactive({
  name: '',
  intro: '',
  startPrice: 0,
  reservePrice: 0,
  startTime: '',
  endTime: '',
  imageUrl: ''
});

const handleUploadSuccess = (res) => {
  if (res.code === '200') {
    form.imageUrl = res.data;
    ElMessage.success("图片上传成功");
  } else {
    ElMessage.error(res.msg);
  }
};

const submitForm = () => {
  if (!form.name || !form.startPrice || !form.endTime || !form.imageUrl) {
    ElMessage.warning("请填写所有必填项并上传图片");
    return;
  }

  loading.value = true;
  const user = JSON.parse(localStorage.getItem('user'));

  // 组装数据
  const data = {
    ...form,
    userAccount: user.account,
    status: 0
  };

  request.post("/goods/add", data).then(res => {
    loading.value = false;
    if (res.code === '200') {
      ElMessage.success("发布成功！请等待管理员审核。");
      router.push("/manager/person"); // 发布成功后跳回个人中心
    } else {
      ElMessage.error(res.msg);
    }
  }).catch(() => {
    loading.value = false;
  });
};

const cancel = () => {
  router.back();
};
</script>

<style scoped>
.goods-uploader .goods-img {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
  border-radius: 6px;
}
.goods-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}
.goods-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}
.uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}
</style>
