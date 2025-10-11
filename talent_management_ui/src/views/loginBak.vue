<template>
  <div class="login">

    <div class="container">
      <!-- 左侧内容 -->
      <div class="left">
        <img src="../assets/images/login/组 1@2x(2).png" alt="logo" /> <!-- 第一张图片 -->
        <img src="../assets/images/login/组 1@2x.png" alt="visual" /> <!-- 第二张图片 -->
      </div>

      <!-- 右侧登录表单 -->
        <div class="right">
          <div class="login-box">
            <el-form ref="loginForm" :model="loginForm" :rules="loginRules">
              <img src="../assets/images/login/组 1(1).png" alt="logo"/> <!-- 第一张图片 -->
              <p>欢迎登录</p>

              <!-- 账号输入框 -->
              <el-form-item prop="username">
                <div class="input-container">
                  <img src="../assets/images/login/用户.png" alt="user" />
                  <div class="input-divider"></div>
                  <input v-model="loginForm.username" @keyup.enter="handleLogin" type="text" placeholder="请输入登录账号" maxlength="20"/>
                </div>
              </el-form-item>

              <!-- 密码输入框 -->
              <el-form-item prop="password">
                <div class="input-container">
                  <img src="../assets/images/login/密码.png" alt="password" />
                  <div class="input-divider"></div>
                  <input v-model="loginForm.password"
                         @keyup.enter="handleLogin"
                         ref="passwordInput"
                         type="password"
                         placeholder="请输入登录密码"
                         maxlength="20" />
                  <img class="eye-toggle"
                       :src="isPasswordVisible ? require('../assets/images/login/visible.png') : require('../assets/images/login/closed-eye.png')"
                       alt="toggle password"
                       @click="togglePasswordVisibility"
                       style="height: 13px; margin-top: 22px;"
                  />
                </div>
              </el-form-item>

              <!-- 记住密码 -->
              <div class="remember">
                <input v-model="loginForm.rememberMe" type="checkbox" id="remember" style="margin-right: 5px;" /> <label for="remember">记住密码</label>
              </div>

              <!-- 登录按钮 -->
              <el-button class="login-button"
                      :loading="loading"
                      @click.prevent="handleLogin">
                <span v-if="!loading">登 录</span>
                <span v-else>登 录 中...</span>
              </el-button>
            </el-form>
          </div>
        </div>
    </div>

    <!--  底部  -->
    <div class="el-login-footer">
      <span>以服务用户为根，成就奋斗者为本.</span>
    </div>
  </div>
</template>

<script>
import Cookies from "js-cookie";
import { encrypt, decrypt } from '@/utils/jsencrypt'

export default {
  name: "Login",
  data() {
    return {
      codeUrl: "",
      loginForm: {
        username: "",
        password: "123456",
        rememberMe: false,
        code: "",
        uuid: ""
      },
      loginRules: {
        username: [
          { required: true, trigger: "blur", message: "请输入您的账号" }
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入您的密码" }
        ],
        code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      loading: false,
      redirect: undefined,
      isPasswordVisible: false,
    };
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect;
      },
      immediate: true
    }
  },
  created() {
    this.getCookie();
  },
  methods: {
    getCookie() {
      const username = Cookies.get("username");
      const password = Cookies.get("password");
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      };
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true;
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 });
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 });
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 });
          } else {
            Cookies.remove("username");
            Cookies.remove("password");
            Cookies.remove('rememberMe');
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$router.push({ path: this.redirect || "/" }).catch(()=>{});
          }).catch(() => {
            this.loading = false;
          });
        }
      });
    },
    togglePasswordVisibility() {
      const input = this.$refs.passwordInput;
      if (!input) return;
      this.isPasswordVisible = !this.isPasswordVisible;
      input.type = this.isPasswordVisible ? 'text' : 'password';
    }
  }
};
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("../assets/images/login/login-background.jpg");
  background-size: cover;
}

.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #333;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}

.container {
  display: flex;
  height: 100vh;
}
.left {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.left img {
  max-width: 300px;
  margin: 20px 0;
}
.right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-box {
  padding: 43px 30px 30px 30px;
  width: 401px;
  height: 427px;
  background: #FFFFFF;
  border-radius: 10px;
  box-sizing: border-box;
  margin-left: 60px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1), 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #e4e7ed;
}
.login-box img {
  display: block;
  margin: 0 auto 20px auto;
  width: 211px;
  height: 35px
}
.login-box p {
  text-align: center;
  font-family: 'Alibaba PuHuiTi 2.0', sans-serif;
  margin: 0 auto 20px auto;
  width: 80px;
  height: 17px;
  font-weight: 600;
  font-size: 18px;
  color: #727272;
  line-height: 18px;
}
.input-container {
  display: flex;
  align-items: center;
  width: 308px;
  height: 45px;
  background: #F2F4F9;
  margin: 12px auto;
  padding: 0 15px;
  box-sizing: border-box;
}
.input-container img {
  width: 17px;
  height: 20px;
  margin-right: 10px;
  margin-top: 20px;
}
.input-divider {
  margin-right: 10px;
  width: 1px;
  height: 21px;
  background: #CCD1DB;
}

.input-container input {
  border: none;
  background: #F2F4F9;
  outline: none;
  flex: 1;
  font-size: 13px;
  font-family: Microsoft YaHei, sans-serif;
  font-weight: 400;
  color: #333333; /* 修改为更深的颜色 */
  line-height: 18px;
  height: 13px;
  width: 74px;
}
.eye-toggle {
  width: 20px;
  margin-left: auto;
  cursor: pointer;
}
.remember {
  width: 410px;
  margin: 10px auto;
  font-size: 14px;
  color: #AAB2C7;
  display: flex;
  align-items: center;
  padding-left: 15px;
}
.remember input[type="checkbox"] {
  cursor: pointer;
}
.remember label {
  cursor: pointer;
}
.login-button {
  width: 308px;
  height: 45px;
  background: #244165;
  border: none;
  margin: 20px auto 0 auto;
  display: block;
  cursor: pointer;
  font-family: Microsoft YaHei;
  font-weight: bold;
  font-size: 13px;
  color: #FFFFFF;
  line-height: 18px;
  letter-spacing: 6px;
}

.el-form-item{
  margin-bottom: 5px;
}
.el-form-item__error {
  color: #ff4949;
  font-size: 12px;
  line-height: 1;
  position: absolute;
  top: 100%;
  left: 20px;
  margin-top: -13px;
}

/* 自定义 hover 样式 */
.login-button:hover {
  background-color: #3b5998 !important;  /* 悬浮背景色 */
  border-color: #3b5998 !important;      /* 边框色 */
  color: #fff !important;                /* 字体颜色 */
}
/* 鼠标点击（按下）时 */
.login-button:active {
  background-color: #2d4373 !important;  /* 点击时更深色 */
  border-color: #2d4373 !important;
  color: #ffffff !important;
}

/* 平滑过渡动画 */
.login-button {
  transition: all 0.3s ease;
}

</style>
