<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="部门名称" prop="deptName">
        <el-input v-model="queryParams.deptName" placeholder="请输入部门名称" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="部门状态" clearable>
          <el-option v-for="dict in dict.type.sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['system:dept:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="el-icon-sort" size="mini" @click="toggleExpandAll">展开/折叠</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <div class="table-wrapper-self">
      <el-table v-if="refreshTable" v-loading="loading" :data="deptList" row-key="deptId" :default-expand-all="isExpandAll" :tree-props="{children: 'children', hasChildren: 'hasChildren'}" stripe>
        <el-table-column prop="deptName" align="center" label="部门名称"></el-table-column>
        <el-table-column prop="orderNum" align="center" label="排序" width="50"></el-table-column>
        <el-table-column prop="status" align="center" label="状态" width="150">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status"/>
          </template>
        </el-table-column>
        <el-table-column prop="leader" align="center" label="负责人" width="150"></el-table-column>
        <el-table-column prop="phone" align="center" label="联系电话" width="200"></el-table-column>
        <el-table-column prop="locationName" align="center" label="面试点位" show-overflow-tooltip></el-table-column>
        <el-table-column prop="exp" align="center" label="经验值" width="100"></el-table-column>
        <el-table-column prop="deliveryPower" align="center" label="交付力" width="100"></el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:dept:edit']">修改</el-button>
            <el-button size="mini" type="text" icon="el-icon-plus" @click="handleAdd(scope.row)" v-hasPermi="['system:dept:add']">新增</el-button>
            <el-button v-if="scope.row.parentId != 0" size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['system:dept:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!-- 添加或修改部门对话框 -->
    <el-dialog :title="title" :visible.sync="open" :close-on-click-modal="false" :close-on-press-escape="false" v-on="$listeners" class="custom-dialog" width="50%" >
      <el-form v-loading="dialogLoading" ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="24" v-if="form.parentId !== 0">
            <el-form-item label="上级部门" prop="parentId">
              <treeselect v-model="form.parentId" :options="deptOptions" :normalizer="normalizer" placeholder="选择上级部门" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="部门名称" prop="deptName">
              <el-input v-model="form.deptName" placeholder="请输入部门名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示排序" prop="orderNum">
              <el-input-number v-model="form.orderNum" controls-position="right" :min="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="负责人" prop="leader">
              <el-input v-model="form.leader" placeholder="请输入负责人" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" maxlength="11" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门状态">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in dict.type.sys_normal_disable" :key="dict.value" :label="dict.value">{{dict.label}}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

<!--        <el-row>-->
<!--          <el-col :span="24">-->
<!--            <el-table :data="form.sysLocationList" border style="width: 100%" max-height="500" min-height="500" class="location-table">-->
<!--              &lt;!&ndash; 序号列 &ndash;&gt;-->
<!--              <el-table-column type="index" label="序号" width="100" align="center" />-->
<!--              <el-table-column label="点位名称" align="center">-->
<!--                <template slot-scope="scope">-->
<!--                  <el-input v-model="scope.row.name" placeholder="请输入名称" clearable maxlength="50" class="center-input"/>-->
<!--                </template>-->
<!--              </el-table-column>-->
<!--              <el-table-column label="操作" align="center" width="200">-->
<!--                <template slot-scope="scope">-->
<!--                  <el-button type="text" size="small" @click="addRow(scope.$index)">新增</el-button>-->
<!--                  <el-button type="text" size="small" @click="removeRow(scope.$index)" :disabled="form.sysLocationList.length === 1">删除</el-button>-->
<!--                </template>-->
<!--              </el-table-column>-->
<!--            </el-table>-->
<!--          </el-col>-->
<!--        </el-row>-->

      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDept, getDept, delDept, addDept, updateDept, listDeptExcludeChild } from "@/api/system/dept";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "Dept",
  dicts: ['sys_normal_disable'],
  components: { Treeselect },
  data() {
    return {
      // 遮罩层
      loading: false,
      dialogLoading: false,
      // 显示搜索条件
      showSearch: true,
      // 表格树数据
      deptList: [],
      // 部门树选项
      deptOptions: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否展开，默认全部展开
      isExpandAll: true,
      // 重新渲染表格状态
      refreshTable: true,
      // 查询参数
      queryParams: {
        deptName: undefined,
        status: undefined
      },
      // 表单参数
      form: {
        parentId: 0,
        deptName: '',
        orderNum: 0,
        leader: '',
        phone: '',
        email: '',
        status: 'Y',
        // 动态表格数据
        // sysLocationList: [
        //   { name: '' }
        // ],
      },
      // 表单校验
      rules: {
        parentId: [
          { required: true, message: "上级部门不能为空", trigger: "blur" }
        ],
        deptName: [
          { required: true, message: "部门名称不能为空", trigger: "blur" }
        ],
        orderNum: [
          { required: true, message: "显示排序不能为空", trigger: "blur" }
        ],
        // email: [
        //   {
        //     type: "email",
        //     message: "请输入正确的邮箱地址",
        //     trigger: ["blur", "change"]
        //   }
        // ],
        phone: [
          {
            pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
            message: "请输入正确的手机号码",
            trigger: "blur"
          }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询部门列表 */
    getList() {
      this.loading = true;
      listDept(this.queryParams).then(response => {
        this.deptList = this.handleTree(response.data, "deptId");
        this.loading = false;
      });
    },
    /** 转换部门数据结构 */
    normalizer(node) {
      if (node.children && !node.children.length) {
        delete node.children;
      }
      return {
        id: node.deptId,
        label: node.deptName,
        children: node.children
      };
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        deptId: undefined,
        parentId: undefined,
        deptName: undefined,
        orderNum: undefined,
        leader: undefined,
        phone: undefined,
        email: undefined,
        status: "0"
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd(row) {
      this.reset();
      if (row != undefined) {
        this.form.parentId = row.deptId;
      }
      this.open = true;
      this.title = "添加部门";

      // ⚡ 初始化 sysLocationList，至少一行
      // if (!this.form.sysLocationList || this.form.sysLocationList.length === 0) {
      //   this.form.sysLocationList = [{ name: '' }];
      // }

      listDept().then(response => {
        this.deptOptions = this.handleTree(response.data, "deptId");
      });
    },
    /** 展开/折叠操作 */
    toggleExpandAll() {
      this.refreshTable = false;
      this.isExpandAll = !this.isExpandAll;
      this.$nextTick(() => {
        this.refreshTable = true;
      });
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      getDept(row.deptId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改部门";

        // ⚡ 初始化 sysLocationList，至少一行
        // if (!this.form.sysLocationList || this.form.sysLocationList.length === 0) {
        //   this.form.sysLocationList = [{ name: '' }];
        // }

        listDeptExcludeChild(row.deptId).then(response => {
          this.deptOptions = this.handleTree(response.data, "deptId");
          if (this.deptOptions.length == 0) {
            const noResultsOptions = { deptId: this.form.parentId, deptName: this.form.parentName, children: [] };
            this.deptOptions.push(noResultsOptions);
          }
        });
      });
    },
    /** 提交按钮 */
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.dialogLoading = true;
          if (this.form.deptId != undefined) {
            updateDept(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.dialogLoading = false;
              this.getList();
            });
          } else {
            addDept(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.dialogLoading = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$modal.confirm('是否确认删除名称为"' + row.deptName + '"的数据项？').then(function() {
        return delDept(row.deptId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },

    // 添加供应商点位
    // addRow(index) {
    //   this.form.sysLocationList.splice(index + 1, 0, { name: '' })
    // },
    // // 删除供应商点位
    // removeRow(index) {
    //   this.form.sysLocationList.splice(index, 1)
    // }
  }
};
</script>


<style scoped lang="scss">
.custom-dialog ::v-deep .el-dialog:not(.is-fullscreen){
  margin-top: 20vh !important;
}

/* 表格 */
.location-table{
  width: 100%;
}
/* 调小 */
::v-deep .location-table .el-table__cell{
  padding: 5px !important;
  height: 20px;
}
/* 调小 */
::v-deep .location-table .cell{
  padding: 5 !important;
}
/* 调小 */
::v-deep .location-table .el-input--medium .el-input__inner{
  height: 30px !important;
  font-size: 13px !important;
}

::v-deep.center-input .el-input__inner {
  text-align: center !important;
}
</style>
