package viewcool.webadmin.upms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据权限与部门关联Dto。
 *
 * @author eris
 * @date 2024-11-16
 */
@Schema(description = "数据权限与部门关联Dto")
@Data
public class SysDataPermDeptDto {

    /**
     * 数据权限Id。
     */
    @Schema(description = "数据权限Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long dataPermId;

    /**
     * 关联部门Id。
     */
    @Schema(description = "关联部门Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long deptId;
}