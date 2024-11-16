package viewcool.webadmin.upms.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.alibaba.fastjson.TypeReference;
import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import lombok.extern.slf4j.Slf4j;
import viewcool.webadmin.upms.dto.SysDataPermDto;
import viewcool.webadmin.upms.dto.SysUserDto;
import viewcool.webadmin.upms.vo.SysDataPermVo;
import viewcool.webadmin.upms.vo.SysUserVo;
import viewcool.webadmin.upms.model.SysDataPerm;
import viewcool.webadmin.upms.model.SysUser;
import viewcool.webadmin.upms.service.SysDataPermService;
import viewcool.webadmin.upms.service.SysUserService;
import viewcool.common.core.validator.UpdateGroup;
import viewcool.common.core.constant.ErrorCodeEnum;
import viewcool.common.core.object.*;
import viewcool.common.core.util.*;
import viewcool.common.core.annotation.MyRequestBody;
import viewcool.common.log.annotation.OperationLog;
import viewcool.common.log.model.constant.SysOperationLogType;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.groups.Default;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据权限接口控制器对象。
 *
 * @author eris
 * @date 2024-11-16
 */
@Tag(name = "数据权限管理接口")
@Slf4j
@RestController
@RequestMapping("/admin/upms/sysDataPerm")
public class SysDataPermController {

    @Autowired
    private SysDataPermService sysDataPermService;
    @Autowired
    private SysUserService sysUserService;

