<template>
  <section class="login-section">
    <!-- 背景色块 -->
    <div class="colour"></div>
    <div class="colour"></div>
    <div class="colour"></div>

    <div class="box">
      <div class="square" style="--i:0;"></div>
      <div class="square" style="--i:1;"></div>
      <div class="square" style="--i:2;"></div>
      <div class="square" style="--i:3;"></div>
      <div class="square" style="--i:4;"></div>

      <div class="container">
        <div class="form">
          <h2>欢迎登录</h2>
          <el-form ref="loginForm" :model="loginForm" :rules="loginRules">
            <!-- 用户名 -->
            <el-form-item prop="username">
              <div class="input__box">
                <input v-model="loginForm.username" type="text" placeholder="请输入登录账号" maxlength="20" @keyup.enter="handleLogin"/>
              </div>
            </el-form-item>

            <!-- 密码 -->
            <el-form-item prop="password">
              <div class="input__box">
                <input v-model="loginForm.password" ref="passwordInput" :type="isPasswordVisible ? 'text' : 'password'" placeholder="请输入登录密码" maxlength="20" @keyup.enter="handleLogin"/>
                <img :src="isPasswordVisible ? require('../assets/images/login/visible.png') : require('../assets/images/login/closed-eye.png')"
                  class="eye-toggle" alt="toggle password" @click="togglePasswordVisibility"/>
              </div>
            </el-form-item>

            <div class="input__box button-row">
              <input type="submit" :value="loading ? '登录中...' : '登 录'" :disabled="loading" class="login-button" @click.prevent="handleLogin"/>
              <input type="submit" value="注 册" class="login-button" @click.prevent="handleRegister"/>
            </div>
          </el-form>
        </div>
      </div>
    </div>

    <!-- 注册区域 -->
    <transition name="drop">
      <div v-if="drawerOpen" class="signup-wrapper">
        <!-- 保持你原来的 signup-card 样式 -->
        <div class="container">
          <div class="login-signup-cards">
            <div class="signup-card">
              <div class="signup-card-items">
                <h1 class="signup-tag">注 册</h1>
                <el-form ref="form" :model="form" :rules="rules" label-width="80px" class="form-items">
                  <el-col :span="24">
                    <el-form-item label="登录账号" prop="userName">
                      <el-input v-model="form.userName" placeholder="请输入用户名称" maxlength="30" @input="form.userName = form.userName.replace(/[\u4e00-\u9fa5]/g, '')"/>
                    </el-form-item>
                  </el-col>
                  <el-col :span="24">
                    <el-form-item label="用户密码" prop="password">
                      <el-input v-model="form.password" placeholder="请输入用户密码" type="password" maxlength="20" show-password/>
                    </el-form-item>
                  </el-col>
                  <el-col :span="24">
                    <el-form-item label="用户昵称" prop="nickName">
                      <el-input v-model="form.nickName" placeholder="请输入公司名+昵称" maxlength="30" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="24">
                    <el-form-item label="手机号码" prop="phoneNumber">
                      <el-input v-model="form.phoneNumber" placeholder="请输入手机号码" maxlength="11" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="24">
                    <el-form-item label="备注">
                      <el-input v-model="form.remark" type="textarea" placeholder="请输入内容"></el-input>
                    </el-form-item>
                  </el-col>
                  <button class="button-item" type="button" @click="submitForm">提 交</button>
                  <div class="have-account-item">
                    <a href="#" @click="closeRegister">已有账户? <span>去登录</span></a>
                  </div>
                </el-form>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </section>
</template>

<script>
import Cookies from "js-cookie";
import { encrypt, decrypt } from "@/utils/jsencrypt";
import Treeselect from "@riophae/vue-treeselect";
import { register } from "@/api/system/user";

