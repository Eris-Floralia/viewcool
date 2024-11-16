package viewcool.common.core.exception;

/**
 * 数据验证失败的自定义异常。
 *
 * @author eris
 * @date 2024-11-16
 */
public class DataValidationException extends RuntimeException {

    /**
     * 构造函数。
     */
    public DataValidationException() {

    }

    /**
     * 构造函数。
     *
     * @param msg 错误信息。
     */
    public DataValidationException(String msg) {
        super(msg);
    }
}
