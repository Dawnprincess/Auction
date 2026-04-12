<template>
  <div style="padding: 20px; max-width: 1200px; margin: 0 auto;">
    <el-card shadow="hover">
      <div style="display: flex; gap: 40px;">

        <!-- 左侧：商品图片 -->
        <div style="flex-shrink: 0;">
          <img
            :src="goods.imageUrl || defaultImage"
            style="width: 400px; height: 400px; object-fit: cover; border-radius: 8px;"
          />
        </div>

        <!-- 右侧：商品信息 + 出价区域 -->
        <div style="flex: 1;">
          <!-- 商品标题 -->
          <h2 style="margin: 0 0 20px 0; color: #333;">{{ goods.name }}</h2>

          <!-- 商品介绍 -->
          <p style="color: #666; line-height: 1.8; margin-bottom: 20px;">{{ goods.intro || '暂无介绍' }}</p>

          <!-- 价格信息 -->
          <div style="background-color: #f5f7fa; padding: 20px; border-radius: 8px; margin-bottom: 20px;">
            <div style="display: flex; justify-content: space-between; margin-bottom: 15px;">
              <span style="color: #999;">起拍价</span>
              <span style="font-size: 18px; color: #333;">¥{{ goods.startPrice }}</span>
            </div>
            <div style="display: flex; justify-content: space-between; margin-bottom: 15px;">
              <span style="color: #999;">当前价</span>
              <span style="font-size: 28px; color: #f56c6c; font-weight: bold;">¥{{ goods.currentPrice }}</span>
            </div>
            <div style="display: flex; justify-content: space-between;">
              <span style="color: #999;">保留价</span>
              <span style="font-size: 18px; color: #333;">¥{{ goods.reservePrice }}</span>
            </div>
          </div>

          <!-- 倒计时 -->
          <div style="text-align: center; margin-bottom: 20px;">
            <div v-if="countdownText" :style="{ fontSize: '32px', fontWeight: 'bold', color: countdownColor }">
              {{ countdownText }}
            </div>
            <div v-else style="font-size: 24px; color: #999;">拍卖已结束</div>
          </div>

          <!-- 出价区域 -->
          <div v-if="canBid && goods.auctionType !== 2" style="display: flex; gap: 10px; margin-bottom: 20px;">
            <el-input-number
              v-model="bidPrice"
              :min="minBidPrice"
              :step="goods.priceChange || 1"
              :precision="2"
              style="flex: 1;"
            />
            <el-button type="danger" size="large" @click="handleBid" :loading="bidding">
              {{ goods.auctionType === 3 ? '提交密封出价' : '立即出价' }}
            </el-button>
          </div>

          <!-- 荷兰式拍卖购买区域 -->
          <div v-else-if="goods.auctionType === 2 && canBid" style="margin-bottom: 20px;">
             <el-alert title="荷兰式拍卖：价格随时间递减，先到先得" type="warning" :closable="false" style="margin-bottom: 10px;" />
             <el-button type="danger" size="large" style="width: 100%;" @click="handleBuyNow">
               以当前价 ¥{{ goods.currentPrice }} 立即购买
             </el-button>
          </div>

          <el-alert
            v-else
            :title="auctionStatusText"
            type="info"
            :closable="false"
            style="margin-bottom: 20px;"
          />

          <!-- 拍卖时间 -->
          <div style="color: #999; font-size: 14px;">
            <div>开始时间：{{ formatTime(goods.startTime) }}</div>
            <div>结束时间：{{ formatTime(goods.endTime) }}</div>
            <div>发布者：{{ goods.userAccount }}</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 出价历史记录 -->
    <el-card shadow="hover" style="margin-top: 20px;">
      <template #header>
        <div style="font-weight: bold; font-size: 16px;">📋 出价记录</div>
      </template>

      <el-table :data="bidList" border stripe v-if="bidList.length > 0">
        <el-table-column prop="userAccount" label="出价人" width="120"></el-table-column>
        <el-table-column prop="price" label="出价金额" width="150">
          <template #default="scope">
            <span style="color: #f56c6c; font-weight: bold;">¥{{ scope.row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="出价时间">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-else description="暂无出价记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRoute } from 'vue-router';
import request from '@/utils/request.js';
import { ElMessage } from 'element-plus';
import defaultImageSrc from '@/assets/very_sorry.png';

const route = useRoute();
const goodsId = route.params.id;

const goods = ref({});
const bidList = ref([]);
const bidPrice = ref(0);
const bidding = ref(false);
const countdownText = ref('');
const countdownColor = ref('#333');
const defaultImage = defaultImageSrc;
let timer = null;

// 计算最低出价（当前价 + 1）
const minBidPrice = computed(() => {
  return goods.value.currentPrice ? Number(goods.value.currentPrice) + 1 : 0;
});

// 判断是否可以出价
const canBid = computed(() => {
  return goods.value.status === 1 && countdownText.value;
});

// 拍卖状态文本
const auctionStatusText = computed(() => {
  if (goods.value.status === 0) return '该商品待审核，暂不可出价';
  if (goods.value.status === 4) return '该商品即将开拍，请耐心等待';
  if (goods.value.status === 2) return '该商品已成交';
  if (goods.value.status === 3) return '该商品已流拍';
  return '拍卖已结束';
});

// 格式化时间
const formatTime = (time) => {
  if (!time) return '';
  return time.replace('T', ' ').substring(0, 19);
};

// 加载商品详情
const loadGoodsDetail = () => {
  request.get(`/goods/detail/${goodsId}`).then(res => {
    if (res.code === '200') {
      goods.value = res.data;
      bidPrice.value = minBidPrice.value;
      updateCountdown();
    }
  });
};

// 加载出价记录
const loadBidList = () => {
  request.get(`/bid/list/${goodsId}`).then(res => {
    if (res.code === '200') {
      bidList.value = res.data;
    }
  });
};

// 更新倒计时
const updateCountdown = () => {
  if (!goods.value.endTime) return;

  const endTime = new Date(goods.value.endTime).getTime();
  const now = new Date().getTime();
  const diff = endTime - now;

  if (diff <= 0) {
    countdownText.value = '';
    if (timer) clearInterval(timer);
    return;
  }

  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
  const seconds = Math.floor((diff % (1000 * 60)) / 1000);

  if (days > 0) {
    countdownText.value = `${days}天 ${hours}时 ${minutes}分 ${seconds}秒`;
  } else {
    countdownText.value = `${hours}时 ${minutes}分 ${seconds}秒`;
  }

  // 最后5分钟变红
  if (diff < 5 * 60 * 1000) {
    countdownColor.value = '#f56c6c';
  } else {
    countdownColor.value = '#333';
  }
};

// 出价
const handleBid = () => {
  if (bidPrice.value <= goods.value.currentPrice) {
    ElMessage.error('出价必须高于当前价格');
    return;
  }

  const user = JSON.parse(localStorage.getItem('user'));
  if (!user) {
    ElMessage.error('请先登录');
    return;
  }

  bidding.value = true;
  request.post('/bid/add', {
    goodsId: goodsId,
    userAccount: user.account,
    price: bidPrice.value
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('出价成功');
      loadGoodsDetail();
      loadBidList();
      bidPrice.value = minBidPrice.value;
    } else {
      ElMessage.error(res.msg);
    }
  }).catch(err => {
    console.error(err);
    ElMessage.error('出价失败，请重试');
  }).finally(() => {
    bidding.value = false;
  });
};

// 荷兰式拍卖立即购买
const handleBuyNow = () => {
  const user = JSON.parse(localStorage.getItem('user'));
  if (!user) {
    ElMessage.error('请先登录');
    return;
  }

  bidding.value = true;
  request.post('/bid/add', {
    goodsId: goodsId,
    userAccount: user.account,
    price: goods.value.currentPrice
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('购买成功！订单已生成');
      loadGoodsDetail();
    } else {
      ElMessage.error(res.msg);
    }
  }).finally(() => {
    bidding.value = false;
  });
};

onMounted(() => {
  loadGoodsDetail();
  loadBidList();

  // 每秒更新倒计时
  timer = setInterval(updateCountdown, 1000);

  // 每5秒刷新一次价格和出价记录
  setInterval(() => {
    loadGoodsDetail();
    loadBidList();
  }, 5000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});
</script>

<style scoped>
/* 可以添加一些自定义样式 */
</style>
