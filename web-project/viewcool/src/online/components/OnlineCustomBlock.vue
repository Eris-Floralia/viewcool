<template>
  <el-row class="online-custom-block">
    <el-col :span="24">
      <el-row :gutter="form().gutter">
        <Draggable
          class="custom-block-draggable"
          draggable=".custom-widget-item"
          :list="value"
          group="componentsGroup"
          ghostClass="ghost"
          chosenClass="chosen"
          :style="getDrableBoxStyle"
          style="overflow: hidden; width: 100%; position: relative"
          :disabled="!isEdit"
          :move="onDragMove"
          @add="onDragAdd"
        >
          <template v-if="Array.isArray(value) && value.length > 0">
            <el-col
              class="custom-widget-item"
              :class="{ active: isEdit && form().isActive(subWidget) }"
              v-for="subWidget in value"
              :key="subWidget.variableName"
              :span="subWidget.props.span || (subWidget.props.basicInfo || {}).span"
            >
              <div
                class="widget-item"
                :class="{ active: isEdit && form().isActive(subWidget) }"
                v-if="form().getWidgetVisible(subWidget)"
                @click.stop="onWidgetClick(subWidget)"
              >
                <div
                  v-if="subWidget.widgetType === SysCustomWidgetType.Table"
                  :style="getTableStyle(subWidget)"
                >
                  <OnlineCardTable
                    :widget="subWidget"
                    :ref="subWidget.variableName"
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
                    :ref="subWidget.variableName"
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
                    :ref="subWidget.variableName"
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
                    :ref="subWidget.variableName"
                    :widget="subWidget"
                    :isEdit="isEdit"
                    @widgetClick="onWidgetClick"
                  />
                </div>
                <div
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.TableContainer"
                  :style="{ 'margin-bottom': (subWidget.props.paddingBottom || 0) + 'px' }"
                  @click.stop="onWidgetClick(subWidget)"
                >
                  <OnlineCustomTableContainer
                    :ref="subWidget.variableName"
                    :widget="subWidget"
                    :isEdit="isEdit"
                    @widgetClick="onWidgetClick"
                  />
                </div>
                <div
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.CellGroup"
                  :style="{
                    'margin-bottom': (subWidget.props.paddingBottom || 0) + 'px',
                    'margin-top': (subWidget.props.paddingTop || 0) + 'px',
                    background: '#F6F7F9'
                  }"
                  @click.stop="onWidgetClick(subWidget)"
                >
                  <OnlineCustomCellGroup
                    :ref="subWidget.variableName"
                    :widget="subWidget"
                    :isEdit="isEdit"
                    @widgetClick="onWidgetClick"
                  />
                </div>
                <div
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.LineChart"
                  :style="getChartStyle(subWidget)"
                  @click.stop="onWidgetClick(subWidget)"
                >
                  <OnlineCustomWidget
                    :ref="subWidget.variableName"
                    :widget="subWidget"
                    :isEdit="isEdit"
                    :value="getWidgetValue(subWidget)"
                    @input="val => onValueChange(subWidget, val)"
                    @change="(val, detail) => onWidgetValueChange(subWidget, val, detail)"
                    @widgetClick="onWidgetClick"
                  />
                </div>
                <div
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.BarChart"
                  :style="getChartStyle(subWidget)"
                  @click.stop="onWidgetClick(subWidget)"
                >
                  <OnlineCustomWidget
                    :ref="subWidget.variableName"
                    :widget="subWidget"
                    :isEdit="isEdit"
                    :value="getWidgetValue(subWidget)"
                    @input="val => onValueChange(subWidget, val)"
                    @change="(val, detail) => onWidgetValueChange(subWidget, val, detail)"
                    @widgetClick="onWidgetClick"
                  />
                </div>
                <div
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.PieChart"
                  :style="getChartStyle(subWidget)"
                  @click.stop="onWidgetClick(subWidget)"
                >
                  <OnlineCustomWidget
                    :ref="subWidget.variableName"
                    :widget="subWidget"
                    :isEdit="isEdit"
                    :value="getWidgetValue(subWidget)"
                    @input="val => onValueChange(subWidget, val)"
                    @change="(val, detail) => onWidgetValueChange(subWidget, val, detail)"
                    @widgetClick="onWidgetClick"
                  />
                </div>
                <div
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.ScatterChart"
                  :style="getChartStyle(subWidget)"
                  @click.stop="onWidgetClick(subWidget)"
                >
                  <OnlineCustomWidget
                    :ref="subWidget.variableName"
                    :widget="subWidget"
                    :isEdit="isEdit"
                    :value="getWidgetValue(subWidget)"
                    @input="val => onValueChange(subWidget, val)"
                    @change="(val, detail) => onWidgetValueChange(subWidget, val, detail)"
                    @widgetClick="onWidgetClick"
                  />
                </div>
                <div
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.PivotTable"
                  :style="getChartStyle(subWidget)"
                  @click.stop="onWidgetClick(subWidget)"
                >
                  <OnlineCustomWidget
                    :ref="subWidget.variableName"
                    :widget="subWidget"
                    :isEdit="isEdit"
                    :value="undefined"
                    @widgetClick="onWidgetClick"
                  />
                </div>
                <div
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.DataViewTable"
                  :style="getChartStyle(subWidget)"
                  @click.stop="onWidgetClick(subWidget)"
                >
                  <OnlineCustomWidget
                    :ref="subWidget.variableName"
                    :widget="subWidget"
                    :isEdit="isEdit"
                    :value="undefined"
                    @widgetClick="onWidgetClick"
                  />
                </div>
                <div
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.Carousel"
                  :style="getChartStyle(subWidget)"
                  @click.stop="onWidgetClick(subWidget)"
                >
                  <OnlineCustomWidget
                    :ref="subWidget.variableName"
                    :widget="subWidget"
                    :isEdit="isEdit"
                    :value="undefined"
                    @widgetClick="onWidgetClick"
                  />
                </div>
                <div
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.RichText"
                  :style="getChartStyle(subWidget)"
                  @click.stop="onWidgetClick(subWidget)"
                >
                  <OnlineCustomWidget
                    :ref="subWidget.variableName"
                    :widget="subWidget"
                    :isEdit="isEdit"
                    :value="undefined"
                    @widgetClick="onWidgetClick"
                  />
                </div>
                <div
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.GaugeChart"
                  :style="getChartStyle(subWidget)"
                  @click.stop="onWidgetClick(subWidget)"
                >
                  <OnlineCustomWidget
                    :ref="subWidget.variableName"
                    :widget="subWidget"
                    :isEdit="isEdit"
                    :value="undefined"
                    @widgetClick="onWidgetClick"
                  />
                </div>
                <div
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.RadarChart"
                  :style="getChartStyle(subWidget)"
                  @click.stop="onWidgetClick(subWidget)"
                >
                  <OnlineCustomWidget
                    :ref="subWidget.variableName"
                    :widget="subWidget"
                    :isEdit="isEdit"
                    :value="undefined"
                    @widgetClick="onWidgetClick"
                  />
                </div>
                <div
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.FunnelChart"
                  :style="getChartStyle(subWidget)"
                  @click.stop="onWidgetClick(subWidget)"
                >
                  <OnlineCustomWidget
                    :ref="subWidget.variableName"
                    :widget="subWidget"
                    :isEdit="isEdit"
                    :value="undefined"
                    @widgetClick="onWidgetClick"
                  />
                </div>
                <div
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.ProgressBar"
                  :style="getChartStyle(subWidget)"
                  @click.stop="onWidgetClick(subWidget)"
                >
                  <OnlineCustomWidget
                    :ref="subWidget.variableName"
                    :widget="subWidget"
                    :isEdit="isEdit"
                    :value="undefined"
                    @widgetClick="onWidgetClick"
                  />
                </div>
                <div
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.ProgressCircle"
                  :style="getChartStyle(subWidget)"
                  @click.stop="onWidgetClick(subWidget)"
                >
                  <OnlineCustomWidget
                    :ref="subWidget.variableName"
                    :widget="subWidget"
                    :isEdit="isEdit"
                    :value="undefined"
                    @widgetClick="onWidgetClick"
                  />
                </div>
                <div
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.DataCard"
                  :style="getChartStyle(subWidget)"
                  @click.stop="onWidgetClick(subWidget)"
                >
                  <OnlineCustomWidget
                    :ref="subWidget.variableName"
                    :widget="subWidget"
                    :isEdit="isEdit"
                    :value="undefined"
                    @widgetClick="onWidgetClick"
                  />
                </div>
                <div
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.DataProgressCard"
                  :style="getChartStyle(subWidget)"
                  @click.stop="onWidgetClick(subWidget)"
                >
                  <OnlineCustomWidget
                    :ref="subWidget.variableName"
                    :widget="subWidget"
                    :isEdit="isEdit"
                    :value="undefined"
                    @widgetClick="onWidgetClick"
                  />
                </div>
                <div
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.CommonList"
                  :style="getChartStyle(subWidget)"
                  @click.stop="onWidgetClick(subWidget)"
                >
                  <OnlineCustomWidget
                    :ref="subWidget.variableName"
                    :widget="subWidget"
                    :isEdit="isEdit"
                    :value="undefined"
                    @widgetClick="onWidgetClick"
                  />
                </div>
                <OnlineCustomWidget
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.Text"
                  :ref="subWidget.variableName"
                  :widget="subWidget"
                  :isEdit="isEdit"
                  :value="getWidgetValue(subWidget) || subWidget.props.text"
                  :style="{ 'margin-bottom': (subWidget.props.paddingBottom || 0) + 'px' }"
                  @widgetClick="onWidgetClick"
                />
                <OnlineCustomWidget
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.Image"
                  :ref="subWidget.variableName"
                  :widget="subWidget"
                  :isEdit="isEdit"
                  :value="getWidgetValue(subWidget)"
                  :src="subWidget.props.src"
                  :style="{ 'margin-bottom': (subWidget.props.paddingBottom || 0) + 'px' }"
                  @widgetClick="onWidgetClick"
                />
                <OnlineCustomImageCard
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.ImageCard"
                  :ref="subWidget.variableName"
                  :widget="subWidget"
                  :isEdit="isEdit"
                  :style="{ 'margin-bottom': (subWidget.props.paddingBottom || 0) + 'px' }"
                  @widgetClick="onWidgetClick"
                />
                <OnlineCustomQueryList
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.QueryList"
                  :widget="subWidget"
                  :isEdit="isEdit"
                  @widgetClick="onWidgetClick"
                />
                <OnlineCustomBaseList
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.List"
                  :widget="subWidget"
                  :isEdit="isEdit"
                  @widgetClick="onWidgetClick"
                />
                <component
                  :is="form().mode === 'pc' ? 'el-form-item' : 'div'"
                  v-else
                  :label="subWidget.showName"
                  inset
                  :prop="subWidget.propString"
                  :class="{
                    'mobile-item': form().mode === 'mobile',
                    'rich-input': subWidget.widgetType === SysCustomWidgetType.RichEditor
                  }"
                  :label-width="
                    subWidget.showName == null || subWidget.showName === ''
                      ? isEdit
                        ? '0px'
                        : '0px'
                      : undefined
                  "
                  @click.native.stop="onWidgetClick(subWidget)"
                >
                  <OnlineCustomWidget
                    :widget="subWidget"
                    :ref="subWidget.variableName"
                    :value="getWidgetValue(subWidget)"
                    @input="val => onValueChange(subWidget, val)"
                    @change="(val, detail) => onWidgetValueChange(subWidget, val, detail)"
                  />
                </component>
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
          <div v-else-if="isEdit" class="info mover">
            <div style="height: 100px; width: 100px">
              <i class="el-icon-upload" />
            </div>
            <span>请拖入组件进行编辑</span>
          </div>
        </Draggable>
      </el-row>
    </el-col>
  </el-row>
