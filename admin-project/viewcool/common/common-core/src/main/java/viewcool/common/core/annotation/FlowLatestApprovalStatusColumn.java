package viewcool.common.core.annotation;

import java.lang.annotation.*;

/**
 * 业务表中记录流程最后审批状态标记的字段。
 *
 * @author eris
 * @date 2024-11-16
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FlowLatestApprovalStatusColumn {

}