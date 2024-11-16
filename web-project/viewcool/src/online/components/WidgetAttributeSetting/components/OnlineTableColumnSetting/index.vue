<template>
  <MultiItemBox
    :data="value"
    addText="添加表格列"
    :supportSort="true"
    @add="onEditTableColumn()"
    @edit="onEditTableColumn"
    @delete="onRemoveTableColumn"
    :prop="{
      label: 'showName',
      value: 'columnId'
    }"
  >
  </MultiItemBox>
</template>

<script>
import refreshDataMixins from '@/views/thirdParty/refreshDataMixins.js';
import MultiItemBox from '@/components/MultiItemBox/index.vue';
import EditOnlineTableColumn from './editOnlineTableColumn.vue';

export default {
  name: 'onlineTableColumnSetting',
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
      if (data.path === 'thirdEditOnlineTableColumn' && data.isSuccess) {
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
          return item.columnId === res.columnId ? res : item;
        });
      }
      this.$emit('input', tempList);
    },
    onEditTableColumn(row) {
      let tableList = this.formConfig().getTableWidgetTableList;
      // 非查询页面只能从组件绑定的表中选择
      if (
        this.formConfig().form.formType !== this.SysOnlineFormType.QUERY &&
        this.formConfig().form.formType !== this.SysOnlineFormType.ADVANCE_QUERY
      ) {
        tableList = (tableList || []).filter(table => {
          return table.tableId === this.formConfig().currentWidget.bindData.tableId;
        });
      }
      this.$dialog
        .show(
          row ? '编辑字段' : '添加字段',
          EditOnlineTableColumn,
          {
            area: '600px'
          },
          {
            rowData: row,
            tableList: tableList,
            usedColumnList: this.value.map(item => item.columnId),
            path: 'thirdEditOnlineTableColumn'
          },
          {
            width: '600px',
            height: '500px',
            pathName: '/thirdParty/thirdEditOnlineTableColumn'
          }
        )
        .then(res => {
          this.handlerEditOperate(row, res);
        })
        .catch(e => {});
    },
    onRemoveTableColumn(row) {
      this.$confirm('是否删除此表格列？')
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
