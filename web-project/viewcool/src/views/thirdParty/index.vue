<template>
  <el-row class="third-party">
    <router-view
      style="background-color: white; min-height: 100vh; display: flex; flex-direction: column;"
      :style="{padding: (dlgFullScreen == '1' ? '0px' : '20px')}"
    />
  </el-row>
</template>

<script>
import { setAppId, setToken } from '@/utils';
import { mapMutations } from 'vuex';

export default {
  name: 'thirdParty',
  props: {
    appId: {
      type: String
    },
    token: {
      type: String
    },
    dlgFullScreen: {
      type: String
    }
  },
  methods: {
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
    ...mapMutations(['setClientHeight', 'setClientWidth'])
  },
  mounted () {
    let resetHeight = this.resetDocumentClientHeight();
    resetHeight();
    
    window.onresize = () => {
      resetHeight();
    }
  },
  watch: {
    appId: {
      handler (appId) {
        setAppId(appId);
      },
      immediate: true
    },
    token: {
      handler (token) {
        setToken(token);
      },
      immediate: true
    }
  }
}
</script>

<style>
</style>
