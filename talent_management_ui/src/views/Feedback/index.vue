<template>
  <div class="app-container">
    <!-- 搜索表单区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" v-show="showSearch" label-width="90px">
      <el-row :gutter="10">
        <el-col :span="6">
          <el-form-item label="面试点位" prop="locationName">
              <el-input v-model="queryParams.locationName" placeholder="请输入面试点位" clearable @keyup.enter.native="handleQuery"/>
          </el-form-item>
        </el-col>
        <el-col :span="3" v-if="deptLevel === 1">
          <el-form-item label="归属供应商" prop="deptName">
              <el-input v-model="queryParams.deptName" placeholder="请输入归属供应商" clearable @keyup.enter.native="handleQuery"/>
          </el-form-item>
        </el-col>
        <el-col :span="3">
          <el-form-item label="姓名" prop="name">
              <el-input v-model="queryParams.name" placeholder="请输入姓名" clearable @keyup.enter.native="handleQuery"/>
          </el-form-item>
        </el-col>
        <el-col :span="3">
          <el-form-item label="性别" prop="sex">
            <el-select v-model="queryParams.sex" placeholder="请选择性别" style="width: 100%;" @change="handleQuery" clearable>
              <el-option v-for="item in dict.type.sys_user_sex" :key="item.value" :label="item.label" :value="item.value"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="3">
          <el-form-item label="学历" prop="education">
            <el-select v-model="queryParams.education" placeholder="请选择学历" style="width: 100%;" @change="handleQuery" clearable>
                <el-option v-for="item in dict.type.sys_education" :key="item.value" :label="item.label" :value="item.value"/>
            </el-select>
          </el-form-item>
        </el-col>

        <!--总部门权限-->  <!--供应商权限-->
        <span v-if="deptLevel === 1 || deptLevel === 2">
          <el-col :span="3">
            <el-form-item label="硬性条件" prop="hardRequirements">
              <el-select v-model="queryParams.hardRequirements" placeholder="请选择硬性条件" style="width: 100%;" @change="handleQuery" clearable>
                <el-option v-for="item in dict.type.sys_judge" :key="item.value" :label="item.label" :value="item.value"/>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="3">
            <el-form-item label="是否计费" prop="isBilling">
              <el-select v-model="queryParams.isBilling" placeholder="请选择是否计费" style="width: 100%;" @change="handleQuery" clearable>
                <el-option v-for="item in dict.type.sys_judge" :key="item.value" :label="item.label" :value="item.value"/>
              </el-select>
            </el-form-item>
          </el-col>
        </span>

        <!--三级员工权限-->
        <span v-if="deptLevel === 3">
           <el-col :span="3">
            <el-form-item label="硬性条件" prop="hardRequirements2">
              <el-select v-model="queryParams.hardRequirements2" placeholder="请选择硬性条件" style="width: 100%;" @change="handleQuery" clearable>
                <el-option v-for="item in dict.type.sys_judge" :key="item.value" :label="item.label" :value="item.value"/>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="3">
            <el-form-item label="是否计费" prop="isBilling2">
              <el-select v-model="queryParams.isBilling2" placeholder="请选择是否计费" style="width: 100%;" @change="handleQuery" clearable>
                <el-option v-for="item in dict.type.sys_judge" :key="item.value" :label="item.label" :value="item.value"/>
              </el-select>
            </el-form-item>
          </el-col>
        </span>

        <el-col :span="3">
          <el-form-item label="招聘人" prop="createName">
            <el-input v-model="queryParams.createName" placeholder="请输入招聘人" clearable @keyup.enter.native="handleQuery"/>
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


        <el-col :span="15" align="right">
           <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <!-- 操作按钮区域 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="info" plain icon="el-icon-upload2" size="mini" @click="handleImport" v-hasPermi="['interview:feedback:import']" v-if="deptLevel === 1">导入</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" >导出</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-upload2" size="mini" @click="pushExport" v-if="deptLevel === 2">一键推送</el-button></el-col>
      <el-col :span="1.5" v-if="deptLevel === 1"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete">删除</el-button></el-col>

      <el-col :span="1.5"><span class="card-sum"><i class="el-icon-user-solid"></i>总送人数：<span class="sum-number">{{ statistics.totalCount }}</span></span></el-col>
      <el-col :span="1.5"><span class="card-sum"><i class="el-icon-s-help"></i>硬性人数：<span class="sum-number">{{ statistics.hardRequirementsYesCount }}</span></span></el-col>
      <el-col :span="1.5"><span class="card-sum"><i class="el-icon-s-marketing"></i>计费人数：<span class="sum-number">{{ statistics.isBillingYesCount }}</span></span></el-col>
      <el-col :span="1.5"><span class="card-sum"><i class="el-icon-warning"></i>未出人数：<span class="sum-number">{{ statistics.bothNullCount }}</span></span></el-col>

      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <div class="table-wrapper-self">
      <!-- 表格展示区域 -->
      <el-table v-loading="loading" :data="dataSource" @selection-change="handleSelectionChange" stripe>
        <el-table-column type="selection" width="55" align="center" fixed/>
        <el-table-column label="面试点位" align="center" prop="locationName" show-overflow-tooltip/>
        <el-table-column label="归属供应商" align="center" prop="deptName" show-overflow-tooltip v-if="deptLevel === 1"/>
        <el-table-column label="姓名" align="center" prop="name" width="100" show-overflow-tooltip/>
        <el-table-column label="性别" align="center" prop="sex" width="80" show-overflow-tooltip>
          <template slot-scope="scope">
            <dict-tag :options="dict.type.sys_user_sex" :value="scope.row.sex"/>
          </template>
         </el-table-column>
        <el-table-column label="电话" align="center" prop="phone" width="150" show-overflow-tooltip/>
        <el-table-column label="年龄" align="center" prop="age" width="80" show-overflow-tooltip/>
        <el-table-column label="学历" align="center" prop="education" width="100" show-overflow-tooltip>
          <template slot-scope="scope">
            <dict-tag :options="dict.type.sys_education" :value="scope.row.education"/>
          </template>
         </el-table-column>
        <el-table-column label="面试日期" align="center" prop="interviewDate" width="100" show-overflow-tooltip/>

        <!--总部门权限-->  <!--供应商权限-->
        <span v-if="deptLevel === 1 || deptLevel === 2">
          <el-table-column label="反馈原因" align="center" prop="reason" show-overflow-tooltip/>
          <el-table-column label="硬性条件" align="center" prop="hardRequirements" width="80" show-overflow-tooltip>
            <template slot-scope="scope">
              <dict-tag :options="dict.type.sys_judge" :value="scope.row.hardRequirements"/>
            </template>
           </el-table-column>
          <el-table-column label="是否计费" align="center" prop="isBilling" width="80" show-overflow-tooltip>
            <template slot-scope="scope">
              <dict-tag :options="dict.type.sys_judge" :value="scope.row.isBilling"/>
            </template>
           </el-table-column>
        </span>

        <!--三级员工权限-->
        <span v-if="deptLevel === 3">
          <el-table-column label="反馈原因" align="center" prop="reason2" show-overflow-tooltip/>
          <el-table-column label="硬性条件" align="center" prop="hardRequirements2" width="80" show-overflow-tooltip>
            <template slot-scope="scope">
              <dict-tag :options="dict.type.sys_judge" :value="scope.row.hardRequirements2"/>
            </template>
          </el-table-column>
          <el-table-column label="是否计费" align="center" prop="isBilling2" width="80" show-overflow-tooltip>
            <template slot-scope="scope">
              <dict-tag :options="dict.type.sys_judge" :value="scope.row.isBilling2"/>
            </template>
           </el-table-column>
        </span>
        <el-table-column label="招聘人" align="center" prop="createName" width="100" show-overflow-tooltip/>

        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200" fixed="right">
          <template slot-scope="{ row }">
            <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(row)" v-hasPermi="['interview:feedback:view']">查看</el-button>
            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(row)" v-hasPermi="['interview:feedback:edit']" v-if="deptLevel === 1 || deptLevel === 2">编辑</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(row)" v-if="deptLevel === 1">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>
    <!-- 编辑 -->
    <EditDialog ref="editDialog" @refresh="getList" @close="handleClose" :dict-map="dict.type" :user-list="userList" :location-list="locationList" :dept-level3-list="deptLevel3List"/>
    <!-- 详情 -->
    <DetailDialog ref="detailDialog" @refresh="getList" @close="handleClose" :dict-map="dict.type" :user-list="userList"/>

    <!-- 面试反馈导入 -->
    <el-dialog title="导入" :visible.sync="upload.open" width="400px" append-to-body>
      <el-upload ref="uploadFile" :limit="1" accept=".xlsx, .xls"
                 :headers="upload.headers"
                 :action="upload.url"
                 :disabled="upload.isUploading"
                 :on-progress="handleFileUploadProgress"
                 :on-success="handleFileSuccess"
                 :auto-upload="false"
                 drag
      >
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip text-center" slot="tip">
          <span>仅允许导入xls、xlsx格式文件。</span>
          <el-link :href="`${upload.baseUrl}/profile/upload/Interview/importTemplate/面试反馈模板.xlsx`"
                   type="primary" :underline="false" style="font-size: 12px; vertical-align: baseline">下载模板</el-link>
        </div>
      </el-upload>
      <div slot="footer">
        <el-button type="primary" @click="submitFileForm">确 定</el-button>
        <el-button @click="upload.open = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {list, delData, secondPushData} from "@/api/feedback";
