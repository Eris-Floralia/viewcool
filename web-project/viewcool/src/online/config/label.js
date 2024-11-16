import { SysCustomWidgetType, SysOnlineFormType, OnlineFormEventType } from '@/staticDict/index.js';

const label = {
  span: {
    name: '组件宽度',
    widgetType: SysCustomWidgetType.Slider,
    value: 12,
    visible: function (formConfig) {
      return formConfig && formConfig.form.formType !== SysOnlineFormType.QUERY;
    },
    min: 1,
    max: 24
  }
};

const labelConfig = {
  widgetType: SysCustomWidgetType.Label,
  icon: 'online-icon icon-text',
  attribute: label,
  allowEventList: [OnlineFormEventType.VISIBLE],
  supportBindTable: true,
  supportBindColumn: true
};

export default labelConfig;
