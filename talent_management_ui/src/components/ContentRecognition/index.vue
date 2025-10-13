<template>
  <!-- 内容识别框 -->
  <div class="show-container">
    <div class="show-frame">
      <div class="show-title">内容识别框</div>
      <el-button type="primary" @click="handleSubmit" size="mini" style="margin-right: 20px">识别</el-button>
      <el-popover
        placement="top-start"
        width="300"
        trigger="click"
      >
      <div style="user-select: text;">
        姓名：刘倩<br>
        电话：13604524587<br>
        年龄：25
      </div>
      <el-button slot="reference" type="text">查看示例</el-button>
      </el-popover>

    </div>
    <el-input
      type="textarea"
      :rows="8"
      v-model="textarea"
      :placeholder="`例如：
姓名：刘倩
电话：13604524587
年龄：25`">
    </el-input>

  </div>
</template>

<script>
export default {
   name:'ContentRecognition',
  props: {
    dictMap: { type: Object, default: () => ({}) },
  },
   data() {
      return {
        textarea: "姓名：\n电话：\n年龄：",       // 使用 \n 实现换行
        sexList:[],         // 性别列表
        educationList:[]    // 学历列表
      }
   },
   mounted(){
     this.sexList = this.dictMap.sys_user_sex;
     this.educationList = this.dictMap.sys_education;
   },
   methods:{
      // 识别信息
      handleSubmit() {
        if (!this.textarea.trim()) {
            this.$message.warning('请输入内容');
            return;
        }
        // 解析文本内容
        const lines = this.textarea.split('\n');
        const result = {};

        lines.forEach(line => {
            // 使用正则表达式匹配键值对
            // 匹配模式：开头是任意字符（键），然后跟着一个或多个分隔符（;:：；，,），最后是值
            const match = line.match(/^([^;:：；，,]+)[;:：；，,]+\s*(.*)$/);
            if (match) {
                const key = this.getChangeKey(match[1].trim());
                const value = match[2].trim();
                if (key && value) {
                    result[key] = value;
                }
                if(result.sex){
                  result.sex = this.sexList?.find(item=>item.label == result.sex)?.value
                }
                if(result.education){
                  result.education = this.educationList?.find(item=>item.label == result.education)?.value
                }
            }
        });
        this.$emit('close', result);
      },
      // 文字转换为key值
      getChangeKey(value) {
        let key = '';
        switch(String(value)) {
            case '姓名':
                key = 'name';
                break;
            case '性别':
                key = 'sex';
                break;
            case '电话':
                key = 'phone';
                break;
            case '年龄':
                key = 'age';
                break;
            case '学历':
                key = 'education';
                break;
            case '面试日期':
                key = 'interviewDate';
                break;
            case '面试时间':
                key = 'interviewTime';
                break;
            default:
                key = value;
        }
        return key;
      },
     /** 重置表单 */
     clear() {
       this.textarea = ''
     }
   },
}
</script>

<style scoped lang="scss">
  .show-frame{
    display: flex;
    align-items: center;
    margin: 20px 0;
    .show-title{
        margin-right: 20px;
        font-size: 18px;
        font-weight: 700;
    }
  }
  /* 允许复制 */
  .custom-tooltip {
    user-select: text !important;
  }
</style>
