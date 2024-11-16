package viewcool.webadmin.upms.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import io.swagger.v3.oas.annotations.tags.Tag;
import viewcool.common.core.annotation.MyRequestBody;
import viewcool.common.core.object.*;
import viewcool.common.core.util.MyModelUtil;
import viewcool.common.core.util.MyPageUtil;
import viewcool.common.log.model.SysOperationLog;
import viewcool.common.log.service.SysOperationLogService;
import viewcool.common.log.dto.SysOperationLogDto;
import viewcool.common.log.vo.SysOperationLogVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作日志接口控制器对象。
 *
 * @author eris
 * @date 2024-11-16
 */
@Tag(name = "操作日志接口")
@Slf4j
@RestController
@RequestMapping("/admin/upms/sysOperationLog")
public class SysOperationLogController {

    @Autowired
    private SysOperationLogService operationLogService;

    /**
     * 数据权限列表。
     *
     * @param sysOperationLogDtoFilter 操作日志查询过滤对象。
     * @param orderParam               排序参数。
     * @param pageParam                分页参数。
     * @return 应答结果对象。包含操作日志列表。
     */
    @SaCheckPermission("sysOperationLog.view")
    @PostMapping("/list")
    public ResponseResult<MyPageData<SysOperationLogVo>> list(
            @MyRequestBody SysOperationLogDto sysOperationLogDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        SysOperationLog filter = MyModelUtil.copyTo(sysOperationLogDtoFilter, SysOperationLog.class);
        String orderBy = MyOrderParam.buildOrderBy(orderParam, SysOperationLog.class);
        List<SysOperationLog> operationLogList = operationLogService.getSysOperationLogList(filter, orderBy);
        List<SysOperationLogVo> operationLogVoList = MyModelUtil.copyCollectionTo(operationLogList, SysOperationLogVo.class);
        long totalCount = 0L;
        if (operationLogList instanceof Page) {
            totalCount = ((Page<SysOperationLog>) operationLogList).getTotal();
        }
        return ResponseResult.success(MyPageUtil.makeResponseData(operationLogVoList, totalCount));
    }
}
