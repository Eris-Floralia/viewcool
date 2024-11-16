<template>
  <MultiItemBox
    :data="value"
    addText="添加排序字段"
    :supportSort="true"
    @add="onEditTableColumn()"
    @edit="onEditTableColumn"
    @delete="onRemoveTableColumn"
    :prop="{
      label: 'showName',
      value: 'id'
    }"
  />
</template>

<script>
import refreshDataMixins from '@/views/thirdParty/refreshDataMixins.js';
import MultiItemBox from '@/components/MultiItemBox/index.vue';
import EditCustomListOrder from './editCustomListOrder.vue';

export default {
  name: 'OnlineCustomListOrderSetting',
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
      if (data.path === 'thirdEditOnlineListOrder' && data.isSuccess) {
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
      let tableList = this.formConfig().getTableWidgetTableList;
      // 非查询页面只能从组件绑定的表中选择
      if (this.formConfig().form.formType !== this.SysOnlineFormType.QUERY) {
        tableList = (tableList || []).filter(table => {
          return table.tableId === this.formConfig().currentWidget.bindData.tableId;
        });
      }
      this.$dialog
        .show(
          row ? '编辑排序字段' : '添加排序字段',
          EditCustomListOrder,
          {
            area: '500px'
          },
          {
            rowData: row,
            tableList: tableList,
            usedColumnList: this.value.map(item => item.columnId),
            path: 'thirdEditOnlineListOrder'
          },
          {
            width: '500px',
            height: '500px',
            pathName: '/thirdParty/thirdEditOnlineListOrder'
          }
        )
        .then(res => {
          this.handlerEditOperate(row, res);
        })
        .catch(e => {});
    },
    onRemoveTableColumn(row) {
      this.$confirm('是否删除此排序字段？')
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
