<template>
  <div class="app-container">
    <!-- 搜索表单区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="归属供应商" prop="deptName" v-if="deptLevel === 1">
        <el-input v-model="queryParams.deptName" placeholder="请输入归属供应商" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="面试点位" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入面试点位" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮区域 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5" v-if="deptLevel === 1"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['interview:location:add']">新增</el-button></el-col>
      <el-col :span="1.5" v-if="deptLevel === 1"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['interview:location:edit']">编辑</el-button></el-col>
      <el-col :span="1.5" v-if="deptLevel === 1"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['interview:location:remove']">删除</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" ></right-toolbar>
    </el-row>

    <div class="table-wrapper-self">
      <!-- 表格展示区域 -->
      <el-table v-loading="loading" :data="dataSource" @selection-change="handleSelectionChange" stripe>
        <el-table-column type="selection" width="55" align="center"/>
        <el-table-column label="归属供应商" align="center" prop="deptName" show-overflow-tooltip v-if="deptLevel === 1"/>
        <el-table-column label="公司名" align="center" prop="companyName" show-overflow-tooltip>
          <template slot-scope="scope">
            <span class="table-btntxt" @click="getDetails(scope.row.details)">{{ scope.row.companyName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="面试点位" align="center" prop="name" show-overflow-tooltip />
        <el-table-column label="需求量" align="center" prop="demand" show-overflow-tooltip/>
        <el-table-column label="面试日期" align="center" prop="date" show-overflow-tooltip/>
        <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip/>
        <el-table-column label="招聘状态" align="center" prop="status" show-overflow-tooltip>
          <template slot-scope="scope">
            <dict-tag :options="dict.type.location_status" :value="scope.row.status"/>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
          <template slot-scope="{ row }">
            <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(row)" v-hasPermi="['interview:location:view']">查看</el-button>
            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(row)" v-hasPermi="['interview:location:edit']" v-if="deptLevel === 1">编辑</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(row)" v-hasPermi="['interview:location:remove']" v-if="deptLevel === 1">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>
    <!-- 编辑 -->
    <EditDialog ref="editDialog" @refresh="getList" @close="handleClose" :dict-map="dict.type" :user-list="userList" :location-list="locationList" :dept-name-list="deptNameList"/>
    <!-- 详情 -->
    <DetailDialog ref="detailDialog" @refresh="getList" @close="handleClose" :dict-map="dict.type" :user-list="userList"/>
    <!-- html展示组件 -->
    <ShowHtml ref="showHtml"></ShowHtml>
  </div>
</template>

<script>
import { list, delData, allListNoDept } from "@/api/location";
import EditDialog from "@/views/Location/edit.vue";
import DetailDialog from "@/views/Location/detail.vue";
import ShowHtml from "@/components/ShowHtml";
import { listUserKv } from "@/api/system/user";
import { selectLevel2DeptName } from "@/api/system/dept";

export default {
  name: "Location",
  dicts: ['location_status'],
  components: { EditDialog, DetailDialog, ShowHtml },
  data() {
    return {
      userList: [], // 用户列表，转义userName
      locationList: [], // 历史公司名称
      deptNameList: [], // 部门名称
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
        companyName: undefined,
        name: undefined
      },
      // 列信息
      // columns: [
      //   { key: 0, label: `归属供应商`, visible: true },
      //   { key: 1, label: `公司名`, visible: true },
      //   { key: 2, label: `面试点位`, visible: true },
      //   { key: 3, label: `需求量`, visible: true },
      //   { key: 4, label: `面试时间`, visible: true },
      //   { key: 5, label: `备注`, visible: true },
      //   { key: 6, label: `招聘状态`, visible: true },
      //   { key: 7, label: `操作`, visible: true },
      // ],
      // 登录人部门权限
      deptLevel: this.$store?.state?.user?.deptLevel || 0,
    };
  },
  created() {
    this.getList();
    this.getUserList();
    this.getLocationList()
    this.getDeptDataList()
  },
  methods: {
    /** 查询用户列表 */
    getUserList() {
      listUserKv().then(response => this.userList = response.data)
    },

    /** 查询历史公司名称 */
    getLocationList() {
      allListNoDept().then(response => this.locationList = response.data)
    },

    /** 查询全部供应商，多级的按照/拼接 */
    getDeptDataList() {
      selectLevel2DeptName().then(response => this.deptNameList = response.data)
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
      this.$modal.confirm('是否确认删除面试点位？').then(function() {
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
    /** 详情展示操作 */
    getDetails(details){
        this.$refs.showHtml.handleOpen('详情',details);
    }
  }
};
</script>
