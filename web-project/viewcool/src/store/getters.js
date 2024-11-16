import { findMenuItem } from './utils';
import { getAppId, findTreeNode } from '@/utils';
import * as StaticDict from '@/staticDict';

export default {
  getMultiTags: state => {
    return state.supportTags;
  },
  getCollapse: state => {
    return state.isCollapse;
  },
  getClientHeight: state => {
    return state.documentClientHeight;
  },
  getClientWidth: state => {
    return state.documentClientWidth;
  },
  getMainContextHeight: state => {
    let appId = getAppId();
    if (appId == null) {
      return state.documentClientHeight - (state.supportTags ? 110 : 60);
    } else {
      return state.documentClientHeight;
    }
  },
  getOnlineFormCache: state => {
    return state.onlineFormCache;
  },
  getUserInfo: state => {
    return state.userInfo;
  },
  getCachePages: state => {
    return state.cachePages;
  },
  getTagList: state => {
    return state.tagList;
  },
  getMenuList: state => {
    return state.menuList;
    // if (state.supportColumn) {
    //   if (state.currentColumnId == null || state.currentColumnId === '') return [];
    //   for (let i = 0; i < state.menuList.length; i++) {
    //     if (state.menuList[i].menuId === state.currentColumnId) {
    //       return state.menuList[i].children || [];
    //     }
    //   }

    //   return [];
    // } else {
    //   return state.menuList;
    // }
  },
  getColumnList: state => {
    if (!state.supportColumn) return [];
    return state.menuList
      .map(menu => {
        if (menu.menuType === StaticDict.SysMenuType.DIRECTORY) {
          return {
            columnId: menu.menuId,
            columnName: menu.menuName
          };
        }
        return null;
      })
      .filter(item => item != null);
  },
  getCurrentMenuId: state => {
    return state.currentMenuId;
  },
  getMenuItem: state => {
    if (Array.isArray(state.menuList)) {
      let menuItem = findTreeNode(state.menuList, state.currentMenuId, 'menuId', 'children');
      return menuItem;
    }
    return null;
  },
  getCurrentMenuPath: state => {
    let menuPath = [];
    if (Array.isArray(state.menuList)) {
      for (let i = 0; i < state.menuList.length; i++) {
        let temp = findMenuItem(state.menuList[i], state.currentMenuId, menuPath);
        if (temp != null) break;
      }
    }

    return menuPath;
  },
  getMultiColumn: state => {
    return state.supportColumn;
  },
  getCurrentColumnId: state => {
    return state.currentColumnId;
  },
  getMessageCount: state => {
    return state.messageCount;
  },
  getUserShowNameData: state => {
    return state.userShowNameData;
  }
};