</template>

<script>
import Draggable from 'vuedraggable';
import OnlineCustomWidget from './OnlineCustomWidget.vue';
import OnlineCardTable from './OnlineCardTable.vue';
import OnlineCustomImageCard from './OnlineCustomImageCard.vue';
import OnlineCustomQueryList from './OnlineCustomQueryList.vue';
import OnlineCustomBaseList from './OnlineCustomList/index.vue';
import OnlineCustomCellGroup from './OnlineCustomCellGroup.vue';
import ActiveWidgetMenu from './ActiveWidgetMenu.vue';

export default {
  name: 'onlineCustomBlock',
  props: {
    value: {
      type: Array,
      default: () => []
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
    OnlineCustomImageCard,
    OnlineCustomQueryList,
    OnlineCustomBaseList,
    OnlineCustomCellGroup,
    ActiveWidgetMenu
  },
  inject: ['form'],
  data() {
    return {};
  },
  methods: {
    onDragAdd(e) {
      this.$emit('dragAdd', { list: this.value, dragEvent: e });
    },
    onDragMove(e) {
      // 容器组件不能改变位置
      let widgetType = e.relatedContext.element ? e.relatedContext.element.widgetType : undefined;
      return (
        widgetType !== this.SysCustomWidgetType.Block &&
        widgetType !== this.SysCustomWidgetType.Card
      );
    },
    onCopyWidget(widget) {
      this.$emit('input', [...this.value, widget]);
    },
    onDeleteWidget(widget) {
      this.$confirm('是否删除此组件？')
        .then(res => {
          this.$emit(
            'input',
            this.value.filter(item => item !== widget)
          );
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
    onWidgetValueChange(widget, value, detail) {
      return this.form().onWidgetValueChange(widget, value, detail);
    },
    getBlockStyle(widget) {
      return {
        'margin-bottom':
          (widget.props.paddingBottom || (widget.props.basicInfo || {}).paddingBottom || 0) + 'px',
        padding: this.isEdit ? '5px' : undefined,
        border: this.isEdit ? '1px solid #e8eaec' : undefined
      };
    },
    getChartStyle(widget) {
      return {
        'margin-bottom':
          (widget.props.paddingBottom || (widget.props.basicInfo || {}).paddingBottom || 0) + 'px'
      };
    },
    getTableStyle(widget) {
      if (widget.widgetType !== this.SysCustomWidgetType.Table) return;
      return {
        'margin-bottom': (widget.props.paddingBottom || 0) + 'px'
      };
    },
    onWidgetClick(widget) {
      this.$emit('widgetClick', widget);
    }
  },
  computed: {
    getDrableBoxStyle() {
      let tempHeight = this.height;
      if (this.height == null || this.height === '') {
        tempHeight = this.isEdit && this.value.length <= 0 ? '150px' : '0px';
      }
      return {
        'min-height': tempHeight
      };
    }
  }
};
</script>

<style>
.ghost {
  height: 30px;
  border-radius: 3px;
}
</style>

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
