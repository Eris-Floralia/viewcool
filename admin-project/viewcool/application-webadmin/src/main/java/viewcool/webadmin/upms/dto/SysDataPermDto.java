package viewcool.webadmin.upms.dto;

import viewcool.common.core.validator.UpdateGroup;
import viewcool.common.core.validator.ConstDictRef;
import viewcool.common.core.constant.DataPermRuleType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;

/**
 * 数据权限Dto。
 *
 * @author eris
 * @date 2024-11-16
 */
@Schema(description = "数据权限Dto")
@Data
public class SysDataPermDto {

    /**
     * 数据权限Id。
     */
    @Schema(description = "数据权限Id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数据权限Id不能为空！", groups = {UpdateGroup.class})
    private Long dataPermId;

    /**
     * 显示名称。
     */
    @Schema(description = "显示名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "数据权限名称不能为空！")
    private String dataPermName;

    /**
     * 数据权限规则类型(0: 全部可见 1: 只看自己 2: 只看本部门 3: 本部门及子部门 4: 多部门及子部门 5: 自定义部门列表)。
     */
    @Schema(description = "数据权限规则类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数据权限规则类型不能为空！")
    @ConstDictRef(constDictClass = DataPermRuleType.class)
    private Integer ruleType;

    /**
     * 部门Id列表(逗号分隔)。
     */
    @Schema(hidden = true)
    private String deptIdListString;

    /**
     * 搜索字符串。
     */
    @Schema(description = "LIKE 模糊搜索字符串")
    private String searchString;
}