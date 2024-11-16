import '@/core/http';
import JSONbig from '@/utils/jsonBigint/index.js';
import Dialog from '@/components/Dialog';
import Vue from 'vue';
import ElementUI from 'element-ui';
// import 'element-ui/lib/theme-chalk/index.css'
import '@/assets/style/index.scss';
import '@/core/mixins/global.js';
import App from './App';
import router from './router';
import store from './store';
import TreeSelect from '@/components/TreeSelect';
import RichEditor from '@/components/RichEditor';
import InputNumberRange from '@/components/InputNumberRange';
import DateRange from '@/components/DateRange';
import FilterBox from '@/components/FilterBox';
import TableProgressColumn from '@/components/TableProgressColumn';
import UserSelect from '@/components/UserSelect';
import DeptSelect from '@/components/DeptSelect';
import TableBox from '@/components/TableBox';
import RightAddBtn from '@/components/Btns/RightAddBtn';
import VXETable from 'vxe-table';
import 'vxe-table/lib/style.css';
import '@/assets/online-icon/iconfont.css';
// 图表
import LineChart from '@/components/Charts3.0/lineChart.vue';
import BarChart from '@/components/Charts3.0/barChart.vue';
import PieChart from '@/components/Charts3.0/pieChart.vue';
import FunnelChart from '@/components/Charts3.0/funnelChart.vue';
import RadarChart from '@/components/Charts3.0/radarChart.vue';
import ScatterChart from '@/components/Charts3.0/scatterChart.vue';
// 行内编辑
import InlineInputColumn from '@/components/TableBox/InlineInputColumn.vue';
import InlineNumberColumn from '@/components/TableBox/InlineNumberColumn.vue';
import InlintSelectColumn from '@/components/TableBox/InlineSelectColumn.vue';
import InlineCascaderColumn from '@/components/TableBox/InlineCascaderColumn.vue';
import InlineDateColumn from '@/components/TableBox/InlineDateColumn.vue';
import InlineSwitchColumn from '@/components/TableBox/InlineSwitchColumn.vue';
import InlineUploadColumn from '@/components/TableBox/InlineUploadColumn.vue';
import InlineAutoSaveColumn from '@/components/TableBox/InlineAutoSaveColumn.vue';


Vue.use(VXETable);
window.JSON = new JSONbig({storeAsString: true});
Vue.component('tree-select', TreeSelect);
Vue.component('rich-editor', RichEditor);
Vue.component('input-number-range', InputNumberRange);
Vue.component('date-range', DateRange);
Vue.component('filter-box', FilterBox);
Vue.component('table-progress-column', TableProgressColumn);
Vue.component('user-select', UserSelect);
Vue.component('dept-select', DeptSelect);
Vue.component('table-box', TableBox);
Vue.component('right-add-btn', RightAddBtn);
// 图表
Vue.component('line-chart', LineChart);
Vue.component('bar-chart', BarChart);
Vue.component('pie-chart', PieChart);
Vue.component('funnel-chart', FunnelChart);
Vue.component('radar-chart', RadarChart);
Vue.component('scatter-chart', ScatterChart);
// 行内编辑
Vue.component('inline-input-column', InlineInputColumn);
Vue.component('inline-number-column', InlineNumberColumn);
Vue.component('inline-select-column', InlintSelectColumn);
Vue.component('inline-cascader-column', InlineCascaderColumn);
Vue.component('inline-date-column', InlineDateColumn);
Vue.component('inline-switch-column', InlineSwitchColumn);
Vue.component('inline-upload-column', InlineUploadColumn);
Vue.component('inline-auto-save-column', InlineAutoSaveColumn);

Vue.use(ElementUI);
Vue.config.productionTip = false;
Vue.prototype.$dialog = Dialog;
/* eslint-disable no-new */
const vm = new Vue({
  router,
  store,
  render: h => h(App)
});
vm.$mount('#app');
Vue.prototype.$vm = vm;
