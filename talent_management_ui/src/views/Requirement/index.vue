<template>
  <div class="app-container">
    <!-- 操作按钮区域 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['interview:requirement:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['interview:requirement:edit']">编辑</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['interview:requirement:remove']">删除</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
    </el-row>
    <div class="table-wrapper-self">
      <!-- 表格展示区域 -->
      <el-table v-loading="loading" :data="dataSource" @selection-change="handleSelectionChange" @sort-change="handleSortChange" stripe>
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="logo" align="center">
          <template v-slot="scope">
            <img v-if="scope.row._logoPath" :src="scope.row._logoPath" class="thumb" />
            <i v-else class="el-icon-loading"></i>
          </template>
        </el-table-column>
        <el-table-column label="排序权重" align="center" prop="orderNum" sortable="custom" show-overflow-tooltip v-if="columns[1].visible" width="200"/>
        <el-table-column label="标题" align="center" prop="title" show-overflow-tooltip v-if="columns[2].visible">
          <template slot-scope="scope">
            <span class="table-btntxt" @click="getDetails(scope.row.content)">{{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" v-if="columns[3].visible" width="200">
          <template slot-scope="scope">
<!--            <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['interview:requirement:view']">查看</el-button>-->
            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['interview:requirement:edit']">编辑</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['interview:requirement:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>
    <!-- 编辑 -->
    <EditDialog ref="editDialog" @refresh="getList" @close="handleClose"/>
    <!-- 详情 -->
    <DetailDialog ref="detailDialog" @refresh="getList" @close="handleClose"/>

    <!-- html展示组件 -->
    <ShowHtml ref="showHtml"></ShowHtml>
  </div>
</template>

<script>
import { list, delData, getItemPic } from "@/api/requirement";
import EditDialog from "@/views/Requirement/edit.vue";
import DetailDialog from "@/views/Requirement/detail.vue";
import ShowHtml from "@/components/ShowHtml";

export default {
  name: "Requirement",
  components: { DetailDialog, EditDialog, ShowHtml },
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
        orderDirection:''
      },
      // 列信息
      columns: [
        { key: 0, label: `logo`, visible: true },
        { key: 1, label: `排序权重`, visible: true },
        { key: 2, label: `标题`, visible: true },
        { key: 3, label: `操作`, visible: true },
      ],
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      list(this.queryParams).then(response => {
        this.dataSource = response.rows;
        this.total = response.total;
        this.loading = false;

        // 查询漫画logo
        this.dataSource.map(row => this.loadRowPicture(row))
      });
    },
    // 查询图片
    loadRowPicture(row) {
      getItemPic(row.id).then(res => {
        const blob = new Blob([res], { type: 'image/jpeg' });
        // 🔑 保证 Vue 能感知
        this.$set(row, '_logoPath', URL.createObjectURL(blob));
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
      this.$modal.confirm('是否确认删除招聘需求？').then(function() {
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
      this.$refs.showHtml.handleOpen('内容',details);
    },
    /** 排序方法 */
    handleSortChange({ prop, order }) {
      if(order){
        this.queryParams.orderDirection = order =='ascending' ? 'asc' : 'desc';
      }else{
        this.queryParams.orderDirection = '';
      }
      this.getList();
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
