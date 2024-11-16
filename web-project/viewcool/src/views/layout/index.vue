<template>
  <el-container :style="getMainStyle" :class="'container-' + defaultFormItemSize">
    <el-container style="background-color: #F5F8F9">
      <el-header class="header" style="padding: 0;" :class="getMultiColumn ? 'multi-column-header' : ''">
        <div class="logo has-multiColumn" v-if="getMultiColumn">
          <img src="@/assets/img/logo_white.png" alt="">
        </div>
        <div class="header-main">
          <div class="logo" v-if="!getMultiColumn" style="margin-right: 8px;padding-left: 8px">
            <img src="@/assets/img/login_logo.png" alt="">
          </div>
          <div class="title">
            {{ projectName }}
          </div>
          <breadcrumb class="breadcrumb-container" />
          <div class="header-menu" style="flex-grow: 1;">
            <img :src="getHeadImageUrl ? getHeadImageUrl : header" class="header-img" />
            <el-dropdown class="user-dropdown" trigger="click" @command="handleCommand">
              <span class="el-dropdown-link">{{(getUserInfo || {}).showName}}<i class="el-icon-arrow-down el-icon--right"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item class="user-dropdown-item" command="modifyPassword">修改密码</el-dropdown-item>
                <el-dropdown-item class="user-dropdown-item" command="modifyHeadImage">修改头像</el-dropdown-item>
                <el-dropdown-item class="user-dropdown-item" command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      <el-main class="layout-main">
        <el-aside :width='getCollapse ? (getMultiColumn ? "80px" : "64px") : (getMultiColumn ? "280px" : "204px")' class="sidebar">
          <side-bar style="overflow: hidden"></side-bar>
        </el-aside>
        <div class="layout-content">
          <div class="tag-wrap" v-if="getMultiTags">
            <i class="online-icon" :class="getCollapse ? 'icon-expand' : 'icon-unexpand'"
              style="font-size: 16px; color: #333333; cursor: pointer;"
              @click="toggleSideBar"
            />
            <tag-panel :tagList="getTagList" />
          </div>
          <el-scrollbar :style="getContextStyle" wrap-class="scrollbar_dropdown__wrap">
            <transition name="fade" mode="out-in">
              <keep-alive :include="getCachePages">
                <router-view class="page-box" style="margin: 16px; overflow: hidden;" :style="getRouterViewStyle" />
              </keep-alive>
            </transition>
          </el-scrollbar>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import SideBar from './components/sidebar/sidebar.vue';
import { mapGetters, mapMutations } from 'vuex';
/* eslint-disable-next-line */
import { uploadMixin, statsDateRangeMixin } from '@/core/mixins';
import Breadcrumb from './components/breadcrumb';
import TagPanel from './components/tags/tagPanel.vue';
import formModifyPassword from './components/formModifyPassword/index.vue';
import formModifyHeadImage from './components/formModifyHeadImage/index.vue';
import { SystemController } from '@/api';
import { getToken, setToken } from '@/utils';
import projectConfig from '@/core/config';

