<template>
  <div>
    <el-dialog :title="title" :visible.sync="visible" :close-on-click-modal="false" :close-on-press-escape="false" v-on="$listeners" @close="handleClose" class="custom-dialog" width="50%" >
        <div class="form-container">
          <editor v-model="details" :readOnly="true"/>
        </div>
      <div slot="footer" class="dialog-footer-self">
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>

export default {
  name: 'ShowHtml',
  data() {
    return {
        // 弹窗标题
        title: '',
        // 展示内容
        details: '',
        // 展示弹窗
        visible: false
    }
  },
  methods: {
    /** 打开弹窗 */
    handleOpen(title, details) {
        this.title = title
        this.details = details
        this.visible = true
    },

    /** 关闭弹窗 */
    handleClose() {
      this.visible = false
    }
  }
}
</script>

<style scoped lang="scss">

.custom-dialog ::v-deep .el-dialog:not(.is-fullscreen){
  margin-top: 15vh !important;
}

.form-container {
  max-height: 60vh;
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
