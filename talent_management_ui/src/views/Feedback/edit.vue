<template>
  <div>
    <el-dialog :title="title" :visible.sync="visible" :close-on-click-modal="false" :close-on-press-escape="false" v-on="$listeners" @close="handleClose" class="custom-dialog" width="50%" >
      <el-form ref="form" v-loading="loading" :model="form" :rules="rules" label-width="100px">
        <div class="form-container">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="面试点位" prop="locationId">
                <el-select v-model="form.locationId" placeholder="请选择面试点位"
                           @change="changeLocation" filterable style="width: 100%;"
                           :disabled="isDisabled('locationId')">
                  <el-option v-for="item in locationList" :key="item.id" :label="item.name" :value="item.id"/>
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="姓名" prop="name">
                <el-input v-model="form.name" placeholder="请输入姓名" maxlength="50"
                          :disabled="isDisabled('name')"/>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="性别" prop="sex">
                <el-select v-model="form.sex" placeholder="请选择性别" style="width: 100%;"
                           :disabled="isDisabled('sex')">
                  <el-option v-for="item in dictMap.sys_user_sex" :key="item.value" :label="item.label" :value="item.value"/>
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="电话" prop="phone">
                <el-input v-model="form.phone" placeholder="请输入电话" maxlength="11"
                          :disabled="isDisabled('phone')"/>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="年龄" prop="age">
                <el-input v-model="form.age" placeholder="请输入年龄" maxlength="3"
                          :disabled="isDisabled('age')"/>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="学历" prop="education">
                <el-select v-model="form.education" placeholder="请选择学历" style="width: 100%;"
                           :disabled="isDisabled('education')">
                  <el-option v-for="item in dictMap.sys_education" :key="item.value" :label="item.label" :value="item.value"/>
                </el-select>
              </el-form-item>
            </el-col>




            <!--总部门权限-->
            <span v-if="deptLevel === 1">
              <el-col :span="12">
                <el-form-item label="面试日期" prop="interviewDate">
                  <el-date-picker v-model="form.interviewDate" type="date" placeholder="请选择面试日期" value-format="yyyy-MM-dd" style="width: 100%;" />
                </el-form-item>
              </el-col>

              <el-col :span="24">
                <el-form-item label="反馈原因" prop="reason">
                  <el-input type="textarea" :rows="4" placeholder="请输入反馈原因" v-model="form.reason" maxlength="500"/>
                </el-form-item>
              </el-col>

              <el-col :span="12">
                <el-form-item label="硬性条件" prop="hardRequirements">
                  <el-radio-group v-model="form.hardRequirements">
                    <el-radio v-for="item in dictMap.sys_judge" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>

              <el-col :span="12">
                <el-form-item label="是否计费" prop="isBilling">
                  <el-radio-group v-model="form.isBilling">
                    <el-radio v-for="item in dictMap.sys_judge" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </span>


            <!--供应商权限-->
            <span v-if="deptLevel === 2">
              <el-col :span="24">
                <el-form-item label="反馈原因" prop="reason">
                  <el-input type="textarea" :rows="4" v-model="form.reason" maxlength="500" disabled="disabled"/>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="硬性条件" prop="hardRequirements">
                  <el-radio-group v-model="form.hardRequirements" disabled="disabled">
                    <el-radio v-for="item in dictMap.sys_judge" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="是否计费" prop="isBilling">
                  <el-radio-group v-model="form.isBilling" disabled="disabled">
                    <el-radio v-for="item in dictMap.sys_judge" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>

              <el-col :span="24">
                <el-form-item label="反馈原因" prop="reason2">
                  <el-input type="textarea" :rows="4" placeholder="请输入反馈原因" v-model="form.reason2" maxlength="500"/>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="硬性条件" prop="hardRequirements2">
                  <el-radio-group v-model="form.hardRequirements2">
                    <el-radio v-for="item in dictMap.sys_judge" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="是否计费" prop="isBilling2">
                  <el-radio-group v-model="form.isBilling2">
                    <el-radio v-for="item in dictMap.sys_judge" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </span>











            <el-col :span="12">
              <el-form-item label="招聘人" prop="createBy">
                <el-input :value="getNickNameByUserName(form.createBy, userList)" disabled/>
              </el-form-item>
            </el-col>

            <span v-if="deptLevel === 1">
              <el-col :span="12">
                <el-form-item label="归属供应商" prop="deptName">
                  <el-input v-model="form.deptName" placeholder="请输入归属供应商" maxlength="50" disabled/>
                </el-form-item>
              </el-col>
            </span>
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
import {save, getInfo, verifyIsExist} from "@/api/feedback";
import { getNickNameByUserName } from "@/utils/ruoyi";
import dayjs from "dayjs";

