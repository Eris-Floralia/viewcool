<template>
  <MultiItemBox
    :data="value"
    addText="添加快捷选项"
    :supportSort="true"
    @add="onEditTableColumn()"
    @edit="onEditTableColumn"
    @delete="onRemoveTableColumn"
    :prop="{
      label: 'name',
      value: 'id'
    }"
  />
</template>

<script>
import refreshDataMixins from '@/views/thirdParty/refreshDataMixins.js';
import MultiItemBox from '@/components/MultiItemBox/index.vue';
import EditNumberRangeQuick from './editNumberRangeQuick.vue';

export default {
  name: 'OnlineMobieNumberRangeQuickSelectSetting',
  props: {
    value: {
      type: Array
    }
  },
  components: {
    MultiItemBox
  },
  inject: ['formConfig'],
  mixins: [refreshDataMixins],
  methods: {
    refreshData(data) {
      if (data.path === 'thirdEditOnlineMobileNumberRangeQuick' && data.isSuccess) {
        this.handlerEditOperate(data.rowData, data.data);
      }
    },
    handlerEditOperate(row, res) {
      let tempList = [];
      if (row == null) {
        tempList = this.value || [];
        tempList.push(res);
      } else {
        tempList = (this.value || []).map(item => {
          return item.id === res.id ? res : item;
        });
      }
      this.$emit('input', tempList);
    },
    onEditTableColumn(row) {
      this.$dialog
        .show(
          row ? '编辑快捷选项' : '添加快捷选项',
          EditNumberRangeQuick,
          {
            area: '500px'
          },
          {
            rowData: row,
            path: 'thirdEditOnlineMobileNumberRangeQuick'
          },
          {
            width: '500px',
            height: '500px',
            pathName: '/thirdParty/thirdEditOnlineMobileNumberRangeQuick'
          }
        )
        .then(res => {
          this.handlerEditOperate(row, res);
        })
        .catch(e => {});
    },
    onRemoveTableColumn(row) {
      this.$confirm('是否删除此快捷选项？')
        .then(res => {
          this.$emit(
            'input',
            (this.value || []).filter(item => item !== row)
          );
        })
        .catch(e => {});
    }
  }
};
</script>

<style></style>
