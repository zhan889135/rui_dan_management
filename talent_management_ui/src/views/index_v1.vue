<template>
  <div class="app-container">
    <!-- ✅ 顶部导航栏 -->
    <div class="nav-bar">
      <el-tooltip content="查看招聘大厦,面试点位" placement="right" v-hasPermi="['interview:location:list']">
        <button class="shine-button button-emerald" @click="openLink('/location')">点位</button>
      </el-tooltip>

      <el-tooltip content="查看人员招聘信息" placement="right" v-hasPermi="['interview:report:list']">
        <button class="shine-button button-gold" @click="openLink('/report')">报备</button>
      </el-tooltip>

      <el-tooltip content="查看面试人员反馈记录" placement="right" v-hasPermi="['interview:feedback:list']">
        <button class="shine-button button-mint" @click="openLink('/feedback')">反馈</button>
      </el-tooltip>

      <el-tooltip content="供应商管理" placement="right" v-hasPermi="['system:dept:list']">
        <button class="shine-button button-lavender" @click="openLink('/system/dept')">供应商</button>
      </el-tooltip>

      <el-tooltip content="查看,发布公告" placement="right" v-hasPermi="['system:notice:list']">
        <button class="shine-button button-sunset" @click="openLink('/system/notice')">公告</button>
      </el-tooltip>
    </div>

    <div class="card-frame">

      <!-- 左侧 判断，如果包含【邀约量】，不包含【top5】【公告】，则要把宽度拉满-->
      <div class="card-frame-item left" v-hasPermi="['home:page:invitation']"
           :class="{ 'full-width':
           $store.state.user.permissions.includes('home:page:invitation') &&
           !$store.state.user.permissions.includes('home:page:invitationTop5')  &&
           !$store.state.user.permissions.includes('home:page:notice')
      }">
        <!--邀约量-->
        <el-card >
          <div class="search-bar">
            <el-form class="queryForm" :model="queryParams" ref="queryForm" size="mini" label-suffix="：" label-width="115px">
              <el-row :gutter="24">
                <el-col :span="8" v-if="deptLevel === 1">
                  <el-form-item label="归属供应商">
                    <el-input v-model="queryParams.deptName" placeholder="请输入归属供应商" clearable @keyup.enter.native="getInvitationList">
                      <el-button slot="append" icon="el-icon-search" @click="getInvitationList"/>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="面试点位">
                    <el-input v-model="queryParams.name" placeholder="请输入面试点位" clearable @keyup.enter.native="getInvitationList">
                      <el-button slot="append" icon="el-icon-search" @click="getInvitationList"/>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </div>
          <div class="table-wrapper-self">
            <el-table v-loading="invitationLoading" :data="invitationData" style="width: 100%;" :height="tableHeight + 'px'" stripe highlight-current-row @sort-change="handleSortChange">
              <el-table-column label="归属供应商" align="center" prop="deptName" v-if="deptLevel === 1"/>
              <el-table-column label="面试点位" align="center" prop="name"/>
              <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip/>
              <el-table-column label="招聘状态" align="center" prop="status" sortable="custom">
                <template slot-scope="scope">
                  <dict-tag :options="dict.type.location_status" :value="scope.row.status"/>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </div>

      <!--右侧-->
      <div class="card-frame-item right">

        <!--TOP5-->
        <el-card v-hasPermi="['home:page:invitationTop5']">
          <div slot="header" class="card-title">TOP5 排行榜</div>
          <div class="table-wrapper-self">
            <el-table v-loading="invitationTop5Loading" :data="invitationTop5Data" stripe>
              <el-table-column prop="deptName" align="center" label="归属供应商" v-if="deptLevel === 1"></el-table-column>
              <el-table-column label="招聘人" align="center" prop="createBy" width="200">
                <template slot-scope="{ row }"><span>{{ getNickNameByUserName(row.createBy, userList) }}</span></template>
              </el-table-column>
              <el-table-column prop="weekCount" align="center" label="本周邀约量"></el-table-column>
              <el-table-column prop="monthCount" align="center" label="本月邀约量"></el-table-column>
            </el-table>
          </div>
        </el-card>

        <!--公告 判断如果不包含左侧【邀约量】 则需要把布局改成横向布局-->
        <el-card style="padding: 0" v-hasPermi="['home:page:notice']"
                 :class="{ 'notice-grid': !$store.state.user.permissions.includes('home:page:invitation') }">
          <div slot="header" class="card-title">公告</div>
          <div class="notice-container">
            <div class="notice-frame" v-for="(item, index) in sysNoticeData" :key="index" v-loading="sysNoticeLoading">
              <div class="notice-head">
                <div>{{item.deptName}}</div>
                <div>{{ parseTime(item.createTime) }}</div>
              </div>
              <div class="notice-type">{{item.noticeTitle}}</div>
              <el-divider></el-divider>
              <div class="form-container">
                <editor v-model="item.noticeContent" :readOnly="true"/>
              </div>
            </div>
          </div>
        </el-card>

      </div>
    </div>
  </div>
