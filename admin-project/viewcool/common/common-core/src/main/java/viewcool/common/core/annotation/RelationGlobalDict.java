package viewcool.common.core.annotation;

import java.lang.annotation.*;

/**
 * 全局字典关联。
 *
 * @author eris
 * @date 2024-11-16
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RelationGlobalDict {

    /**
     * 是否为多选。多选为true时，返回的数据类型为List<Map<String, Object>>。
     *
     * @return true是，否则false。
     */
    boolean multiSelect() default false;

    /**
     * 当前对象的关联Id字段名称。
     *
     * @return 当前对象的关联Id字段名称。
     */
    String masterIdField();

    /**
     * 全局字典编码。
     *
     * @return 全局字典编码。空表示为不使用全局字典。
     */
    String dictCode();
}
