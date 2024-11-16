package viewcool.webadmin.upms.vo;

import viewcool.common.core.base.vo.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 角色VO。
 *
 * @author eris
 * @date 2024-11-16
 */
@Schema(description = "角色VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRoleVo extends BaseVo {

    /**
     * 角色Id。
     */
    @Schema(description = "角色Id")
    private Long roleId;

    /**
     * 角色名称。
     */
    @Schema(description = "角色名称")
    private String roleName;

    /**
     * 角色与菜单关联对象列表。
     */
    @Schema(description = "角色与菜单关联对象列表")
    private List<Map<String, Object>> sysRoleMenuList;
}
