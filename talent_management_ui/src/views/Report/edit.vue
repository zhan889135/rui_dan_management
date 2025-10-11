<template>
  <div class="app-container">
    <el-dialog :title="title" :visible.sync="visible" :close-on-click-modal="false" :close-on-press-escape="false" v-on="$listeners" @close="handleClose" class="custom-dialog" width="50%" >
      <el-form ref="form" v-loading="loading" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
           <el-col :span="24" align="right">
          </el-col>
          <el-col :span="12">
            <el-form-item label="面试点位" prop="locationId">
               <el-select v-model="form.locationId" placeholder="请选择面试点位" @change="changeLocation" filterable style="width: 100%;">
                    <el-option v-for="item in locationList" :key="item.id" :label="item.name" :value="item.id"/>
                </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" maxlength="50" clearable/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="sex" >
               <el-select v-model="form.sex" placeholder="请选择性别" style="width: 100%;">
                    <el-option v-for="item in dictMap.sys_user_sex" :key="item.value" :label="item.label" :value="item.value"/>
                </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入电话" maxlength="11" clearable/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input v-model="form.age" placeholder="请输入年龄" maxlength="3" clearable/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学历" prop="education">
                <el-select v-model="form.education" placeholder="请选择学历" style="width: 100%;">
                    <el-option v-for="item in dictMap.sys_education" :key="item.value" :label="item.label" :value="item.value"/>
                </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="面试日期" prop="interviewDate">
              <el-date-picker v-model="form.interviewDate" type="date" placeholder="请选择面试日期" value-format="yyyy-MM-dd" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="面试时间" prop="interviewTime">
               <el-time-picker v-model="form.interviewTime" value-format="HH:mm" format="HH:mm" style="width: 100%;" placeholder="请选择面试时间"></el-time-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="过往经历" prop="experience">
              <el-input type="textarea" :rows="4" placeholder="请输入内容" v-model="form.experience" maxlength="500"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="deptLevel === 1">
            <el-form-item label="归属供应商">
              <el-input v-model="form.deptName" disabled/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="招聘人">
              <el-input :value="getNickNameByUserName(form.createBy, userList)" disabled/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <ContentRecognition ref="contentRecognition" @close="recognitionForm" :dict-map="dictMap"></ContentRecognition>
          </el-col>
        </el-row>
      </el-form>
       <div slot="footer" class="dialog-footer-self">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="btnLoading">保存</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { save , getInfo } from "@/api/report";
import ContentRecognition from "@/components/ContentRecognition";
import { getNickNameByUserName } from "@/utils/ruoyi";

export default {
  name: 'EditDialog',
  props: {
    dictMap: { type: Object, default: () => ({}) },
    userList: { type: Array, default: () => [] },
    locationList: { type: Array, default: () => [] },
  },
  components: {
    ContentRecognition
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
        id:undefined,
        deptId:undefined,
        deptName:undefined,
        locationId:undefined,
        locationName:undefined,
        name:undefined,
        sex:undefined,
        phone:undefined,
        age:undefined,
        education:undefined,
        interviewDate:undefined,
        interviewTime:undefined,
        experience:undefined,
        createBy:undefined,
      },
      // 表单验证规则
      rules: {
        locationId: [{ required: true, message: '请选择面试点位', trigger: 'change' }],
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        phone: [{ required: true, message: '请输入电话', trigger: 'blur' },
            { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        age: [
            { required: true, message: '请输入年龄', trigger: 'blur' },
            { pattern: /^[1-9]\d*$/, message: '年龄必须为正整数', trigger: 'blur' }
        ],
        education: [{ required: true, message: '请选择学历', trigger: 'change' }],
        interviewDate: [{ required: true, message: '请选择面试日期', trigger: 'change' }],
        interviewTime: [{ required: true, message: '请选择面试时间', trigger: 'change' }],
      },

      // 登录人部门权限
      deptLevel: this.$store?.state?.user?.deptLevel || 0,
    }
  },
  methods: {
    getNickNameByUserName,
    /** 打开新增弹窗 */
    handleAdd() {
      this.title = '新增'
      this.resetForm()
      this.$set(this.form, 'interviewDate', this.$dayjs().add(1, 'day').format('YYYY-MM-DD')); // 面试日期默认第二天
      this.$set(this.form, 'deptId', this.$store.state.user.deptId);
      this.$set(this.form, 'deptName', this.$store.state.user.deptName);
      this.$set(this.form, 'createBy', this.$store.state.user.userName);
      this.visible = true
    },

    /** 打开编辑弹窗 */
    handleUpdate(id) {
      this.title = '编辑'
      this.visible = true
      this.resetForm()
      this.loading = true;
      getInfo(id).then(response => {
        this.form = response.data
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
        };
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
      this.$refs.contentRecognition && this.$refs.contentRecognition.clear();
    },

    /** 获取面试点位名称 */
    changeLocation(val){
      this.form.locationName = this.locationList?.find(item => item.id === val)?.name
    },

    /** 识别内容 */
    recognitionForm(val) {
      // 只更新 val 里有的字段
      Object.keys(val).forEach(key => {
        if (val[key] !== undefined && val[key] !== null && val[key] !== '') {
          this.$set(this.form, key, val[key])
        }
      })
    }
  },
}
</script>

<style scoped lang="scss">

.app-container{
    padding: 20px 40px;
    box-sizing: border-box;
}

.custom-dialog ::v-deep .el-dialog:not(.is-fullscreen){
  margin-top: 3vh !important;
}

/* 弹出框设置滚动条 */
.custom-dialog .dialog-scroll-area {
  max-height: 82vh;
  min-height: 82vh;
  overflow-y: auto;
}
</style>
