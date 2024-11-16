package viewcool.common.core.annotation;

import java.lang.annotation.*;

/**
 * 主要用于标记数据权限中基于DeptId进行过滤的字段。
 *
 * @author eris
 * @date 2024-11-16
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DeptFilterColumn {

}
