<!--suppress VueMissingComponentImportInspection -->
<template>
  <div>
    <div style="font-size: 20px; font-weight: bold;color: red">
      My first spring project
    </div>

    <div style="font-size: 30px">
      {{a}}
      {{data.b}}
      {{data.a}}
    </div>

    <div>
      <input v-model="data.a" />
    </div>

    <div>
      <span style="color: red" v-if="data.name === '佩奇'">小猪佩奇</span>
      <span style="color: blue" v-if="data.name === '乔治'">小猪乔治</span>
    </div>

    <div>
      <select style="width: 200px">
        <option v-for="item in data.fruits">{{ item }}</option>
      </select>
    </div>

    <div>
      <button @click="click">点我加功德</button>
    </div>

    <div class="button-row">
      <el-button>Default</el-button>
      <el-button type="primary">Primary</el-button>
      <el-button type="success">Success</el-button>
      <el-button type="info">Info</el-button>
      <el-button type="warning">Warning</el-button>
      <el-button type="danger">Danger</el-button>
    </div>
    <div style="margin: 30px">
      <el-icon :size="20">
        <!--suppress VueMissingComponentImportInspection -->
        <Edit />
      </el-icon>

      <span style="margin-left: 30px">
        <el-icon size="20" color="#666" style="top: 4px"><View /></el-icon> 100
      </span>

      <span style="margin-left: 30px">
          <el-icon class="is-loading">
            <Loading />
          </el-icon>
      </span>
      <el-button type="primary">
        <el-icon style="vertical-align: middle">
          <Search />
        </el-icon>
        <span style="vertical-align: middle"> Search </span>
      </el-button>
    </div>


  </div>
</template>

<script setup>
import {reactive, ref} from "vue";

const a = ref(1);
const data = reactive({
  a:123,
  b:"hello",
  name: "佩奇",
  fruits: ['apple','banana','orange'],
  box:{
    width: '100px',
    height: '100px',
    backgroundColor: 'red'
  }
})
const show = (fruits) => {
  alert(fruits)
}

const click = () => {
  alert("加功德成功")
}

</script>
