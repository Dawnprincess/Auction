<template>
  <div>
    <div class="card" style="margin-bottom: 10px">
      <el-input style="width: 240px" v-model="data.name" placeholder="请输入名字查询" prefix-icon="search"></el-input>
      <el-button type="primary" style="margin-left: 10px" @click="load">查询</el-button>
      <el-button type="warning" style="margin-left: 10px" @click="reset">清空</el-button>
    </div>
    <div class="card" style="margin-bottom: 10px">
      <el-button type="primary" @click="handleAdd">新增</el-button>
      <el-button type="danger" @click="delBatch">删除</el-button>
      <el-button type="info">导出</el-button>
      <el-button type="success">导入</el-button>
    </div>
    <div class="card" style="margin-bottom: 10px">
      <el-table :data="data.tableData" style="width: 100%" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="序列"></el-table-column>
        <el-table-column prop="name" label="名字"></el-table-column>
        <el-table-column prop="account" label="账号"></el-table-column>
        <el-table-column prop="password" label="密码"></el-table-column>
        <el-table-column>
          <template #default="scope">
            <el-button type="primary" @click="handleUpdate(scope.row)" :icon="Edit" circle></el-button>
            <el-button type="danger" @click="handleDelete(scope.row.id)" :icon="Delete" circle></el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 10px">
        <!-- 分页,@标注事件,当页数改变或当前页改变时，触发load再次加载数据刷新 -->
        <el-pagination
            @size-change="load"
            @current-change="load"
            v-model:current-page="data.currentPage"
            v-model:page-size="data.pageSize"
            :page-sizes="[5,10,15,20]"
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="data.total"
        />
      </div>
    </div>

    <el-dialog v-model="data.dialogVisible" title="管理员信息" width="500" destroy-on-close>
      <el-form ref="formRef" :rules="data.rules" :model="data.form" style="padding-right: 70px">
        <el-form-item label="名字" :label-width=" '100px'" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" placeholder="请输入名字"/>
        </el-form-item>
        <el-form-item label="账号" :label-width=" '100px'" prop="account">
          <el-input v-model="data.form.account" @input="updateAccessByAccount" autocomplete="off" placeholder="请输入账号"/>
        </el-form-item>
        <el-form-item label="密码" :label-width=" '100px'" prop="account">
          <el-input v-model="data.form.password" autocomplete="off" placeholder="请输入密码"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="save">
            确认
          </el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import {reactive, ref} from 'vue';
import request from '../utils/request';
import {ElMessage, ElMessageBox} from "element-plus";
import {Delete, Edit} from "@element-plus/icons-vue";
const data = reactive({
  name: null,
  tableData: [],
  currentPage: 1,
  pageSize: 8,
  total: 0,
  dialogVisible: false,
  form: null,
  ids: [],
  rules: {
    name: [
      { required: true, message: '请输入名字', trigger: 'blur' },
      { min: 2, max: 15, message: '名字长度在 2 到 15 个字符', trigger: 'blur' }
    ],
    //账号必须以0开头,长度为3
    account:[
      { required: true, message: '请输入账号', trigger: 'blur' },
      { pattern: /^0\d{2}$/, message: '账号必须以0开头,且长度为3位', trigger: 'blur' }
    ],
  }
})

const formRef = ref()

// 监听账号变化，自动设置权限值
const updateAccessByAccount = (newAccount) => {
  if(newAccount && newAccount.length > 0) {
    const firstChar = newAccount.charAt(0);
    if(firstChar === '0' || firstChar === '1') {
      data.form.accessId = parseInt(firstChar);
    }
  }
};

const load = () => {
  //selectPage接收参数为?类型
  request.get('/admin/selectPage',{
    params: {
      pageNum: data.currentPage,
      pageSize: data.pageSize,
      name: data.name
    }
  }).then(res => {
    data.tableData = res.data.list
    data.total = res.data.total
  })
}
load()

const reset =() => {
  data.name = null
  load()
}
const handleAdd =() =>{
  data.dialogVisible = true
  // 清空表单
  data.form = {}
}
const save =() =>{
  formRef.value.validate((valid) => {
    if (valid) {
      data.form.id ? update() : add()
    } else {
      console.log('保存失败')
      return false
    }
  })
}

const add =() =>{
  request.post('/admin/add',data.form).then(res => {
    if(res.code === '200'){
      data.dialogVisible = false
      ElMessage.success('保存成功')
      load()
    }
    else{
      ElMessage.error(res.msg)
    }
  })
}

const update =() =>{
  request.put('/admin/update',data.form).then(res => {
    if(res.code === '200'){
      data.dialogVisible = false
      ElMessage.success('修改成功')
      load()
    }
    else{
      ElMessage.error(res.msg)
    }
  })
}

const handleUpdate = (row) => {
  data.form = JSON.parse(JSON.stringify(row))//深拷贝,防止修改数据
  data.dialogVisible = true
}

const handleDelete =(id) =>{
  ElMessageBox.confirm('删除数据后无法恢复，是否继续？','确认删除', { type: 'warning'}).then(()=> {
    request.delete('admin/deleteById/' + id).then(res => {
      if(res.code === '200'){
        ElMessage.success('删除成功')
        load()
      }else{
        ElMessage.error(res.msg)
      }
    })
  }).catch()
}

const handleSelectionChange = (rows) => { // 多选框选中行
  data.ids = rows.map(row => row.id)
  //console.log(data.ids)
}

const delBatch =() =>{
  if(data.ids.length === 0){
    ElMessage.warning('请选择要删除的数据')
    return
  }
  ElMessageBox.confirm('删除数据后无法恢复，是否继续？','确认删除', { type: 'warning'}).then(()=> {
    request.delete('/admin/deleteBatch',{data: data.ids}).then(res => {
      if(res.code === '200'){
        ElMessage.success('删除成功')
        load()
      }else{
        ElMessage.error(res.msg)
      }
    })
  }).catch()
}
</script>