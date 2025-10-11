<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="6" :xs="24">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>个人信息</span>
          </div>
          <div>
            <div class="text-center">
              <userAvatar />
            </div>
            <ul class="list-group list-group-striped">
              <li class="list-group-item item-flex">
                <svg-icon icon-class="user" />登录账号
                <div class="item-value">{{ user.userName }}</div>
              </li>
              <li class="list-group-item item-flex">
                <svg-icon icon-class="user" />用户昵称
                <div class="item-value">{{ user.nickName }}</div>
              </li>
              <li class="list-group-item item-flex">
                <svg-icon icon-class="tree" />所属部门
                <div class="item-value">{{ user.deptName }}</div>
              </li>
              <li class="list-group-item item-flex">
                <svg-icon icon-class="phone" />手机号码
                <div class="item-value">{{ user.phoneNumber }}</div>
              </li>
              <li class="list-group-item item-flex">
                <svg-icon icon-class="email" />用户邮箱
                <div class="item-value">{{ user.email }}</div>
              </li>
              <li class="list-group-item item-flex">
                <svg-icon icon-class="email" />身份证号
                <div class="item-value">{{ user.idNumber }}</div>
              </li>
              <li class="list-group-item item-flex">
                <svg-icon icon-class="peoples" />岗位
                <div class="item-value">{{ postGroup }}</div>
              </li>
              <li class="list-group-item item-flex">
                <svg-icon icon-class="peoples" />所属角色
                <div class="item-value">{{ roleGroup }}</div>
              </li>
              <li class="list-group-item item-flex">
                <svg-icon icon-class="date" />创建日期
                <div class="item-value">{{ user.createTime }}</div>
              </li>
            </ul>
          </div>
        </el-card>
      </el-col>
      <el-col :span="18" :xs="24">
        <el-card>
          <div slot="header" class="clearfix">
            <span>基本资料</span>
          </div>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本资料" name="userinfo">
              <userInfo :user="user" />
            </el-tab-pane>
            <el-tab-pane label="修改密码" name="resetPwd">
              <resetPwd />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import userAvatar from "./userAvatar";
import userInfo from "./userInfo";
import resetPwd from "./resetPwd";
import { getUserProfile } from "@/api/system/user";

export default {
  name: "Profile",
  components: { userAvatar, userInfo, resetPwd },
  data() {
    return {
      user: {},
      roleGroup: {},
      postGroup: {},
      activeTab: "userinfo"
    };
  },
  created() {
    this.getUser();
  },
  methods: {
    getUser() {
      getUserProfile().then(response => {
        this.user = response.data;
        this.roleGroup = response.roleGroup;
        this.postGroup = response.postGroup;
      });
    }
  }
};
</script>
<style>
.item-flex {
  display: -webkit-box;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
}

.item-value {
  text-align: right;
  flex: 1;
  color: #666;
  word-break: break-all;  /* 避免长角色名不换行 */
}

</style>
