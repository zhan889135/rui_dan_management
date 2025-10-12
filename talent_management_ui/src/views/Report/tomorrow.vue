<template>
  <div class="app-container">
    <!-- 搜索表单区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" v-show="showSearch" label-width="90px">
      <el-row :gutter="10">
        <el-col :span="4" v-if="deptLevel === 1">
          <el-form-item label="归属供应商" prop="deptName">
              <el-input v-model="queryParams.deptName" placeholder="请输入归属供应商" clearable @keyup.enter.native="handleQuery"/>
          </el-form-item>
        </el-col>
        <el-col :span="4">
           <el-form-item label="面试点位" prop="locationName">
              <el-input v-model="queryParams.locationName" placeholder="请输入面试点位" clearable @keyup.enter.native="handleQuery"/>
           </el-form-item>
        </el-col>
        <el-col :span="4">
           <el-form-item label="姓名" prop="name">
              <el-input v-model="queryParams.name" placeholder="请输入姓名" clearable @keyup.enter.native="handleQuery"/>
           </el-form-item>
        </el-col>
        <el-col :span="4">
           <el-form-item label="电话" prop="phone">
              <el-input v-model="queryParams.phone" placeholder="请输入电话" clearable @keyup.enter.native="handleQuery"/>
           </el-form-item>
        </el-col>
        <el-col :span="4" align="right">
           <el-form-item>
              <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
              <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
           </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <!-- 人数展示区域 -->
    <div class="report-summary-inline">
      <span v-for="(item, index) in tomorrowReportCountList" :key="index" class="report-item" @click="handleCardClick(item)">
        <i class="el-icon-location-outline"></i>
        {{ item.label }}：<b>{{ item.value }}</b> 人
      </span>
    </div>

    <div class="table-wrapper-self">
      <!-- 表格展示区域 -->
      <el-table v-loading="loading" :data="dataSource" stripe>
        <el-table-column label="面试点位" align="center" prop="locationName" show-overflow-tooltip/>
        <el-table-column label="归属供应商" align="center" prop="deptName" width="200" show-overflow-tooltip v-if="deptLevel === 1"/>
        <el-table-column label="姓名" align="center" prop="name" width="100" show-overflow-tooltip/>
        <el-table-column label="性别" align="center" prop="sex" width="50" show-overflow-tooltip>
          <template slot-scope="scope"><dict-tag :options="dict.type.sys_user_sex" :value="scope.row.sex"/></template>
        </el-table-column>
        <el-table-column label="电话" align="center" prop="phone" width="150" show-overflow-tooltip/>
        <el-table-column label="年龄" align="center" prop="age" width="100" show-overflow-tooltip/>
        <el-table-column label="学历" align="center" prop="education" width="100" show-overflow-tooltip>
          <template slot-scope="scope"><dict-tag :options="dict.type.sys_education" :value="scope.row.education"/></template>
        </el-table-column>
        <el-table-column label="面试日期" align="center" prop="interviewDate" width="150" show-overflow-tooltip/>
        <el-table-column label="面试时间" align="center" prop="interviewTime" width="150" show-overflow-tooltip/>
        <el-table-column label="招聘人" align="center" prop="createBy" width="100" show-overflow-tooltip>
          <template slot-scope="{ row }"><span>{{ getNickNameByUserName(row.createBy, userList) }}</span></template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200" fixed="right">
          <template slot-scope="{ row }" >
            <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(row)" v-hasPermi="['interview:tomorrow:view']">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList()"/>

    <!-- 详情 -->
    <DetailDialog ref="detailDialog" @refresh="getList()" @close="handleClose" :dict-map="dict.type" :user-list="userList"/>

  </div>
</template>

<script>
import { list } from "@/api/report";
import DetailDialog from "@/views/Report/detail.vue";
import { getNickNameByUserName } from "@/utils/ruoyi";
import {listUserKv} from "@/api/system/user";
import {tomorrowReportCount} from "@/api/homePage";

export default {
  name: "Tomorrow",
  components: { DetailDialog },
  dicts: ['sys_education', 'sys_user_sex'],
  data() {
    return {
      userList: [], // 用户列表，转义userName
      tomorrowReportCountList: [], // 第二天面试人员总数
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 表格数据
      dataSource: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        deptName: undefined,
        interviewDate: undefined,
        locationName: undefined,
        name: undefined,
        phone: undefined,
      },
      // 登录人部门权限
      deptLevel: this.$store?.state?.user?.deptLevel || 0,
    };
  },
  created() {
    this.getList();
    this.getUserList();
    this.getTomorrowReportCount();
  },
  methods: {
    getNickNameByUserName,
    /** 查询用户列表 */
    getUserList() {
      listUserKv().then(response => this.userList = response.data)
    },
    /** 获取第二天面试人员总数 */
    getTomorrowReportCount() {
      tomorrowReportCount({interviewDate: this.$dayjs().add(1, 'day').format('YYYY-MM-DD')}).then(response => this.tomorrowReportCountList = response.data)
    },
    /** 查询列表 */
    getList(locationId) {
      this.loading = true;
      let params = {
        ...this.queryParams,
        interviewDate: this.$dayjs().add(1, 'day').format('YYYY-MM-DD'),
        locationId: locationId // 传入点位id
      }
      list(params).then(response => {
        this.dataSource = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 查看按钮操作 */
    handleView(row) {
      this.$refs.detailDialog.handleDetail(row.id);
    },
    /** 编辑组件关闭后操作 */
    handleClose() {
      this.handleQuery();
    },

    /** 卡片点击 */
    handleCardClick(item) {
      this.queryParams.pageNum = 1;
      this.getList(item.key); // 把 id 传给 getList
    },
  }
};
</script>

<style scoped>
.report-summary-inline {
  display: flex;
  flex-wrap: wrap;          /* 自动换行 */
  gap: 12px 18px;           /* 行间距/列间距 */
  margin-bottom: 5px;
}

.report-item {
  font-size: 14px;
  color: #606266;
  background: #f9fafc;
  border: 2px solid #ebeef5;
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  line-height: 1.6;
  display: flex;
  align-items: center;
}

.report-item i {
  margin-right: 6px;
  font-size: 16px;
  color: #909399;
}

.report-item b {
  font-size: 15px;
  color: #409eff;
  margin: 0 2px;
}

.report-item:hover {
  background: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
  box-shadow: 0 2px 6px rgba(64,158,255,0.15);
}



</style>
