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
                  <el-button size="mini" @click="openAddUserDialog">添加用户</el-button>
                </div>
                <!-- 成员ID用 span 平铺 -->
                <div class="member-grid">
                  <span v-for="m in members" :key="m.userId" class="member-span">
                    <!-- 群成员 -->
                    {{ getNickNameByUserId(m.userId, userList) }}
                    <i class="el-icon-close" @click="confirmRemove(m.userId)"></i>
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

      <!-- 添加用户弹窗 -->
      <el-dialog title="添加用户" :visible.sync="showAddUserDialog" :close-on-click-modal="false" :close-on-press-escape="false" v-on="$listeners" class="custom-dialog add-user-dialog" width="70%" >
        <!-- 选中的用户昵称显示在上面 -->
        <div class="selected-users">
          <span class="label">已选用户：</span>
          <div class="names">
            <span v-for="(n,i) in selectedNames" :key="i" class="name-tag">{{ n }}</span>
          </div>
        </div>

        <splitpanes :horizontal="this.$store.getters.device === 'mobile'" class="default-theme">
          <!--部门数据-->
          <pane size="12">
            <el-col>
              <div class="head-container">
                <el-tree :data="deptOptions" :props="defaultProps" :expand-on-click-node="false" :filter-node-method="filterNode" ref="tree" node-key="id" default-expand-all highlight-current @node-click="handleNodeClick" />
              </div>
            </el-col>
          </pane>
          <!--用户数据-->
          <pane size="88">
            <el-col>
              <div class="table-wrapper-self">
                <el-table v-loading="addUserLoading" :data="addUserList" stripe height="600" @selection-change="handleSelectionChange">
                  <el-table-column type="selection" width="50" align="center" />
                  <el-table-column label="登录账号" align="center" key="userName" prop="userName"/>
                  <el-table-column label="用户昵称" align="center" key="nickName" prop="nickName"/>
                  <el-table-column label="手机号码" align="center" key="phoneNumber" prop="phoneNumber"/>
                  <el-table-column label="创建时间" align="center" prop="createTime">
                    <template slot-scope="scope">
                      <span>{{ parseTime(scope.row.createTime) }}</span>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-col>
          </pane>
        </splitpanes>
        <span slot="footer">
          <el-button @click="showAddUserDialog=false">取消</el-button>
          <el-button type="primary" @click="addUserToGroup">添加</el-button>
        </span>
      </el-dialog>

      <!--右侧 今日部门邀约统计、今日邀约明细-->
      <div class="card-frame-item right">
        <!-- 今日部门邀约统计 -->
        <el-card style="padding: 0">
          <div slot="header" class="card-header">
            <span class="card-title">邀约统计</span>
            <div class="card-actions">
              <!-- 刷新 -->
              <el-tooltip effect="dark" content="刷新" placement="top">
                <i class="el-icon-refresh" @click="getDeptInvitationCount();getInvitationInfo()"></i>
              </el-tooltip>
            </div>
          </div>
          <div class="table-wrapper-self">
            <el-table stripe :data="invitationCountData" v-loading="invitationCountLoading" height="250">
              <el-table-column prop="name" align="center" label="招聘部门"/>
              <el-table-column prop="count" align="center" label="今日邀约量"/>
            </el-table>
          </div>
        </el-card>
        <!-- 今日邀约明细 -->
        <el-card style="padding: 0">
          <div slot="header" class="card-header"> <span class="card-title">邀约明细</span></div>
          <div class="table-wrapper-self">
            <el-table stripe :data="invitationInfoData" v-loading="invitationInfoLoading" height="368">
              <el-table-column type="index" label="序号" width="50" align="center"/>
              <el-table-column label="面试点位" align="center" prop="locationName" show-overflow-tooltip/>
              <el-table-column label="姓名" align="center" prop="name" width="80"/>
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
            </el-table>
          </div>
        </el-card>
      </div>
    </div>

  </div>
