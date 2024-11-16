import state from '../store/state.js';
// 开发环境不使用懒加载, 因为懒加载页面太多的话会造成webpack热更新太慢
const _import = require('./import-' + process.env.NODE_ENV)

function getProps (route) {
  return route.query;
}

// 系统生成路由
const routers = [
  { path: '/login', component: _import('login/index'), name: 'login', props: getProps, desc: '登录' },
  {
    path: '/',
    component: _import('login/index'),
    name: 'root'
  },
  {
    path: '/main',
    component: _import('layout/index'),
    name: 'main',
    props: getProps,
    redirect: {
      name: 'welcome'
    },
    meta: {
      title: '主页',
      showOnly: true
    },
    children: [
      {path: 'formSysUser', component: _import('upms/formSysUser/index'), name: 'formSysUser', meta: {title: '用户管理'}},
      {path: 'formSysDept', component: _import('upms/formSysDept/index'), name: 'formSysDept', meta: {title: '部门管理'}},
      {path: 'formSysRole', component: _import('upms/formSysRole/index'), name: 'formSysRole', meta: {title: '角色管理'}},
      {path: 'formSysDataPerm', component: _import('upms/formSysDataPerm/index'), name: 'formSysDataPerm', meta: {title: '数据权限管理'}},
      {path: 'formSysMenu', component: _import(state.supportColumn ? 'upms/formSysMenu/formSysColumnMenu' : 'upms/formSysMenu/index'), name: 'formSysMenu', meta: {title: '菜单列表'}},
      {path: 'formSysDict', component: _import('upms/formDictManagement/index'), name: 'formSysDict', meta: {title: '字典管理'}},
      {path: 'formSysOperationLog', component: _import('upms/formSysOperationLog/index'), name: 'formSysOperationLog', meta: {title: '操作日志'}},
      {path: 'formSysLoginUser', component: _import('upms/formSysLoginUser/index'), name: 'formSysLoginUser', meta: {title: '在线用户'}},
      // 岗位模块路由配置
      {path: 'formSysPost', component: _import('upms/formSysPost/index'), name: 'formSysPost', meta: {title: '岗位管理'}},
      {path: 'formSysDeptPost', component: _import('upms/formSysDeptPost/index'), name: 'formSysDeptPost', props: getProps, meta: {title: '设置部门岗位'}},
      {path: 'welcome', component: _import('welcome/index'), name: 'welcome', meta: {title: '欢迎'}}
    ]
  },
  // 第三方接入路由
  {
    path: '/thirdParty',
    name: 'thirdParty',
    props: getProps,
    component: _import('thirdParty/index'),
    children: [
      // 通用
      {
        path: 'thirdEditDictParamValue2',
        name: 'thirdEditDictParamValue2',
        props: getProps,
        component: _import('components/WidgetAttributeSetting/components/CustomWidgetDictSetting/editDictParamValue', 'online')
      },
      {
        path: 'thirdEditOnlineTableColumn2',
        name: 'thirdEditOnlineTableColumn2',
        props: getProps,
        component: _import('components/WidgetAttributeSetting/components/OnlineTableColumnSetting/editOnlineTableColumn', 'online')
      },
      {
        path: 'thirdEditOnlineTabPanel2',
        name: 'thirdEditOnlineTabPanel2',
        props: getProps,
        component: _import('components/WidgetAttributeSetting/components/OnlineTabPanelSetting/editOnlineTabPanel', 'online')
      },
      {
        path: 'thirdSelectDept',
        name: 'thirdSelectDept',
        props: getProps,
        component: _import('DeptSelect/deptSelectDlg', 'components')
      },
      {
        path: 'thirdSelectUser',
        name: 'thirdSelectUser',
        props: getProps,
        component: _import('UserSelect/userSelectDlg', 'components')
      },
      {
        path: 'thirdSelectData',
        name: 'thirdSelectData',
        props: getProps,
        component: _import('components/OnlineCustomDataSelect/dataSelectDlg', 'online')
      },
      {
        path: 'thirdEditCustomEvent',
        name: 'thirdEditCustomEvent',
        props: getProps,
        component: _import('components/CustomEventSetting/editCustomEvent', 'online')
      }
    ]
  }
];

export default routers;
