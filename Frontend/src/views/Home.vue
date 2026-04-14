<template>
  <div style="padding: 20px; max-width: 1400px; margin: 0 auto;">

    <!-- 1. 顶部：走马灯 -->
    <el-carousel
        height="300px"
        style="margin: 0 auto 30px auto; border-radius: 10px; overflow: hidden; background-color: #f0f0f0; max-width: 650px;"
    >
      <el-carousel-item v-for="item in banners" :key="item.id">
        <img :src="item.url" style="width: 100%; height: 100%; object-fit: contain;" />
      </el-carousel-item>
    </el-carousel>

    <!-- 2. 中部：搜索与筛选区 -->
    <div style="background: #f5f7fa; padding: 20px; border-radius: 8px; margin-bottom: 30px; display: flex; gap: 15px; align-items: center;">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索你感兴趣的拍卖品..."
        prefix-icon="Search"
        clearable
        @keyup.enter="handleSearch"
        style="max-width: 400px;"
      />
      <el-select v-model="searchType" placeholder="拍卖类型" style="width: 120px;">
        <el-option label="全部类型" :value="0" />
        <el-option label="英式拍卖" :value="1" />
        <el-option label="荷兰式" :value="2" />
        <el-option label="密封式" :value="3" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- 3. 底部：左右分栏内容区 -->
    <div style="display: flex; gap: 30px;">

      <!-- 左侧：商品展示 -->
      <div style="flex: 1;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
          <!-- 标题只认 isSearching 变量 -->
          <h2 style="color: #333; margin: 0;"> {{ isSearching ? '🔎 搜索结果' : '🔥 正在热拍' }}</h2>

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

        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 20px; margin-bottom: 40px;">
          <el-card v-for="goods in auctionList" :key="goods.id" shadow="hover" class="goods-card" @click="handleDetail(goods.id)">
            <img :src="goods.imageUrl" class="card-img" />
            <div style="padding: 10px 0;">
              <div class="goods-name">{{ goods.name }}</div>

              <!-- 根据拍卖类型显示不同价格 -->
              <div v-if="goods.auctionType === 3" style="display: flex; justify-content: space-between; margin-top: 10px; color: #666; font-size: 14px;">
                <span>起拍价:</span>
                <span style="color: #409eff; font-weight: bold; font-size: 16px;">¥{{ goods.startPrice }}</span>
              </div>
              <div v-else style="display: flex; justify-content: space-between; margin-top: 10px; color: #666; font-size: 14px;">
                <span>当前价:</span>
                <span style="color: #f56c6c; font-weight: bold; font-size: 16px;">¥{{ goods.currentPrice }}</span>
              </div>
              <div style="margin-top: 5px; font-size: 12px; color: #999;">
                截止: {{ formatTime(goods.endTime) }}
              </div>
            </div>
          </el-card>
          <el-empty v-if="auctionList.length === 0" description="暂无相关商品" />
        </div>

        <!-- 新增：近期成交展示 (仅在非搜索模式下显示) -->
        <div style="margin-top: 20px;" v-if="!isSearching">
          <h2 style="color: #333; margin-bottom: 20px;">🏆 近期成交</h2>
          <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 20px;">
            <el-card v-for="goods in soldList" :key="goods.id" shadow="hover" class="goods-card" @click="handleDetail(goods.id)">
              <img :src="goods.imageUrl" class="card-img" />
              <div style="padding: 10px 0;">
                <div class="goods-name">{{ goods.name }}</div>
                <div style="display: flex; justify-content: space-between; margin-top: 10px; color: #666; font-size: 14px;">
                  <span>成交价:</span>
                  <span style="color: #67c23a; font-weight: bold; font-size: 16px;">¥{{ goods.currentPrice }}</span>
                </div>
                <div style="margin-top: 5px; font-size: 12px; color: #999;">
                  结束于: {{ formatTime(goods.endTime) }}
                </div>
              </div>
            </el-card>
            <el-empty v-if="soldList.length === 0" description="暂无成交记录" :image-size="60" />
          </div>
        </div>
      </div>

      <!-- 右侧：即将上架 -->
      <div style="width: 280px; flex-shrink: 0;">
        <el-card shadow="never" style="position: sticky; top: 20px;">
          <template #header>
            <span style="font-weight: bold;">⏰ 即将上架</span>
          </template>
          <div v-for="item in upcomingList" :key="item.id" class="upcoming-item">
            <img :src="item.imageUrl" class="small-img" />
            <div class="upcoming-info">
              <div class="small-name">{{ item.name }}</div>
              <div style="font-size: 12px; color: #e6a23c;">{{ formatTime(item.startTime) }} 开始</div>
            </div>
          </div>
          <el-empty v-if="upcomingList.length === 0" description="暂无即将上架商品" :image-size="60" />
        </el-card>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from "vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";
import { Plus } from '@element-plus/icons-vue';
import router from "@/router/index.js"; // 记得引入图标

// 获取当前登录用户信息
const user = ref(JSON.parse(localStorage.getItem('user')));

const auctionList = ref([]); // 正在拍卖
const soldList = ref([]);    // 已成交商品
const upcomingList = ref([]); // 即将开始
const banners = ref([
  { id: 1, url: new URL('@/assets/carousel1.jpg', import.meta.url).href },
  { id: 2, url: new URL('@/assets/carousel2.jpg', import.meta.url).href },
  { id: 3, url: new URL('@/assets/carousel3.jpg', import.meta.url).href }
]);
const searchKeyword = ref('');
const searchType = ref(0);
const isSearching = ref(false); // 只有点搜索按钮才变 true

// 格式化时间
const formatTime = (time) => {
  if (!time) return '';
  return time.replace('T', ' ').substring(0, 16);
};

// 【核心修改】监听搜索关键词的变化
watch(searchKeyword, (newVal) => {
  // 如果关键词变为空字符串，且当前处于搜索模式
  if (newVal === '' && isSearching.value) {
    resetSearch(); // 直接调用重置逻辑
  }
});

// 搜索按钮点击 / 回车触发
const handleSearch = () => {
  if (!searchKeyword.value && searchType.value === 0) {
    ElMessage.warning("请输入搜索内容或选择类型");
    return;
  }
  isSearching.value = true;
  soldList.value = [];
  loadData();
};

// 重置按钮点击
const resetSearch = () => {
  searchKeyword.value = '';
  searchType.value = 0;
  isSearching.value = false;
  loadData();
};

// 核心加载方法
const loadData = () => {
  //查询所有商品
  const params = { };

  // 只有在“搜索模式”下，才把筛选条件传给后端
  if (isSearching.value) {
    if (searchKeyword.value) {
      params.name = searchKeyword.value;
    }
    if (searchType.value !== 0) {
      params.auctionType = searchType.value;
    }
    request.get("/goods/list", { params }).then(res => {
      if (res.code === '200') {
        auctionList.value = res.data;
      }
    });
  }else{
    //正常模式
    params.status = 1;
    request.get("/goods/list", { params }).then(res => {
      if (res.code === '200') {
        auctionList.value = res.data;
      }
    });
    // 侧边栏和底部成交列表不受搜索影响，始终加载最新
    request.get("/goods/list", { params: { status: 2 } }).then(res => {
      if (res.code === '200') soldList.value = res.data.slice(0, 8);
    });

  }
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

// 页面初始化时调用一次
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
