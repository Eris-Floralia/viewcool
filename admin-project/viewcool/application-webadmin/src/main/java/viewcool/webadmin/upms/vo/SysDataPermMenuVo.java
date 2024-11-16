package viewcool.webadmin.upms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据权限与菜单关联VO。
 *
 * @author eris
 * @date 2024-11-16
 */
@Schema(description = "数据权限与菜单关联VO")
@Data
public class SysDataPermMenuVo {

    /**
     * 数据权限Id。
     */
    @Schema(description = "数据权限Id")
    private Long dataPermId;

    /**
     * 关联菜单Id。
     */
    @Schema(description = "关联菜单Id")
    private Long menuId;
}