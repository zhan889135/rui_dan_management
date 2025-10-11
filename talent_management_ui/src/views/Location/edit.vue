<template>
  <div>
    <el-dialog :title="title" :visible.sync="visible" :close-on-click-modal="false" :close-on-press-escape="false" v-on="$listeners" @close="handleClose" class="custom-dialog" width="90%" >
      <el-form ref="form" v-loading="loading" :model="form" :rules="rules" label-width="100px">
        <div class="form-container">
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="归属供应商" prop="deptId">
                <el-checkbox-group v-model="deptIdArr" @change="handleDeptChange">
                  <el-checkbox v-for="item in deptNameList" :key="item.deptId" :label="item.deptId">
                    {{ item.deptName }}
                  </el-checkbox>
                </el-checkbox-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="公司名称" prop="companyName">
                <el-autocomplete
                  v-model="form.companyName"
                  :fetch-suggestions="(q, cb) => suggestFieldHistory(locationList, q, cb, 'companyName')"
                  placeholder="请输入面试点位"
                  :trigger-on-focus="true"
                  :highlight-first-item="true"
                  @input="val => form.companyName = val.slice(0, 100)"
                  clearable
                  style="width: 100%"
                >
                  <template slot-scope="{ item }">
                    <i class="el-icon-time" style="margin-right: 4px; color: #409EFF;" /> {{ item.value }}
                  </template>
                </el-autocomplete>

              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="面试点位" prop="name">
                <el-input v-model="form.name" placeholder="请输入面试点位" maxlength="50" clearable/>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="面试日期" prop="date">
                <el-date-picker v-model="form.date" type="date" placeholder="请选择面试日期" value-format="yyyy-MM-dd" style="width: 100%;" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="需求量" prop="demand">
                <el-input v-model="form.demand" placeholder="最多10位整数" maxlength="10" clearable/>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="招聘状态" prop="status">
                <el-radio-group v-model="form.status">
                  <el-radio v-for="item in dictMap.location_status" :key="item.value" :label="item.value" border>{{ item.label }}</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="备注" prop="remark">
                <el-input v-model="form.remark" placeholder="请输入备注" maxlength="50" clearable/>
              </el-form-item>
            </el-col>

            <el-col :span="24">
              <el-form-item label="详情" prop="details">
                <editor v-model="form.details" :min-height="192" @blur="$refs.form.validateField('details')" @change="$refs.form.validateField('details')"/>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="创建人" prop="createBy">
                <el-input :value="getNickNameByUserName(form.createBy, userList)" disabled/>
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
import { save, getInfo } from "@/api/location";
import { getNickNameByUserName, suggestFieldHistory } from "@/utils/ruoyi";

export default {
  name: 'EditDialog',
  props: {
    dictMap: { type: Object, default: () => ({}) },
    userList: { type: Array, default: () => [] },
    locationList: { type: Array, default: () => [] },
    deptNameList: { type: Array, default: () => [] },
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
      // 供应商多选
      deptIdArr: [],
      // 表单数据
      form: {
        // 表单id
        id:undefined,
        // 归属供应商ID
        deptId: [],     // 数组
        // 归属供应商名称
        deptName: '',    // 名称拼接
        // 创建人
        createBy:undefined,
        // 公司名称
        companyName:'太平洋保险',
        // 面试点位名称
        name:undefined,
        // 面试日期
        date:undefined,
        // 需求量
        demand:undefined,
        // 招聘状态
        status:'Y',
        // 详情
        details:undefined,
        // 备注
        remark:undefined,
      },
      // 表单验证规则
      rules: {
        deptId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
        companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
        name: [{ required: true, message: '请输入面试点位', trigger: 'blur' }],
        demand: [
            { required: true, message: '请输入需求量', trigger: 'blur' },
            { pattern: /^[1-9]\d*$/, message: '需求量必须为正整数', trigger: 'blur' }
        ],
        status: [{ required: true, message: '请选择招聘状态', trigger: 'change' }],
        details: [{ required: true, message: '请输入详情', trigger: ['blur', 'change'] },
            {
            validator: (rule, value, callback) => {
            if (!value || value.trim() === '<p><br></p>') {
                callback(new Error('请输入详情'));
            } else {
                callback();
            }
            },
            trigger: ['blur', 'change']
        }],
      }
    }
  },
  methods: {
    suggestFieldHistory,
    getNickNameByUserName,

    // 供应商选择
    handleDeptChange(val) {
      const selectedItems = this.deptNameList.filter(d => val.includes(d.deptId));
      this.form.deptName = selectedItems.map(d => d.deptName).join(','); // 只处理名称
      this.form.deptId = selectedItems.map(d => d.deptId).join(','); // 只处理id
    },

    /** 打开新增弹窗 */
    handleAdd() {
      this.title = '新增'
      this.deptIdArr = [];
      this.resetForm()
      this.$set(this.form, 'createBy', this.$store.state.user.userName);
      this.visible = true
    },

    /** 打开编辑弹窗 */
    handleUpdate(id) {
      this.title = '编辑'
      this.deptIdArr = [];
      this.visible = true
      this.resetForm()
      this.loading = true;
      getInfo(id).then(response => {
        this.form = response.data

        // ⚡ deptId 兼容处理（后端返回可能是 null / "" / "1,2,3"）
        if (this.form.deptId) {
          this.deptIdArr = this.form.deptId.split(',').map(id => Number(id)) // 保证是数组
        } else {
          this.deptIdArr = [] // 默认空数组，避免报错
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

        const params = {
          ...this.form,
        }
        save(params).then((response) => {
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
      this.$refs.form && this.$refs.form.clearValidate();
    },
  }
}
</script>

<style scoped lang="scss">

.custom-dialog ::v-deep .el-dialog:not(.is-fullscreen){
  margin-top: 10vh !important;
}

.form-container {
  max-height: 82vh;
  overflow-y: auto;
  overflow-x: hidden;
}
</style>
