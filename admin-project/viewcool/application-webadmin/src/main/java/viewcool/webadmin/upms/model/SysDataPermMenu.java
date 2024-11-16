package viewcool.webadmin.upms.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.ToString;

/**
 * 数据权限与菜单关联实体对象。
 *
 * @author eris
 * @date 2024-11-16
 */
@Data
@ToString(of = {"menuId"})
@TableName(value = "sys_data_perm_menu")
public class SysDataPermMenu {

    /**
     * 数据权限Id。
     */
    @TableField(value = "data_perm_id")
    private Long dataPermId;

    /**
     * 关联菜单Id。
     */
    @TableField(value = "menu_id")
    private Long menuId;
}
