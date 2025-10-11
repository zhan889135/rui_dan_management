<template>
  <div class="navbar">
    <hamburger id="hamburger-container" :is-active="sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />

    <breadcrumb id="breadcrumb-container" class="breadcrumb-container" v-if="!topNav"/>
    <top-nav id="topmenu-container" class="topmenu-container" v-if="topNav"/>

    <div class="right-menu">
      <template v-if="device!=='mobile'">
        <search id="header-search" class="right-menu-item" />

        <screenfull id="screenfull" class="right-menu-item hover-effect" />

        <el-tooltip content="布局大小" effect="dark" placement="bottom">
          <size-select id="size-select" class="right-menu-item hover-effect" />
        </el-tooltip>

        <!-- 在线用户功能 -->
        <el-dropdown class="online-users-container right-menu-item hover-effect" trigger="click">
          <div class="online-users-wrapper">
            <i class="el-icon-user-solid" style="margin-right: 4px;"></i>
            <span class="online-count">{{ onlineCount }}</span>
          </div>
          <el-dropdown-menu slot="dropdown" class="custom-dropdown">
            <div class="online-users-header">在线用户 ({{ onlineCount }})</div>
            <div class="users-list">
              <el-dropdown-item v-for="user in onlineList" :key="user.userId" class="user-item">
                <div class="user-info">
                  <i class="el-icon-user user-icon"></i>
                  <span class="user-name">{{ getNickNameByUserName(user.userName, userList)}} </span>
                </div>
                <span class="user-status"></span>
              </el-dropdown-item>
              <el-dropdown-item v-if="onlineList.length === 0" class="no-users">
                <i class="el-icon-user"></i>
                <span>暂无在线用户</span>
              </el-dropdown-item>
            </div>
          </el-dropdown-menu>
        </el-dropdown>

      </template>

      <el-dropdown class="avatar-container right-menu-item hover-effect" trigger="click">
        <div class="avatar-wrapper">
          <img :src="avatar" class="user-avatar">
          <i class="el-icon-caret-bottom" />
        </div>
        <el-dropdown-menu slot="dropdown">
          <router-link to="/user/profile">
            <el-dropdown-item>个人中心</el-dropdown-item>
          </router-link>
          <el-dropdown-item @click.native="setting = true">
            <span>布局设置</span>
          </el-dropdown-item>
          <el-dropdown-item divided @click.native="logout">
            <span>退出登录</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Breadcrumb from '@/components/Breadcrumb'
import TopNav from '@/components/TopNav'
import Hamburger from '@/components/Hamburger'
import Screenfull from '@/components/Screenfull'
import SizeSelect from '@/components/SizeSelect'
import Search from '@/components/HeaderSearch'
import RuoYiGit from '@/components/RuoYi/Git'
import RuoYiDoc from '@/components/RuoYi/Doc'
import {allOnlineList} from "@/api/monitor/online";
import {listUserKv} from "@/api/system/user";
import {getNickNameByUserName} from "../../utils/ruoyi";

export default {
  components: {
    Breadcrumb,
    TopNav,
    Hamburger,
    Screenfull,
    SizeSelect,
    Search,
    RuoYiGit,
    RuoYiDoc
  },
  computed: {
    ...mapGetters([
      'sidebar',
      'avatar',
      'device'
    ]),
    setting: {
      get() {
        return this.$store.state.settings.showSettings
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'showSettings',
          value: val
        })
      }
    },
    topNav: {
      get() {
        return this.$store.state.settings.topNav
      }
    }
  },
  data() {
    return {
      // 登录用户，登录用户数量
      onlineList: [],
      onlineCount: 0,
      // 用户列表，转义userName
      userList: [],
    };
  },
  created() {
    this.getOnlineList();
    this.getUserList();
  },
  methods: {
    getNickNameByUserName,

    /** 查询登录日志列表 */
    getOnlineList() {
      this.loading = true;
      allOnlineList(this.queryParams).then(response => {
        this.onlineList = response.data;
        this.onlineCount = response.data.length;
      });
    },

    /** 查询用户列表 */
    getUserList() {
      listUserKv().then(response => this.userList = response.data)
    },

    toggleSideBar() {
      this.$store.dispatch('app/toggleSideBar')
    },
    async logout() {
      this.$confirm('确定注销并退出系统吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('LogOut').then(() => {
          window.location.href = '/index';
        })
      }).catch(() => {});
    }
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);

  .hamburger-container {
    line-height: 46px;
    height: 100%;
    float: left;
    cursor: pointer;
    transition: background .3s;
    -webkit-tap-highlight-color:transparent;

    &:hover {
      background: rgba(0, 0, 0, .025)
    }
  }

  .breadcrumb-container {
    float: left;
  }

  .topmenu-container {
    position: absolute;
    left: 50px;
  }

  .errLog-container {
    display: inline-block;
    vertical-align: top;
  }

  .right-menu {
    float: right;
    height: 100%;
    line-height: 50px;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-block;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #5a5e66;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background .3s;

        &:hover {
          background: rgba(0, 0, 0, .025)
        }
      }
    }

    .avatar-container {
      margin-right: 30px;

      .avatar-wrapper {
        margin-top: 5px;
        position: relative;

        .user-avatar {
          cursor: pointer;
          width: 40px;
          height: 40px;
          border-radius: 10px;
        }

        .el-icon-caret-bottom {
          cursor: pointer;
          position: absolute;
          right: -20px;
          top: 25px;
          font-size: 12px;
        }
      }
    }
  }
}




// 在线用户样式显示
.online-users-container {
  cursor: pointer;
  transition: background .3s;
  display: flex;
  align-items: center;
  padding: 0 12px;
}

.online-users-wrapper {
  display: flex;
  align-items: center;
  height: 100%;
}

.online-count {
  font-weight: bold;
  margin-left: 4px;
}

.custom-dropdown {
  border: none !important;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1) !important;
  border-radius: 8px !important;
  overflow: hidden;
  width: 200px;
}

.users-list {
  max-height: 300px;
  overflow-y: auto;
}

/* 滚动条样式 */
.users-list::-webkit-scrollbar {
  width: 6px;
}

.users-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.users-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 10px;
}

.users-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.online-users-header {
  padding: 12px 16px;
  font-weight: 600;
  color: #333;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-bottom: 1px solid #ebeef5;
  font-size: 14px;
}

.user-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px !important;
  border-bottom: 1px solid #f5f5f5;
  transition: all 0.3s;
}

.user-item:hover {
  background-color: #f5f7fa;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #409EFF 0%, #1e6eea 100%);
  border-radius: 50%;
  color: white;
  font-size: 12px;
  margin-right: 10px;
}

.user-name {
  flex: 1;
  font-size: 14px;
  color: #333;
  font-weight: 500;
  font-family: 'Helvetica Neue', Arial, sans-serif;
  letter-spacing: 0.2px;
}

.user-status {
  width: 8px;
  height: 8px;
  background-color: #67c23a;
  border-radius: 50%;
  display: inline-block;
  margin-left: 15px;
}

.no-users {
  text-align: center;
  color: #999;
  font-style: italic;
  padding: 20px !important;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.no-users i {
  font-size: 24px;
  margin-bottom: 8px;
  color: #ccc;
}
</style>