export default {
  name: "Login",
  components: { Treeselect },
  data() {
    return {
      loginForm: {
        username: "",
        password: "",
        rememberMe: false,
      },
      loginRules: {
        username: [{ required: true, trigger: "blur", message: "请输入您的账号" },],
        password: [{ required: true, trigger: "blur", message: "请输入您的密码" },],
      },
      loading: false,
      redirect: undefined,
      isPasswordVisible: false,

      // 注册抽屉相关
      drawerOpen: false,
      form: {
        userName: "",
        password: "",
        nickName: "",
        phoneNumber: "",
        remark: ""
      },
      rules: {
        userName: [{ required: true, message: "请输入账号", trigger: "blur" }],
        password: [{ required: true, message: "请输入密码", trigger: "blur" }],
        nickName: [{ required: true, message: "请输入昵称", trigger: "blur" }],
        phoneNumber: [
          {
            required: true,
            pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
            message: "请输入正确的手机号码",
            trigger: "blur"
          }
        ]
        // email: [{ type: "email", message: "请输入正确邮箱", trigger: "blur" }],
      },
      postOptions: [],
      roleOptions: [],
      enabledDeptOptions: [],
    };
  },
  watch: {
    $route: {
      handler(route) {
        this.redirect = route.query && route.query.redirect;
      },
      immediate: true,
    },
  },
  created() {
    this.getCookie();
  },
  methods: {
    getCookie() {
      const username = Cookies.get("username");
      const password = Cookies.get("password");
      const rememberMe = Cookies.get("rememberMe");

      this.loginForm = {
        username: username || "",
        password: password ? decrypt(password) : "",
        rememberMe: rememberMe ? Boolean(rememberMe) : false,
      };
    },
    handleLogin() {
      this.$refs.loginForm.validate((valid) => {
        if (valid) {
          this.loading = true;
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 });
            Cookies.set("password", encrypt(this.loginForm.password), {expires: 30,});
            Cookies.set("rememberMe", this.loginForm.rememberMe, {expires: 30,});
          } else {
            Cookies.remove("username");
            Cookies.remove("password");
            Cookies.remove("rememberMe");
          }

          this.$store
            .dispatch("Login", this.loginForm)
            .then(() => {
              this.$router.push({ path: this.redirect || "/" }).catch(() => {});
            })
            .catch(() => {
              this.loading = false;
            });
        }
      });
    },
    togglePasswordVisibility() {
      this.isPasswordVisible = !this.isPasswordVisible;
    },
    // 注册用户
    handleRegister() {
      this.drawerOpen = true;
    },
    // 注册
    closeRegister() {
      this.drawerOpen = false;
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          register(this.form).then(() => {
            this.$modal.msgSuccess("提交成功,请等待审核");
            this.drawerOpen = false;
            this.resetForm();
          });
        }
      });
    },
    cancel() {
      this.drawerOpen = false;
      this.resetForm();
    },
    resetForm() {
      this.$refs["form"] && this.$refs["form"].resetFields();
      this.form = {
        userName: "",
        password: "",
        nickName: "",
        phoneNumber: "",
        // email: "",
        // idNumber: "",
        // sex: "",
        remark: ""
      };
    }
  },
};
</script>

<style lang="scss" scoped>

/* 调整 Vue 特有部分 */
.login-button {
  width: 100%;
  height: 45px;
  font-size: 14px;
  letter-spacing: 4px;
}

.eye-toggle {
  width: 20px;
  margin-left: -30px;
  cursor: pointer;
}
.remember {
  margin: 10px 0;
  font-size: 14px;
  color: #ddd;
  display: flex;
  align-items: center;
}


.button-row {
  display: flex;
  justify-content: center;   /* 居中 */
  gap: 60px;                 /* 按钮间距 */
  margin-top: 20px;
  width: 100%;               /* 占满整行 */
}

.button-row .login-button {
  flex: 1;                   /* 两个按钮平均分布 */
  max-width: 200px;          /* 每个按钮最大宽度，可调 */
  height: 50px !important;   /* 强制高度 */
  font-size: 16px !important;/* 强制字体大小 */
}


