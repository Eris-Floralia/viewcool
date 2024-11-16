package viewcool.common.core.object;

import lombok.Data;

/**
 * 编码字段的编码规则。
 *
 * @author eris
 * @date 2024-11-16
 */
@Data
public class ColumnEncodedRule {

    /**
     * 是否显示是计算并回显。
     */
    private Boolean calculateWhenView;

    /**
     * 字段名，目前只有路由表单计算时使用。
     */
    private String fieldName;

    /**
     * 前缀。
     */
    private String prefix;

    /**
     * 精确到YEAR/MONTH/DAYS/HOURS/MINUTES/SECONDS
     */
    private String precisionTo;

    /**
     * 中缀。
     */
    private String middle;

    /**
     * 流水序号的字符宽度，不足的前面补0。
     */
    private Integer idWidth;
}
