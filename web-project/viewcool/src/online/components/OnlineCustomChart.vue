<template>
  <el-card
    class="custom-chart"
    :body-style="getBodyStyle"
    :shadow="widget.widgetType === SysCustomWidgetType.Carousel ? 'none' : 'always'"
    :style="{ border: widget.widgetType === SysCustomWidgetType.Carousel ? 'none' : undefined }"
    @click.native="onClickWidget()"
  >
    <LineChart
      ref="innerObj"
      v-if="widget.widgetType === SysCustomWidgetType.LineChart"
      :value="value"
      @input="onValueChange"
      :style="{ height: '100%' }"
      :data="chartData"
      :options="getChartOptions"
      :categrayColumnList="categrayColumnList"
      :valueColumnList="valueColumnList"
      @click="onClickWidget"
      @dblclick="onDblClick"
    />
    <BarChart
      ref="innerObj"
      v-if="widget.widgetType === SysCustomWidgetType.BarChart"
      :value="value"
      @input="onValueChange"
      :style="{ height: '100%' }"
      :data="chartData"
      :options="getChartOptions"
      :categrayColumnList="categrayColumnList"
      :valueColumnList="valueColumnList"
      @click="onClickWidget()"
      @dblclick="onDblClick"
    />
    <PieChart
      ref="innerObj"
      v-if="widget.widgetType === SysCustomWidgetType.PieChart"
      :value="value"
      @input="onValueChange"
      :style="{ height: '100%' }"
      :data="chartData"
      :options="getChartOptions"
      :categrayColumnList="categrayColumnList"
      :valueColumnList="valueColumnList"
      @click="onClickWidget()"
      @dblclick="onDblClick"
    />
    <ScatterCharts
      ref="innerObj"
      v-if="widget.widgetType === SysCustomWidgetType.ScatterChart"
      :value="value"
      @input="onValueChange"
      :style="{ height: '100%' }"
      :data="chartData"
      :options="getChartOptions"
      :categrayColumnList="categrayColumnList"
      :valueColumnList="valueColumnList"
      @click="onClickWidget()"
      @dblclick="onDblClick"
    />
    <PivotTable
      ref="innerObj"
      v-if="widget.widgetType === SysCustomWidgetType.PivotTable"
      :data="chartData"
      :height="(widget || {}).props.basicInfo.height"
      :options="pivotTableOptions"
      :rowGroupColumnList="rowGroupColumnList"
      :columnGroupColumnList="columnGroupColumnList"
      :valueColumnList="valueColumnList"
      @click="onClickWidget()"
    />
    <DataViewTable
      ref="innerObj"
      v-if="widget.widgetType === SysCustomWidgetType.DataViewTable"
      :data="chartData"
      :height="(widget || {}).props.basicInfo.height"
      :options="dataViewTableOptions"
      :columnList="dataViewColumnList"
      @click="onClickWidget()"
      @refresh="refreshDataViewTable"
    />
    <CarouselChart
      ref="innerObj"
      v-if="widget.widgetType === SysCustomWidgetType.Carousel"
      :data="chartData"
      :height="(widget || {}).props.basicInfo.height"
      :options="dataViewTableOptions"
      :imagePath="imagePath"
      :imageName="imageName"
      @click="onClickWidget()"
    />
    <RichText
      ref="innerObj"
      v-if="widget.widgetType === SysCustomWidgetType.RichText"
      :data="chartData"
      :height="(widget || {}).props.basicInfo.height"
      :options="dataViewTableOptions"
      @click="onClickWidget()"
    />
    <GaugeChart
      ref="innerObj"
      v-if="widget.widgetType === SysCustomWidgetType.GaugeChart"
      :value="value"
      @input="onValueChange"
      :style="{ height: '100%' }"
      :data="chartData"
      :options="getChartOptions"
      :categrayColumnList="categrayColumnList"
      :valueColumnList="valueColumnList"
      @click="onClickWidget"
      @dblclick="onDblClick"
    />
    <FunnelChart
      ref="innerObj"
      v-if="widget.widgetType === SysCustomWidgetType.FunnelChart"
      :value="value"
      @input="onValueChange"
      :style="{ height: '100%' }"
      :data="chartData"
      :options="getChartOptions"
      :categrayColumnList="categrayColumnList"
      :valueColumnList="valueColumnList"
      @click="onClickWidget"
      @dblclick="onDblClick"
    />
    <RadarChart
      ref="innerObj"
      v-if="widget.widgetType === SysCustomWidgetType.RadarChart"
      :value="value"
      @input="onValueChange"
      :style="{ height: '100%' }"
      :data="chartData"
      :options="getChartOptions"
      :categrayColumnList="categrayColumnList"
      :valueColumnList="valueColumnList"
      @click="onClickWidget"
      @dblclick="onDblClick"
    />
    <ProgressBar
      ref="innerObj"
      v-if="widget.widgetType === SysCustomWidgetType.ProgressBar"
      :data="chartData"
      :height="(widget || {}).props.basicInfo.height"
      :options="dataViewTableOptions"
      @click="onClickWidget()"
      @dblclick="onDblClick"
    />
    <ProgressCircle
      ref="innerObj"
      v-if="widget.widgetType === SysCustomWidgetType.ProgressCircle"
      :data="chartData"
      :height="(widget || {}).props.basicInfo.height"
      :options="dataViewTableOptions"
      @click="onClickWidget()"
      @dblclick="onDblClick"
    />
    <DataCard
      ref="innerObj"
      v-if="widget.widgetType === SysCustomWidgetType.DataCard"
      :data="chartData"
      :height="(widget || {}).props.basicInfo.height"
      :options="dataViewTableOptions"
      @click="onClickWidget()"
      @dblclick="onDblClick"
    />
    <DataProgressCard
      ref="innerObj"
      v-if="widget.widgetType === SysCustomWidgetType.DataProgressCard"
      :data="chartData"
      :height="(widget || {}).props.basicInfo.height"
      :options="dataViewTableOptions"
      @click="onClickWidget()"
      @dblclick="onDblClick"
    />
    <CommonList
      ref="innerObj"
      v-if="widget.widgetType === SysCustomWidgetType.CommonList"
      :data="chartData"
      :height="(widget || {}).props.basicInfo.height"
      :options="dataViewTableOptions"
      @click="onClickWidget()"
      @dblclick="onDblClick"
    />
    <i
      v-if="widget.widgetType !== SysCustomWidgetType.Carousel"
      class="refresh-btn el-icon-refresh"
      @click="refresh"
    />
  </el-card>
