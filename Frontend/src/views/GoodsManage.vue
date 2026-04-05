<template>
  <div style="padding: 20px">
    <!-- 搜索栏 -->
    <div style="margin-bottom: 20px; display: flex; gap: 10px">
      <el-input v-model="searchName" placeholder="搜索商品名称" style="width: 200px"></el-input>
      <el-select v-model="searchStatus" placeholder="选择状态" style="width: 150px" clearable>
        <el-option label="待审核" :value="0"></el-option>
        <el-option label="拍卖中" :value="1"></el-option>
        <el-option label="已成交" :value="2"></el-option>
        <el-option label="流拍" :value="3"></el-option>
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button type="success" @click="openAddDialog">新增商品</el-button>
    </div>

    <!-- 商品表格 -->
    <el-table :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="name" label="商品名称"></el-table-column>
      <el-table-column prop="startPrice" label="起拍价" width="100"></el-table-column>
      <el-table-column prop="currentPrice" label="当前价" width="100"></el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="success">拍卖中</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="info">已成交</el-tag>
          <el-tag v-else type="danger">流拍</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="180"></el-table-column>
      <el-table-column prop="endTime" label="结束时间" width="180"></el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="scope">
          <!-- 审核按钮：只有待审核时显示 -->
          <el-button v-if="scope.row.status === 0" size="small" type="success" @click="handleCheck(scope.row, 1)">通过</el-button>
          <el-button v-if="scope.row.status === 0" size="small" type="danger" @click="handleCheck(scope.row, 3)">驳回</el-button>

          <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div style="margin-top: 20px; display: flex; justify-content: flex-end">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadData"
      />
    </div>

    <!-- 新增/编辑弹窗 (复用之前的逻辑) -->
    <el-dialog v-model="dialogVisible" title="商品信息" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="商品名称"><el-input v-model="form.name"></el-input></el-form-item>
        <el-form-item label="起拍价"><el-input-number v-model="form.startPrice"></el-input-number></el-form-item>
        <el-form-item label="保留价"><el-input-number v-model="form.reservePrice"></el-input-number></el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import request from "@/utils/request.js";
import { ElMessage, ElMessageBox } from "element-plus";

const tableData = ref([]);
const searchName = ref("");
const searchStatus = ref(null);
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);
const dialogVisible = ref(false);
const form = reactive({});

const loadData = () => {
  request.get("/goods/selectPage", {
    params: { pageNum: pageNum.value, pageSize: pageSize.value, name: searchName.value, status: searchStatus.value }
  }).then(res => {
    if (res.code === '200') {
      tableData.value = res.data.list;
      total.value = res.data.total;
    }
  });
};

// 审核操作
const handleCheck = (row, status) => {
  const msg = status === 1 ? "确认通过审核吗？" : "确认驳回该商品吗？";
  ElMessageBox.confirm(msg, "提示").then(() => {
    row.status = status;
    request.put("/goods/update", row).then(res => {
      if (res.code === '200') {
        ElMessage.success("操作成功");
        loadData();
      }
    });
  });
};

const handleDelete = (id) => {
  ElMessageBox.confirm("确认删除该商品吗？", "警告").then(() => {
    request.delete(`/goods/deleteById/${id}`).then(res => {
      if (res.code === '200') {
        ElMessage.success("删除成功");
        loadData();
      }
    });
  });
};

const openAddDialog = () => {
  Object.assign(form, { startPrice: 0, reservePrice: 0 });
  dialogVisible.value = true;
};

// ... existing code ...
const save = () => {
  // 1. 前端时间校验
  if (form.startTime && form.endTime) {
    const start = new Date(form.startTime);
    const end = new Date(form.endTime);
    if (end <= start) {
      ElMessage.error("结束时间必须晚于开始时间");
      return; // 阻止发送请求
    }
  }

  // 2. 根据是否有 id 决定请求方式
  let requestPromise;
  if (form.id) {
    // 编辑：使用 PUT 请求
    requestPromise = request.put("/goods/update", form);
  } else {
    // 新增：使用 POST 请求
    requestPromise = request.post("/goods/add", form);
  }

  requestPromise.then(res => {
    if (res.code === '200') {
      ElMessage.success("操作成功");
      dialogVisible.value = false;
      loadData();
    } else {
      ElMessage.error(res.msg);
    }
  }).catch(err => {
    console.error(err);
    ElMessage.error("请求失败，请检查网络或后端日志");
  });
};


const handleEdit = (row) => {
  Object.assign(form, row);
  dialogVisible.value = true;
};

loadData();
</script>
