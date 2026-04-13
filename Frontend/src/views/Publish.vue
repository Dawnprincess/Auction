<template>
  <div style="padding: 20px; max-width: 800px; margin: 0 auto;">
    <el-card>
      <template #header>
        <div style="font-weight: bold;">发布拍卖商品</div>
      </template>

      <el-form ref="formRef" :model="form" label-width="120px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称"></el-input>
        </el-form-item>

        <el-form-item label="商品分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" @change="handleCategoryChange" style="width: 100%;">
            <el-option label="艺术品" value="艺术品"></el-option>
            <el-option label="收藏品" value="收藏品"></el-option>
            <el-option label="生鲜食品" value="生鲜食品"></el-option>
            <el-option label="库存清理" value="库存清理"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="拍卖类型" prop="auctionType">
          <el-radio-group v-model="form.auctionType">
            <el-radio :label="1">英式拍卖（竞价）</el-radio>
            <el-radio :label="2">荷兰式拍卖（降价）</el-radio>
            <el-radio :label="3">密封式拍卖（暗标）</el-radio>
          </el-radio-group>
          <div style="color: #999; font-size: 12px; margin-top: 5px;">
            {{ auctionTypeDesc }}
          </div>
        </el-form-item>

        <el-form-item label="起拍价" prop="startPrice">
          <el-input-number v-model="form.startPrice" :min="0" :precision="2" style="width: 100%;"></el-input-number>
        </el-form-item>

        <el-form-item label="保留价（底价）" prop="reservePrice">
          <el-input-number v-model="form.reservePrice" :min="0" :precision="2" style="width: 100%;"></el-input-number>
        </el-form-item>

        <el-form-item label="价格梯度" prop="priceChange">
          <el-input-number
            v-model="form.priceChange"
            :min="0"
            :precision="2"
            :disabled="form.auctionType === 3"
            style="width: 100%;"
          />
          <span style="margin-left: 10px; color: #666; font-size: 13px;">
            {{ form.auctionType === 1 ? '每次最少加价金额' : (form.auctionType === 2 ? '每分钟自动降价金额' : '密封拍卖无需设置') }}
          </span>
        </el-form-item>

        <el-form-item label="拍卖时间" prop="timeRange">
          <el-date-picker
            v-model="form.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="商品介绍" prop="intro">
          <el-input v-model="form.intro" type="textarea" :rows="4"></el-input>
        </el-form-item>

        <el-form-item label="商品图片">
          <el-upload
            class="goods-uploader"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="handleImageChange"
          >
            <img v-if="form.imageUrl" :src="form.imageUrl" class="goods-img" />
            <el-icon v-else class="uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">立即发布</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed, watch } from 'vue';
import request from '@/utils/request.js';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import router from '@/router/index.js';

const formRef = ref();
const loading = ref(false);
const pendingImageFile = ref(null);

const form = reactive({
  name: '',
  category: '其他',
  auctionType: 1,
  startPrice: 0,
  reservePrice: 0,
  priceChange: 10,
  timeRange: [],
  intro: '',
  imageUrl: ''
});

const auctionTypeDesc = computed(() => {
  const descs = {
    1: '传统拍卖，价格由低到高，价高者得。',
    2: '价格由高到低递减，第一个接受价格的买家成交。',
    3: '所有买家秘密出价，结束后最高价者成交。'
  };
  return descs[form.auctionType];
});

const handleCategoryChange = (val) => {
  const recommendations = {
    '艺术品': { type: 1, change: 50 },
    '收藏品': { type: 1, change: 100 },
    '生鲜食品': { type: 2, change: 5 },
    '库存清理': { type: 2, change: 10 },
  };

  if (recommendations[val]) {
    form.auctionType = recommendations[val].type;
    form.priceChange = recommendations[val].change;
    ElMessage.success(`已为您智能推荐：${form.auctionType === 1 ? '英式' : '荷兰式'}拍卖模式`);
  }
};

// 监听拍卖类型变化
watch(() => form.auctionType, (newType) => {
  if (newType === 3) {
    // 切换到密封式：清空梯度，因为用不到
    form.priceChange = 0;
  } else if (newType === 1) {
    // 切换到英式：给一个合理的默认加价幅度
    form.priceChange = 10;
  } else if (newType === 2) {
    // 切换到荷兰式：给一个合理的默认降价幅度
    form.priceChange = 5;
  }
});

const handleImageChange = (file) => {
  if (file.size / 1024 > 1024) {
    ElMessage.error('图片大小不能超过1MB');
    return;
  }
  form.imageUrl = URL.createObjectURL(file.raw);
  pendingImageFile.value = file.raw;
};

const submitForm = () => {
  if (!form.name || !form.startPrice) {
    ElMessage.warning('请填写完整商品信息');
    return;
  }

  loading.value = true;
  const user = JSON.parse(localStorage.getItem('user'));

  // 处理时间
  let startTime = '';
  let endTime = '';
  if (form.timeRange && form.timeRange.length === 2) {
    const start = new Date(form.timeRange[0]);
    startTime = start.getFullYear() + '-' +
      String(start.getMonth() + 1).padStart(2, '0') + '-' +
      String(start.getDate()).padStart(2, '0') + ' ' +
      String(start.getHours()).padStart(2, '0') + ':' +
      String(start.getMinutes()).padStart(2, '0') + ':' +
      String(start.getSeconds()).padStart(2, '0');

    const end = new Date(form.timeRange[1]);
    endTime = end.getFullYear() + '-' +
      String(end.getMonth() + 1).padStart(2, '0') + '-' +
      String(end.getDate()).padStart(2, '0') + ' ' +
      String(end.getHours()).padStart(2, '0') + ':' +
      String(end.getMinutes()).padStart(2, '0') + ':' +
      String(end.getSeconds()).padStart(2, '0');

    if (end <= start) {
      ElMessage.error("结束时间必须晚于开始时间");
      loading.value = false;
      return;
    }
  }

  // 如果有图片先上传图片
  const uploadAndSubmit = (imageUrl) => {
    const submitData = {
      name: form.name,
      category: form.category,
      auctionType: form.auctionType,
      startPrice: form.startPrice,
      reservePrice: form.reservePrice,
      priceChange: form.priceChange,
      intro: form.intro,
      imageUrl: imageUrl,
      userAccount: user.account,
      startTime: startTime,
      endTime: endTime,
      status: 0 // 默认待审核
    };

    request.post('/goods/add', submitData).then(res => {
      if (res.code === '200') {
        ElMessage.success('发布成功，请等待管理员审核');
        router.push('/manager/person');
      } else {
        ElMessage.error(res.msg);
      }
    }).finally(() => {
      loading.value = false;
    });
  };

  if (pendingImageFile.value) {
    const formData = new FormData();
    formData.append('file', pendingImageFile.value);
    formData.append('type', 'goods');
    request.post('/files/upload', formData).then(res => {
      if (res.code === '200') {
        uploadAndSubmit(res.data);
      } else {
        ElMessage.error('图片上传失败');
        loading.value = false;
      }
    });
  } else {
    uploadAndSubmit(form.imageUrl);
  }
};

const resetForm = () => {
  formRef.value?.resetFields();
  form.imageUrl = '';
  pendingImageFile.value = null;
};
</script>

<style scoped>
.goods-img {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}
.uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  line-height: 178px;
}
</style>