</template>

<script>
import { treeDataTranslate, findItemFromList, findTreeNodeObjectPath } from '@/utils';
import { getValueColumnName } from '../utils';
import LineChart from '@/components/Charts/lineCharts.vue';
import BarChart from '@/components/Charts/barCharts.vue';
import PieChart from '@/components/Charts/pieCharts.vue';
import ScatterCharts from '@/components/Charts/scatterCharts.vue';
import PivotTable from '@/components/Charts/pivotTable.vue';
import DataViewTable from '@/components/Charts/dataViewTable.vue';
import CarouselChart from '@/components/Charts/carouselChart.vue';
import RichText from '@/components/Charts/richText.vue';
import GaugeChart from '@/components/Charts/gaugeChart.vue';
import FunnelChart from '@/components/Charts/funnelChart.vue';
import RadarChart from '@/components/Charts/radarChart.vue';
import ProgressBar from '@/components/Charts/progressBar.vue';
import ProgressCircle from '@/components/Charts/progressCircle.vue';
import DataCard from '@/components/Charts/dataCard.vue';
import DataProgressCard from '@/components/Charts/dataProgressCard.vue';
import CommonList from '@/components/Charts/commonList.vue';

export default {
  props: {
    value: {
      type: [Number, Array, String, Date, Object]
    },
    isEdit: {
      type: Boolean,
      default: false
    },
    widget: {
      type: Object
    }
  },
  inject: ['form'],
  components: {
    LineChart,
    BarChart,
    PieChart,
    ScatterCharts,
    PivotTable,
    DataViewTable,
    CarouselChart,
    RichText,
    GaugeChart,
    FunnelChart,
    RadarChart,
    ProgressBar,
    ProgressCircle,
    DataCard,
    DataProgressCard,
    CommonList
  },
  data() {
    return {
      chartData: [],
      loadTimer: undefined,
      categrayColumnList: [],
      valueColumnList: [],
      // 透视表
      pivotTableOptions: undefined,
      dataViewTableOptions: undefined,
      rowGroupColumnList: [],
      columnGroupColumnList: [],
      dataViewColumnList: [],
      imagePath: [],
      imageName: [],
      textColumn: [],
      refreshTimer: undefined
    };
  },
  methods: {
    onClickWidget() {
      this.$emit('widgetClick', this.widget);
    },
    refreshDataViewTable(pageParam) {
      this.$nextTick(() => {
        this.loadListData(pageParam);
      });
    },
    refresh() {
      if (this.refreshTimer != null) clearTimeout(this.refreshTimer);
      this.refreshTimer = setTimeout(() => {
        if (this.$refs.innerObj && typeof this.$refs.innerObj.resize === 'function') {
          this.$refs.innerObj.resize();
        }

        if (
          this.widget.widgetType !== this.SysCustomWidgetType.DataViewTable &&
          this.widget.widgetType !== this.SysCustomWidgetType.ProgressBar
        ) {
          this.$nextTick(this.loadChartData);
        } else {
          this.$nextTick(this.loadListData);
        }
        this.refreshTimer = null;
      }, 200);
    },
    onDblClick(data) {
      this.form().onChartDblClick(this.widget, data);
    },
    loadChartData() {
      if (this.loadTimer != null) clearTimeout(this.loadTimer);
      this.loadTimer = setTimeout(() => {
        let params = this.getLoadChartParams();
        if (params != null && this.widget.dataset != null) {
          this.form()
            .listDataWithGroup(params)
            .then(res => {
              let data = res.dataList;
              let columnPath = null;
              if (this.widget.dataset.datasetType === this.DatasetType.API) {
                columnPath = this.getColumnPath(this.widget.props.datasetInfo.bindColumnId);
              }
              if (Array.isArray(columnPath) && columnPath.length > 0) {
                let rootColumn = columnPath[0];
                let rootData = rootColumn && rootColumn.fieldType === 'Object' ? data[0] : data;
                if (columnPath.length > 1) {
                  for (let i = 1; i < columnPath.length; i++) {
                    let tempColumn = columnPath[i];
                    rootData = rootData[tempColumn.columnName];
                    if (rootData == null) break;
                  }
                }

                if (rootData != null) {
                  if (this.widget.widgetType === this.SysCustomWidgetType.DataViewTable) {
                    this.chartData = res;
                  } else {
                    this.chartData = rootData;
                  }
                }
              } else {
                this.chartData = data;
              }
              this.loadTimer = null;
            })
            .catch(e => {
              console.log(e);
              this.loadTimer = null;
            });
        } else {
          this.chartData = [];
        }
      }, 50);
    },
    onValueChange(value) {
      this.$emit('input', value);
      this.$emit('change', value);
    },
    cloneObject(obj) {
      if (obj == null) return undefined;
      return JSON.parse(JSON.stringify(obj));
    },
    getLoadChartParams() {
      if (
        // 不检查数值轴
        ![
          this.SysCustomWidgetType.DataViewTable,
          this.SysCustomWidgetType.Carousel,
          this.SysCustomWidgetType.RichText,
          this.SysCustomWidgetType.CommonList,
          this.SysCustomWidgetType.DataCard,
          this.SysCustomWidgetType.DataProgressCard,
          this.SysCustomWidgetType.ProgressBar,
          this.SysCustomWidgetType.ProgressCircle
        ].find(x => x === this.widget.widgetType)
      ) {
        if (
          this.widget == null ||
          this.widget.dataset == null ||
          !Array.isArray(this.widget.props.datasetInfo.valueColumnList) ||
          this.widget.props.datasetInfo.valueColumnList.length === 0
        ) {
          return null;
        }
      }

      let categroyColumnList = this.widget.props.datasetInfo.categroyColumnList;
      if (this.widget.widgetType === this.SysCustomWidgetType.PivotTable) {
        if (
          !Array.isArray(this.widget.props.datasetInfo.rowGroupColumnList) ||
          this.widget.props.datasetInfo.rowGroupColumnList.length === 0
        ) {
          return null;
        }
        categroyColumnList = (this.widget.props.datasetInfo.rowGroupColumnList || []).concat(
          this.widget.props.datasetInfo.columnGroupColumnList || []
        );
      }

      if (
        // 这三个组件不检查类别轴 / 维度
        ![
          this.SysCustomWidgetType.DataViewTable,
          this.SysCustomWidgetType.Carousel,
          this.SysCustomWidgetType.RichText,
          this.SysCustomWidgetType.CommonList,
          this.SysCustomWidgetType.DataCard,
          this.SysCustomWidgetType.DataProgressCard,
          this.SysCustomWidgetType.ProgressBar,
          this.SysCustomWidgetType.ProgressCircle
        ].find(x => x === this.widget.widgetType)
      ) {
        if (!Array.isArray(categroyColumnList) || categroyColumnList.length === 0) {
          return null;
        }
      }

      let orderList = [];
      if (this.widget.widgetType === this.SysCustomWidgetType.PivotTable) {
        if (Array.isArray(this.widget.props.datasetInfo.rowGroupColumnList)) {
          this.widget.props.datasetInfo.rowGroupColumnList.forEach(item => {
            if (item.columnId) {
              orderList.push({
                columnId: item.columnId,
                orderType: this.OrderType.ASC
              });
            }
          });
        }
        if (Array.isArray(this.widget.props.datasetInfo.columnGroupColumnList)) {
          this.widget.props.datasetInfo.columnGroupColumnList.forEach(item => {
            if (item.columnId) {
              orderList.push({
                columnId: item.columnId,
                orderType: this.OrderType.ASC
              });
            }
          });
        }
      }
      if (
        this.widget.props.datasetInfo &&
        Array.isArray(this.widget.props.datasetInfo.orderInfoList)
      ) {
        this.widget.props.datasetInfo.orderInfoList.forEach(item => {
          orderList.push({
            columnId: item.orderColumnId,
            calculateType: item.calculateType,
            orderType: item.orderType
          });
        });
      }
      let isApi = (this.widget.dataset || {}).datasetType === this.DatasetType.API;
      return {
        datasetId: (this.widget.dataset || {}).datasetId,
        dimensionDataList: isApi
          ? []
          : (categroyColumnList || []).map(item => {
              return {
                columnId: item.columnId
              };
            }),
        indexDataList: isApi
          ? []
          : (this.widget.props.datasetInfo.valueColumnList || []).map(item => {
              return {
                columnId: item.columnId,
                calculateType: item.calculateType,
                filterParams: Array.isArray(item.filterList)
                  ? item.filterList
                      .map(filter => {
                        return this.form().getQueryParam(filter);
                      })
                      .filter(item => item.paramValue != null)
                  : []
              };
            }),
        orderDataList: isApi ? [] : orderList,
        filterParams: isApi
          ? []
          : Array.isArray(this.widget.props.datasetInfo.filterList)
          ? this.widget.props.datasetInfo.filterList
              .map(filter => {
                return this.form().getQueryParam(filter);
              })
              .filter(item => item.paramValue != null)
          : [],
        datasetFilterParams: Array.isArray(this.widget.props.datasetInfo.datasetFilterParams)
          ? this.widget.props.datasetInfo.datasetFilterParams
              .map(filter => {
                return this.form().getQueryParam(filter);
              })
              .filter(item => item.paramValue != null)
          : []
      };
    },
    // 读取列表数据
    loadListData(pageParam) {
      if (this.loadTimer != null) clearTimeout(this.loadTimer);
      this.loadTimer = setTimeout(() => {
        let params = this.getLoadListDataParams(pageParam);
        if (params != null && this.widget.dataset != null) {
          this.form()
            .listDataWithGroup(params)
            .then(res => {
              this.chartData = res;
              this.loadTimer = null;
            })
            .catch(e => {
              console.log(e);
              this.loadTimer = null;
            });
        } else {
          this.chartData = [];
        }
      }, 50);
    },
    // 构建读取列表数据的参数
    getLoadListDataParams(pageParam) {
      if (this.widget == null || this.widget.dataset == null) {
        return null;
      }
      let tempPageParams =
        pageParam ||
        (this.widget.props.pagerSetting == null || this.widget.props.pagerSetting.show
          ? this.widget.props.datasetInfo.pageParam
          : undefined);
      return {
        datasetId: this.widget.dataset.datasetId,
        orderDataList: this.widget.props.datasetInfo.orderInfoList,
        pageParam: tempPageParams,
        datasetFilterParams: (this.widget.props.datasetInfo.datasetFilterParams || [])
          .map(filter => {
            return this.form().getQueryParam(filter);
          })
          .filter(item => item.paramValue != null),
        filterParams: Array.isArray(this.widget.props.datasetInfo.filterList)
          ? this.widget.props.datasetInfo.filterList
              .map(filter => {
                return this.form().getQueryParam(filter);
              })
              .filter(item => item.paramValue != null)
          : []
      };
    },
    getChartCategrayColumnList() {
      if (
        this.widget == null ||
        this.widget.dataset == null ||
        this.widget.props.datasetInfo.categroyColumnList == null ||
        this.widget.props.datasetInfo.categroyColumnList.length === 0
      ) {
        return [];
      }
      return this.widget.props.datasetInfo.categroyColumnList.map(item => {
        let columnName = this.getColumnName(item.columnId);
        return {
          columnName: columnName != null ? columnName : undefined
        };
      });
    },
    getChartValueColumnList() {
      if (
        this.widget == null ||
        this.widget.dataset == null ||
        this.widget.props.datasetInfo.valueColumnList == null ||
        this.widget.props.datasetInfo.valueColumnList.length === 0
      ) {
        return [];
      }
      return this.widget.props.datasetInfo.valueColumnList
        .map(item => {
          let calculateName = this.getColumnName(item.columnId, item.calculateType);
          if (calculateName == null) return null;
          return {
            columnName: calculateName,
            name: item.showName,
            columnWidth: item.columnWidth,
            fixed: item.fixed
          };
        })
        .filter(item => item != null);
    },
    getRowGroupColumnList() {
      if (
        this.widget == null ||
        this.widget.dataset == null ||
        this.widget.props.datasetInfo.rowGroupColumnList == null ||
        this.widget.props.datasetInfo.rowGroupColumnList.length === 0
      ) {
        return [];
      }
      return this.widget.props.datasetInfo.rowGroupColumnList
        .map(item => {
          let columnName = this.getColumnName(item.columnId);
          if (columnName == null) return null;
          return {
            columnName: columnName,
            columnWidth: item.columnWidth,
            showName: item.showName
          };
        })
        .filter(item => item != null);
    },
    getDataViewColumnList() {
      if (
        this.widget == null ||
        this.widget.dataset == null ||
        this.widget.props.datasetInfo.showColumnList == null ||
        this.widget.props.datasetInfo.showColumnList.length === 0
      ) {
        return [];
      }
      return this.widget.props.datasetInfo.showColumnList
        .map(item => {
          let columnName = this.getColumnName(item.columnId);
          if (columnName == null) return null;
          return {
            ...item,
            columnName: columnName
          };
        })
        .filter(item => item != null);
    },
    getColumnGroupColumnList() {
      if (
        this.widget == null ||
        this.widget.dataset == null ||
        this.widget.props.datasetInfo.columnGroupColumnList == null ||
        this.widget.props.datasetInfo.columnGroupColumnList.length === 0
      ) {
        return [];
      }
      return this.widget.props.datasetInfo.columnGroupColumnList
        .map(item => {
          let columnName = this.getColumnName(item.columnId);
          if (columnName == null) return null;
          return {
            columnName: columnName,
            columnWidth: item.columnWidth,
            showName: item.showName
          };
        })
        .filter(item => item != null);
    },
    getColumnPath(columnId, bindColumnId) {
      if (
        this.widget == null ||
        this.widget.dataset == null ||
        !Array.isArray(this.widget.dataset.columnList)
      )
        return [];
      let columnTree = treeDataTranslate(
        this.widget.dataset.columnList.map(column => {
          return {
            ...column,
            children: undefined
          };
        }),
        'columnId',
        'parentId'
      );
      let tempPath = findTreeNodeObjectPath(columnTree, columnId, 'columnId');
      let columnPath = [];
      let bFindBindColumn = false;
      tempPath.forEach(column => {
        if (bFindBindColumn || bindColumnId == null) {
          columnPath.push(column);
        }
        if (!bFindBindColumn && column.columnId === bindColumnId) bFindBindColumn = true;
      });
      return columnPath;
    },
    getColumnName(columnId, calculateType) {
      if (
        this.widget == null ||
        this.widget.dataset == null ||
        !Array.isArray(this.widget.dataset.columnList)
      )
        return null;
      let isApi = this.widget.dataset.datasetType === this.DatasetType.API;
      if (isApi) {
        let columnPath = this.getColumnPath(columnId, this.widget.props.datasetInfo.bindColumnId);
        if (Array.isArray(columnPath)) {
          return columnPath.map(column => column.columnName);
        }
      } else {
        let column = findItemFromList(this.widget.dataset.columnList, columnId, 'columnId');
        if (column != null)
          return calculateType == null
            ? column.columnName
            : getValueColumnName(calculateType, column.columnName);
      }
      return null;
    },
    getPivotTableConfig() {
      if (this.widget.widgetType !== this.SysCustomWidgetType.PivotTable) return;

      this.pivotTableOptions = {
        // 基础设置
        borderColor: this.widget.props.basicInfo.borderColor,
        cellBackgroundColor: this.widget.props.basicInfo.cellBackgroundColor,
        cellFontColor: this.widget.props.basicInfo.cellFontColor,
        cellFontSize: this.widget.props.basicInfo.cellFontSize,
        cellAlign: this.widget.props.basicInfo.cellAlign,
        // 列分组设置
        headerCellBackgroundColor: this.widget.props.columnGroupSetting.backgroundColor,
        headerCellFontColor: this.widget.props.columnGroupSetting.fontColor,
        headerCellFontSize: this.widget.props.columnGroupSetting.fontSize,
        headerRowHeight: this.widget.props.columnGroupSetting.headerRowHeight,
        headerAlign: this.widget.props.columnGroupSetting.align,
        // 行分组设置
        rowGroupBackgroundColor: this.widget.props.rowGroupSetting.backgroundColor,
        rowGroupFontColor: this.widget.props.rowGroupSetting.fontColor,
        rowGroupFontSize: this.widget.props.rowGroupSetting.fontSize,
        rowGroupAlign: this.widget.props.rowGroupSetting.align,
        cellRowHeight: this.widget.props.basicInfo.cellRowHeight,
        // 总计设置
        totalSetting: {
          row: {
            show: this.widget.props.rowTotalSetting.show,
            showName: this.widget.props.rowTotalSetting.showName,
            position: this.widget.props.rowTotalSetting.position
          },
          col: {
            show: this.widget.props.colTotalSetting.show,
            showName: this.widget.props.colTotalSetting.showName,
            position: this.widget.props.colTotalSetting.position
          }
        },
        // 小计设置
        subTotalSetting: {
          row: {
            show: this.widget.props.rowSubTotalSetting.show,
            showName: this.widget.props.rowSubTotalSetting.showName,
            position: this.widget.props.rowSubTotalSetting.position
          },
          col: {
            show: this.widget.props.colSubTotalSetting.show,
            showName: this.widget.props.colSubTotalSetting.showName,
            position: this.widget.props.colSubTotalSetting.position
          }
        },
        // 标题设置
        title: JSON.parse(JSON.stringify(this.widget.props.titleSetting)),
        // 单元格规则设置
        cellRule: {
          // 单元格阈值规则
          cellThreshold: this.widget.props.cellRule
        }
      };
    },
    buildDatasetInfo(datasetInfo) {
      let cardDatasetColumnFieldNameList = [
        'mainTextColumn',
        'numTextColumn',
        'footTextColumn',
        'footNumTextColumn',
        'textColumn',
        'titleColumn',
        'timeColumn',
        'progressColumn'
      ];
      let temp = JSON.parse(JSON.stringify(datasetInfo));
      cardDatasetColumnFieldNameList.forEach(fieldName => {
        if (temp[fieldName] != null) {
          if (Array.isArray(temp[fieldName])) {
            temp[fieldName].forEach(item => {
              item.columnName = this.getColumnName(item.columnId);
            });
          } else {
            temp[fieldName].columnName = this.getColumnName(temp[fieldName].columnId);
          }
        }
      });
      return temp;
    },
    getDataViewTableConfig() {
      const useDataViewTableConfig = [
        this.SysCustomWidgetType.DataViewTable,
        this.SysCustomWidgetType.Carousel,
        this.SysCustomWidgetType.RichText,
        this.SysCustomWidgetType.GaugeChart,
        this.SysCustomWidgetType.FunnelChart,
        this.SysCustomWidgetType.RadarChart,
        this.SysCustomWidgetType.ProgressBar,
        this.SysCustomWidgetType.ProgressCircle,
        this.SysCustomWidgetType.DataCard,
        this.SysCustomWidgetType.DataProgressCard,
        this.SysCustomWidgetType.CommonList
      ];
      if (!useDataViewTableConfig.find(x => x === this.widget.widgetType)) return;

      this.dataViewTableOptions = {
        datasetInfo: this.buildDatasetInfo(this.widget.props.datasetInfo),
        basicInfo: this.widget.props.basicInfo,
        // 基础设置
        borderColor: this.widget.props.basicInfo.borderColor,
        cellAlign: this.widget.props.basicInfo.cellAlign,
        cellRowHeight: this.widget.props.basicInfo.cellRowHeight,
        rowSetting: this.widget.props.rowSetting,
        columnSetting: this.widget.props.columnSetting,
        pagerSetting: this.widget.props.pagerSetting,
        seriesSetting: this.widget.props.seriesSetting,
        routeSetting: this.widget.props.routeSetting,
        pageParam: this.widget.props.datasetInfo.pageParam,
        // 标题设置
        title: this.widget.props.titleSetting
          ? JSON.parse(JSON.stringify(this.widget.props.titleSetting))
          : {},
        // 单元格规则设置
        cellRule: {
          // 单元格阈值规则
          cellThreshold: this.widget.props.cellRule
        }
      };
    },
    buildColumnInfo(columnList) {
      if (Array.isArray(columnList)) {
        return columnList.map(item => {
          return {
            ...item,
            columnName: this.getColumnName(item.columnId)
          };
        });
      } else {
        return [];
      }
    }
  },
  computed: {
    getChartOptions() {
      if (this.widget == null) return null;
      return {
        basic: {
          color: this.cloneObject(this.widget.props.basicInfo.chartColors),
          grid: this.widget.props.basicInfo.grid
            ? this.cloneObject(this.widget.props.basicInfo.grid)
            : undefined,
          label: this.cloneObject(this.widget.props.labelSetting),
          tooltip: this.cloneObject(this.widget.props.tooltipSetting),
          xAxis: this.widget.props.xAxisSetting
            ? this.cloneObject(this.widget.props.xAxisSetting)
            : undefined,
          yAxis: this.widget.props.yAxisSetting
            ? {
                ...this.cloneObject(this.widget.props.yAxisSetting),
                min:
                  this.widget.props.yAxisSetting == null ||
                  this.widget.props.yAxisSetting.yAxisValueAuto
                    ? undefined
                    : this.widget.props.yAxisSetting.min,
                max:
                  this.widget.props.yAxisSetting == null ||
                  this.widget.props.yAxisSetting.yAxisValueAuto
                    ? undefined
                    : this.widget.props.yAxisSetting.max,
                interval:
                  this.widget.props.yAxisSetting == null ||
                  this.widget.props.yAxisSetting.yAxisValueAuto
                    ? undefined
                    : this.widget.props.yAxisSetting.interval
              }
            : undefined,
          title: {
            ...this.cloneObject(this.widget.props.titleSetting),
            textStyle: {
              ...this.cloneObject(this.widget.props.titleSetting.textStyle),
              fontStyle: this.widget.props.titleSetting.italics ? 'italic' : 'normal',
              fontWeight: this.widget.props.titleSetting.bold ? 'bold' : 'normal'
            }
          },
          legend: this.cloneObject(this.widget.props.legendSetting)
        },
        series: {
          ...this.cloneObject(this.widget.props.seriesSetting),
          barWidth: this.widget.props.seriesSetting.autoWidth
            ? undefined
            : this.widget.props.seriesSetting.barWidth
        },
        radar: this.widget.props.radarSetting
          ? this.cloneObject(this.widget.props.radarSetting)
          : null,
        datasetInfo: this.widget.props.datasetInfo
      };
    },
    getBodyStyle() {
      if (
        this.widget.widgetType !== this.SysCustomWidgetType.PivotTable &&
        this.widget.widgetType !== this.SysCustomWidgetType.DataViewTable
      ) {
        return {
          padding: this.widget.widgetType === this.SysCustomWidgetType.Carousel ? '0px' : '15px',
          height: (this.widget || {}).props.basicInfo.height
        };
      }
      return null;
    }
  },
  watch: {
    'widget.dataset': {
      handler() {
        this.widget.widgetImpl = this;
        this.valueColumnList = this.getChartValueColumnList();
        this.categrayColumnList = this.getChartCategrayColumnList();
        this.rowGroupColumnList = this.getRowGroupColumnList();
        this.columnGroupColumnList = this.getColumnGroupColumnList();
        this.dataViewColumnList = this.getDataViewColumnList();
        this.imagePath = this.buildColumnInfo(this.widget.props.datasetInfo.imagePath);
        this.imageName = this.buildColumnInfo(this.widget.props.datasetInfo.imageName);
        this.textColumn = this.buildColumnInfo(this.widget.props.datasetInfo.textColumn);
        this.getPivotTableConfig();
        this.getDataViewTableConfig();
      },
      deep: true,
      immediate: true
    },
    'widget.props': {
      handler() {
        this.valueColumnList = this.getChartValueColumnList();
        this.categrayColumnList = this.getChartCategrayColumnList();
        this.rowGroupColumnList = this.getRowGroupColumnList();
        this.columnGroupColumnList = this.getColumnGroupColumnList();
        this.dataViewColumnList = this.getDataViewColumnList();
        this.imagePath = this.buildColumnInfo(this.widget.props.datasetInfo.imagePath);
        this.imageName = this.buildColumnInfo(this.widget.props.datasetInfo.imageName);
        this.textColumn = this.buildColumnInfo(this.widget.props.datasetInfo.textColumn);
        this.getPivotTableConfig();
        this.getDataViewTableConfig();
        this.refresh();
      },
      deep: true,
      immediate: true
    }
  },
  mounted() {
    this.widget.widgetImpl = this;
  },
  destroyed() {
    if (this.refreshTimer != null) clearTimeout(this.refreshTimer);
    this.refreshTimer = null;
  }
};
</script>

<style scoped>
.custom-chart {
  position: relative;
}

.refresh-btn {
  position: absolute;
  display: none;
  right: 10px;
  top: 10px;
  width: 30px;
  height: 30px;
  font-size: 20px;
  color: #383838;
  cursor: pointer;
}

.custom-chart:hover .refresh-btn {
  display: block;
}
</style>
