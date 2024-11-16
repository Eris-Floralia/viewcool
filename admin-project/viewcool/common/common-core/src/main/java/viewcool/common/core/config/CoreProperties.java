package viewcool.common.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * common-core的配置属性类。
 *
 * @author eris
 * @date 2024-11-16
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "common-core")
public class CoreProperties {

    public static final String MYSQL_TYPE = "mysql";
    public static final String POSTGRESQL_TYPE = "postgresql";
    public static final String ORACLE_TYPE = "oracle";
    public static final String SQLSERVER_TYPE = "sqlserver";
    public static final String DM_TYPE = "dm8";
    public static final String KINGBASE_TYPE = "kingbase";
    public static final String OPENGAUSS_TYPE = "opengauss";

    /**
     * 数据库类型。
     */
    private String databaseType = MYSQL_TYPE;

    /**
     * 是否为MySQL。
     *
     * @return 是返回true，否则false。
     */
    public boolean isMySql() {
        return this.databaseType.equals(MYSQL_TYPE);
    }

    /**
     * 是否为PostgreSQl。
     *
     * @return 是返回true，否则false。
     */
    public boolean isPostgresql() {
        return this.databaseType.equals(POSTGRESQL_TYPE);
    }

    /**
     * 是否为Oracle。
     *
     * @return 是返回true，否则false。
     */
    public boolean isOracle() {
        return this.databaseType.equals(ORACLE_TYPE);
    }

    /**
     * 是否为SQLServer。
     *
     * @return 是返回true，否则false。
     */
    public boolean isSqlServer() {
        return this.databaseType.equals(SQLSERVER_TYPE);
    }

    /**
     * 是否为达梦8。
     *
     * @return 是返回true，否则false。
     */
    public boolean isDm() {
        return this.databaseType.equals(DM_TYPE);
    }

    /**
     * 是否为人大金仓。
     *
     * @return 是返回true，否则false。
     */
    public boolean isKingbase() {
        return this.databaseType.equals(KINGBASE_TYPE);
    }

    /**
     * 是否为华为高斯。
     *
     * @return 是返回true，否则false。
     */
    public boolean isOpenGauss() {
        return this.databaseType.equals(OPENGAUSS_TYPE);
    }
}