</template>

<script>
import {
  groupList,
  getHistory,
  markRead,
  selectInvitationInfo,
  selectPeopleInvitationCount,
  removeMember,
  addMember,
} from '@/api/speak'
import {deptTreeSelect, deptTreeSelectSubDept, listUserKv, listUserKvSubDept} from "@/api/system/user";
import { getNickNameByUserId, parseTime } from "@/utils/ruoyi";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";
import { Splitpanes, Pane } from "splitpanes";
import "splitpanes/dist/splitpanes.css";
import { getToken } from "@/utils/auth";

export default {
  name: 'SpeakLevel2',
  components: { Treeselect, Splitpanes, Pane},
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

      // 弹窗控制
      showAddUserDialog: false,           // 添加用户弹窗

      // 聊天记录
      messagesLoading: false,             // 加载聊天内容

      // 添加用户弹窗
      deptOptions: undefined,             // 添加用户 - 部门树
      // 所有部门树选项
      defaultProps: {
        children: "children",
        label: "label"
      },
      addUserLoading: false,             // 添加用户 - loading
      addUserList: [],                   // 添加用户 - 数据集合
      selectedUserIds: [],               // 选中用户的id集合
      selectedNames: [],                 // 选中用户的昵称集合，用于显示

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

      // 今日邀约统计
      invitationCountData: [],
      invitationCountLoading: false,

      // 今日邀约明细
      invitationInfoData: [],
      invitationInfoLoading: false,

      // 表情
      emojis: []
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
    this.getDeptInvitationCount();   // 查询查询供应商邀约总数
    this.getInvitationInfo();        // 查询邀约明细
  },
  methods: {
    parseTime,
    getNickNameByUserId,
    /** 查询用户列表 */
    getUserList() {
      listUserKv().then(response => this.userList = response.data)
    },
    /** 查询查询供应商邀约总数 */
    getDeptInvitationCount() {
      this.invitationCountLoading = true;
      selectPeopleInvitationCount({
        interviewDate: this.$dayjs().format('YYYY-MM-DD'),
        deptId: this.$store.state.user.deptId
      })
        .then(res => (this.invitationCountData = res.data || []))
        .finally(() => (this.invitationCountLoading = false));
    },
    /** 查询邀约明细 */
    getInvitationInfo() {
      this.invitationInfoLoading = true;
      selectInvitationInfo({
        interviewDate: this.$dayjs().format('YYYY-MM-DD'),
        deptId: this.$store.state.user.deptId
      })
        .then(res => (this.invitationInfoData = res.data || []))
        .finally(() => (this.invitationInfoLoading = false));
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

    // 打开添加成员弹窗
    openAddUserDialog() {
      this.getDeptTree(); // 查询部门
      this.handleNodeClick(); // 查询用户
      this.showAddUserDialog = true
    },
    // 选中用户
    handleSelectionChange(selection) {
      // selection 是选中的整行数据数组
      this.selectedUserIds = selection.map(item => item.userId)     // 取用户ID集合
      this.selectedNames   = selection.map(item => item.nickName)   // 取昵称集合
    },
    // 添加成员
    async addUserToGroup() {
      if (!this.selectedUserIds.length) {
        this.$message.warning('请先勾选用户');
        return;
      }

      const res = await addMember({
        id: this.currentGroupId,
        userIdParam: this.selectedUserIds
      })
      this.$message.success('添加成功');
      this.showAddUserDialog = false
      // res.data 是后端返回的 ChatGroup
      const updatedGroup = res.data
      this.members = this.parseUserIds(updatedGroup.userIds)
      await this.refreshGroups() // 更新人数
    },
    // 二次确认删除
    confirmRemove(userId) {
      this.$confirm('确定要移除该成员吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.removeUserFromGroup(userId)
      }).catch(() => {})
    },
    // 删除成员
    async removeUserFromGroup(userId) {
      const res = await removeMember({
        id: this.currentGroupId,
        userIdParam: [userId]
      })
      const updatedGroup = res.data
      this.members = this.parseUserIds(updatedGroup.userIds)
      await this.refreshGroups()
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

    // 添加用户弹窗
    /** 查询部门下拉树结构 */
    getDeptTree() {
      deptTreeSelectSubDept().then(response => {
        this.deptOptions = response.data;
        this.enabledDeptOptions = this.filterDisabledDept(JSON.parse(JSON.stringify(response.data)));
      });
    },
    // 过滤禁用的部门
    filterDisabledDept(deptList) {
      return deptList.filter(dept => {
        if (dept.disabled) {
          return false;
        }
        if (dept.children && dept.children.length) {
          dept.children = this.filterDisabledDept(dept.children);
        }
        return true;
      });
    },
    // 筛选节点
    filterNode(value, data) {
      if (!value) return true;
      return data && data.label.indexOf(value) !== -1;
    },

    // 查询部门下的用户
    handleNodeClick(data) {
      this.addUserLoading = true;

      // 如果 data 为 null，就传 null；否则传 data.id
      const deptId = data && data.id ? data.id : null;

      listUserKvSubDept({ deptId })
        .then(response => {
          this.addUserList = response.data || [];
        })
        .catch(() => {
          this.addUserList = [];
        })
        .finally(() => {
          this.addUserLoading = false;
        });
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

  }
}
</script>

<style lang="scss" scoped>
.app-container{
  padding: 0;
}

/* 创建群聊弹窗 30vh */
.create-group-dialog ::v-deep .el-dialog:not(.is-fullscreen) {
  margin-top: 30vh !important;
}

/* 添加用户弹窗 10vh */
.add-user-dialog ::v-deep .el-dialog:not(.is-fullscreen) {
  margin-top: 10vh !important;
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

    .el-button {
      font-size: 12px;
      color: #999;
      padding: 0 4px;
      &:hover {
        color: #F56C6C;
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
  background:
    url('~@/assets/logo/logo.png') no-repeat center center / contain,
    rgba(255, 255, 255, 0.8); /* 添加背景色并设置透明度 */
  background-blend-mode: overlay; /* 混合模式 */

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
/*.chat-messages::before {
  content: "";
  position: fixed;         !* ✅ 改成 fixed *!
  top: 50%;
  left: 41%;
  transform: translate(-50%, -41%);
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
}*/


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
    padding: 5px 15px;
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

    .el-icon-close {
      cursor: pointer;
      margin-left: 6px;
      color: #bbb;
      font-size: 14px;
      transition: color 0.2s;

      &:hover {
        color: #F56C6C; /* 红色叉号 */
      }
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

// 选择用户
.selected-users {
  display: flex;
  align-items: flex-start;
  background: #f0f9f0;            /* 淡绿色背景 */
  border: 1px solid #c8e6c9;      /* 浅绿色边框 */
  border-radius: 6px;
  padding: 8px 10px;
  margin-top: -16px;
  margin-bottom: 6px;
  font-size: 13px;
  color: #2f4f2f;                 /* 深绿文字 */

  .label {
    flex-shrink: 0;
    font-weight: bold;
    margin-right: 6px;
    margin-top: 2px;
  }

  .names {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
  }

  .name-tag {
    display: inline-block;
    background: #ffffff;
    border: 1px solid #c8e6c9;
    border-radius: 4px;
    padding: 2px 6px;
    font-size: 12px;
    color: #2f4f2f;
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

// 归属供应商，
.link-cell {
  cursor: pointer;
  font-weight: 600;       /* 加粗 */
  color: #2c3e50;         /* 默认深灰 */
  transition: all 0.2s ease;
}

.link-cell:hover {
  color: #409EFF;         /* 鼠标悬浮变蓝 */
  text-decoration: underline;
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

