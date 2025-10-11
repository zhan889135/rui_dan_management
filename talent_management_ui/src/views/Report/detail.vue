<template>
  <div class="app-container">
    <el-dialog title="详情" :visible.sync="visible" :close-on-click-modal="false" :close-on-press-escape="false" class="custom-dialog" width="50%">
      <el-form ref="form" v-loading="loading" :model="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="面试点位："><span>{{ form.locationName }}</span></el-form-item></el-col>

          <el-col :span="12"><el-form-item label="姓名："><span>{{ form.name }}</span></el-form-item></el-col>

          <el-col :span="12"><el-form-item label="性别："><span><dict-tag :options="dictMap.sys_user_sex" :value="form.sex"/></span></el-form-item></el-col>

          <el-col :span="12"><el-form-item label="电话："><span>{{ form.phone }}</span></el-form-item></el-col>

          <el-col :span="12"><el-form-item label="年龄："><span>{{ form.age }}</span></el-form-item></el-col>

          <el-col :span="12"><el-form-item label="学历："><span><dict-tag :options="dictMap.sys_education" :value="form.education"/></span></el-form-item></el-col>

          <el-col :span="12"><el-form-item label="面试日期："><span>{{ form.interviewDate }}</span></el-form-item></el-col>

          <el-col :span="12"><el-form-item label="面试时间："><span>{{ form.interviewTime }}</span></el-form-item></el-col>

          <el-col :span="24"><el-form-item label="过往经历："><div class="detail-cell-modality-textarea">{{ form.experience }}</div></el-form-item></el-col>

          <el-col :span="12" v-if="deptLevel === 1"><el-form-item label="归属供应商："><span>{{ form.deptName }}</span></el-form-item></el-col>

          <el-col :span="12"><el-form-item label="招聘人："><span>{{ getNickNameByUserName(form.createBy, userList) }}</span></el-form-item></el-col>

        </el-row>
      </el-form>

      <div slot="footer" class="dialog-footer-self">
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getInfo } from "@/api/report";
import { getNickNameByUserName } from "@/utils/ruoyi";

export default {
  name: "DetailDialog",
  props: {
    dictMap: { type: Object, default: () => ({}) },
    userList: { type: Array, default: () => [] },
  },
  data() {
    return {
      loading: false,
      visible: false,
      title: "详情",
      form: {},
      // 登录人部门权限
      deptLevel: this.$store?.state?.user?.deptLevel || 0,
    };
  },
  methods: {
    getNickNameByUserName,
    /** 打开详情弹窗 */
    handleDetail(id) {
      this.visible = true;
      this.loading = true;
      getInfo(id)
        .then((response) => {
          this.form = response.data;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    /** 关闭 */
    handleClose() {
      this.visible = false;
      this.resetForm();
    },
    /** 重置表单 */
    resetForm() {
      Object.assign(this.form, this.$options.data().form);
    },
  },
};
</script>

<style scoped lang="scss">
.app-container {
  padding: 20px 40px;
  box-sizing: border-box;
}

.custom-dialog ::v-deep .el-dialog:not(.is-fullscreen) {
  margin-top: 3vh !important;
}

.readonly-text {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #333;
}
</style>
