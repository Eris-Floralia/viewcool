<template>
  <MultiItemBox
    :data="(formConfig().currentWidget || {}).childWidgetList"
    addText="添加标签页"
    @add="onEditTabPanel()"
    @edit="onEditTabPanel"
    @delete="onRemoveTabPanel"
    :prop="{
      label: 'showName',
      value: 'variableName'
    }"
  />
</template>

<script>
import MultiItemBox from '@/components/MultiItemBox/index.vue';
import EditOnlineTabPanel from './editOnlineTabPanel.vue';
import blockConfig from '@/online/config/customBlock.js';
import refreshDataMixins from '@/views/thirdParty/refreshDataMixins.js';

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
  mixins: [refreshDataMixins],
  inject: ['formConfig'],
  methods: {
    refreshData(data) {
      if (data.path === 'thirdEditOnlineTabPanel' && data.isSuccess) {
        this.handlerEditOperate(data.rowData, data.data);
      }
    },
    handlerEditOperate(row, res) {
      if (row == null) {
        res = this.createTabPanel(res);
        this.formConfig().currentWidget.childWidgetList.push(res);
      } else {
        this.formConfig().currentWidget.childWidgetList =
          this.formConfig().currentWidget.childWidgetList.map(item => {
            return item.variableName === row.variableName ? res : item;
          });
      }
    },
    onEditTabPanel(row) {
      this.$dialog
        .show(
          row ? '编辑标签页' : '添加标签页',
          EditOnlineTabPanel,
          {
            area: '600px'
          },
          {
            rowData: row,
            tabPanelList: (this.formConfig().currentWidget.childWidgetList || [])
              .filter(item => {
                return row != null ? item.variableName !== row.variableName : true;
              })
              .map(item => item.variableName),
            path: 'thirdEditOnlineTabPanel'
          },
          {
            width: '600px',
            height: '500px',
            pathName: '/thirdParty/thirdEditOnlineTabPanel'
          }
        )
        .then(res => {
          this.handlerEditOperate(row, res);
        })
        .catch(e => {
          console.log(e);
        });
    },
    onRemoveTabPanel(row) {
      this.$confirm('是否删除此标签页？')
        .then(res => {
          this.formConfig().currentWidget.childWidgetList =
            this.formConfig().currentWidget.childWidgetList.filter(item => {
              return item.variableName !== row.variableName;
            });
        })
        .catch(e => {});
    },
    createTabPanel(res) {
      let temp = this.formConfig().getWidgetObject(blockConfig);
      temp.showName = res.showName;
      temp.variableName = res.variableName;
      this.$set(temp, 'relation', undefined);
      this.$set(temp, 'datasource', undefined);
      this.$set(temp, 'column', undefined);

      return temp;
    }
  }
};
</script>

<style></style>
