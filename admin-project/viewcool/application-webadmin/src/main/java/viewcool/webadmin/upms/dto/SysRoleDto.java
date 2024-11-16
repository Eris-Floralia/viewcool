package viewcool.webadmin.upms.dto;

import viewcool.common.core.validator.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;

/**
 * 角色Dto。
 *
 * @author eris
 * @date 2024-11-16
 */
@Schema(description = "角色Dto")
@Data
public class SysRoleDto {

    /**
     * 角色Id。
     */
    @Schema(description = "角色Id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "角色Id不能为空！", groups = {UpdateGroup.class})
    private Long roleId;

    /**
     * 角色名称。
     */
    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色名称不能为空！")
    private String roleName;
}
