<template>
  <div>
    <el-dialog :title="title" :visible.sync="visible" :close-on-click-modal="false" :close-on-press-escape="false" v-on="$listeners" @close="handleClose" class="custom-dialog" width="50%" >
      <el-form ref="form" v-loading="loading" :model="form" :rules="rules" label-width="100px">
        <div class="form-container">
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="上传图片" prop="logoPath">
                <el-upload
                  class="avatar-uploader"
                  action="#"
                  :auto-upload="false"
                  :show-file-list="false"
                  :before-upload="beforePictureUpload"
                  :on-change="handlePictureChange"
                  accept="image/*"
                >
                  <div class="preview-box">
                    <img v-if="form.logoPath" :src="form.logoPath" class="avatar" />
                    <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                  </div>
                </el-upload>
              </el-form-item>

            </el-col>
            <el-col :span="24">
              <el-form-item label="排序权重" prop="orderNum">
                <el-input v-model="form.orderNum" placeholder="最多10位整数" maxlength="10" clearable/>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="标题" prop="title">
                <el-input v-model="form.title" placeholder="请输入标题" maxlength="50" clearable/>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="内容" prop="content">
                <editor v-model="form.content" :min-height="192" @blur="$refs.form.validateField('content')" @change="$refs.form.validateField('content')"/>
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
import { save, getInfo, getItemPic } from "@/api/requirement";

export default {
  name: 'EditDialog',
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
        // logo
        logoPath:undefined,
        // 排序权重
        orderNum:undefined,
        // 标题
        title:undefined,
        // 内容
        content:undefined,
        pictureFile: null, // 前端文件对象
      },
      // 表单验证规则
      rules: {
        // ✅ 改这里：用自定义校验 pictureFile
        logoPath: [{
          required: true,
          validator: (rule, value, callback) => {
            if (!value) {
              callback(new Error('请选择logo图片'));
            } else {
              callback();
            }
          },
          trigger: 'change'
        }],
        orderNum: [
            { required: true, message: '请输入排序权重量', trigger: 'blur' },
            { pattern: /^[1-9]\d*$/, message: '排序权重必须为正整数', trigger: 'blur' }
        ],
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入内容', trigger: ['blur', 'change'] },
            {
            validator: (rule, value, callback) => {
            if (!value || value.trim() === '<p><br></p>') {
                callback(new Error('请输入内容'));
            } else {
                callback();
            }
            },
            trigger: ['blur', 'change']
        }],
      },
      mimeTypes: [
        "image/png",
        "image/jpeg",
      ]
    }
  },
  methods: {
    /** 打开新增弹窗 */
    handleAdd() {
        this.title = '新增'
        this.resetForm()
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

        // 查询漫画
        getItemPic(this.form.id).then(res => {
          const blob = new Blob([res], { type: 'image/jpeg' });
          const url = URL.createObjectURL(blob);
          this.$set(this.form, 'logoPath', url);
        });

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
        const formData = new FormData();
        formData.append('entity', new Blob([JSON.stringify(this.form)], { type: 'application/json' }));

        if (this.form.pictureFile) {
          formData.append('picture', this.form.pictureFile);
        }
        save(formData).then((response) => {
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

    /** 图片选择后回调 */
    handlePictureChange(file) {
      // ✅ 只保留一张：最新覆盖
      this.form.pictureFile = file.raw;
      this.form.logoPath = URL.createObjectURL(file.raw);
    },

    /** 上传前校验 */
    beforePictureUpload(file) {
      const isImage = file.type.startsWith('image/');
      const isLt20M = file.size / 1024 / 1024 < 20;

      if (!isImage) {
        this.$message.error('只能上传图片文件！');
        return false;
      }
      if (!isLt20M) {
        this.$message.error('图片大小不能超过 20MB！');
        return false;
      }
      return true;
    },
  }
}
</script>

<style scoped lang="scss">

.custom-dialog ::v-deep .el-dialog:not(.is-fullscreen){
  margin-top: 5vh !important;
}

.form-container {
    max-height: 75vh;
  overflow-y: auto;
  overflow-x: hidden;
}

.avatar-uploader ::v-deep .el-upload {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
  }
  .avatar-uploader .el-upload:hover {
    border-color: #409EFF;
  }
  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    line-height: 178px;
    text-align: center;
  }
  .avatar {
    width: 178px;
    height: 178px;
    display: block;
  }
</style>
