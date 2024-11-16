<template>
  <el-row>
    <el-form
      :label-position="labelPosition"
      :label-width="labelWidth"
      size="small"
      @submit.native.prevent
    >
      <MultiItemList
        v-if="labelPosition === 'top'"
        :label="label"
        :data="value"
        addText="添加脚本"
        @add="onEditEvent()"
        @edit="onEditEvent"
        @delete="onRemoveEvent"
        :prop="{
          label: 'showName',
          value: 'eventType'
        }"
      />
      <el-form-item v-else :label="label">
        <MultiItemBox
          :data="value"
          addText="添加脚本"
          @add="onEditEvent()"
          @edit="onEditEvent"
          @delete="onRemoveEvent"
          :prop="{
            label: 'showName',
            value: 'eventType'
          }"
        />
      </el-form-item>
    </el-form>
  </el-row>
</template>

<script>
import refreshDataMixins from '@/views/thirdParty/refreshDataMixins.js';
import MultiItemBox from '@/components/MultiItemBox/index.vue';
import MultiItemList from '@/components/MultiItemList/index.vue';
import EditCustomEvent from './editCustomEvent.vue';

export default {
  name: 'customEventSetting',
  props: {
    value: {
      type: Array
    },
    label: {
      type: String,
      default: '表单脚本'
    },
    labelPosition: {
      type: String,
      default: 'top'
    },
    labelWidth: {
      type: String,
      default: '110px'
    },
    allowEventList: {
      type: Array,
      default: () => []
    },
    formConfig: {
      type: Object,
      required: true
    },
    mode: {
      type: String,
      default: 'pc'
    }
  },
  mixins: [refreshDataMixins],
  components: {
    MultiItemBox,
    MultiItemList
  },
  methods: {
    refreshData(data) {
      if (data.path === 'thirdEditCustomEvent' && data.isSuccess) {
        this.handlerEditOperate(data.rowData, data.data);
      }
    },
    handlerEditOperate(row, res) {
      let temp = [];
      if (row == null) {
        temp = [...this.value, res];
      } else {
        temp = (this.value || []).map(item => {
          return row.eventType === item.eventType ? res : item;
        });
      }
      this.$emit('input', temp);
    },
    onEditEvent(row) {
      this.$dialog
        .show(
          row ? '编辑脚本' : '添加脚本',
          EditCustomEvent,
          {
            area: ['1200px', '792px']
          },
          {
            rowData: row,
            eventList: this.eventList,
            formConfig: this.formConfig,
            mode: this.mode,
            path: 'thirdEditCustomEvent'
          },
          {
            width: '1200px',
            height: '800px',
            pathName: '/thirdParty/thirdEditCustomEvent'
          }
        )
        .then(res => {
          this.handlerEditOperate(row, res);
        })
        .catch(e => {
          console.log(e);
        });
    },
    onRemoveEvent(row) {
      this.$confirm('是否移除此脚本？').then(res => {
        this.$emit(
          'input',
          (this.value || []).filter(item => {
            return item.eventType !== row.eventType;
          })
        );
      });
    }
  },
  computed: {
    eventList() {
      let usedList = (this.value || []).map(eventItem => eventItem.eventType);
      return this.allowEventList.map(item => {
        return {
          id: item,
          name: this.OnlineFormEventType.getValue(item),
          disabled: usedList.indexOf(item) !== -1
        };
      });
    }
  }
};
</script>

<style></style>