export default {
  name: 'EditDialog',
  props: {
    dictMap: { type: Object, default: () => ({}) },
    userList: { type: Array, default: () => [] },
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
        // 表单id
        id:undefined,
        // 面试点位ID
        locationId:undefined,
        // 面试点位名称
        locationName:undefined,
        // 归属供应商ID
        deptId:undefined,
        // 归属供应商名称
        deptName:undefined,
        // 姓名
        name:'',
        // 性别
        sex:undefined,
        // 电话
        phone:undefined,
        // 学历
        education:undefined,
        // 反馈原因
        reason:undefined,
        // 硬性条件
        hardRequirements:undefined,
        // 是否计费
        isBilling:undefined,
        // 面试日期
        interviewDate:undefined,

        // 反馈原因2
        reason2:undefined,
        // 硬性条件2
        hardRequirements2:undefined,
        // 是否计费2
        isBilling2:undefined,

        // 创建人
        createBy:undefined,
      },
      // 表单验证规则
      rules: {
        locationId: [{ required: true, message: '请选择面试点位', trigger: 'change' }],
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
        phone: [{ required: true, message: '请输入电话', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }],
        age: [{ required: true, pattern: /^[1-9]\d*$/, message: '年龄必须为正整数', trigger: 'blur' }],
        education: [{ required: true, message: '请选择学历', trigger: 'change' }],
        interviewDate: [{ required: true, message: '请选择面试日期', trigger: 'change' }],

        // 第一套反馈
        reason: [{ required: true, message: '请输入反馈原因', trigger: 'blur' }],
        hardRequirements: [{ required: true, message: '请选择硬性条件', trigger: 'change' }],
        isBilling: [{ required: true, message: '请选择是否计费', trigger: 'change' }],

        // 第二套反馈
        reason2: [{ required: true, message: '请输入反馈原因', trigger: 'blur' }],
        hardRequirements2: [{ required: true, message: '请选择硬性条件', trigger: 'change' }],
        isBilling2: [{ required: true, message: '请选择是否计费', trigger: 'change' }],
      },

      // 登录人部门权限
      deptLevel: this.$store?.state?.user?.deptLevel || 0,
    }
  },

  methods: {
    getNickNameByUserName,

    // 根据 deptLevel 动态调整 rules
    adjustRules() {
      // level=1，反馈原因非必填
      if (this.deptLevel === 1) {
        delete this.rules.reason;
      }else

      // level=2，不显示 hardRequirements，isBilling
      if (this.deptLevel === 2) {

        delete this.rules.interviewDate;
        delete this.rules.reason;
        delete this.rules.reason2;
        delete this.rules.hardRequirements;
        delete this.rules.isBilling;
      } else

      // level=3，不显示 reason / hardRequirements / isBilling，所以删除对应校验
      if (this.deptLevel === 3) {
        delete this.rules.sex;
        delete this.rules.phone;
        delete this.rules.age;
        delete this.rules.education;

        delete this.rules.interviewDate;
        delete this.rules.reason;
        delete this.rules.hardRequirements;
        delete this.rules.isBilling;

        delete this.rules.reason2;
        delete this.rules.hardRequirements2;
        delete this.rules.isBilling2;
      }
    },

    // 控制不同等级，显示不同的表单，
    isDisabled(field) {
      const level = this.deptLevel;

      // 总部门权限，
      if (level === 1) {
        // 只能编辑：面试点位、姓名、性别、电话、年龄、学历、反馈原因、硬性条件、是否计费
        return ![
          'locationId',
          'name',
          'sex',
          'phone',
          'age',
          'education',
          'interviewDate',
          'reason',
          'hardRequirements',
          'isBilling'
        ].includes(field);
      } else
      // 供应商权限，
      if (level === 2) {
        // 只能编辑：面试点位、姓名、性别、电话、年龄、学历、反馈原因、硬性条件、是否计费
        return ![
          'locationId',
          'name',
          'sex',
          'phone',
          'age',
          'education',
          'reason2',
          'hardRequirements2',
          'isBilling2'
        ].includes(field);
      } else
      // 员工权限
      if (level === 3) {
        // 只能编辑：面试点位、姓名、性别、电话、年龄、学历
        return ![
          'locationId',
          'name',
          'sex',
          'phone',
          'age',
          'education',
        ].includes(field);
      }
      return true; // 其他情况默认禁用
    },

    /** 打开编辑弹窗 */
    handleUpdate(id) {
      this.title = '编辑'
      this.visible = true
      this.resetForm()
      this.adjustRules()   // ✅ 打开时调整 rules()
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
        const params = {
          ...this.form,
        };

        const level = this.deptLevel;
        // 总部门权限，
        if (level === 1) {
          params.level1Person = this.$store?.state?.user?.userName;
          params.level1Time  = dayjs().format("YYYY-MM-DD HH:mm:ss");
        }
        // 供应商权限，
        if (level === 2) {
          params.level2Person = this.$store?.state?.user?.userName;
          params.level2Time  = dayjs().format("YYYY-MM-DD HH:mm:ss");
        }

        verifyIsExist(params).then((response) => {
          // 说明有重复的
          if(response.data === true){
            this.$modal.confirm('手机号码重复,是否重复录入？').then(() => {
              this.saveInvitationInfoMethod(params);
            });
          }else{
            // 保存面试反馈信息
            this.saveInvitationInfoMethod(params);
          }
        })

      })
    },

    // 保存面试反馈信息
    saveInvitationInfoMethod(params){
      this.loading = true;
      this.btnLoading = true;
      save(params).then((response) => {
        this.form = response.data
        this.$modal.msgSuccess('保存成功')
        this.handleClose()
      }).finally(() =>{
        this.loading = false;
        this.btnLoading = false;
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

    /** 获取面试点位名称 */
    changeLocation(val){
      this.form.locationName = this.locationList?.find(item => item.id === val)?.name
    },
  }
}
</script>

<style scoped lang="scss">

.custom-dialog ::v-deep .el-dialog:not(.is-fullscreen){
  margin-top: 5vh !important;
}

.form-container {
  max-height: 80vh;
  overflow-y: auto;
  overflow-x: hidden;
}
</style>
