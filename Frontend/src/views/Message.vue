<template>
  <div style="padding: 20px; max-width: 1000px; margin: 0 auto;">
    <el-card shadow="hover">
      <template #header>
        <div style="font-weight: bold; font-size: 18px;">📬 消息中心</div>
      </template>

      <el-table :data="messageList" stripe style="width: 100%">
        <el-table-column prop="title" label="标题" width="150">
          <template #default="scope">
            <span :style="{ fontWeight: scope.row.isRead === 0 ? 'bold' : 'normal', color: scope.row.isRead === 0 ? '#333' : '#999' }">
              {{ scope.row.title }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="content" label="内容"></el-table-column>

        <el-table-column prop="createTime" label="时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button link type="primary" @click="handleJump(scope.row)">
              {{ scope.row.isRead === 0 ? '查看' : '详情' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import request from '@/utils/request.js';
import { ElMessage } from 'element-plus';

const router = useRouter();
const messageList = ref([]);

const formatTime = (time) => {
  if (!time) return '';
  return time.replace('T', ' ').substring(0, 19);
};

const loadMessages = () => {
  request.get('/message/list').then(res => {
    if (res.code === '200') {
      messageList.value = res.data;
    }
  });
};

// 【核心逻辑】根据消息类型和关联ID进行跳转
const handleJump = (msg) => {
  // 1. 先调用接口标记为已读（或标记相关业务已读）
  request.post(`/message/read/related/${msg.relatedId}`);

  // 2. 根据标题或类型判断跳转路径
  if (msg.title.includes('待审核')) {
    // 管理员跳转到商品管理页，并可以自动筛选出该商品
    router.push({ path: '/manager/goodsManage', query: { id: msg.relatedId } });
  } else if (msg.title.includes('被超越')) {
    // 用户跳转到商品详情页继续竞拍
    router.push(`/goodsDetail/${msg.relatedId}`);
  } else if (msg.title.includes('成功') || msg.title.includes('支付')) {
    // 买家跳转到个人中心-我的订单
    router.push({ path: '/manager/person', query: { tab: 'myOrders' }});
  } else if (msg.title.includes('流拍') || msg.title.includes('售出')) {
    // 卖家跳转到个人中心-我的发布
    router.push({path: '/manager/person', query: { tab: 'myGoods' }});
  } else {
    ElMessage.info('这是一条系统通知');
  }
};

onMounted(() => {
  loadMessages();
});
</script>