import EditDialog from "@/views/Feedback/edit.vue";
import DetailDialog from "@/views/Feedback/detail.vue";
import {listUserKv} from "@/api/system/user";
import {getToken} from "@/utils/auth";
import {allListNoDept} from "@/api/location";
import {listDept} from "@/api/system/dept";

export default {
  name: "Report",
  dicts: [ 'sys_user_sex','sys_education','sys_judge' ],
  components: { DetailDialog, EditDialog },
  data() {
    return {
      // 登录人部门等级
      deptLevel: this.$store?.state?.user?.deptLevel,
      userList: [], // 用户列表，转义userName
      locationList: [], // 查询点位名称
      deptLevel3List: [], // 三级部门列表
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
      // 统计总数
      statistics: {
        totalCount: 0,
        hardRequirementsYesCount: 0,
        isBillingYesCount: 0,
        bothNullCount: 0,
      },
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        locationName: undefined,
        deptName: undefined,
        name: undefined,
        sex: undefined,
        education: undefined,
        hardRequirements: undefined,
        isBilling: undefined,
        hardRequirements2: undefined,
        isBilling2: undefined,
        deptLevel: this.$store.state.user.deptLevel,
        createName: undefined,
        dateRange: [], // ["2025-10-01", "2025-10-09"]
      },
      // 面试反馈导入
      upload: {
        baseUrl: process.env.VUE_APP_BASE_API,
        // 是否显示弹出层
        open: false,
        // 是否禁用上传
        isUploading: false,
        // 设置上传的请求头部
        headers: { Authorization: "Bearer " + getToken() },
        // 上传的地址
        url: process.env.VUE_APP_BASE_API + "/interview/feedback/importData"
      },
    };
  },
  created() {
    this.getList();
    this.getUserList();
    this.getLocationList()
    this.getDeptLevel3List()
  },
  methods: {
    /** 查询用户列表 */
    getUserList() {
      listUserKv().then(response => this.userList = response.data)
    },

    /** 查询当前供应商,所关联的点位 */
    getLocationList() {
      allListNoDept({ deptId : this.$store.state.user.deptId }).then(response => this.locationList = response.data)
    },

    /** 查询三级部门列表 */
    getDeptLevel3List() {
      listDept({ deptLevel : 3 }).then(response => this.deptLevel3List = response.data)
    },

    /** 查询列表（分为三个列表，deptLevel：1总部，能看所有的，2供应商，能看总部推送的，3员工，能看供应商推送的） */
    getList() {
      this.loading = true;
      list(this.queryParams).then(response => {
        this.dataSource = response.rows;
        this.total = response.total;

        // 统计总数
        this.statistics.totalCount = response.map.totalCount;
        this.statistics.hardRequirementsYesCount = response.map.hardRequirementsYesCount;
        this.statistics.isBillingYesCount = response.map.isBillingYesCount;
        this.statistics.bothNullCount = response.map.bothNullCount;

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
    // handleAdd() {
    //     this.$refs.editDialog.handleAdd();
    // },
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
      this.$modal.confirm('是否确认删除面试反馈信息？').then(function() {
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

    /** 导入按钮操作 */
    handleImport() {
      this.upload.open = true;
    },
    // 文件上传中处理
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true;
    },

    // 文件上传成功处理
    handleFileSuccess(response, file, fileList) {
      this.upload.open = false;
      this.upload.isUploading = false;
      this.$refs.uploadFile.clearFiles();

      if (response.code === 200) {
        // ===== 成功 =====
        this.$message.success(response.msg || `导入成功，共导入 ${response.successCount || 0} 条数据`);
        this.handleQuery();
      } else {
        // ===== 有错误 =====
        this.$message.warning(response.msg ||
          `导入完成，成功 ${response.successCount || 0} 条，失败 ${response.failCount || 0} 条，请下载文件查看`
        );

        if (response.fileUrl) {
          // 自动触发文件下载
          window.open(this.upload.baseUrl + response.fileUrl, '_blank');
        }
      }
    },

    // 提交上传文件
    submitFileForm() {
      this.$refs.uploadFile.submit();
    },
    /** 导出列表按钮操作 */
    handleExport() {
      this.download('/interview/feedback/export', {
        ...this.queryParams
      }, `面试反馈_${new Date().getTime()}.xlsx`)
    },

    /** 一键推送方法 */
    pushExport() {
      let data = this.queryParams.dateRange;
      // 必须选择日期范围才可以一键推送
      if(null == data || data.length !== 2){
        this.$message.warning("必须选择日期范围才可以一键推送");
        return;
      }

      if(null == this.dataSource || this.dataSource.length === 0){
        this.$message.warning("无数据");
        return;
      }

      this.loading = true;
      secondPushData(this.queryParams).then(response => {
        this.loading = false;
        this.$message.success("推送成功")
      });
    },
  }
};
</script>
<style lang="scss" scoped>
// 合计统计
.card-sum {
  //cursor: pointer;
  font-weight: 700;
  background: #e8f9f0; /* 柔和的绿色底 */
  color: #2e7d32;      /* 深绿色文字 */
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 4px;
  line-height: 1.28;
  transition: all 0.25s ease;
}

.card-sum:hover {
  background: #d3f3e0;
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
}

.card-sum i {
  font-size: 14px;
  color: #43a047;
}

.sum-number {
  font-weight: 700;
  font-size: 15px;
  color: #1b5e20;
  margin-left: 2px;
}
</style>
