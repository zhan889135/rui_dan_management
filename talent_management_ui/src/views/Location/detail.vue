<template>
  <div>
    <el-dialog title="详情" :visible.sync="visible" :close-on-click-modal="false" :close-on-press-escape="false" class="custom-dialog" width="60%">
      <el-form ref="form" v-loading="loading" :model="form" label-width="100px">
        <div class="form-container">
          <el-row :gutter="20">
            <el-col :span="24" v-if="deptLevel === 1"><el-form-item label="归属供应商："><span>{{ form.deptName }}</span></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="公司名称："><span>{{ form.companyName }}</span></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="面试点位："><span>{{ form.name }}</span></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="面试日期："><span>{{ form.date }}</span></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="需求量："><span>{{ form.demand }}</span></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="招聘状态："><dict-tag :options="dictMap.location_status" :value="form.status"/></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="备注："><span>{{ form.remark }}</span></el-form-item></el-col>
            <el-col :span="24">
              <el-form-item label="详情：">
                <editor v-model="form.details" :readOnly="true"/>
              </el-form-item>
            </el-col>
            <el-col :span="24"><el-form-item label="创建人："><span>{{ getNickNameByUserName(form.createBy, userList) }}</span></el-form-item></el-col>
          </el-row>
        </div>
      </el-form>

      <div slot="footer" class="dialog-footer-self">
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getInfo } from "@/api/location";
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
      form: {},
      // 登录人部门等级
      deptLevel: this.$store?.state?.user?.deptLevel,
    };
  },
  methods: {
    getNickNameByUserName,
    /** 打开详情弹窗 */
    handleDetail(id) {
      this.visible = true;
      this.loading = true;

      getInfo(id).then((response) => {
        this.form = response.data;
      })
      .finally(() => {
        this.loading = false;
      });
    },

    /** 关闭弹窗 */
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
.custom-dialog ::v-deep .el-dialog:not(.is-fullscreen) {
  margin-top: 10vh !important;
}

.form-container {
  max-height: 82vh;
  overflow-y: auto;
  overflow-x: hidden;
}
/* TinyMCE 常见 toolbar 容器 */
::v-deep .tox-editor-header {
  display: none !important;
}

/* Quill 常见 toolbar */
::v-deep .ql-toolbar {
  display: none !important;
}
</style>
