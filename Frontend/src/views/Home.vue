<template>
  <div style="padding: 20px; background-color: #f5f7fa; min-height: 100vh;">

    <!-- 1. 顶部走马灯 (Banner) -->
    <!-- 修改点：增加 margin: 0 auto 实现居中，设置 max-width 限制最大宽度 -->
    <el-carousel
      height="300px"
      style="margin: 0 auto 30px auto; border-radius: 10px; overflow: hidden; background-color: #f0f0f0; max-width: 650px;"
    >
      <el-carousel-item v-for="item in banners" :key="item.id">
        <img :src="item.url" style="width: 100%; height: 100%; object-fit: contain;" />
      </el-carousel-item>
    </el-carousel>

    <!-- 主体内容区：左侧商品 + 右侧侧边栏 -->
    <div style="display: flex; gap: 20px;">

      <!-- 2. 左侧：商品展示卡片 (Grid布局) -->
      <div style="flex: 1;">
        <!-- 修改点：使用 flex 布局让标题和按钮在同一行两端对齐 -->
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
          <h2 style="color: #333; margin: 0;">🔥 正在热拍</h2>

          <!-- 仅普通用户可见的发布按钮 -->
          <el-button
            v-if="user && user.accessId === 1"
            type="warning"
            size="large"
            @click="handlePublish"
          >
            <el-icon style="margin-right: 5px"><Plus /></el-icon> 发布我的拍卖品
          </el-button>
        </div>

        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 20px;">
          <el-card
            v-for="goods in auctionList"
            :key="goods.id"
            shadow="hover"
            class="goods-card"
            @click="handleDetail(goods.id)"
          >
            <img :src="goods.imageUrl" class="card-img" />
            <div style="padding: 10px 0;">
              <div class="goods-name">{{ goods.name }}</div>
              <div style="display: flex; justify-content: space-between; margin-top: 10px; color: #666; font-size: 14px;">
                <span>当前价:</span>
                <span style="color: #f56c6c; font-weight: bold; font-size: 16px;">¥{{ goods.currentPrice }}</span>
              </div>
              <div style="margin-top: 5px; font-size: 12px; color: #999;">
                截止: {{ formatTime(goods.endTime) }}
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 3. 右侧：即将开始 (Sidebar) -->
      <div style="width: 280px; flex-shrink: 0;">
        <el-card shadow="never">
          <template #header>
            <div style="font-weight: bold;">⏰ 即将开拍</div>
          </template>
          <div v-for="item in upcomingList" :key="item.id" class="upcoming-item">
            <img :src="item.imageUrl" class="small-img" />
            <div class="upcoming-info">
              <div class="small-name">{{ item.name }}</div>
              <div style="font-size: 12px; color: #e6a23c;">{{ formatTime(item.startTime) }} 开始</div>
            </div>
          </div>
          <el-empty v-if="upcomingList.length === 0" description="暂无即将开拍商品" :image-size="60" />
        </el-card>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";
import { Plus } from '@element-plus/icons-vue';
import router from "@/router/index.js"; // 记得引入图标

// 获取当前登录用户信息
const user = ref(JSON.parse(localStorage.getItem('user')));

const auctionList = ref([]); // 正在拍卖
const upcomingList = ref([]); // 即将开始
const banners = ref([
  { id: 1, url: new URL('@/assets/carousel1.jpg', import.meta.url).href },
  { id: 2, url: new URL('@/assets/carousel2.jpg', import.meta.url).href },
  { id: 3, url: new URL('@/assets/carousel3.jpg', import.meta.url).href }
]);

// 格式化时间
const formatTime = (time) => {
  if (!time) return '';
  return time.replace('T', ' ').substring(0, 16);
};

// 加载数据
const loadData = () => {
  // 获取拍卖中的商品 (status=1)
  request.get("/goods/list", { params: { status: 1 } }).then(res => {
    if (res.code === '200') auctionList.value = res.data;
  });

  // 获取即将开始的商品 (status=4)
  request.get("/goods/list", { params: { status: 4 } }).then(res => {
    if (res.code === '200') upcomingList.value = res.data;
  });
};

const handleDetail = (id) => {
  router.push(`/manager/goodsDetail/${id}`);
};

const handlePublish = () => {
  // 这里可以跳转到发布页面，或者弹出一个发布商品的 Dialog
  router.push('/manager/publish');
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.goods-card {
  cursor: pointer;
  transition: transform 0.3s;
}
.goods-card:hover {
  transform: translateY(-5px);
}
.card-img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 4px;
}
.goods-name {
  font-weight: bold;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.upcoming-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}
.upcoming-item:last-child {
  border-bottom: none;
}
.small-img {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  object-fit: cover;
  margin-right: 10px;
}
.upcoming-info {
  flex: 1;
  overflow: hidden;
}
.small-name {
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}
</style>
