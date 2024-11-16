package viewcool.common.core.base.vo;

import lombok.Data;

import java.util.Date;

/**
 * VO对象的公共基类。
 *
 * @author eris
 * @date 2024-11-16
 */
@Data
public class BaseVo {

    /**
     * 创建者Id。
     */
    private Long createUserId;

    /**
     * 创建时间。
     */
    private Date createTime;

    /**
     * 更新者Id。
     */
    private Long updateUserId;

    /**
     * 更新时间。
     */
    private Date updateTime;
}
