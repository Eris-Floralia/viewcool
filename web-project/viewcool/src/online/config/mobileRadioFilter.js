import { SysCustomWidgetType, OnlineFormEventType } from '@/staticDict/index.js';
const radioFilter = {
  dictInfo: {
    name: '下拉字典',
    value: {},
    customComponent: {
      component: 'CustomWidgetDictSetting'
    }
  }
};

const radioFilterConfig = {
  widgetType: SysCustomWidgetType.MobileRadioFilter,
  icon: 'online-icon icon-radio',
  attribute: radioFilter,
  allowEventList: [
    OnlineFormEventType.CHANGE,
    OnlineFormEventType.DISABLE,
    OnlineFormEventType.VISIBLE
  ],
  supportBindTable: true,
  supportBindColumn: true
};

export default radioFilterConfig;
