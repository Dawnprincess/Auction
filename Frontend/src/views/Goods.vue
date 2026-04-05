<template>
  <div style="padding: 20px">
    <!-- 顶部操作栏 -->
    <div style="margin-bottom: 20px; display: flex; gap: 10px">
      <el-input v-model="searchName" placeholder="请输入商品名称" style="width: 200px" clearable></el-input>
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="success" @click="dialogVisible = true">发布商品</el-button>
    </div>

    <!-- 商品表格 -->
    <el-table :data="tableData" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="name" label="商品名称"></el-table-column>
      <el-table-column prop="startPrice" label="起拍价" width="100"></el-table-column>
      <el-table-column prop="currentPrice" label="当前价" width="100"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="success">拍卖中</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="info">已成交</el-tag>
          <el-tag v-else type="danger">流拍</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="endTime" label="结束时间" width="180"></el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
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

    <!-- 发布/编辑商品弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '发布商品'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="商品名称">
          <el-input v-model="form.name"></el-input>
        </el-form-item>
        <el-form-item label="商品介绍">
          <el-input v-model="form.intro" type="textarea"></el-input>
        </el-form-item>
        <el-form-item label="起拍价">
          <el-input-number v-model="form.startPrice" :precision="2" :step="1"></el-input-number>
        </el-form-item>
        <el-form-item label="保留价">
          <el-input-number v-model="form.reservePrice" :precision="2" :step="1"></el-input-number>
        </el-form-item>
        <el-form-item label="拍卖时间">
          <el-date-picker
            v-model="form.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item label="商品图片">
          <el-upload
            action="http://localhost:8080/files/upload"
            :on-success="handleUploadSuccess"
            :show-file-list="false"
          >
            <el-button type="primary">点击上传</el-button>
          </el-upload>
          <img v-if="form.imageUrl" :src="form.imageUrl" style="width: 100px; margin-top: 10px" />
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
import { reactive, ref } from "vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";

const tableData = ref([]);
const searchName = ref("");
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);
const dialogVisible = ref(false);

const form = reactive({
  id: null,
  name: "",
  intro: "",
  startPrice: 0,
  reservePrice: 0,
  timeRange: [],
  imageUrl: ""
});

const loadData = () => {
  request.get("/goods/selectPage", {
    params: {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      name: searchName.value
    }
  }).then(res => {
    if (res.code === '200') {
      tableData.value = res.data.list;
      total.value = res.data.total;
    }
  });
};

const handleUploadSuccess = (res) => {
  if (res.code === '200') {
    form.imageUrl = res.data;
    ElMessage.success("上传成功");
  } else {
    ElMessage.error(res.msg);
  }
};

const save = () => {
  // 处理时间范围
  if (form.timeRange && form.timeRange.length === 2) {
    form.startTime = form.timeRange[0];
    form.endTime = form.timeRange[1];
  }

  const url = form.id ? "/goods/update" : "/goods/add";
  request.post(url, form).then(res => {
    if (res.code === '200') {
      ElMessage.success("操作成功");
      dialogVisible.value = false;
      loadData();
    } else {
      ElMessage.error(res.msg);
    }
  });
};

const handleEdit = (row) => {
  Object.assign(form, row);
  dialogVisible.value = true;
};

const handleDelete = (id) => {
  request.delete(`/goods/deleteById/${id}`).then(res => {
    if (res.code === '200') {
      ElMessage.success("删除成功");
      loadData();
    }
  });
};

loadData();
</script>
