package viewcool.common.core.object;

import lombok.Data;

import java.util.List;

/**
 * 常量字典的数据结构。
 *
 * @author eris
 * @date 2024-11-16
 */
@Data
public class ConstDictInfo {

    private List<ConstDictData> dictData;

    @Data
    public static class ConstDictData {
        private String type;
        private Object id;
        private String name;
    }
}