</template>

<script>
import {invitationCount, invitationTop5, sysNoticeList} from "@/api/homePage";
import { listUserKv } from "@/api/system/user";
import { getNickNameByUserName, parseTime } from "@/utils/ruoyi";

export default {
  name: "Index",
  dicts: ['location_status'],
  data() {
    return {
      userList: [], // 用户列表，转义userName
      queryParams: {
        deptName: "",
        name: "",
        // dateRange: [],
        sortField: "",
        sortOrder: "",
      },
      tableHeight: window.innerHeight - 220,
      invitationData: [],
      invitationLoading: false,
      invitationTop5Data: [],
      invitationTop5Loading: false,
      sysNoticeData: [],
      sysNoticeLoading: false,
      // 登录人部门权限
      deptLevel: this.$store?.state?.user?.deptLevel || 0,
    };
  },
  created() {
    this.getUserList();
    this.getInvitationList();
    this.getInvitationTop5();
    this.getSysNoticeList();
  },
  methods: {
    parseTime,
    getNickNameByUserName,
    /** 查询用户列表 */
    getUserList() {
      listUserKv().then(response => this.userList = response.data)
    },
    /** 排序方法 */
    handleSortChange({ prop, order }) {
      this.queryParams.sortField = prop; // 排序字段
      this.queryParams.sortOrder = order; // 排序方式
      this.getInvitationList();
    },
    /** 查询邀约量 */
    getInvitationList() {
      this.invitationLoading = true;
      invitationCount(this.queryParams).then(response => {
        this.invitationData = response.data;
        this.invitationLoading = false;
      });
    },
    /** 邀约量top5 */
    getInvitationTop5() {
      this.invitationTop5Loading = true;
      invitationTop5().then(response => {
        this.invitationTop5Data = response.data;
        this.invitationTop5Loading = false;
      });
    },
    /** 供应商公告 */
    getSysNoticeList() {
      this.sysNoticeLoading = true;
      sysNoticeList().then(response => {
        this.sysNoticeData = response.data;
        this.sysNoticeLoading = false;
      });
    },
    /** 顶部导航栏路由跳转 */
    openLink(path) {
      this.$router.push(path).catch(() => {}); // 使用 Vue Router 跳转
    }
  },
};
</script>

<style lang="scss" scoped>

.app-container{
  padding: 0;
}
// 卡片整体布局
.card-frame {
  display: flex;
  justify-content: space-between;

  .card-frame-item {
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    flex: 1; // 默认占比
    .el-card {
      margin: 0 10px 10px 10px;
      .search-bar {
        width: 100%;
      }
      .card-title {
        font-size: 14px;
        color: #303133;
      }
    }
  }

  .card-frame-item.left {
    flex: 1.8;
  }

  .card-frame-item.right {
    flex: 1;
  }

  // 提高优先级覆盖 flex:1 和 flex:1.8
  .card-frame-item.left.full-width {
    flex: 1 1 100% !important;
    max-width: 100% !important;
  }
}

