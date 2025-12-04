<template>
  <div class="app-container">
    <!-- 操作按钮区域 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate">编辑</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete">删除</el-button></el-col>
    </el-row>
    <div class="table-wrapper-self">
      <!-- 表格展示区域 -->
      <el-table v-loading="loading" :data="dataSource" @selection-change="handleSelectionChange" stripe>

        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="供应商名称" align="center" prop="deptName" show-overflow-tooltip />
        <el-table-column label="面试点位名称" align="center" prop="locationName" show-overflow-tooltip />
        <el-table-column label="金额" align="center" prop="price"/>
        <el-table-column label="年龄区间" align="center">
          <template slot-scope="scope">
            {{ scope.row.ageStart }} ~ {{ scope.row.ageEnd }}
          </template>
        </el-table-column>
        <el-table-column label="额外金额" align="center" prop="extraPrice"/>
        <el-table-column label="总金额" align="center" prop="totalPrice"/>
        <el-table-column label="创建时间" align="center" prop="createTime">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">编辑</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <!-- 编辑 -->
    <EditDialog ref="editDialog" @refresh="getList" @close="handleClose" :dept-level2-list="deptLevel2List" :location-list="locationList" />

  </div>
</template>

<script>
import { list, delData } from "@/api/finance";
import EditDialog from "@/views/Finance/edit.vue";
import { parseTime } from "@/utils/ruoyi";
import { allListNoDept } from "@/api/location";
import { listDept } from "@/api/system/dept";

export default {
  name: "Finance",
  components: { EditDialog },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 总条数
      total: 0,
      // 表格数据
      dataSource: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
      },
      deptLevel2List: [], // 二级部门列表
      locationList: [], // 查询点位名称
    };
  },
  created() {
    this.getList();
    this.getDeptLevel2List();
    this.getLocationList()
  },
  methods: {
    parseTime,
    /** 查询列表 */
    getList() {
      this.loading = true;
      list(this.queryParams).then(response => {
        this.dataSource = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },

    /** 查询供应商 */
    getDeptLevel2List() {
      listDept({ deptLevel : 2 }).then(response => this.deptLevel2List = response.data)
    },

    /** 查询点位 */
    getLocationList() {
      allListNoDept().then(response => this.locationList = response.data)
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
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids?.join(',');
      this.$modal.confirm('是否确认删除？').then(function() {
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
  }
};
</script>
<style scoped lang="scss">
.thumb {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border: 1px solid #ddd;
}
</style>