export default {
  data () {
    return {
      projectName: projectConfig.projectName,
      header: require('../../assets/img/default-header.jpg')
    };
  },
  components: {
    'side-bar': SideBar,
    'breadcrumb': Breadcrumb,
    'tag-panel': TagPanel
  },
  mixins: [uploadMixin, statsDateRangeMixin],
  methods: {
    toggleSideBar () {
      this.setCollapse(!this.getCollapse);
    },
    onColumnChange (columnId) {
      this.setCurrentColumnId(columnId);
      this.clearCachePage();
      this.$router.replace({
        name: 'welcome'
      });
    },
    resetDocumentClientHeight () {
      let timerID;
      let _this = this;
      return function () {
        clearTimeout(timerID);
        timerID = setTimeout(() => {
          var h = document.documentElement['clientHeight'];
          var w = document.documentElement['clientWidth'];
          _this.setClientHeight(h);
          _this.setClientWidth(w);
        }, 50);
      }
    },
    handleCommand (command) {
      if (command === 'logout') {
        this.$confirm('是否退出登录？', '', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          let options = {
            headers: {
              Authorization: getToken()
            },
            showMask: false
          }
          SystemController.logout(this, {}, options).catch(e => {});
          this.clearAllTags();
          setToken();
          window.sessionStorage.removeItem('isUaaLogin');
          this.$router.replace({name: 'login'});
        }).catch(e => {});
      } else if (command === 'modifyPassword') {
        this.$dialog.show('修改密码', formModifyPassword, {
          area: ['500px']
        }, {}).catch(e => {});
      } else if (command === 'modifyHeadImage') {
        this.$dialog.show('修改头像', formModifyHeadImage, {
          area: ['500px']
        }, {}).catch(e => {});
      }
    },
    jumpTo (menuItem) {
      if (menuItem != null) {
        // 路由菜单
        if (menuItem.bindType === this.SysMenuBindType.ROUTER && menuItem.formRouterName != null && menuItem.formRouterName !== '') {
          this.$router.replace({
            name: menuItem.formRouterName
          });
          return;
        }
        // 外部链接
        if (menuItem.bindType === this.SysMenuBindType.THRID_URL && menuItem.targetUrl != null && menuItem.targetUrl !== '') {
          let token = getToken();
          let targetUrl = menuItem.targetUrl;
          if (targetUrl.indexOf('?') === -1) {
            targetUrl = targetUrl + '?';
          }
          targetUrl = targetUrl + 'token=' + token;
          window.open(targetUrl);
          return;
        }
      }
      if (this.$route.name !== 'welcome') {
        this.$router.replace({
          name: 'welcome'
        });
      }
    },
    ...mapMutations([
      'setClientHeight',
      'setClientWidth',
      'setCurrentColumnId',
      'clearCachePage',
      'clearAllTags',
      'setUserInfo',
      'setMenuList',
      'setCollapse'
    ])
  },
  computed: {
    getMainStyle () {
      return [
        {'height': this.getClientHeight + 'px'}
      ]
    },
    getContextStyle () {
      return [
        {'height': this.getMainContextHeight + 'px'}
      ]
    },
    getRouterViewStyle () {
      return [
        {'min-height': this.getMainContextHeight - 32 + 'px'}
      ]
    },
    getHeadImageUrl () {
      if (this.getUserInfo && this.getUserInfo.headImageUrl != null && this.getUserInfo.headImageUrl !== '') {
        let temp = this.getUploadFileUrl(this.getUserInfo.headImageUrl, { filename: this.getUserInfo.headImageUrl.filename });
        return temp;
      } else {
        return null;
      }
    },
    ...mapGetters([
      'getMultiTags',
      'getClientHeight',
      'getUserInfo',
      'getCollapse',
      'getCachePages',
      'getTagList',
      'getMultiColumn',
      'getCurrentColumnId',
      'getColumnList',
      'getMenuItem',
      'getMainContextHeight',
      'getCollapse'
    ])
  },
  mounted () {
    let resetHeight = this.resetDocumentClientHeight();
    resetHeight();

    window.onresize = () => {
      resetHeight();
    }

    // 重新获取登录信息
    if (getToken() != null && getToken() !== '' && this.getUserInfo == null) {
      SystemController.getLoginInfo(this, {}).then(data => {
        this.setMenuList(data.data.menuList);
        delete data.data.menuList;
        this.setUserInfo(data.data);
      }).catch(e => {});
    }
  },
  watch: {
    getMenuItem: {
      handler (newValue) {
        this.jumpTo(newValue);
      },
      immediate: true
    }
  }
}
</script>

<style lang="scss">
@import '@/assets/style/element-variables.scss';
  .header{
    z-index: 1;
    .header-main{
      flex:1;
      display: flex;
      align-items: center;
      box-shadow: 0px 2px 10px 1px rgba(65,64,133,0.1);
      padding-left: 4px;
      .title{
        width: 144px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
    .logo{
      padding-left: 16px;
      img{
        width: 40px;
      }
    }
    .has-multiColumn{
      width: 80px;
      height: 60px;
      padding-left: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #FF7700;
      img{
        width: 36px;
      }
    }
    .title{
      font-size: 22px;
      font-weight: bold;
      color: #434344;
    }

  }
  .sidebar{
    transition: 0.3s;
  }
  .el-menu{
    background-color: #2D3039 !important;
  }
  .layout-main{
    display: flex !important;
    .layout-content{
      width: 0px;
      flex: 1;
    }
    .tag-wrap {
      display: flex;
      align-items: center;
      background-color: white;
      padding: 0 25px;
      .collapse-img {
        padding: 4px;
        background-color: #F6F7F9;
        border-radius: 4px;
        cursor: pointer;
        margin-right: 12px;
        img{
          vertical-align: middle;
        }
      }

      .collapse {
        transform: rotate(180deg);
      }
    }
  }
  .header-menu{
    align-items: center;
    padding-right: 45px;
    .header-img{
      width: 31px;
      height: 31px;
      margin-right: 8px;
    }
    .line{
      display: inline-block;
      width: 1px;
      height: 24px;
      background-color: #E8E8E8;
      vertical-align: middle;
    }
  }
  .user-dropdown{
    color: #333333 !important;
    .el-icon-arrow-down{
      color: #333333;
    }
  }
  .code-generation-btn{
    margin-right: 20px !important;
    padding: 0 !important;
    width: 82px;
    height: 32px;
    display: flex;
    justify-content: center;
    align-items: center;
    border-color: $--color-primary !important;
    color: $--color-primary !important;
    span{
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      img{
        margin-right: 4px;
      }
    }
  }
  .multi-column-header .header-main{
    padding-left: 16px;
    .title{
      width: 185px;
    }
  }
  .page-box{
    display: flex!important;
    flex-direction: column;
  }
</style>
