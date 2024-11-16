package viewcool.webadmin.upms.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 数据权限与用户关联实体对象。
 *
 * @author eris
 * @date 2024-11-16
 */
@Data
@TableName(value = "sys_data_perm_user")
public class SysDataPermUser {

    /**
     * 数据权限Id。
     */
    @TableField(value = "data_perm_id")
    private Long dataPermId;

    /**
     * 用户Id。
     */
    @TableField(value = "user_id")
    private Long userId;
}
