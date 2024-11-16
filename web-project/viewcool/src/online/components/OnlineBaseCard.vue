<template>
  <el-card
    class="online-base-card form-card base-card"
    :shadow="widget.props.shadow"
    :body-style="{ padding: (widget.props.padding == null ? 15 : widget.props.padding) + 'px' }"
    :style="{
      'margin-bottom':
        (widget.props.paddingBottom || (widget.props.basicInfo || {}).paddingBottom || 0) + 'px'
    }"
  >
    <div slot="header" class="base-card-header table-draggable">
      <span>{{ widget.showName }}</span>
      <div class="base-card-operation"></div>
    </div>
    <el-row>
      <el-col :span="24">
        <el-row :gutter="form().gutter">
          <Draggable
            draggable=".custom-widget-item"
            :list="widget.childWidgetList"
            group="componentsGroup"
            :style="getDrableBoxStyle"
            style="overflow: hidden; width: 100%; position: relative"
            :disabled="!isEdit"
            :move="onDragMove"
          >
            <template
              v-if="Array.isArray(widget.childWidgetList) && widget.childWidgetList.length > 0"
            >
              <el-col
                class="custom-widget-item"
                :class="{ active: isEdit && form().isActive(subWidget) }"
                v-for="subWidget in widget.childWidgetList"
                :key="subWidget.variableName"
                :span="subWidget.props.span"
              >
                <div
                  class="widget-item"
                  :class="{ active: isEdit && form().isActive(subWidget) }"
                  v-if="form().getWidgetVisible(subWidget)"
                >
                  <div
                    v-if="subWidget.widgetType === SysCustomWidgetType.Table"
                    :style="getTableStyle(subWidget)"
                    style="margin-bottom: 18px"
                  >
                    <OnlineCardTable
                      :widget="subWidget"
                      :value="form().getTableData(subWidget)"
                      @input="dataList => form().setTableData(subWidget, dataList)"
                      @click.native.stop="onWidgetClick(subWidget)"
                    />
                  </div>
                  <div
                    v-else-if="subWidget.widgetType === SysCustomWidgetType.Block"
                    :style="getBlockStyle(subWidget)"
                    @click.stop="onWidgetClick(subWidget)"
                  >
                    <OnlineCustomBlock
                      v-model="subWidget.childWidgetList"
                      :isEdit="isEdit"
                      @widgetClick="onWidgetClick"
                    />
                  </div>
                  <div
                    v-else-if="subWidget.widgetType === SysCustomWidgetType.Card"
                    @click.stop="onWidgetClick(subWidget)"
                  >
                    <OnlineBaseCard
                      :widget="subWidget"
                      :isEdit="isEdit"
                      @widgetClick="onWidgetClick"
                    />
                  </div>
                  <div
                    v-else-if="subWidget.widgetType === SysCustomWidgetType.Tabs"
                    @click.stop="onWidgetClick(subWidget)"
                  >
                    <OnlineCustomTabs
                      :widget="subWidget"
                      :isEdit="isEdit"
                      @widgetClick="onWidgetClick"
                    />
                  </div>
                  <div
                    v-else-if="subWidget.widgetType === SysCustomWidgetType.Text"
                    @click.stop="onWidgetClick(subWidget)"
                  >
                    <OnlineCustomWidget
                      :ref="subWidget.variableName"
                      :widget="subWidget"
                      :isEdit="isEdit"
                      :value="getWidgetValue(subWidget) || subWidget.props.text"
                      :style="{ 'margin-bottom': (subWidget.props.paddingBottom || 0) + 'px' }"
                      @widgetClick="onWidgetClick"
                    />
                  </div>
                  <div
                    v-else-if="subWidget.widgetType === SysCustomWidgetType.Image"
                    @click.stop="onWidgetClick(subWidget)"
                  >
                    <OnlineCustomWidget
                      :ref="subWidget.variableName"
                      :widget="subWidget"
                      :isEdit="isEdit"
                      :value="getWidgetValue(subWidget)"
                      :src="subWidget.props.src"
                      :style="{ 'margin-bottom': (subWidget.props.paddingBottom || 0) + 'px' }"
                      @widgetClick="onWidgetClick"
                    />
                  </div>
                  <el-form-item
                    v-else
                    :label="subWidget.showName"
                    :prop="subWidget.propString"
                    @click.native.stop="onWidgetClick(subWidget)"
                  >
                    <OnlineCustomWidget
                      :widget="subWidget"
                      :value="getWidgetValue(subWidget)"
                      @input="val => onValueChange(subWidget, val)"
                      @change="(val, dictData) => onWidgetValueChange(subWidget, val, dictData)"
                    />
                  </el-form-item>
                  <ActiveWidgetMenu
                    v-if="isEdit && form().isActive(subWidget)"
                    :widget="subWidget"
                    :clone="form().cloneWidget"
                    @copy="onCopyWidget"
                    @delete="onDeleteWidget(subWidget)"
                  />
                </div>
              </el-col>
            </template>
            <div v-else-if="isEdit" class="info">
              <div style="height: 100px; width: 100px">
                <i class="el-icon-upload" />
              </div>
              <span>请拖入组件进行编辑</span>
            </div>
          </Draggable>
        </el-row>
      </el-col>
    </el-row>
  </el-card>