// 公告下划线
::v-deep .el-divider--horizontal{
  margin: 0 !important;
}
// 公告
.notice-frame {
  font-family: Arial, sans-serif;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 16px;
  max-width: 560px;
  margin: 6px auto 0;
  background-color: #f9f9f9;
  .notice-head {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    font-weight: 700;
    color: #00000073;
  }
  .notice-type {
    font-size: 14px;
    margin-top: 10px;
    margin-bottom: 10px;
    color: #e74c3c;
  }
}

// 公告的富文本编辑器
.form-container {
  max-height: 50vh;
  overflow-y: auto;
  overflow-x: hidden;
}
/* TinyMCE 常见 toolbar 容器 */
::v-deep .tox-editor-header {
  display: none !important;
}

/* Quill 常见 toolbar */
::v-deep .ql-toolbar {
  display: none !important;
}

/* 公告富文本编辑器，如果不包含左侧的邀约量，需要改成横向布局 */
.notice-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px; /* 卡片间距 */
  justify-content: flex-start;
}
/* 公告富文本编辑器，如果不包含左侧的邀约量，需要改成横向布局 */
.notice-grid .notice-container {
  justify-content: space-between; /* 没有左边表格时公告横排分布 */
}
/* 公告富文本编辑器，如果不包含左侧的邀约量，需要改成横向布局 */
.notice-grid .notice-frame {
  flex: 1 1 calc(33.33% - 12px);  /* 三列布局 */
  max-width: calc(33.33% - 12px);
}
/* 公告富文本编辑器，如果不包含左侧的邀约量，需要改成横向布局 */
@media (max-width: 1200px) {
  .notice-grid .notice-frame {
    flex: 1 1 100%; /* 小屏时单列 */
    max-width: 100%;
  }
}

/* 顶部导航栏 */
.nav-bar {
  display: flex;
  justify-content: space-evenly; /* ✅ 均匀分布，不拉伸按钮 */
  align-items: center;
  padding: 5px 0 5px 0;
}

/* 酷炫的按钮样式 */
.shine-button {
  position: relative;
  padding: 0.3rem 0.3rem;
  font-size: 1.1rem;
  font-weight: 600;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  overflow: hidden;
  -webkit-transition: all 0.3s ease;
  transition: all 0.3s ease;
  letter-spacing: 0.5px;
  width: 150px;
}

.shine-button::before {
  content: '';
  position: absolute;
  height: 250%;
  width: 40px;
  top: 0;
  left: -60px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transform: rotate(45deg) translateY(-35%);
  animation: shine 3s ease infinite;
}

@keyframes shine {
  0% {
    left: -80px;
  }
  40% {
    left: calc(100% + 20px);
  }
  100% {
    left: calc(100% + 20px);
  }
}

.button-emerald {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: #fff;
  box-shadow:
    0 10px 30px rgba(17, 153, 142, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.button-emerald:hover {
  transform: translateY(-3px);
  box-shadow:
    0 15px 40px rgba(17, 153, 142, 0.6),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.button-gold {
  background: linear-gradient(135deg, #f7971e 0%, #ffd200 100%);
  color: #fff;
  box-shadow:
    0 10px 30px rgba(247, 151, 30, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.button-gold:hover {
  transform: translateY(-3px);
  box-shadow:
    0 15px 40px rgba(247, 151, 30, 0.6),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.button-mint {
  background: linear-gradient(135deg, #2dd4bf 0%, #06b6d4 100%);
  color: #fff;
  box-shadow:
    0 10px 30px rgba(45, 212, 191, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.button-mint:hover {
  transform: translateY(-3px);
  box-shadow:
    0 15px 40px rgba(45, 212, 191, 0.6),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.button-lavender {
  background: linear-gradient(135deg, #9ca3af 0%, #c084fc 50%, #a78bfa 100%);
  color: #fff;
  box-shadow:
    0 10px 30px rgba(192, 132, 252, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.button-lavender:hover {
  transform: translateY(-3px);
  box-shadow:
    0 15px 40px rgba(192, 132, 252, 0.6),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}
.button-sunset {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: #fff;
  box-shadow:
    0 10px 30px rgba(240, 147, 251, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.button-sunset:hover {
  transform: translateY(-3px);
  box-shadow:
    0 15px 40px rgba(240, 147, 251, 0.6),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}
</style>
