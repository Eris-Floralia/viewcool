<template>
  <el-row class="flex-box" type="flex" ref="flexbox">
    <slot />
    <div class="search-box" v-if="hasSearch" :style="{ 'min-width': minMenuWidth + 'px' }">
      <el-button
        class="search-btn"
        type="default"
        :plain="true"
        :size="defaultFormItemSize"
        @click="search"
        icon="el-icon-search"
        >查询</el-button
      >
      <el-button
        v-if="hasReset"
        type="default"
        :plain="true"
        :size="defaultFormItemSize"
        @click="reset"
        style="width: 72px"
        >重置</el-button
      >
      <div style="float: right">
        <slot name="operation" />
      </div>
    </div>
  </el-row>
</template>

<script>
export default {
  name: 'FilterBox',
  props: {
    /**
     * 每一个过滤项宽度（包含标题和输入框宽度总和）
     */
    itemWidth: {
      type: Number,
      required: true
    },
    /**
     * 每一项下间距
     */
    marginBottom: {
      type: String,
      default: '16px'
    },
    /**
     * 按钮块最小宽度默认350，当每一行剩余空间大于此值，按钮块将不会折行
     */
    minMenuWidth: {
      type: Number,
      default: 350
    },
    /**
     * 按钮位置，默认为end，可选值为start/end/center/space-around/space-between
     */
    operatorPosition: {
      type: String,
      default: 'end'
    },
    hasSearch: {
      type: Boolean,
      default: true
    },
    hasReset: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      tempDomWidth: undefined,
      placeholderNumber: 0,
      flexNumber: 1
    };
  },
  computed: {
    getMenuBoxStyle() {
      return {
        width: '100%',
        'margin-bottom': this.marginBottom,
        'align-items': 'center'
      };
    }
  },
  mounted() {
    console.log(this.$slots.default);
  },
  methods: {
    search() {
      this.$emit('search');
    },
    reset() {
      this.$emit('reset');
    }
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/style/element-variables.scss';
.flex-box {
  margin-bottom: 16px;
  background-color: white;
  padding: 16px 24px 0px;
  .search-btn {
    color: $--color-primary;
    border-color: $--color-primary;
    &:hover {
      background-color: $--color-primary-light-9;
    }
  }
  ::v-deep .el-form-item {
    margin-bottom: 16px;
    margin-right: 8px;
  }
  .extend-box {
    img {
      cursor: pointer;
      margin-left: 8px;
    }
  }
  .search-box {
    padding-left: 8px;
    margin-bottom: 16px;
    flex-grow: 1;
    flex-shrink: 0;
  }
}
</style>