</template>

<script>
import Draggable from 'vuedraggable';
import OnlineCustomWidget from './OnlineCustomWidget.vue';
import OnlineCardTable from './OnlineCardTable.vue';
import ActiveWidgetMenu from './ActiveWidgetMenu.vue';

export default {
  name: 'baseCard',
  props: {
    widget: {
      type: Object,
      required: true
    },
    // 是否表单编辑模式
    isEdit: {
      type: Boolean,
      default: false
    },
    // 是否显示边框
    showBorder: {
      type: Boolean,
      default: true
    },
    height: {
      type: String
    }
  },
  components: {
    Draggable,
    OnlineCustomWidget,
    OnlineCardTable,
    ActiveWidgetMenu
  },
  inject: ['form'],
  data() {
    return {};
  },
  methods: {
    onDragMove(e) {
      // 容器组件不能改变位置
      let widgetType = e.relatedContext.element ? e.relatedContext.element.widgetType : undefined;
      return (
        widgetType !== this.SysCustomWidgetType.Block &&
        widgetType !== this.SysCustomWidgetType.Card
      );
    },
    onCopyWidget(widget) {
      this.widget.childWidgetList.push(widget);
    },
    onDeleteWidget(widget) {
      this.$confirm('是否删除此组件？')
        .then(res => {
          this.widget.childWidgetList = this.widget.childWidgetList.filter(item => item !== widget);
          this.onWidgetClick(null);
        })
        .catch(e => {});
    },
    getWidgetValue(widget) {
      return this.form().getWidgetValue(widget);
    },
    onValueChange(widget, value) {
      return this.form().onValueChange(widget, value);
    },
    onWidgetValueChange(widget, value, dictData) {
      return this.form().onWidgetValueChange(widget, value, dictData);
    },
    getBlockStyle(widget) {
      return {
        'margin-bottom': (widget.props.paddingBottom || 0) + 'px',
        padding: this.isEdit ? '5px' : undefined,
        border: this.isEdit ? '1px solid #e8eaec' : undefined
      };
    },
    getTableStyle(widget) {
      if (widget.widgetType !== this.SysCustomWidgetType.Table) return;
      return {};
    },
    onWidgetClick(widget) {
      this.$emit('widgetClick', widget);
    }
  },
  computed: {
    getDrableBoxStyle() {
      let tempHeight = this.height;
      if (this.height == null || this.height === '') {
        tempHeight = this.isEdit && this.widget.childWidgetList.length <= 0 ? '150px' : '0px';
      }
      return {
        'min-height': tempHeight
      };
    }
  }
};
</script>

<style scoped>
.info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: absolute;
  width: 100%;
  height: 100%;
  text-align: center;
  vertical-align: middle;
  color: #999999;
}

.info div {
  width: 80px;
  height: 80px;
  border: 1px dashed #d9dbdd;
  line-height: 100px;
  text-align: center;
  border-radius: 6px;
  font-size: 60px;
}
.info span {
  margin-top: 10px;
  font-size: 16px;
}
</style>
