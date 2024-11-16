<template>
  <div ref="formWrapper" class="form-wrapper">
    <el-row>
      <div>没有支持在线表单功能，无法使用！</div>
    </el-row>
  </div>
</template>

<script>
import thirdPartyMixin from '@/views/thirdParty/thirdPartyMixin.js';

export default {
  name: 'onlineCustomDataSelectDlg',
  props: {
    value: {
      type: String
    },
    relativeTable: {
      type: Object
    },
    formConfig: {
      type: Object
    }
  },
  mixins: [thirdPartyMixin],
  components: {
  },
  data () {
    return {
      dialogSelectItems: {},
      tableHeight: ''
    }
  },
  computed: {
    dialogParams () {
      return {
        value: this.value || this.thirdParams.value,
        formConfig: this.formConfig || this.thirdParams.formConfig,
        relativeTable: this.relativeTable || this.thirdParams.relativeTable || {}
      }
    }
  },
  methods: {
    onCancel (isSuccess = false) {
      if (this.observer != null) {
        this.observer.cancel(isSuccess, this.dialogSelectItems);
      } else {
        this.onCloseThirdDialog(isSuccess, this.thirdParams.path, this.dialogParams.value, this.dialogSelectItems);
      }
    },
    onSubmit () {
      this.onCancel(true);
    },
    onRadioChange (data) {
      this.dialogSelectItems = data;
    }
  },
  mounted () {
    this.$nextTick(() => {
      this.tableHeight = (this.$refs.formWrapper.offsetHeight) + 'px';
    })
  }
}
</script>

<style lang="scss" scoped>
.form-wrapper{
  position:relative;
  height:100%
}
.form-footer{
  width:100%;
  text-align:right;
  position:absolute;
  bottom:0px;

  .btn-confirm{
    height: 28px;
    display:inline-block
  }
}
</style>
