<template>
  <el-form
    label-position="top"
    size="small"
    v-if="pagerSetting"
    style="padding: 10px 16px 0px"
    @submit.native.prevent
  >
    <el-form-item class="view-attribute-item" label="是否显示">
      <el-switch v-model="pagerSetting.show" />
    </el-form-item>
    <el-form-item class="view-attribute-item" label="尺寸">
      <el-select v-model="pagerSetting.size" placeholder="">
        <el-option label="默认" value="default" />
        <el-option label="小" value="small" />
      </el-select>
    </el-form-item>
    <el-form-item class="view-attribute-item" label="样式">
      <el-select v-model="pagerSetting.background" placeholder="">
        <el-option label="按钮" value="1" />
        <el-option label="文字" value="0" />
      </el-select>
    </el-form-item>
    <el-form-item class="view-attribute-item" label="对齐方式">
      <el-radio-group v-model="pagerSetting.align">
        <el-radio-button label="left">居左</el-radio-button>
        <el-radio-button label="center">居中</el-radio-button>
        <el-radio-button label="right">居右</el-radio-button>
      </el-radio-group>
    </el-form-item>
  </el-form>
</template>

<script>
export default {
  name: 'dateViewTablePagerSetting',
  props: {
    fieldName: {
      type: String,
      default: 'pagerSetting'
    }
  },
  inject: ['formConfig'],
  data() {
    return {
      layouts: [
        // { label: '切换页数', name: 'Sizes'},
        // { label: '总页数', name: 'SizTotales'},
        // { label: '跳到首页', name: 'PrevJump'},
        // { label: '上一页', name: 'PrevPage'},
        // { label: '跳转到', name: 'JumpNumber'},
        // { label: '下一页', name: 'NextPage'},
        // { label: '跳到末页', name: 'NextJump'},
        // { label: '跳转', name: 'FullJump'}
      ]
    };
  },
  computed: {
    widget() {
      return this.formConfig().currentWidget;
    },
    pagerSetting() {
      let props = (this.widget || {}).props;
      if (props) {
        return props[this.fieldName];
      } else {
        return {};
      }
    }
  }
};
</script>

<style></style>
