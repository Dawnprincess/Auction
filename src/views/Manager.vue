<template>
  <!---头部--->
  <div style="height: 60px; background-color: #12d8f3; display:flex; align-items: center">
    <div style="width: 200px;margin-left: 20px; display: flex; align-items: center">
      <img style="width:40px; padding-right: 10px" src="../assets/logo.png" alt="">
      <span style="font-size:30px; color: white ">拍卖系统</span>
    </div>
    <div style="flex: 1"></div>
    <div style="width:fit-content;display: flex;align-items: center">
      <img src="../assets/user.png" alt="" style="width: 40px; height: 40px; border-radius: 50%; margin-right: 5px">
      <span style="color: white; font-size: 18px; margin-right: 10px">用户</span>
    </div>
  </div>

  <!---主体--->
  <div style="display: flex">
    <!-- 左侧菜单栏 -->
    <div style="width: 200px;border-right: 1px solid #ddd;min-height: calc(100vh - 60px)">
      <el-menu style="border: 0" router :default-active="$router.currentRoute.value.path">
        <el-menu-item index="/manager/home">
          <el-icon><House /></el-icon>
          系统首页
        </el-menu-item>
        <el-menu-item index="/manager/data">
          <el-icon><DataAnalysis /></el-icon>
          数据统计
        </el-menu-item>
        <el-sub-menu index="1">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </template>
          <el-menu-item>管理员信息</el-menu-item>
          <el-menu-item>用户信息</el-menu-item>
        </el-sub-menu>
        <el-menu-item>
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-menu-item>
      </el-menu>

    </div>
    <!-- 右侧内容 -->
    <div style="flex: 1; width: 0; background-color: #faf7f7; padding: 10px">
      <RouterView />
    </div>

  </div>
</template>


<script setup>

</script>

<style>
/* 选中菜单时背景颜色,el-menu组件被点击时会添加is-active类，这里设置了el-menu中is-active类的背景颜色,important设置该颜色为最高级 */
.el-menu .is-active{
  background-color: #e6ecf7 !important;
}
</style>