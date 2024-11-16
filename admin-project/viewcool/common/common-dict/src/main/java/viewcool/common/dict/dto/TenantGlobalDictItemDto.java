package viewcool.common.dict.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户全局系统字典项目Dto。
 *
 * @author eris
 * @date 2024-11-16
 */
@Schema(description = "租户全局系统字典项目Dto")
@EqualsAndHashCode(callSuper = true)
@Data
public class TenantGlobalDictItemDto extends GlobalDictItemDto {

}