section {
  display: flex;
  min-height: 100vh;
  align-items: center;
  justify-content: center;
  background: linear-gradient(to bottom, #f7f7fe, #dff1ff);
}

section .colour {
  position: absolute;
  filter: blur(150px);
}

section .colour:nth-child(1) {
  top: -350px;
  width: 600px;
  height: 600px;
  background: #bf4ad4;
}

section .colour:nth-child(2) {
  left: 100px;
  width: 500px;
  height: 500px;
  bottom: -150px;
  background: #ffa500;
}

section .colour:nth-child(3) {
  right: 100px;
  bottom: 50px;
  width: 300px;
  height: 300px;
  background: #2b67f3;
}

.box {
  position: relative;
}

.box .square {
  position: absolute;
  border-radius: 10px;
  backdrop-filter: blur(5px);
  background: rgba(255, 255, 255, 0.1);
  animation-delay: calc(-1s * var(--i));
  animation: animate 10s linear infinite;
  box-shadow: 0 25px 45px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-right: 1px solid rgba(255, 255, 255, 0.2);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

@keyframes animate {
  0%,
  100% {
    transform: translateY(-40px);
  }

  50% {
    transform: translateY(40px);
  }
}

.box .square:nth-child(1) {
  top: -50px;
  left: -60px;
  width: 100px;
  height: 100px;
}

.box .square:nth-child(2) {
  z-index: 2;
  top: 150px;
  left: -100px;
  width: 120px;
  height: 120px;
}

.box .square:nth-child(3) {
  z-index: 2;
  width: 80px;
  height: 80px;
  right: -50px;
  bottom: -60px;
}

.box .square:nth-child(4) {
  left: 100px;
  width: 50px;
  height: 50px;
  bottom: -80px;
}

.box .square:nth-child(5) {
  top: -80px;
  left: 140px;
  width: 60px;
  height: 60px;
}

.container {
  width: 400px;
  display: flex;
  min-height: 400px;
  position: relative;
  border-radius: 10px;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(5px);
  background: rgba(255, 255, 255, 0.1);
  box-shadow: 0 25px 45px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-right: 1px solid rgba(255, 255, 255, 0.2);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.form {
  width: 100%;
  height: 100%;
  padding: 40px;
  position: relative;
}

.form h2 {
  color: #fff;
  font-size: 24px;
  font-weight: 600;
  position: relative;
  letter-spacing: 1px;
  margin-bottom: 40px;
}

.form h2::before {
  left: 0;
  width: 80px;
  height: 4px;
  content: "";
  bottom: -10px;
  background: #fff;
  position: absolute;
}

.form .input__box {
  width: 100%;
  margin-top: 20px;
}

.form .input__box input {
  width: 100%;
  color: #666;
  border: none;
  outline: none;
  font-size: 16px;
  padding: 10px 20px;
  letter-spacing: 1px;
  border-radius: 35px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
  border-right: 1px solid rgba(255, 255, 255, 0.2);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.form::placeholder {
  color: #666;
}

.form .input__box input[type="submit"] {
  color: #666;
  cursor: pointer;
  background: #fff;
  max-width: 100px;
  font-weight: 600;
  margin-bottom: 20px;
}

.forget {
  color: #fff;
  margin-top: 5px;
}

.forget a {
  color: #fff;
  font-weight: 600;
  text-decoration: none;
}


/* 外层容器：只是定位 + 动画载体，不加任何背景/阴影 */
.signup-wrapper {
  position: absolute;
  top: 16%; /* 你想掉落到哪里自己调 */
  left: 50%;
  transform: translateX(-50%);
  z-index: 999;
}

/* 掉落动画 */
.drop-enter-active {
  animation: dropDown 0.6s ease forwards;
}
.drop-leave-active {
  animation: dropUp 0.5s ease forwards;
}

@keyframes dropDown {
  0% {
    transform: translate(-50%, -200px); /* 初始在上方 */
    opacity: 0;
  }
  100% {
    transform: translate(-50%, 0); /* 掉到正常位置 */
    opacity: 1;
  }
}

@keyframes dropUp {
  from {
    transform: translate(-50%, 0);
    opacity: 1;
  }
  to {
    transform: translate(-50%, -200px);
    opacity: 0;
  }
}


/* 注册界面表单 */
.signup-card {
  /* 穿透 ElementUI 内部 input 样式 */
  ::v-deep .el-input__inner {
    background-color: #f3f4f6 !important;
    font-size: 16px !important;
    border: none !important;
    border-radius: 15px !important;
    outline: none !important;
    height: 40px;
    padding: 5px 20px;
  }

  /* 文本域（textarea） */
  ::v-deep .el-textarea__inner {
    background-color: #f3f4f6 !important;
    font-size: 16px !important;
    border: none !important;
    border-radius: 15px !important;
    outline: none !important;
    padding: 10px 20px;
    line-height: 1.5;
    resize: none; /* 你可以改成 vertical，如果只想允许上下缩放 */
  }
}

// 注册界面
.login-card,
.signup-card {
  width: 500px;
  height: auto;
  background-color: white;
  box-shadow: 0 3px 50px 0 rgba(0, 0, 0, 0.2), 0 1px 50px 0 rgba(0, 0, 0, 0.19);
  border-radius: 45px;
}

.login-card-items,
.signup-card-items {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 35px;
  margin: 30px 30px 30px;
  width: auto;
  height: auto;
}

.login-tag,
.signup-tag {
  font-size: 35px;
  font-weight: 700;
  letter-spacing: 0.85px;
}

.form-items {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 20px;
  position: relative;
  width: 100%;
  height: auto;
}

.create-account-item,
.have-account-item {
  position: absolute;
  bottom: 30px;
}
.create-account-item a,
.have-account-item a {
  color: #1d1e22;
  text-decoration: none;
  transition: color 0.4s;
  font-size: 15px;
}
.create-account-item a span,
.have-account-item a span {
  font-weight: 700;
}
.create-account-item a:hover, .create-account-item a:active,
.have-account-item a:hover,
.have-account-item a:active {
  color: #858585;
}

* {
  margin: 0;
  padding: 0;
}

body {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  color: #1d1e22;
  background-color: #f3f4f6;
  font-family: "Raleway", sans-serif;
}

.login-signup-cards {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 150px;
}
.login-signup-cards .login-card .login-card-items .form-items input[type=email] {
  padding: 5px 20px;
  width: calc(100% - 125px);
  height: 40px;
  background-color: #f3f4f6;
  font-size: 16px;
  border: none;
  border-radius: 15px;
  outline: none;
}
.login-signup-cards .login-card .login-card-items .form-items .password-item {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 10px;
  width: 100%;
  height: auto;
}
.login-signup-cards .login-card .login-card-items .form-items .password-item input[type=password] {
  padding: 5px 20px;
  width: calc(100% - 125px);
  height: 40px;
  background-color: #f3f4f6;
  font-size: 16px;
  border: none;
  border-radius: 15px;
  outline: none;
}
.login-signup-cards .login-card .login-card-items .form-items .password-item .forgot-password-item a {
  color: #1d1e22;
  text-decoration: none;
  transition: color 0.4s;
  font-size: 15px;
  font-weight: 700;
}
.login-signup-cards .login-card .login-card-items .form-items .password-item .forgot-password-item a:hover, .login-signup-cards .login-card .login-card-items .form-items .password-item .forgot-password-item a:active {
  color: #858585;
}
.login-signup-cards .login-card .login-card-items .form-items button[type=button] {
  position: relative;
  margin: 0 0 60px;
  width: 60%;
  height: 60px;
  color: white;
  background: linear-gradient(-135deg, #1d2b64 0%, #f8cdda 100%);
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.85px;
  box-shadow: 0 1px 15px 0 rgba(255, 0, 0, 0.1), 0 -1px 15px 0 rgba(0, 0, 0, 0.1);
  border: none;
  border-radius: 25px;
  cursor: pointer;
  z-index: 1;
}
.login-signup-cards .login-card .login-card-items .form-items button[type=button]:hover:before {
  opacity: 1;
}
.login-signup-cards .login-card .login-card-items .form-items button[type=button]:before {
  content: "";
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  background: linear-gradient(-135deg, #4a69e2 0%, #ff6c98 100%);
  border-radius: 25px;
  opacity: 0;
  z-index: -1;
  transition: opacity 0.65s;
}
.login-signup-cards .signup-card .signup-card-items .form-items input[type=email] {
  padding: 5px 20px;
  width: calc(100% - 125px);
  height: 40px;
  background-color: #f3f4f6;
  font-size: 16px;
  border: none;
  border-radius: 15px;
  outline: none;
}
.login-signup-cards .signup-card .signup-card-items .form-items input[type=password] {
  padding: 5px 20px;
  width: calc(100% - 125px);
  height: 40px;
  background-color: #f3f4f6;
  font-size: 16px;
  border: none;
  border-radius: 15px;
  outline: none;
}
.login-signup-cards .signup-card .signup-card-items .form-items button[type=button] {
  position: relative;
  margin: 0 0 65px;
  width: 56%;
  height: 47px;
  color: white;
  background: linear-gradient(-135deg, #1d2b64 0%, #f8cdda 100%);
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 4.85px;
  -webkit-box-shadow: 0 1px 15px 0 rgba(255, 0, 0, 0.1), 0 -1px 15px 0 rgba(0, 0, 0, 0.1);
  box-shadow: 0 1px 15px 0 rgba(255, 0, 0, 0.1), 0 -1px 15px 0 rgba(0, 0, 0, 0.1);
  border: none;
  border-radius: 25px;
  cursor: pointer;
  z-index: 1;
}
.login-signup-cards .signup-card .signup-card-items .form-items button[type=button]:hover:before {
  opacity: 1;
}
.login-signup-cards .signup-card .signup-card-items .form-items button[type=button]:before {
  content: "";
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  background: linear-gradient(-135deg, #4a69e2 0%, #ff6c98 100%);
  border-radius: 25px;
  opacity: 0;
  z-index: -1;
  transition: opacity 0.65s;
}
</style>