    /**
     * 添加新数据权限操作。
     *
     * @param sysDataPermDto   新增对象。
     * @param deptIdListString 数据权限关联的部门Id列表，多个之间逗号分隔。
     * @param menuIdListString 数据权限关联的菜单Id列表，多个之间逗号分隔。
     * @return 应答结果对象。包含新增数据权限对象的主键Id。
     */
    @ApiOperationSupport(ignoreParameters = {
            "sysDataPermDto.dataPermId",
            "sysDataPermDto.createTimeStart",
            "sysDataPermDto.createTimeEnd",
            "sysDataPermDto.searchString"})
    @SaCheckPermission("sysDataPerm.add")
    @OperationLog(type = SysOperationLogType.ADD)
    @PostMapping("/add")
    public ResponseResult<Long> add(
            @MyRequestBody SysDataPermDto sysDataPermDto,
            @MyRequestBody String deptIdListString,
            @MyRequestBody String menuIdListString) {
        String errorMessage = MyCommonUtil.getModelValidationError(sysDataPermDto);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        SysDataPerm sysDataPerm = MyModelUtil.copyTo(sysDataPermDto, SysDataPerm.class);
        CallResult result = sysDataPermService.verifyRelatedData(sysDataPerm, deptIdListString, menuIdListString);
        if (!result.isSuccess()) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, result.getErrorMessage());
        }
        Set<Long> menuIdSet = null;
        if (result.getData() != null) {
            menuIdSet = result.getData().getObject("menuIdSet", new TypeReference<Set<Long>>(){});
        }
        Set<Long> deptIdSet = null;
        if (result.getData() != null) {
            deptIdSet = result.getData().getObject("deptIdSet", new TypeReference<Set<Long>>(){});
        }
        sysDataPermService.saveNew(sysDataPerm, deptIdSet, menuIdSet);
        return ResponseResult.success(sysDataPerm.getDataPermId());
    }

    /**
     * 更新数据权限操作。
     *
     * @param sysDataPermDto   更新的数据权限对象。
     * @param deptIdListString 数据权限关联的部门Id列表，多个之间逗号分隔。
     * @param menuIdListString 数据权限关联的菜单Id列表，多个之间逗号分隔。
     * @return 应答结果对象。
     */
    @ApiOperationSupport(ignoreParameters = {
            "sysDataPermDto.createTimeStart",
            "sysDataPermDto.createTimeEnd",
            "sysDataPermDto.searchString"})
    @SaCheckPermission("sysDataPerm.update")
    @OperationLog(type = SysOperationLogType.UPDATE)
    @PostMapping("/update")
    public ResponseResult<Void> update(
            @MyRequestBody SysDataPermDto sysDataPermDto,
            @MyRequestBody String deptIdListString,
            @MyRequestBody String menuIdListString) {
        String errorMessage = MyCommonUtil.getModelValidationError(sysDataPermDto, Default.class, UpdateGroup.class);
        if (errorMessage != null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        SysDataPerm originalSysDataPerm = sysDataPermService.getById(sysDataPermDto.getDataPermId());
        if (originalSysDataPerm == null) {
            errorMessage = "数据验证失败，当前数据权限并不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        SysDataPerm sysDataPerm = MyModelUtil.copyTo(sysDataPermDto, SysDataPerm.class);
        CallResult result = sysDataPermService.verifyRelatedData(sysDataPerm, deptIdListString, menuIdListString);
        if (!result.isSuccess()) {
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, result.getErrorMessage());
        }
        Set<Long> deptIdSet = null;
        if (result.getData() != null) {
            deptIdSet = result.getData().getObject("deptIdSet", new TypeReference<Set<Long>>(){});
        }
        Set<Long> menuIdSet = null;
        if (result.getData() != null) {
            menuIdSet = result.getData().getObject("menuIdSet", new TypeReference<Set<Long>>(){});
        }
        if (!sysDataPermService.update(sysDataPerm, originalSysDataPerm, deptIdSet, menuIdSet)) {
            errorMessage = "更新失败，数据不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        return ResponseResult.success();
    }

    /**
     * 删除数据权限操作。
     *
     * @param dataPermId 待删除数据权限主键Id。
     * @return 应答数据结果。
     */
    @SaCheckPermission("sysDataPerm.delete")
    @OperationLog(type = SysOperationLogType.DELETE)
    @PostMapping("/delete")
    public ResponseResult<Void> delete(@MyRequestBody Long dataPermId) {
        if (MyCommonUtil.existBlankArgument(dataPermId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        if (!sysDataPermService.remove(dataPermId)) {
            String errorMessage = "数据操作失败，数据权限不存在，请刷新后重试！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        return ResponseResult.success();
    }

    /**
     * 查看数据权限列表。
     *
     * @param sysDataPermDtoFilter 数据权限查询过滤对象。
     * @param orderParam           排序参数。
     * @param pageParam            分页参数。
     * @return 应答结果对象。包含数据权限列表。
     */
    @SaCheckPermission("sysDataPerm.view")
    @PostMapping("/list")
    public ResponseResult<MyPageData<SysDataPermVo>> list(
            @MyRequestBody SysDataPermDto sysDataPermDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        SysDataPerm filter = MyModelUtil.copyTo(sysDataPermDtoFilter, SysDataPerm.class);
        String orderBy = MyOrderParam.buildOrderBy(orderParam, SysDataPerm.class);
        List<SysDataPerm> dataPermList = sysDataPermService.getSysDataPermListWithRelation(filter, orderBy);
        List<SysDataPermVo> dataPermVoList = MyModelUtil.copyCollectionTo(dataPermList, SysDataPermVo.class);
        long totalCount = 0L;
        if (dataPermList instanceof Page) {
            totalCount = ((Page<SysDataPerm>) dataPermList).getTotal();
        }
        return ResponseResult.success(MyPageUtil.makeResponseData(dataPermVoList, totalCount));
    }

    /**
     * 查看单条数据权限详情。
     *
     * @param dataPermId 数据权限的主键Id。
     * @return 应答结果对象，包含数据权限的详情。
     */
    @SaCheckPermission("sysDataPerm.view")
    @GetMapping("/view")
    public ResponseResult<SysDataPermVo> view(@RequestParam Long dataPermId) {
        if (MyCommonUtil.existBlankArgument(dataPermId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        SysDataPerm dataPerm = sysDataPermService.getByIdWithRelation(dataPermId, MyRelationParam.full());
        if (dataPerm == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        SysDataPermVo dataPermVo = MyModelUtil.copyTo(dataPerm, SysDataPermVo.class);
        return ResponseResult.success(dataPermVo);
    }

    /**
     * 拥有指定数据权限的用户列表。
     *
     * @param dataPermId       数据权限Id。
     * @param sysUserDtoFilter 用户过滤对象。
     * @param orderParam       排序参数。
     * @param pageParam        分页参数。
     * @return 应答结果对象，包含用户列表数据。
     */
    @SaCheckPermission("sysDataPerm.view")
    @PostMapping("/listDataPermUser")
    public ResponseResult<MyPageData<SysUserVo>> listDataPermUser(
            @MyRequestBody Long dataPermId,
            @MyRequestBody SysUserDto sysUserDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        ResponseResult<Void> verifyResult = this.doDataPermUserVerify(dataPermId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        SysUser filter = MyModelUtil.copyTo(sysUserDtoFilter, SysUser.class);
        String orderBy = MyOrderParam.buildOrderBy(orderParam, SysUser.class);
        List<SysUser> userList = sysUserService.getSysUserListByDataPermId(dataPermId, filter, orderBy);
        return ResponseResult.success(MyPageUtil.makeResponseData(userList, SysUserVo.class));
    }

    /**
     * 获取不包含指定数据权限Id的用户列表。
     * 用户和数据权限是多对多关系，当前接口将返回没有赋值指定DataPermId的用户列表。可用于给数据权限添加新用户。
     *
     * @param dataPermId       数据权限主键Id。
     * @param sysUserDtoFilter 用户数据的过滤对象。
     * @param orderParam       排序参数。
     * @param pageParam        分页参数。
     * @return 应答结果对象，包含用户列表数据。
     */
    @SaCheckPermission("sysDataPerm.update")
    @PostMapping("/listNotInDataPermUser")
    public ResponseResult<MyPageData<SysUserVo>> listNotInDataPermUser(
            @MyRequestBody Long dataPermId,
            @MyRequestBody SysUserDto sysUserDtoFilter,
            @MyRequestBody MyOrderParam orderParam,
            @MyRequestBody MyPageParam pageParam) {
        ResponseResult<Void> verifyResult = this.doDataPermUserVerify(dataPermId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize());
        }
        SysUser filter = MyModelUtil.copyTo(sysUserDtoFilter, SysUser.class);
        String orderBy = MyOrderParam.buildOrderBy(orderParam, SysUser.class);
        List<SysUser> userList =
                sysUserService.getNotInSysUserListByDataPermId(dataPermId, filter, orderBy);
        return ResponseResult.success(MyPageUtil.makeResponseData(userList, SysUserVo.class));
    }

    /**
     * 为指定数据权限添加用户列表。该操作可同时给一批用户赋值数据权限，并在同一事务内完成。
     *
     * @param dataPermId       数据权限主键Id。
     * @param userIdListString 逗号分隔的用户Id列表。
     * @return 应答结果对象。
     */
    @SaCheckPermission("sysDataPerm.update")
    @OperationLog(type = SysOperationLogType.ADD_M2M)
    @PostMapping("/addDataPermUser")
    public ResponseResult<Void> addDataPermUser(
            @MyRequestBody Long dataPermId, @MyRequestBody String userIdListString) {
        if (MyCommonUtil.existBlankArgument(dataPermId, userIdListString)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        Set<Long> userIdSet =
                Arrays.stream(userIdListString.split(",")).map(Long::valueOf).collect(Collectors.toSet());
        if (!sysDataPermService.existId(dataPermId)
                || !sysUserService.existUniqueKeyList("userId", userIdSet)) {
            return ResponseResult.error(ErrorCodeEnum.INVALID_RELATED_RECORD_ID);
        }
        sysDataPermService.addDataPermUserList(dataPermId, userIdSet);
        return ResponseResult.success();
    }

    /**
     * 为指定用户移除指定数据权限。
     *
     * @param dataPermId 指定数据权限主键Id。
     * @param userId     指定用户主键Id。
     * @return 应答数据结果。
     */
    @SaCheckPermission("sysDataPerm.update")
    @OperationLog(type = SysOperationLogType.DELETE_M2M)
    @PostMapping("/deleteDataPermUser")
    public ResponseResult<Void> deleteDataPermUser(
            @MyRequestBody Long dataPermId, @MyRequestBody Long userId) {
        if (MyCommonUtil.existBlankArgument(dataPermId, userId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        if (!sysDataPermService.removeDataPermUser(dataPermId, userId)) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success();
    }

    /**
     * 以字典形式返回全部数据权限管理数据集合。字典的键值为[dataPermId, dataPermName]。
     * 白名单接口，登录用户均可访问。
     *
     * @param filter 过滤对象。
     * @return 应答结果对象，包含的数据为 List<Map<String, String>>，map中包含两条记录，key的值分别是id和name，value对应具体数据。
     */
    @GetMapping("/listDict")
    public ResponseResult<List<Map<String, Object>>> listDict(@ParameterObject SysDataPermDto filter) {
        List<SysDataPerm> resultList =
                sysDataPermService.getListByFilter(MyModelUtil.copyTo(filter, SysDataPerm.class));
        return ResponseResult.success(
                MyCommonUtil.toDictDataList(resultList, SysDataPerm::getDataPermId, SysDataPerm::getDataPermName));
    }

    private ResponseResult<Void> doDataPermUserVerify(Long dataPermId) {
        if (MyCommonUtil.existBlankArgument(dataPermId)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        if (!sysDataPermService.existId(dataPermId)) {
            return ResponseResult.error(ErrorCodeEnum.INVALID_RELATED_RECORD_ID);
        }
        return ResponseResult.success();
    }
}
