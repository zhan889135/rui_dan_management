<template>
  <div class="app-container">
    <!-- 搜索表单区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" v-show="showSearch" label-width="90px">
      <el-row :gutter="10">
        <el-col :span="6" v-if="deptLevel === 1">
          <el-form-item label="归属供应商" prop="deptName">
              <el-input v-model="queryParams.deptName" placeholder="请输入归属供应商" clearable @keyup.enter.native="handleQuery"/>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="面试日期">
            <el-date-picker
              v-model="queryParams.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="yyyy-MM-dd"
              value-format="yyyy-MM-dd"
              clearable
              style="width: 100%"
              @change="handleQuery"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
           <el-form-item label="面试点位" prop="locationName">
              <el-input v-model="queryParams.locationName" placeholder="请输入面试点位" clearable @keyup.enter.native="handleQuery"/>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="姓名" prop="name">
              <el-input v-model="queryParams.name" placeholder="请输入姓名" clearable @keyup.enter.native="handleQuery"/>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="电话" prop="phone">
              <el-input v-model="queryParams.phone" placeholder="请输入电话" clearable @keyup.enter.native="handleQuery"/>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="招聘人" prop="createName">
            <el-input v-model="queryParams.createName" placeholder="请输入招聘人" clearable @keyup.enter.native="handleQuery"/>
          </el-form-item>
        </el-col>
        <el-col :span="12" align="right">
           <el-form-item>
              <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
              <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
           </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <!-- 操作按钮区域 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['interview:report:add']">新增</el-button></el-col>
<!--      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['interview:report:edit']">编辑</el-button></el-col>-->
<!--      <el-col :span="1.5" v-if="deptLevel === 1 || deptLevel === 2"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['interview:report:remove']">删除</el-button></el-col>-->
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <div class="table-wrapper-self">
      <!-- 表格展示区域 -->
      <el-table v-loading="loading" :data="dataSource" @selection-change="handleSelectionChange" stripe>
        <el-table-column type="selection" width="55" align="center" fixed/>
        <el-table-column label="归属供应商" align="center" prop="deptName" width="200" show-overflow-tooltip  v-if="deptLevel === 1"/>
        <el-table-column label="面试点位" align="center" prop="locationName" show-overflow-tooltip/>
        <el-table-column label="姓名" align="center" prop="name" width="100" show-overflow-tooltip/>
        <el-table-column label="性别" align="center" prop="sex" width="50" show-overflow-tooltip>
          <template slot-scope="scope"><dict-tag :options="dict.type.sys_user_sex" :value="scope.row.sex"/></template>
        </el-table-column>
        <el-table-column label="年龄" align="center" prop="age" width="100" show-overflow-tooltip/>
        <el-table-column label="学历" align="center" prop="education" width="100" show-overflow-tooltip>
          <template slot-scope="scope"><dict-tag :options="dict.type.sys_education" :value="scope.row.education"/></template>
         </el-table-column>
        <el-table-column label="面试日期" align="center" prop="interviewDate" width="150" show-overflow-tooltip/>
        <el-table-column label="面试时间" align="center" prop="interviewTime" width="150" show-overflow-tooltip/>
        <el-table-column label="电话" align="center" prop="phone" width="150" show-overflow-tooltip/>
        <el-table-column label="招聘人" align="center" prop="createName" width="100" show-overflow-tooltip/>
        <el-table-column label="创建时间" align="center" prop="createTime" width="150">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="250" fixed="right">
          <template slot-scope="{ row }" >
            <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(row)" v-hasPermi="['interview:report:view']">查看</el-button>
            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(row)" v-hasPermi="['interview:report:edit']" v-if="deptLevel === 1 || deptLevel === 2 || row.createBy === userName">编辑</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(row)" v-hasPermi="['interview:report:remove']" v-if="deptLevel === 1 || deptLevel === 2">删除</el-button>
            <el-button size="mini" type="text" icon="el-icon-thumb" @click="handleArrive(row)">到场</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>
    <!-- 编辑 -->
    <EditDialog ref="editDialog" @refresh="getList" @close="handleClose" :dict-map="dict.type" :user-list="userList" :location-list="locationList"/>
    <!-- 详情 -->
    <DetailDialog ref="detailDialog" @refresh="getList" @close="handleClose" :dict-map="dict.type" :user-list="userList"/>

  </div>
</template>

<script>
import {list, delData, personToFeedback} from "@/api/report";
import EditDialog from "@/views/Report/edit.vue";
import DetailDialog from "@/views/Report/detail.vue";
import { listUserKv } from "@/api/system/user";
import {getNickNameByUserName, parseTime} from "@/utils/ruoyi";
import { allListNoDept } from "@/api/location";

export default {
  name: "Report",
  dicts: ['sys_education', 'sys_user_sex'],
  components: { DetailDialog, EditDialog },
  data() {
    return {
      userList: [], // 用户列表，转义userName
      locationList: [], // 查询点位名称
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
        createName: undefined,
        dateRange: [], // ["2025-10-01", "2025-10-09"]
      },

      // 登录人部门权限
      deptLevel: this.$store?.state?.user?.deptLevel || 0,
      userName: this.$store.state.user.userName,  // 当前登录用户id
    };
  },
  created() {
    this.getList();
    this.getUserList();
    this.getLocationList()
  },
  methods: {
    parseTime,
    getNickNameByUserName,
    /** 查询用户列表 */
    getUserList() {
      listUserKv().then(response => this.userList = response.data)
    },

    /** 查询当前供应商,所关联的点位 */
    getLocationList() {
      allListNoDept({ deptId : this.$store.state.user.deptId }).then(response => this.locationList = response.data)
    },

    /** 查询列表 */
    getList() {
      this.loading = true;
      list(this.queryParams).then(response => {
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
      this.queryParams.dateRange = [];
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!=1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
        this.$refs.editDialog.handleAdd();
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
        this.$refs.editDialog.handleUpdate(row.id || this.ids?.join(','))
    },
    /** 查看按钮操作 */
    handleView(row) {
      this.$refs.detailDialog.handleDetail(row.id);
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids?.join(',');
      this.$modal.confirm('是否确认删除面试报备信息？').then(function() {
        return delData(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 编辑组件关闭后操作 */
    handleClose() {
        this.handleQuery();
    },

    /** 人员到场按钮 */
    handleArrive(row) {
      this.$modal.confirm('是否确认人员到达现场？').then(function() {
        return personToFeedback({ id : row.id });
      }).then(() => {
        this.handleCopy(row);
      }).catch(() => {});
    },

    /** 复制按钮 */
    handleCopy(row) {
      // 拼接多行文本
      const text =
        `姓名：${row.name || ''}\n` +
        `性别：${row.sex || ''}\n` +
        `电话：${row.phone || ''}\n` +
        `年龄：${row.age || ''}\n` +
        `学历：${row.education || ''}`;

      // ② 尝试现代API
      if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(text).then(() => {
          this.$modal.msgSuccess("确认成功,内容已复制并写入输入框");
        }).catch(() => this.copyFallback(text));
      } else {
        this.copyFallback(text);
      }
    },

    copyFallback(text) {
      const input = document.createElement('textarea');
      input.value = text;
      document.body.appendChild(input);
      input.focus();
      input.select();
      try {
        document.execCommand('copy');
        this.$modal.msgSuccess("确认成功,内容已复制并写入输入框");
      } catch (err) {
        this.$modal.msgError("浏览器不支持自动复制，请手动复制");
      }
      document.body.removeChild(input);
    }
  }
};
</script>
