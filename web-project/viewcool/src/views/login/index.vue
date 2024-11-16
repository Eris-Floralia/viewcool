<template>
  <div class="login-form">
    <div class="logo-box">
      <img src="@/assets/img/login_logo.png" alt="">
      <span>橙单低代码生成工具</span>
    </div>
    <div class="login-box">
      <div class="img-box">
        <img class="img-title" src="@/assets/img/login_title.png" alt="">
        <img :src="bkImg" style="height: 100%; flex-shrink: 0" />
      </div>
      <div class="login-input">
        <span class="title">欢迎登录</span>
        <span class="desc">橙单低代码生成演示工程</span>
        <el-form :model="dataForm" :rules="dataRule" size="medium" ref="dataForm" @keyup.enter.native="dataFormSubmit()">
          <el-col :span="24">
            <el-form-item prop="mobilePhone" label="用户名" style="margin-bottom: 12px;">
              <el-input v-model="dataForm.mobilePhone" style="width: 100%;" placeholder="帐号">
                <img slot="prefix" src="@/assets/img/login_username.png" alt="">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item prop="password" label="密码">
              <el-input v-model="dataForm.password" style="width: 100%;" type="password" placeholder="密码" show-password>
                <img slot="prefix" src="@/assets/img/login_password.png" alt="">
              </el-input>
            </el-form-item>
          </el-col>
          <el-button class="login-btn-submit" type="primary" style="width: 100%;margin-top: 10px;"
            @click="dataFormSubmit()"
          >
            登录
          </el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { SystemController } from '@/api';
import { mapMutations } from 'vuex';
import projectConfig from '@/core/config';
import { encrypt, setToken } from '@/utils';

export default {
  data () {
    return {
      bkImg: require('@/assets/img/login_icon.png'),
      dataForm: {
        mobilePhone: 'admin',
        password: '123456'
      },
      dataRule: {
        mobilePhone: [
          { required: true, message: '帐号不能为空', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '密码不能为空', trigger: 'blur' }
        ]
      },
      projectName: projectConfig.projectName
    };
  },
  methods: {
    dataFormSubmit () {
      this.$refs['dataForm'].validate(valid => {
        if (valid) {
          this.login();
        }
      });
    },
    login () {
      let params = {
        loginName: this.dataForm.mobilePhone,
        password: encrypt(this.dataForm.password)
      };
      setToken(null);
      SystemController.login(this, params, null, {showMask: false}).then(data => {
        this.setMenuList(data.data.menuList);
        delete data.data.menuList;

        this.setUserInfo(data.data);
        setToken(data.data.tokenData);
        this.setCurrentMenuId(null);
        this.$router.replace({ name: 'main' });
      }).catch(e => {});
    },
    ...mapMutations(['setUserInfo', 'setMenuList', 'setCurrentMenuId'])
  },
  mounted () {
    this.setMenuList([]);
    this.setUserInfo({});
    setToken();
  }
};
</script>

<style lang="scss">
.login-form {
  width: 100vw;
  height: 100vh;
  background: url(~@/assets/img/login_bg.png) center center;
  background-size: cover;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  .logo-box{
    position: absolute;
    left: 5.5vw;
    top: 6.66vh;
    display: flex;
    align-items: center;
    font-size: 28px;
    color: #333333;
    font-weight: bold;
    img{
      margin-right: 11px;
    }
  }
  .login-box {
    display: flex;
    align-items: flex-start;
    border-radius: 3px;
    height: 408px;
    .login-input {
      width: 448px;
      border-radius: 16px;
      background-color: #fff;
      display: flex;
      flex-direction: column;
      padding: 46px 40px;
      box-shadow: 0px 2px 20px 1px rgba(79,79,79,0.1);
      .title {
        color: #333333;
        font-size: 22px;
        font-weight: bold;
        margin-bottom: 5px;
      }
      .desc {
        font-size: 14px;
        margin-bottom: 24px;
        color: #666666;
      }
    }
    .img-box{
      margin-right: 163px;
      position: relative;
      .img-title{
        position: absolute;
        width: 375px;
        top: -80px;
        left: 0;
        right: 0;
        margin: 0 auto;

      }
    }
    .el-input--prefix .el-input__inner{
      padding-left: 42px;
    }
    .el-input__prefix{
      display: flex;
      align-items: center;
      margin-left: 4px;
    }
  }
  .el-form-item__label{
    color: #333333;
  }
}

.login-form .el-input__inner {
  height: 40px!important;
}
</style>
