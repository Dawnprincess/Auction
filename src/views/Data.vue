<template>
  <div>
    <div class="card" style="margin-bottom: 10px">
      <el-input style="width: 240px" v-model="data.name" placeholder="请输入名称查询" prefix-icon="search"></el-input>
      <el-button type="primary" style="margin-left: 10px">查询</el-button>
      <el-button type="warning" style="margin-left: 10px">清空</el-button>
    </div>
    <div class="card" style="margin-bottom: 10px">
      <el-button type="primary">新增</el-button>
      <el-button type="danger">删除</el-button>
      <el-button type="info">导出</el-button>
      <el-button type="success">导入</el-button>
    </div>
    <div class="card" style="margin-bottom: 10px">
      <el-table :data="data.tableData" style="width: 100%" stripe>
        <el-table-column prop="date" label="日期" width="200"></el-table-column>
        <el-table-column prop="name" label="名称" width="200"></el-table-column>
        <el-table-column prop="address" label="地址" width="200"></el-table-column>
      </el-table>
      <div style="margin-top: 10px">
        <el-pagination
            v-model:current-page="data.currentPage"
            v-model:page-size="data.pageSize"
            :page-sizes="[5,10,15,20]"
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="data.tableData.length"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import {reactive} from 'vue';
  const data = reactive({
    name: null,
    tableData: [
      {id: 1,date: '2022-01-01', name: 'John', address: 'New York No. 1 Lake Park'},
      {id: 2,date: '2022-01-02', name: 'Tom', address: 'London No. 1 Lake Park'},
      {id: 3,date: '2022-01-03', name: 'Jony', address: 'Sydney No. 1 Lake Park',},
      {id: 4,date: '2022-01-01', name: 'John', address: 'New York No. 1 Lake Park'},
      {id: 5,date: '2022-01-02', name: 'Tom', address: 'London No. 1 Lake Park'},
      {id: 6,date: '2022-01-03', name: 'Jony', address: 'Sydney No. 1 Lake Park',},
      {id: 7,date: '2022-01-01', name: 'John', address: 'New York No. 1 Lake Park'},
      {id: 8,date: '2022-01-01', name: 'John', address: 'New York No. 1 Lake Park'},
      {id: 9,date: '2022-01-02', name: 'Tom', address: 'London No. 1 Lake Park'},
      {id: 10,date: '2022-01-03', name: 'Jony', address: 'Sydney No. 1 Lake Park',},
      {id: 11,date: '2022-01-02', name: 'Tom', address: 'London No. 1 Lake Park'},
      {id: 12,date: '2022-01-03', name: 'Jony', address: 'Sydney No. 1 Lake Park',},
    ],
    currentPage: 1,
    pageSize: 8,
  })

</script>

<style scoped>
.card{
  background-color: white;
  padding: 10px;
  border-radius: 5px;
  box-shadow: 0 0 12px rgba(0, 0, 0, .12)
}

</style>