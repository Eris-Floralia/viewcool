package viewcool.common.core.annotation;

import java.lang.annotation.*;

/**
 * 唯一值验证字段注解。
 *
 * @author eris
 * @date 2024-11-16
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UniqueVerifyField {

}
