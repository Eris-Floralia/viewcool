package viewcool.webadmin.config;

import viewcool.common.core.constant.ApplicationConstant;

import java.util.HashMap;
import java.util.Map;

/**
 * 表示数据源类型的常量对象。
 *
 * @author eris
 * @date 2024-11-16
 */
public final class DataSourceType {

    public static final int MAIN = 0;
    /**
     * 以下所有数据源的类都型是固定值。如果有冲突，请修改上面定义的业务服务的数据源类型值。
     */
    public static final int OPERATION_LOG = ApplicationConstant.OPERATION_LOG_DATASOURCE_TYPE;
    public static final int GLOBAL_DICT = ApplicationConstant.COMMON_GLOBAL_DICT_TYPE;

    private static final Map<String, Integer> TYPE_MAP = new HashMap<>(8);
    static {
        TYPE_MAP.put("main", MAIN);
        TYPE_MAP.put("operation-log", OPERATION_LOG);
        TYPE_MAP.put("global-dict", GLOBAL_DICT);
    }

    /**
     * 根据名称获取字典类型。
     *
     * @param name 数据源在配置中的名称。
     * @return 返回可用于多数据源切换的数据源类型。
     */
    public static Integer getDataSourceTypeByName(String name) {
        return TYPE_MAP.get(name);
    }

    /**
     * 私有构造函数，明确标识该常量类的作用。
     */
    private DataSourceType() {
    }
}
