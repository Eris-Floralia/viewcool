package viewcool.webadmin.upms.service;

import viewcool.common.core.base.service.IBaseService;
import viewcool.common.core.object.CallResult;
import viewcool.webadmin.upms.model.SysRole;
import viewcool.webadmin.upms.model.SysUserRole;

import java.util.*;

/**
 * 角色数据服务接口。
 *
 * @author eris
 * @date 2024-11-16
 */
public interface SysRoleService extends IBaseService<SysRole, Long> {

    /**
     * 保存新增的角色对象。
     *
     * @param role      新增的角色对象。
     * @param menuIdSet 菜单Id列表。
     * @return 新增后的角色对象。
     */
    SysRole saveNew(SysRole role, Set<Long> menuIdSet);

    /**
     * 更新角色对象。
     *
     * @param role         更新的角色对象。
     * @param originalRole 原有的角色对象。
     * @param menuIdSet    菜单Id列表。
     * @return 更新成功返回true，否则false。
     */
    boolean update(SysRole role, SysRole originalRole, Set<Long> menuIdSet);

    /**
     * 删除指定角色。
     *
     * @param roleId 角色主键Id。
     * @return 删除成功返回true，否则false。
     */
    boolean remove(Long roleId);

    /**
     * 获取角色列表。
     *
     * @param filter  角色过滤对象。
     * @param orderBy 排序参数。
     * @return 角色列表。
     */
    List<SysRole> getSysRoleList(SysRole filter, String orderBy);

    /**
     * 获取用户的用户角色对象列表。
     *
     * @param userId 用户Id。
     * @return 用户角色对象列表。
     */
    List<SysUserRole> getSysUserRoleListByUserId(Long userId);

    /**
     * 批量新增用户角色关联。
     *
     * @param userRoleList 用户角色关系数据列表。
     */
    void addUserRoleList(List<SysUserRole> userRoleList);

    /**
     * 移除指定用户和指定角色的关联关系。
     *
     * @param roleId 角色主键Id。
     * @param userId 用户主键Id。
     * @return 移除成功返回true，否则false。
     */
    boolean removeUserRole(Long roleId, Long userId);

    /**
     * 验证角色对象关联的数据是否都合法。
     *
     * @param sysRole          当前操作的对象。
     * @param originalSysRole  原有对象。
     * @param menuIdListString 逗号分隔的menuId列表。
     * @return 验证结果。
     */
    CallResult verifyRelatedData(SysRole sysRole, SysRole originalSysRole, String menuIdListString);
}
