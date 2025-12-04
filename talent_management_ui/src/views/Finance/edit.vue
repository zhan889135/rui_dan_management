<template>
  <div>
    <el-dialog :title="title" :visible.sync="visible" :close-on-click-modal="false" :close-on-press-escape="false" v-on="$listeners" @close="handleClose" class="custom-dialog" width="50%" >
      <el-form ref="form" v-loading="loading" :model="form" label-width="100px">
        <div class="form-container">
          <el-row :gutter="20">
            <!-- 供应商 -->
            <el-col :span="24">
              <el-form-item label="供应商" prop="deptId">
                <el-select v-model="form.deptId" placeholder="请选择供应商" style="width: 100%" @change="handleDeptChange">
                  <el-option v-for="(item, index) in deptLevel2List" :key="index" :label="item.deptName" :value="item.deptId"/>
                </el-select>
              </el-form-item>
            </el-col>

            <!-- 面试点位 -->
            <el-col :span="24">
              <el-form-item label="面试点位" prop="locationId">
                <el-select v-model="form.locationId" placeholder="请选择面试点位" style="width: 100%" clearable filterable @change="handleLocationChange">
                  <el-option v-for="item in filteredLocationList" :key="item.id" :label="item.name" :value="item.id"/>
                </el-select>
              </el-form-item>
            </el-col>

            <!-- 金额 -->
            <el-col :span="24">
              <el-form-item label="金额" prop="price">
                <el-input v-model="form.price" placeholder="请输入金额，最多保留2位小数" @input="(val) => validateDecimalField(form,'price', val, 8, 3)" clearable>
                  <template slot="prepend">¥</template>
                </el-input>
              </el-form-item>
            </el-col>

            <!-- 年龄区间（同行） -->
            <el-col :span="12">
              <el-form-item label="年龄起始" prop="ageStart">
                <el-input v-model="form.ageStart" placeholder="如：18" @input="(val) => validateDecimalField(form,'ageStart', val, 2, 0)" clearable/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="年龄结束" prop="ageEnd">
                <el-input v-model="form.ageEnd" placeholder="如：60" @input="(val) => validateDecimalField(form,'ageEnd', val, 2, 0)" clearable/>
              </el-form-item>
            </el-col>

            <!-- 额外金额 -->
            <el-col :span="24">
              <el-form-item label="额外金额" prop="extraPrice">
                <el-input v-model="form.extraPrice" placeholder="请输入金额，最多保留2位小数" @input="(val) => validateDecimalField(form,'extraPrice', val, 8, 3)" clearable>
                  <template slot="prepend">¥</template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </el-form>

      <div slot="footer" class="dialog-footer-self">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="btnLoading">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { save, getInfo } from "@/api/finance";
import { validateDecimalField } from "@/utils/ruoyi";

export default {
  name: 'EditDialog',
  props: {
    deptLevel2List: { type: Array, default: () => [] },
    locationList: { type: Array, default: () => [] },
  },
  data() {
    return {
      // 表单加载状态
      loading: false,
      // 按钮加载状态
      btnLoading: false,
      // 是否弹窗可见
      visible: false,
      // 标题
      title: '',
      // 表单数据
      form: {
        id: undefined,
        deptId: undefined,
        deptName: undefined,
        locationId: undefined,
        locationName: undefined,
        price: undefined,
        ageStart: undefined,
        ageEnd: undefined,
        extraPrice: undefined,
      },

      // 动态过滤后的面试点位
      filteredLocationList: [],
    }
  },
  methods: {
    validateDecimalField,
    /** 打开新增弹窗 */
    handleAdd() {
      this.title = '新增'
      this.resetForm()
      this.visible = true
      this.filteredLocationList = []; // 初始为空
    },

    /** 打开编辑弹窗 */
    handleUpdate(id) {
      this.title = '编辑'
      this.visible = true
      this.resetForm()
      this.loading = true;

      getInfo(id).then(response => {
        this.form = { ...this.form, ...response.data };

        if (this.form.deptId) {
          this.handleDeptChange(String(this.form.deptId));

          this.form.deptId = response.data.deptId;
          this.form.deptName = response.data.deptName;
          this.form.locationId = response.data.locationId;
          this.form.locationName = response.data.locationName;
        }
      }).finally(() =>{
        this.loading = false;
      })
    },
    /** 提交表单 */
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.loading = true;
        this.btnLoading = true;

        save(this.form).then((response) => {
          this.form = response.data
          this.$modal.msgSuccess('保存成功')
          this.handleClose()
        }).finally(() =>{
          this.loading = false;
          this.btnLoading = false;
        })
      })
    },

    /** 关闭弹窗 */
    handleClose() {
      this.visible = false
      this.resetForm()
    },

    /** 重置表单 */
    resetForm() {
      Object.assign(this.form, this.$options.data().form);
      this.filteredLocationList = [];
      this.$refs.form && this.$refs.form.clearValidate();
    },

    /** 下拉切换部门选择 */
    handleDeptChange(deptId) {
      const dept = this.deptLevel2List.find(item => item.deptId === deptId);
      this.form.deptName = dept ? dept.deptName : "";

      // 清空旧点位
      this.form.locationId = undefined;
      this.form.locationName = undefined;

      // 🍀 核心修改：item.deptId 可能为 "150,151,155"
      this.filteredLocationList = this.locationList.filter(item => {
        if (!item.deptId) return false;
        return item.deptId.split(",").map(s => s.trim()).includes(String(deptId));
      });
    },

    /** 下拉切换点位选择 */
    handleLocationChange(locationId) {
      const selected = this.locationList.find(item => item.id === locationId);
      this.form.locationName = selected ? selected.name : "";
    }
  }
}
</script>

<style scoped lang="scss">

.custom-dialog ::v-deep .el-dialog:not(.is-fullscreen){
  margin-top: 25vh !important;
}

.form-container {
  max-height: 75vh;
  overflow-y: auto;
  overflow-x: hidden;
}

</style>
