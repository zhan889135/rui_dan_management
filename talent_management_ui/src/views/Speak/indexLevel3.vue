<template>
  <div class="app-container">

    <div class="card-frame">

      <!-- 左侧 聊天框 -->
      <div class="card-frame-item left">
        <el-card>
          <div slot="header" class="card-header"> <span class="card-title">群聊</span></div>


          <div class="chat-container">
            <!-- 左侧 群组列表 -->
            <div class="group-list">
              <div class="group-actions">
                <div class="button-grid">
                  <a href="javascript:void(0)" class="blended-button" @click.prevent="refreshGroups">刷新</a>
                </div>
              </div>
              <ul class="group-ul">
                <li v-for="g in groups" :key="g.id" :class="{active:g.id===currentGroupId}" @click="selectGroup(g.id)">
                  <el-tooltip class="item" effect="dark" :content="g.name" placement="left">
                    <span class="group-name-span">{{ g.name }}</span>
                  </el-tooltip>

                  <!--未读数量-->
                  <span v-if="g.unreadCount && g.unreadCount>0" class="badge">{{ g.unreadCount }}</span>
                </li>
              </ul>
            </div>

            <!-- 右侧 消息列表 + 输入框 -->
            <div class="chat-main" v-if="currentGroupId" v-loading="messagesLoading">
              <!-- 成员列表 -->
              <div class="member-list">
                <!-- 顶部：群名 + 添加用户按钮 -->
                <div class="member-header">
                  <span class="group-name">{{ currentGroupName }}</span>
                </div>
                <!-- 成员ID用 span 平铺 -->
                <div class="member-grid">
                  <span v-for="m in members" :key="m.userId" class="member-span">
                    <!-- 群成员 -->
                    {{ getNickNameByUserId(m.userId, userList) }}
                  </span>
                </div>
              </div>

              <!-- 消息列表，聊天内容 -->
              <div class="chat-messages" ref="chatContainer">
                <div v-for="(m,i) in messages" :key="i">
                  <!-- 别人的消息居左 -->
                  <div v-if="m.from + '' !== userId + ''" class="chat-message">
                    <div class="msg-inner">
                      <!-- 发送人 -->
                      <strong class="msg-name">{{ getNickNameByUserId(m.from, userList) }}：</strong>
                      <!-- 发送消息 -->
                      <div class="msg-content">
                        <template v-if="m.content && m.content.startsWith('http')">
                          <div class="img-wrapper">
                            <img :src="m.content" class="chat-image" @click="previewImage(m.content)" />
                            <i class="el-icon-zoom-in enlarge-icon" @click="previewImage(m.content)"></i>
                          </div>
                        </template>
                        <template v-else>
                          {{ m.content }}
                        </template>
                      </div>
                      <!-- 发送时间 -->
                      <span class="msg-time">{{ m.time }}</span>
                    </div>
                  </div>

                  <!-- 自己的消息居右 -->
                  <div v-else class="chat-message mine">
                    <div class="msg-inner">
                      <!-- 发送人 -->
                      <strong class="msg-name">我：</strong>
                      <!-- 发送消息 -->
                      <div class="msg-content">
                        <template v-if="m.content && m.content.startsWith('http')">
                          <div class="img-wrapper">
                            <img :src="m.content" class="chat-image" @click="previewImage(m.content)" />
                            <i class="el-icon-zoom-in enlarge-icon" @click="previewImage(m.content)"></i>
                          </div>
                        </template>
                        <template v-else>
                          {{ m.content }}
                        </template>
                      </div>
                      <!-- 发送时间 -->
                      <span class="msg-time">{{ m.time }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 图片查看大图 -->
              <el-dialog title="预览" :visible.sync="previewVisible" class="custom-dialog preview-picture-dialog" width="60%">
                <div class="preview-container">
                  <img :src="previewUrl" class="preview-img">
                </div>
              </el-dialog>

              <!-- 输入框 -->
              <div class="chat-input">
                <el-input v-model="inputMsg" placeholder="输入消息后回车或点击发送" @keyup.enter.native="sendMessage()"></el-input>
                <!-- 表情选择器 -->
                <el-popover placement="top" trigger="hover" width="350">
                  <div class="emoji-panel">
                    <span v-for="(emoji, index) in emojis" :key="index" class="emoji-item" @click="insertEmoji(emoji)">
                      {{ emoji }}
                    </span>
                  </div>
                  <el-button slot="reference" icon="el-icon-smile" style="margin-left:8px;">表情</el-button>
                </el-popover>

                <!-- 上传图片 -->
                <el-upload
                  class="upload-btn"
                  :action="upload.uploadUrl"
                  :data="upload.uploadParams"
                  :headers="upload.headers"
                  :show-file-list="false"
                  accept="image/*"
                  :before-upload="beforeUploadImage"
                  :on-success="handleUploadSuccess"
                >
                  <el-button icon="el-icon-picture" style="margin-left:8px;">图片</el-button>
                </el-upload>
                <el-button type="primary" @click="sendMessage()" style="margin-left:8px;">发送</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!--右侧 今日部门邀约统计、今日邀约明细-->
      <div class="card-frame-item right">
        <!-- 内容识别区域 -->
        <el-card :body-style="{ padding: '15px 20px 0 20px' }">
          <div slot="header" class="card-header">
            <span class="card-title">邀约录入</span>
            <div class="card-actions">
              <!-- 刷新 -->
              <el-tooltip effect="dark" content="刷新" placement="top">
                <i class="el-icon-refresh" @click="getInvitationInfo()"></i>
              </el-tooltip>
            </div>
          </div>
          <el-form ref="speakForm" v-loading="saveInvitationLoading" :model="speakForm" label-width="100px" label-position="left">
            <el-row :gutter="24">
              <el-col :span="24">
                <ContentRecognition ref="contentRecognition" @close="recognitionForm" :dict-map="dict.type" :location-list="locationList"></ContentRecognition>
              </el-col>
            </el-row>
            <el-row :gutter="24" style="margin: 10px -12px 0">
              <el-col :span="24">
                <el-form-item label="招聘人：">
                  <el-input :value="getNickNameByUserName(speakForm.createBy, userList)" disabled/>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>
        <!-- 今日邀约明细 -->
        <el-card style="padding: 0">
          <div slot="header" class="card-header"> <span class="card-title">邀约明细</span></div>
          <div class="table-wrapper-self">
            <el-table stripe :data="invitationInfoData" v-loading="invitationInfoLoading" height="368">
              <el-table-column type="index" label="序号" width="50" align="center"/>
              <el-table-column label="面试点位" align="center" prop="locationName" show-overflow-tooltip/>
              <el-table-column label="姓名" align="center" prop="name" width="70"/>
              <el-table-column label="性别" align="center" prop="sex" width="50">
                <template slot-scope="scope">
                  <dict-tag :options="dict.type.sys_user_sex" :value="scope.row.sex"/>
                </template>
              </el-table-column>
              <el-table-column label="电话" align="center" prop="phone" width="110"/>
              <el-table-column label="年龄" align="center" prop="age" width="50"/>
              <el-table-column label="学历" align="center" prop="education" width="80">
                <template slot-scope="scope">
                  <dict-tag :options="dict.type.sys_education" :value="scope.row.education"/>
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="50" fixed="right">
                <template slot-scope="{ row }" >
                  <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(row)"/>
                  <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(row)"/>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 编辑 -->
          <EditDialog ref="editDialog" @refresh="getInvitationInfo" @close="handleClose" :dict-map="dict.type" :user-list="userList" :location-list="locationList"/>

        </el-card>
      </div>
    </div>

  </div>
</template>

<script>
import {groupList, getHistory, markRead, selectInvitationInfo, saveInvitationInfo} from '@/api/speak'
import { listUserKv } from "@/api/system/user";
import {getNickNameByUserId, getNickNameByUserName, parseTime} from "@/utils/ruoyi";
import { getToken } from "@/utils/auth";
import ContentRecognition from "@/components/ContentRecognition/indexSpeak.vue";
import {save} from "@/api/report";
import {delData} from "@/api/feedback";
import EditDialog from "@/views/Feedback/edit.vue";
import {allListNoDept} from "@/api/location";

export default {
  name: 'SpeakLevel3',
  components: {EditDialog, ContentRecognition},
  dicts: [ 'sys_user_sex','sys_education' ],
  data() {
    return {
      userList: [],                       // 用户列表，转义userName

      groups: [],                         // 聊天组
      currentGroupId: null,               // 选中聊天组ID
      currentGroupName: '',               // 选中聊天组Name
      members: [],                        // 聊天组成员
      messages: [],                       // 聊天内容
      inputMsg: '',                       // 准备发送消息内容
      ws: null,                           // websocket
      userId: this.$store.state.user.id,  // 当前登录用户id

      // 聊天记录
      messagesLoading: false,             // 加载聊天内容

      // 图片上传
      upload: {
        baseUrl: process.env.VUE_APP_BASE_API,
        uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload',
        headers: { Authorization: 'Bearer ' + getToken() },
        uploadParams: { path: 'speak' },
      },
      // 图片预览
      previewVisible: false,            // 预览弹窗
      previewUrl: '',                   // 预览url

      // 今日邀约明细
      invitationInfoData: [],
      invitationInfoLoading: false,

      // 表情
      emojis: [],

      // 识别内容表单
      speakForm: {
        // 这里因为是员工录入，所以现获取当前员工的部门
        subDeptId: this.$store.state.user.deptId,
        subDeptName: this.$store.state.user.deptName,
        createBy: this.$store.state.user.userName,
        interviewDate: this.$dayjs().format('YYYY-MM-DD')  // 面试日期，默认今天
      },
      saveInvitationLoading: false,

      locationList: [], // 查询点位名称
    }
  },
  watch: {
    async messages() {
      await this.scrollAfterRender()  // 监听聊天内容持续在最底部
    }
  },

  mounted() {
    this.refreshGroups()    // 加载群组列表
    this.initWebSocket();   // 👈 页面一加载就建立连接并发 init
  },
  created() {
    // 😀 (U+1F600) 到 🙏 (U+1F64F) 一共有 80 多个表情
    for (let i = 0x1F600; i <= 0x1F64F; i++) {
      this.emojis.push(String.fromCodePoint(i));
    }
    this.getUserList();              // 获取用户集合
    this.getInvitationInfo();        // 查询邀约明细
    this.getLocationList()           // 查询面试点位
  },
  methods: {
    getNickNameByUserName,
    parseTime,
    getNickNameByUserId,
    /** 查询用户列表 */
    getUserList() {
      listUserKv().then(response => this.userList = response.data)
    },
    /** 查询邀约明细 */
    getInvitationInfo() {
      this.invitationInfoLoading = true;
      selectInvitationInfo({
        interviewDate: this.$dayjs().format('YYYY-MM-DD'),
        createBy: this.$store.state.user.userName
      })
        .then(res => (this.invitationInfoData = res.data || []))
        .finally(() => (this.invitationInfoLoading = false));
    },
    /** 查询当前供应商,所关联的点位 */
    getLocationList() {
      allListNoDept({ deptId: this.$store.state.user.deptId }).then(response => this.locationList = response.data)
    },

    // 群组列表
    async refreshGroups() {
      // 后端返回 AjaxResult，要取 data
      const res = await groupList()
      this.groups = res.data || []
    },

    // 选中群
    async selectGroup(id) {
      this.messagesLoading = true;
      const g = this.groups.find(item => item.id === id)
      this.currentGroupId = id
      this.currentGroupName = g ? g.name : ''
      this.members = this.parseUserIds(g.userIds)

      // 👇 先拉历史消息
      const res = await getHistory(id)
      // 后端返回的是 [{userId:'U1',content:'xxx',sendTime:'2025-09-17 22:15:31'},…]
      this.messages = res.data.map(m => ({
        from: m.fromUser,
        content: m.content,
        time: m.sendTime
      }))

      // 进入群再发送join
      this.ws.send(JSON.stringify({
        type: 'join',
        groupId: id,
        userId: this.userId
      }))

      this.messagesLoading = false;

      // 👇 关键：首次打开拉完历史消息后，等图片加载再滚底 先滚底再标记读
      await this.scrollAfterRender()

      // ✅ 标记已读（持久化）
      await markRead(id)

      // 本地把该群未读清0（UI 立即响应；刷新后也会被后端覆盖为0）
      const idx = this.groups.findIndex(x => x.id + '' === id + '')
      if (idx > -1) this.$set(this.groups[idx], 'unreadCount', 0)
    },

    // 监听WebSocket  8096端口
    initWebSocket() {
      if (this.ws) this.ws.close()
      this.ws = new WebSocket(`ws://${location.hostname}:8096/ws`)
      this.ws.onopen = () => {
        // 先只绑定 userId，不绑定任何 groupId
        this.ws.send(JSON.stringify({
          type: 'init',
          userId: this.userId
        }))
      }
      this.ws.onmessage = async e => {
        const msg = JSON.parse(e.data)
        if (msg.type === 'chat') {
          this.messages.push({
            from: msg.from,
            content: msg.content,
            time: msg.time || new Date().toLocaleString()
          })
          if (msg.content && msg.content.startsWith('http')) {
            await this.scrollAfterRender()
          } else {
            this.$nextTick(() => this.scrollToBottom())
          }
        } else if (msg.type === 'unread') {
          // 未读 +1
          const idx = this.groups.findIndex(x => String(x.id) === String(msg.groupId))
          if (idx > -1 && this.currentGroupId + '' !== msg.groupId + '') {
            const cur = this.groups[idx].unreadCount || 0
            this.$set(this.groups[idx], 'unreadCount', cur + 1)
          }
        }
      }
    },
    // 发送消息（支持文字和图片）
    sendMessage(content) {
      const msgContent = content || this.inputMsg;
      if (!msgContent || !this.ws) return;
      this.ws.send(JSON.stringify({
        type: 'chat',
        content: msgContent,
        userId: this.userId,
        groupId: this.currentGroupId
      }));

      this.inputMsg = '';
    },

    // 群成员拆分回显
    parseUserIds(userIds) {
      if (!userIds) return []
      return userIds
        .split(',')
        .filter(u => u) // 去空
        .map(u => ({ userId: u }))
    },

    // 压缩图片
    beforeUploadImage(file) {
      // 校验 MIME 类型
      if (!file.type.startsWith('image/')) {
        this.$message.error('只能上传图片文件');
        return false; // 阻止上传
      }

      return new Promise((resolve) => {
        const reader = new FileReader();
        reader.onload = e => {
          const img = new Image();
          img.src = e.target.result;
          img.onload = () => {
            const canvas = document.createElement('canvas');
            const maxWidth = 800;
            const scale = img.width > maxWidth ? maxWidth / img.width : 1;
            canvas.width = img.width * scale;
            canvas.height = img.height * scale;
            const ctx = canvas.getContext('2d');
            ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
            canvas.toBlob(
              blob => {
                const compressedFile = new File([blob], file.name, { type: file.type });
                resolve(compressedFile);
              },
              file.type, 0.7 // 压缩质量
            );
          };
        };
        reader.readAsDataURL(file);
      });
    },

    // 上传成功
    handleUploadSuccess(res) {
      if (res.code === 200 && res.url) {
        // 把图片URL当作content发送
        let newUrl = res.url;

        // ✅ 无条件替换掉 127.0.0.1，无论开发还是生产
        // 同时兼容 127.0.1.1 这种变体
        newUrl = newUrl.replace(/127(?:\.\d+){3}/, process.env.VUE_APP_REPLACE_IP);

        this.sendMessage(newUrl);
      } else {
        this.$message.error('图片上传失败');
      }
    },
    // 图片预览
    previewImage(url) {
      this.previewUrl = url
      this.previewVisible = true
    },

    // 仅滚到底
    scrollToBottom() {
      const el = this.$refs.chatContainer
      if (el) el.scrollTop = el.scrollHeight
    },

    // 等待当前消息区域中的图片加载（带超时兜底）
    waitImagesLoad(timeoutMs = 1500) {
      return new Promise(resolve => {
        const el = this.$refs.chatContainer
        if (!el) return resolve()

        const imgs = Array.from(el.querySelectorAll('img'))
          .filter(img => !img.complete)

        if (imgs.length === 0) return resolve()

        let done = false
        const finish = () => { if (!done) { done = true; resolve() } }

        let left = imgs.length
        const onSettled = () => { if (--left <= 0) finish() }

        imgs.forEach(img => {
          img.addEventListener('load',  onSettled, { once: true })
          img.addEventListener('error', onSettled, { once: true })
        })

        // 超时兜底，避免某些图一直卡住
        setTimeout(finish, timeoutMs)
      })
    },

    // 渲染 -> 等图 -> 滚底
    async scrollAfterRender() {
      await this.$nextTick()
      await this.waitImagesLoad()
      this.scrollToBottom()
    },

    // 表情包
    insertEmoji(emoji) {
      this.inputMsg += emoji; // 把选择的 emoji 插入输入框
    },

    /** 识别内容 */
    recognitionForm(val) {
      this.saveInvitationLoading = true;
      // 只更新 val 里有的字段
      Object.keys(val).forEach(key => {
        if (val[key] !== undefined && val[key] !== null && val[key] !== '') {
          this.$set(this.speakForm, key, val[key])
        }
      });

      // 保存面试反馈信息
      saveInvitationInfo(this.speakForm).then((response) => {
        this.$modal.msgSuccess('保存成功')
      }).finally(() =>{
        this.getInvitationInfo();
        this.saveInvitationLoading = false;
      })
    },

    /** 修改按钮操作 */
    handleUpdate(row) {
      this.$refs.editDialog.handleUpdate(row.id)
    },
    /** 编辑组件关闭后操作 */
    handleClose() {
      this.getInvitationInfo();
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$modal.confirm('是否确认删除面试反馈信息？').then(function() {
        return delData(row.id);
      }).then(() => {
        this.getInvitationInfo();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
  }
}
</script>

<style lang="scss" scoped>
.app-container{
  padding: 0;
}

/* 图片查看大图 10vh */
.preview-picture-dialog ::v-deep .el-dialog:not(.is-fullscreen) {
  margin-top: 10vh !important;
}

.card-frame {
  display: flex;
  justify-content: space-between;
  .card-frame-item {
    display: flex;
    flex-direction: column;
    .el-card {
      margin: 0 10px 10px 10px;
      .card-title {
        font-size: 14px;
        color: #303133;
      }
    }
  }
  .card-frame-item.left { flex: 1; }           /* 左边自适应 */
  .card-frame-item.right { width: 750px; }     /* 右边固定 320px */
}

/* 聊天容器左右布局 */
.chat-container {
  display: flex;
  height: 710px;
}
/* 群组列表整体容器 */
.group-list {
  width: 220px;
  border: 1px solid #e0e0e0;
  background-color: #fafafa;
  padding: 10px;

  .group-actions {
    display: flex;
    align-items: center; /* 👈 垂直居中 */
    justify-content: space-between;

    .el-button {
      flex: 1;
      &:last-child {
        margin-right: 0;
      }
    }
    .el-button + .el-button {
      margin-left: 0;
    }
  }

  /* 👇只让ul滚动 */
  .group-ul {
    list-style: none;
    padding: 0;
    margin: 0;
    max-height: 650px;    /* 根据页面高度调整 */
    overflow-y: auto;
  }

  .group-ul li {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 10px;
    margin-bottom: 5px;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover {
      background-color: #f0f0f0;
    }

    &.active {
      background-color: #5daeff; /* 浅一点的蓝 */
      color: #fff;
      .el-button {
        color: #fff;
      }
    }
  }
}

.chat-main {
  flex:1;
  display:flex;
  flex-direction:column;
  padding:0 5px 5px 5px;
}

.chat-messages {
  position: relative;
  overflow-y: auto;
  flex: 1;
  border: 1px solid #eee;
  padding: 5px;
  margin-bottom: 5px;

  .chat-message {
    display: flex;
    justify-content: flex-start;
    align-items: center;
    margin-bottom: 5px;

    &.mine {
      justify-content: flex-end;
      text-align: right;
      border-radius: 4px;
      padding: 2px 4px;
    }

    .msg-time {
      font-size: 12px;
      color: #999;
      margin-left: 6px;
    }
  }
}

/* 背景图片 */
.chat-messages::before {
  content: "";
  position: fixed;         /* ✅ 改成 fixed */
  top: 50%;
  left: 41%;
  transform: translate(-50%, -41%);
  background: url('~@/assets/logo/logo.png') no-repeat center center;
  background-size: 300px auto;
  opacity: 0.1;
  width: 300px;
  height: 300px;
  pointer-events: none;
  z-index: 0;
}
.chat-messages > div {
  position: relative;
  z-index: 1;
}


.chat-input {
  display:flex;
  align-items:center;
}

.member-list {
  background-color: #fff;
  border-bottom: 1px solid #e0e0e0;
  padding: 0; /* 顶部自己控制padding */

  /* 顶部群名 + 添加按钮 */
  .member-header {
    display:flex;
    justify-content:space-between;
    align-items:center;
    padding: 11px 15px;
    background: rgba(255,255,255,0.6);
    backdrop-filter: blur(6px);        /* 毛玻璃 */
    border: 1px solid rgba(0,0,0,0.05);
    border-radius: 8px;

    .group-name {
      font-weight:600;
      font-size:16px;
      color:#333;
    }

    .el-button {
      background: linear-gradient(90deg, #a6c8ff, #409EFF); /* 浅蓝 → Element蓝 */
      color: #fff;               /* 白字 */
      border:none;
      font-size:12px;
      border-radius:4px;
      transition:transform 0.2s;
      &:hover{transform:scale(1.05);}
    }
  }

  /* 成员区 */
  .member-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 3px; /* 给成员区留点内边距 */
  }

  .member-span {
    display: inline-flex;
    align-items: center;
    justify-content: space-between;
    padding: 4px 6px;
    min-width: 100px;
    background-color: #fafafa;
    border: 1px solid #e0e0e0;
    border-radius: 4px;
    font-size: 13px;
    color: #606266;
    box-sizing: border-box;
    transition: background-color 0.2s, box-shadow 0.2s;

    &:hover {
      background-color: #f0f0f0;
      box-shadow: 0 0 2px rgba(0, 0, 0, 0.1);
    }
  }
}

// 按钮样式
.button-grid {
  display: flex;            /* 👈 改这里 */
  gap: 8px;                /* 按钮间距 */
  margin-bottom: 5px;
}
.button-row {
  display: flex;
  gap: 12px; /* 按钮之间的间距 */
}

.blended-button {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.3rem 1.0rem; /* 调小点适配侧边栏 */
  border-radius: 8px;
  text-decoration: none;
  font-size: 0.9rem;
  font-weight: bold;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  transition: transform 0.2s ease-out, box-shadow 0.2s ease-out;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);

  background-image:
    linear-gradient(45deg, #f0f7ff 0%, transparent 100%),   /* 非常淡的天蓝 */
    linear-gradient(-45deg, #dcefff 0%, #b3d9ff 100%);     /* 浅蓝渐变 */

  color: #1a3d6d; /* 深蓝色文字 */

  background-blend-mode: overlay;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
  }
  &:active {
    transform: translateY(0);
    box-shadow: 0 3px 10px rgba(0, 0, 0, 0.2);
  }
}

.group-name-span {
  display: inline-block;
  max-width: 160px;     /* 根据需要调宽度 */
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  vertical-align: middle;
}
.chat-image {
  max-width: 200px;
  max-height: 200px;
  border-radius: 4px;
  display: block;
  margin-top: 4px;
}

.chat-message .msg-inner {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  flex-wrap: nowrap;
}

.msg-name {
  width: 70px; /* 👈 固定昵称宽度 */
  flex-shrink: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.msg-content {
  flex: 1; /* 自适应剩余空间 */
  word-break: break-word; /* 内容自动换行 */
}

.msg-time {
  margin-left: 8px;
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
}

.img-wrapper {
  position: relative;
  display: inline-block;
}

.chat-image {
  max-width: 150px;
  max-height: 150px;
  border-radius: 4px;
  display: block;
}

.enlarge-icon {
  position: absolute;
  bottom: 4px;
  right: 4px;
  font-size: 16px;
  color: rgba(255,255,255,0.9);
  background: rgba(0,0,0,0.4);
  border-radius: 50%;
  padding: 2px;
  cursor: pointer;
}

// 图片预览
.preview-picture-dialog ::v-deep .el-dialog__body {
  padding: 0; /* 去掉内边距 */
}

.preview-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 80vh; /* 高度随弹窗 */
  background: #000; /* 可选黑背景更突出 */
}

// 预览图片
.preview-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain; /* 等比缩放完整显示 */
}

// 红色气泡，未读数量
.badge {
  display: inline-block;
  min-width: 16px;
  padding: 0 4px;
  font-size: 12px;
  color: #fff;
  background-color: #F56C6C;
  border-radius: 8px;
  text-align: center;
  margin-left: 6px;
}


// 供应商邀约刷新与重置
.card-header {
  display: flex;
  justify-content: space-between; /* 左右对齐 */
  align-items: center;

  .card-title {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
  }
  .card-actions {
    display: flex;
    align-items: center;

    i {
      font-size: 18px;
      margin-left: 12px;
      color: #909399;
      cursor: pointer;              /* 鼠标悬浮变小手 */
      transition: all 0.2s ease;

      &:hover {
        color: #409EFF;             /* hover 蓝色 */
        transform: scale(1.2);      /* 鼠标悬浮放大一点点 */
      }
    }
  }
}
// 表情包样式
.emoji-panel {
  display: flex;
  flex-wrap: wrap;
  max-height: 200px;
  overflow-y: auto;
  padding: 5px;
}

.emoji-item {
  font-size: 23px;   /* ← 这个就是控制 emoji 大小的关键 */
  padding: 6px 6px 0 6px;      /* ← 这个是每个表情周围的留白，会影响间距 */
  cursor: pointer;   /* ✅ 鼠标悬浮变小手 */
  transition: transform 0.2s;
}

.emoji-item:hover {
  transform: scale(1.2); /* 悬浮放大 */
}
</style>

