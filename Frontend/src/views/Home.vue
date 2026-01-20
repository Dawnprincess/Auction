<template>
  <a href="/test">通过a标签跳转</a> ||
  <RouterLink to="/test">通过RouterLink跳转</RouterLink>

  <div>
    <el-button type="primary" @click="router.push('/manager/test')">编程式跳转,push</el-button>
    <el-button type="primary" @click="router.replace('/manager/test')">编程式跳转,replace</el-button>
  </div>
  <!-- 路由传参 -->
  <div style="margin-bottom: 20px">
    <el-button type="primary" @click="router.push('/manager/test?id=2&name=Dawn')">路由传参id=2</el-button>
    <el-button type="primary" @click="router.push({path: '/manager/test', query: {id: 2, name: 'Dawn'}})">路由传参id=2</el-button>
  </div>
  <div>

    <div>
      <el-input v-model="data.input" style="width: 240px" placeholder="请输入内容" :prefix-icon="Search" /> {{ data.input }}
      <el-input
          style="width: 240px"
          type = "textarea"
          v-model="data.description"
          class="responsive-input"
          placeholder="当输入的字数太多显示不全，设置textarea来改为多行下拉方式当输入的字数太多显示不全，设置textarea来改为多行下拉方式"
          :suffix-icon="Calendar"
      />
    </div>
    <el-select clearable multiple v-model="data.value" placeholder="选择喜爱的水果" style="width: 240px">
      <el-option
          v-for="item in data.options"
          :key="item.id"
          :label="item.label"
          :value="item.name"
      />
    </el-select> {{data.value}}

    <div style="margin-top: 20px">
      <el-radio-group v-model="data.sex">
        <el-radio value="男">男</el-radio>
      <el-radio value="女">女</el-radio>
      </el-radio-group>
    </div>{{data.sex}}
  </div>

  <el-radio-group v-model="data.tag">
    <el-radio :value="3">Option A</el-radio>
    <el-radio :value="6">Option B</el-radio>
    <el-radio :value="9">Option C</el-radio>
  </el-radio-group>

  <div style="margin-top: 20px" class="block">
    <span class="demonstration">Default</span>
    <el-date-picker
        v-model="data.date"
        type="datetime"
        placeholder="选择日期和时间"
        format="YYYY/MM/DD HH:mm:ss"
        value-format="YYYY/MM/DD HH:mm:ss"
    />
    {{data.date}}
  </div>

  <div>
    <el-table :data="data.tableData" style="width: 100%">
      <el-table-column prop="date" label="Date" width="180" />
      <el-table-column prop="name" label="Name" width="180" />
      <el-table-column prop="address" label="Address" />
      <el-table-column label="操作" />
      <el-table-column>
        <template #default="scope">
          <el-button type="primary" circle @click=edit(scope.row)>
            <el-icon><Edit /></el-icon>
          </el-button>

          <el-button type="danger" circle @click=del(scope.row.id)>
            <el-icon><Delete /></el-icon>
          </el-button>

        </template>
      </el-table-column>
    </el-table>
    <div style="padding : 10px 0">
      <el-pagination
          v-model:current-page="data.currentPage4"
          v-model:page-size="data.pageSize4"
          :page-sizes="[5,10,15,20]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="data.tableData.length"
      />
    </div>
  </div>

  <el-dialog v-model="data.dialogVisible" title="编辑行对象" width="800">
    <div style="padding:20px">
      <div style="margin-bottom: 20px">日期:{{data.row.date}}</div>
      <div style="margin-bottom: 20px">名称:{{data.row.name}}</div>
      <div style="margin-bottom: 20px">地址:{{data.row.address}}</div>
    </div>
  </el-dialog>

</template>

<script setup>
import {reactive} from "vue"
import {Edit, Search, Calendar, Delete} from "@element-plus/icons-vue"
import router from "@/router/index.js";
import request from "@/utils/request.js";


const data = reactive({

  input: null,
  description: null,
  value: null,
  options: [
    {id: 1, label: 'apple', name: 'apple'}, {id: 2, label: 'banana', name: 'banana'}, {
      id: 3,
      label: 'orange',
      name: 'orange'
    }, {id: 4, label: 'apple', name: 'apple1'}
  ],
  sex: '男',
  tag: 3,
  date: '',
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
  currentPage4: 1,
  pageSize4: 8,
  dialogVisible: false,
  row :null,
  userList :[],
})

const edit = (row) => {
  data.dialogVisible = true
  data.row = row
}
const del = (id) => {
  alert("删除id为" + id + "的数据")
}
</script>