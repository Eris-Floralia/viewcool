package viewcool.webadmin.upms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.page.PageMethod;
import viewcool.common.core.base.service.BaseService;
import viewcool.common.core.base.dao.BaseDaoMapper;
import viewcool.common.core.object.*;
import viewcool.common.core.util.MyModelUtil;
import viewcool.common.core.util.MyPageUtil;
import viewcool.common.ext.base.BizWidgetDatasource;
import viewcool.common.ext.constant.BizWidgetDatasourceType;
import viewcool.common.ext.util.BizWidgetDatasourceExtHelper;
import viewcool.common.sequence.wrapper.IdGeneratorWrapper;
import viewcool.webadmin.upms.dao.SysRoleMapper;
import viewcool.webadmin.upms.dao.SysRoleMenuMapper;
import viewcool.webadmin.upms.dao.SysUserRoleMapper;
import viewcool.webadmin.upms.model.SysRole;
import viewcool.webadmin.upms.model.SysRoleMenu;
import viewcool.webadmin.upms.model.SysUserRole;
import viewcool.webadmin.upms.service.SysMenuService;
import viewcool.webadmin.upms.service.SysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色数据服务类。
 *
 * @author eris
 * @date 2024-11-16
 */
@Slf4j
@Service("sysRoleService")
public class SysRoleServiceImpl extends BaseService<SysRole, Long> implements SysRoleService, BizWidgetDatasource {

    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;
    @Autowired
    private SysMenuService sysMenuService;
    @Autowired
    private IdGeneratorWrapper idGenerator;
    @Autowired
    private BizWidgetDatasourceExtHelper bizWidgetDatasourceExtHelper;

    /**
     * 返回主对象的Mapper对象。
     *
     * @return 主对象的Mapper对象。
     */
    @Override
    protected BaseDaoMapper<SysRole> mapper() {
        return sysRoleMapper;
    }

    @PostConstruct
    private void registerBizWidgetDatasource() {
        bizWidgetDatasourceExtHelper.registerDatasource(BizWidgetDatasourceType.UPMS_ROLE_TYPE, this);
    }

    @Override
    public MyPageData<Map<String, Object>> getDataList(
            String type, Map<String, Object> filter, MyOrderParam orderParam, MyPageParam pageParam) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize(), pageParam.getCount());
        }
        String orderBy = orderParam == null ? null : MyOrderParam.buildOrderBy(orderParam, SysRole.class);
        SysRole roleFilter = filter == null ? null : BeanUtil.toBean(filter, SysRole.class);
        List<SysRole> roleList = this.getSysRoleList(roleFilter, orderBy);
        return MyPageUtil.makeResponseData(roleList, BeanUtil::beanToMap);
    }

    @Override
    public List<Map<String, Object>> getDataListWithInList(String type, String fieldName, List<String> fieldValues) {
        List<SysRole> roleList;
        if (StrUtil.isBlank(fieldName)) {
            roleList = this.getInList(fieldValues.stream().map(Long::valueOf).collect(Collectors.toSet()));
        } else {
            roleList = this.getInList(fieldName, MyModelUtil.convertToTypeValues(SysRole.class, fieldName, fieldValues));
        }
        return MyModelUtil.beanToMapList(roleList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SysRole saveNew(SysRole role, Set<Long> menuIdSet) {
        role.setRoleId(idGenerator.nextLongId());
        MyModelUtil.fillCommonsForInsert(role);
        sysRoleMapper.insert(role);
        if (menuIdSet != null) {
            for (Long menuId : menuIdSet) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(role.getRoleId());
                roleMenu.setMenuId(menuId);
                sysRoleMenuMapper.insert(roleMenu);
            }
        }
        return role;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(SysRole role, SysRole originalRole, Set<Long> menuIdSet) {
        MyModelUtil.fillCommonsForUpdate(role, originalRole);
        if (sysRoleMapper.updateById(role) != 1) {
            return false;
        }
        SysRoleMenu deletedRoleMenu = new SysRoleMenu();
        deletedRoleMenu.setRoleId(role.getRoleId());
        sysRoleMenuMapper.delete(new QueryWrapper<>(deletedRoleMenu));
        if (menuIdSet != null) {
            for (Long menuId : menuIdSet) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(role.getRoleId());
                roleMenu.setMenuId(menuId);
                sysRoleMenuMapper.insert(roleMenu);
            }
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean remove(Long roleId) {
        if (sysRoleMapper.deleteById(roleId) != 1) {
            return false;
        }
        SysRoleMenu roleMenu = new SysRoleMenu();
        roleMenu.setRoleId(roleId);
        sysRoleMenuMapper.delete(new QueryWrapper<>(roleMenu));
        SysUserRole userRole = new SysUserRole();
        userRole.setRoleId(roleId);
        sysUserRoleMapper.delete(new QueryWrapper<>(userRole));
        return true;
    }

    @Override
    public List<SysRole> getSysRoleList(SysRole filter, String orderBy) {
        return sysRoleMapper.getSysRoleList(filter, orderBy);
    }

    @Override
    public List<SysUserRole> getSysUserRoleListByUserId(Long userId) {
        SysUserRole filter = new SysUserRole();
        filter.setUserId(userId);
        return sysUserRoleMapper.selectList(new QueryWrapper<>(filter));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addUserRoleList(List<SysUserRole> userRoleList) {
        for (SysUserRole userRole : userRoleList) {
            sysUserRoleMapper.insert(userRole);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean removeUserRole(Long roleId, Long userId) {
        SysUserRole userRole  = new SysUserRole();
        userRole.setRoleId(roleId);
        userRole.setUserId(userId);
        return sysUserRoleMapper.delete(new QueryWrapper<>(userRole)) == 1;
    }

    @Override
    public CallResult verifyRelatedData(SysRole sysRole, SysRole originalSysRole, String menuIdListString) {
        JSONObject jsonObject = null;
        if (StringUtils.isNotBlank(menuIdListString)) {
            Set<Long> menuIdSet = Arrays.stream(
                    menuIdListString.split(",")).map(Long::valueOf).collect(Collectors.toSet());
            if (!sysMenuService.existAllPrimaryKeys(menuIdSet)) {
                return CallResult.error("数据验证失败，存在不合法的菜单权限，请刷新后重试！");
            }
            jsonObject = new JSONObject();
            jsonObject.put("menuIdSet", menuIdSet);
        }
        return CallResult.ok(jsonObject);
    }
}
