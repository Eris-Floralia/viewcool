package viewcool.common.core.upload;

import lombok.Data;

/**
 * 上传数据存储信息对象。这里之所以使用对象，主要是便于今后扩展。
 *
 * @author eris
 * @date 2024-11-16
 */
@Data
public class UploadStoreInfo {

    /**
     * 是否支持上传。
     */
    private boolean supportUpload;
    /**
     * 上传数据存储类型。
     */
    private UploadStoreTypeEnum storeType;
}
