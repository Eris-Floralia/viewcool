package viewcool.webadmin.upms.model;

import com.baomidou.mybatisplus.annotation.*;
import viewcool.common.core.annotation.RelationManyToMany;
import viewcool.common.core.base.model.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

/**
 * 角色实体对象。
 *
 * @author eris
 * @date 2024-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_role")
public class SysRole extends BaseModel {

    /**
     * 角色Id。
     */
    @TableId(value = "role_id")
    private Long roleId;

    /**
     * 角色名称。
     */
    @TableField(value = "role_name")
    private String roleName;

    @RelationManyToMany(
            relationMasterIdField = "roleId",
            relationModelClass = SysRoleMenu.class)
    @TableField(exist = false)
    private List<SysRoleMenu> sysRoleMenuList;
}
