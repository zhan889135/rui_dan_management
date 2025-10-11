<!--<template>-->
<!--  <div>-->
<!--    <el-dialog title="查看" :visible.sync="visible" :close-on-click-modal="false" :close-on-press-escape="false" class="custom-dialog" width="50%">-->
<!--      <el-form ref="form" v-loading="loading" :model="form" label-width="100px">-->
<!--        <div class="form-container">-->
<!--          <el-row :gutter="20">-->
<!--            <el-col :span="24"><el-form-item label="面试点位："><span>{{ form.locationName }}</span></el-form-item></el-col>-->

<!--            <el-col :span="24"><el-form-item label="归属供应商："><span>{{ form.deptName }}</span></el-form-item></el-col>-->

<!--            <el-col :span="24"><el-form-item label="姓名："><span>{{ form.name }}</span></el-form-item></el-col>-->

<!--            <el-col :span="24"><el-form-item label="性别："><dict-tag :options="dictMap.sys_user_sex" :value="form.sex" /></el-form-item></el-col>-->

<!--            <el-col :span="24"><el-form-item label="学历："><dict-tag :options="dictMap.sys_education" :value="form.education" /></el-form-item></el-col>-->

<!--            <el-col :span="24"><el-form-item label="反馈原因："><div class="detail-cell-modality-textarea">{{ form.reason }}</div></el-form-item></el-col>-->

<!--            <el-col :span="24"><el-form-item label="硬性条件："><dict-tag :options="dictMap.sys_judge" :value="form.hardRequirements" /></el-form-item></el-col>-->

<!--            <el-col :span="24"><el-form-item label="是否计费："><dict-tag :options="dictMap.sys_judge" :value="form.isBilling" /></el-form-item></el-col>-->

<!--            <el-col :span="24"><el-form-item label="反馈人："><span>{{ getNickNameByUserName(form.createBy, userList) }}</span></el-form-item></el-col>-->
<!--          </el-row>-->
<!--        </div>-->
<!--      </el-form>-->

<!--      <div slot="footer" class="dialog-footer-self">-->
<!--        <el-button @click="handleClose">关闭</el-button>-->
<!--      </div>-->
<!--    </el-dialog>-->
<!--  </div>-->
<!--</template>-->

<!--<script>-->
<!--import { getInfo } from "@/api/feedback";-->
<!--import DictTag from "@/components/DictTag";-->
<!--import { getNickNameByUserName } from "@/utils/ruoyi";-->

<!--export default {-->
<!--  name: "DetailDialog",-->
<!--  props: {-->
<!--    dictMap: { type: Object, default: () => ({}) },-->
<!--    userList: { type: Array, default: () => [] },-->
<!--  },-->
<!--  components: {-->
<!--    DictTag,-->
<!--  },-->
<!--  data() {-->
<!--    return {-->
<!--      loading: false,-->
<!--      visible: false,-->
<!--      title: "详情",-->
<!--      form: {},-->
<!--    };-->
<!--  },-->
<!--  methods: {-->
<!--    getNickNameByUserName,-->
<!--    /** 打开详情弹窗 */-->
<!--    handleDetail(id) {-->
<!--      this.visible = true;-->
<!--      this.title = "详情";-->
<!--      this.loading = true;-->
<!--      getInfo(id).then((response) => {-->
<!--        this.form = response.data;-->
<!--      })-->
<!--      .finally(() => {-->
<!--        this.loading = false;-->
<!--      });-->
<!--    },-->
<!--    /** 关闭 */-->
<!--    handleClose() {-->
<!--      this.visible = false;-->
<!--      this.resetForm();-->
<!--    },-->
<!--    /** 重置 */-->
<!--    resetForm() {-->
<!--      Object.assign(this.form, this.$options.data().form);-->
<!--    },-->
<!--  },-->
<!--};-->
<!--</script>-->

<!--<style scoped lang="scss">-->
<!--.custom-dialog ::v-deep .el-dialog:not(.is-fullscreen) {-->
<!--  margin-top: 5vh !important;-->
<!--}-->

<!--.form-container {-->
<!--  max-height: 75vh;-->
<!--  overflow-y: auto;-->
<!--  overflow-x: hidden;-->
<!--}-->

<!--.readonly-text {-->
<!--  white-space: pre-wrap;-->
<!--  line-height: 1.6;-->
<!--  color: #333;-->
<!--}-->
<!--</style>-->
