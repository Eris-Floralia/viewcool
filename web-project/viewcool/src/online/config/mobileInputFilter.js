import { SysCustomWidgetType, OnlineFormEventType } from '@/staticDict/index.js';
const inputFilter = {
  placeholder: {
    name: '占位文本',
    widgetType: SysCustomWidgetType.Input,
    value: ''
  }
};

const inputFilterConfig = {
  widgetType: SysCustomWidgetType.MobileInputFilter,
  icon: 'online-icon icon-input',
  attribute: inputFilter,
  allowEventList: [
    OnlineFormEventType.CHANGE,
    OnlineFormEventType.DISABLE,
    OnlineFormEventType.VISIBLE
  ],
  supportBindTable: true,
  supportBindColumn: true
};

export default inputFilterConfig;
