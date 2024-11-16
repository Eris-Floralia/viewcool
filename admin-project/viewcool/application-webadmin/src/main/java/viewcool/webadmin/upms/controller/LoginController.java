package viewcool.webadmin.upms.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import viewcool.webadmin.config.ApplicationConfig;
import viewcool.webadmin.upms.bo.SysMenuExtraData;
import viewcool.webadmin.upms.service.*;
import viewcool.webadmin.upms.model.*;
import viewcool.webadmin.upms.model.constant.SysUserStatus;
import viewcool.webadmin.upms.model.constant.SysUserType;
import viewcool.webadmin.upms.model.constant.SysMenuType;
import viewcool.common.core.annotation.MyRequestBody;
import viewcool.common.core.annotation.DisableDataFilter;
import viewcool.common.core.constant.ErrorCodeEnum;
import viewcool.common.core.constant.ApplicationConstant;
import viewcool.common.core.object.*;
import viewcool.common.core.util.*;
import viewcool.common.core.upload.*;
import viewcool.common.redis.cache.SessionCacheHelper;
import viewcool.common.log.annotation.OperationLog;
import viewcool.common.log.model.constant.SysOperationLogType;
import viewcool.common.satoken.util.SaTokenUtil;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 登录接口控制器类。
 *
 * @author eris
 * @date 2024-11-16
 */
@ApiSupport(order = 1)
@Tag(name = "用户登录接口")
@DisableDataFilter
@Slf4j
@RestController
@RequestMapping("/admin/upms/login")
public class LoginController {

    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysDeptService sysDeptService;
    @Autowired
    private SysMenuService sysMenuService;
    @Autowired
    private SysPostService sysPostService;
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysDataPermService sysDataPermService;
    @Autowired
    private SysPermWhitelistService sysPermWhitelistService;
    @Autowired
    private ApplicationConfig appConfig;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private SessionCacheHelper cacheHelper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UpDownloaderFactory upDownloaderFactory;
    @Autowired
    private SaTokenUtil saTokenUtil;

    private static final String IS_ADMIN = "isAdmin";
    private static final String SHOW_NAME_FIELD = "showName";
    private static final String SHOW_ORDER_FIELD = "showOrder";
    private static final String HEAD_IMAGE_URL_FIELD = "headImageUrl";

    /**
     * 登录接口。
     *
     * @param loginName 登录名。
     * @param password  密码。
     * @return 应答结果对象，其中包括Token数据，以及菜单列表。
     */
    @Parameter(name = "loginName", example = "admin")
    @Parameter(name = "password", example = "IP3ccke3GhH45iGHB5qP9p7iZw6xUyj28Ju10rnBiPKOI35sc%2BjI7%2FdsjOkHWMfUwGYGfz8ik31HC2Ruk%2Fhkd9f6RPULTHj7VpFdNdde2P9M4mQQnFBAiPM7VT9iW3RyCtPlJexQ3nAiA09OqG%2F0sIf1kcyveSrulxembARDbDo%3D")
    @SaIgnore
    @OperationLog(type = SysOperationLogType.LOGIN, saveResponse = false)
    @PostMapping("/doLogin")
    public ResponseResult<JSONObject> doLogin(
            @MyRequestBody String loginName,
            @MyRequestBody String password) throws UnsupportedEncodingException {
        if (MyCommonUtil.existBlankArgument(loginName, password)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        ResponseResult<SysUser> verifyResult = this.verifyAndHandleLoginUser(loginName, password);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        JSONObject jsonData = this.buildLoginDataAndLogin(verifyResult.getData());
        return ResponseResult.success(jsonData);
    }

    /**
     * 登出操作。同时将Session相关的信息从缓存中删除。
     *
     * @return 应答结果对象。
     */
    @OperationLog(type = SysOperationLogType.LOGOUT)
    @PostMapping("/doLogout")
    public ResponseResult<Void> doLogout() {
        String sessionId = TokenData.takeFromRequest().getSessionId();
        redissonClient.getBucket(TokenData.takeFromRequest().getMySessionId()).deleteAsync();
        redissonClient.getBucket(RedisKeyUtil.makeSessionPermCodeKey(sessionId)).deleteAsync();
        redissonClient.getBucket(RedisKeyUtil.makeSessionPermIdKey(sessionId)).deleteAsync();
        sysDataPermService.removeDataPermCache(sessionId);
        cacheHelper.removeAllSessionCache(sessionId);
        StpUtil.logout();
        return ResponseResult.success();
    }

    /**
     * 在登录之后，通过token再次获取登录信息。
     * 用于在当前浏览器登录系统后，在新tab页中可以免密登录。
     *
     * @return 应答结果对象，其中包括JWT的Token数据，以及菜单列表。
     */
    @GetMapping("/getLoginInfo")
    public ResponseResult<JSONObject> getLoginInfo() {
        TokenData tokenData = TokenData.takeFromRequest();
        JSONObject jsonData = new JSONObject();
        jsonData.put(SHOW_NAME_FIELD, tokenData.getShowName());
        jsonData.put(IS_ADMIN, tokenData.getIsAdmin());
        if (StrUtil.isNotBlank(tokenData.getHeadImageUrl())) {
            jsonData.put(HEAD_IMAGE_URL_FIELD, tokenData.getHeadImageUrl());
        }
        Collection<SysMenu> allMenuList;
        if (BooleanUtil.isTrue(tokenData.getIsAdmin())) {
            allMenuList = sysMenuService.getAllListByOrder(SHOW_ORDER_FIELD);
        } else {
            allMenuList = sysMenuService.getMenuListByRoleIds(tokenData.getRoleIds());
        }
        List<String> menuCodeList = new LinkedList<>();
        allMenuList.stream().filter(m -> m.getExtraData() != null)
                .forEach(m -> m.setExtraObject(JSON.parseObject(m.getExtraData(), SysMenuExtraData.class)));
        this.appendResponseMenuAndPermCodeData(jsonData, allMenuList, menuCodeList);
        return ResponseResult.success(jsonData);
    }

    /**
     * 返回所有可用的权限字列表。
     * 
     * @return 整个系统所有可用的权限字列表。
     */
    @GetMapping("/getAllPermCodes")
    public ResponseResult<List<String>> getAllPermCodes() {
        List<String> permCodes = saTokenUtil.getAllPermCodes();
        return ResponseResult.success(permCodes);
    }

    /**
     * 用户修改自己的密码。
     *
     * @param oldPass 原有密码。
     * @param newPass 新密码。
     * @return 应答结果对象。
     */
    @PostMapping("/changePassword")
    public ResponseResult<Void> changePassword(
            @MyRequestBody String oldPass, @MyRequestBody String newPass) throws UnsupportedEncodingException {
        if (MyCommonUtil.existBlankArgument(newPass, oldPass)) {
            return ResponseResult.error(ErrorCodeEnum.ARGUMENT_NULL_EXIST);
        }
        TokenData tokenData = TokenData.takeFromRequest();
        SysUser user = sysUserService.getById(tokenData.getUserId());
        oldPass = URLDecoder.decode(oldPass, StandardCharsets.UTF_8.name());
        // NOTE: 第一次使用时，请务必阅读ApplicationConstant.PRIVATE_KEY的代码注释。
        // 执行RsaUtil工具类中的main函数，可以生成新的公钥和私钥。
        oldPass = RsaUtil.decrypt(oldPass, ApplicationConstant.PRIVATE_KEY);
        if (user == null || !passwordEncoder.matches(oldPass, user.getPassword())) {
            return ResponseResult.error(ErrorCodeEnum.INVALID_USERNAME_PASSWORD);
        }
        newPass = URLDecoder.decode(newPass, StandardCharsets.UTF_8.name());
        newPass = RsaUtil.decrypt(newPass, ApplicationConstant.PRIVATE_KEY);
        if (!sysUserService.changePassword(tokenData.getUserId(), newPass)) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success();
    }
    
    /**
     * 上传并修改用户头像。
     *
     * @param uploadFile 上传的头像文件。
     */
    @PostMapping("/changeHeadImage")
    public void changeHeadImage(@RequestParam("uploadFile") MultipartFile uploadFile) throws IOException {
        UploadStoreInfo storeInfo = MyModelUtil.getUploadStoreInfo(SysUser.class, HEAD_IMAGE_URL_FIELD);
        BaseUpDownloader upDownloader = upDownloaderFactory.get(storeInfo.getStoreType());
        UploadResponseInfo responseInfo = upDownloader.doUpload(null,
                appConfig.getUploadFileBaseDir(), SysUser.class.getSimpleName(), HEAD_IMAGE_URL_FIELD, true, uploadFile);
        if (BooleanUtil.isTrue(responseInfo.getUploadFailed())) {
            ResponseResult.output(HttpServletResponse.SC_FORBIDDEN,
                    ResponseResult.error(ErrorCodeEnum.UPLOAD_FAILED, responseInfo.getErrorMessage()));
            return;
        }
        responseInfo.setDownloadUri("/admin/upms/login/downloadHeadImage");
        String newHeadImage = JSONArray.toJSONString(CollUtil.newArrayList(responseInfo));
        if (!sysUserService.changeHeadImage(TokenData.takeFromRequest().getUserId(), newHeadImage)) {
            ResponseResult.output(HttpServletResponse.SC_FORBIDDEN, ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST));
            return;
        }
        ResponseResult.output(ResponseResult.success(responseInfo));
    }

    /**
     * 下载用户头像。
     *
     * @param filename 文件名。如果没有提供该参数，就从当前记录的指定字段中读取。
     * @param response Http 应答对象。
     */
    @GetMapping("/downloadHeadImage")
    public void downloadHeadImage(String filename, HttpServletResponse response) {
        try {
            UploadStoreInfo storeInfo = MyModelUtil.getUploadStoreInfo(SysUser.class, HEAD_IMAGE_URL_FIELD);
            BaseUpDownloader upDownloader = upDownloaderFactory.get(storeInfo.getStoreType());
            upDownloader.doDownload(appConfig.getUploadFileBaseDir(),
                    SysUser.class.getSimpleName(), HEAD_IMAGE_URL_FIELD, filename, true, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error(e.getMessage(), e);
        }
    }

    private ResponseResult<SysUser> verifyAndHandleLoginUser(
            String loginName, String password) throws UnsupportedEncodingException {
        String errorMessage;
        SysUser user = sysUserService.getSysUserByLoginName(loginName);
        password = URLDecoder.decode(password, StandardCharsets.UTF_8.name());
        // NOTE: 第一次使用时，请务必阅读ApplicationConstant.PRIVATE_KEY的代码注释。
        // 执行RsaUtil工具类中的main函数，可以生成新的公钥和私钥。
        password = RsaUtil.decrypt(password, ApplicationConstant.PRIVATE_KEY);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseResult.error(ErrorCodeEnum.INVALID_USERNAME_PASSWORD);
        }
        if (user.getUserStatus() == SysUserStatus.STATUS_LOCKED) {
            errorMessage = "登录失败，用户账号被锁定！";
            return ResponseResult.error(ErrorCodeEnum.INVALID_USER_STATUS, errorMessage);
        }
        if (BooleanUtil.isTrue(appConfig.getExcludeLogin())) {
            String deviceType = MyCommonUtil.getDeviceTypeWithString();
            LoginUserInfo userInfo = BeanUtil.copyProperties(user, LoginUserInfo.class);
            String loginId = SaTokenUtil.makeLoginId(userInfo);
            StpUtil.kickout(loginId, deviceType);
        }
        return ResponseResult.success(user);
    }

    private JSONObject buildLoginDataAndLogin(SysUser user) {
        TokenData tokenData = this.loginAndCreateToken(user);
        // 这里手动将TokenData存入request，便于OperationLogAspect统一处理操作日志。
        TokenData.addToRequest(tokenData);
        JSONObject jsonData = this.createResponseData(user);
        Collection<SysMenu> allMenuList;
        boolean isAdmin = user.getUserType() == SysUserType.TYPE_ADMIN;
        if (isAdmin) {
            allMenuList = sysMenuService.getAllListByOrder(SHOW_ORDER_FIELD);
        } else {
            allMenuList = sysMenuService.getMenuListByRoleIds(tokenData.getRoleIds());
        }
        allMenuList.stream().filter(m -> m.getExtraData() != null)
                .forEach(m -> m.setExtraObject(JSON.parseObject(m.getExtraData(), SysMenuExtraData.class)));
        Collection<String> permCodeList = new LinkedList<>();
        allMenuList.stream().filter(m -> m.getExtraObject() != null)
                .forEach(m -> CollUtil.addAll(permCodeList, m.getExtraObject().getPermCodeList()));
        Set<String> permSet = new HashSet<>();
        if (!isAdmin) {
            // 所有登录用户都有白名单接口的访问权限。
            CollUtil.addAll(permSet, sysPermWhitelistService.getWhitelistPermList());
        }
        List<String> menuCodeList = new LinkedList<>();
        if (!isAdmin) {
            String sessionId = tokenData.getSessionId();
            // 缓存用户的权限资源，这里缓存的是基于URL验证的权限资源，比如在线表单、工作流和数据表中的白名单资源。
            this.putUserSysPermCache(sessionId, permSet);
            // 缓存权限字字段，StpInterfaceImpl中会从缓存中读取，并交给satoken进行接口权限的验证。
            this.putUserSysPermCodeCache(sessionId, permCodeList);
            sysDataPermService.putDataPermCache(sessionId, user.getUserId(), user.getDeptId());
        }
        this.appendResponseMenuAndPermCodeData(jsonData, allMenuList, menuCodeList);
        return jsonData;
    }

    private TokenData loginAndCreateToken(SysUser user) {
        String deviceType = MyCommonUtil.getDeviceTypeWithString();
        LoginUserInfo userInfo = BeanUtil.copyProperties(user, LoginUserInfo.class);
        String loginId = SaTokenUtil.makeLoginId(userInfo);
        StpUtil.login(loginId, deviceType);
        SaSession session = StpUtil.getTokenSession();
        TokenData tokenData = this.buildTokenData(user, session.getId(), StpUtil.getLoginDevice());
        String mySessionId = RedisKeyUtil.getSessionIdPrefix(tokenData, user.getLoginName()) + MyCommonUtil.generateUuid();
        tokenData.setMySessionId(mySessionId);
        tokenData.setToken(session.getToken());
        redissonClient.getBucket(mySessionId)
                .set(JSON.toJSONString(tokenData), appConfig.getSessionExpiredSeconds(), TimeUnit.SECONDS);
        session.set(TokenData.REQUEST_ATTRIBUTE_NAME, tokenData);
        return tokenData;
    }

    private JSONObject createResponseData(SysUser user) {
        JSONObject jsonData = new JSONObject();
        jsonData.put(TokenData.REQUEST_ATTRIBUTE_NAME, StpUtil.getTokenValue());
        jsonData.put(SHOW_NAME_FIELD, user.getShowName());
        jsonData.put(IS_ADMIN, user.getUserType() == SysUserType.TYPE_ADMIN);
        if (user.getDeptId() != null) {
            SysDept dept = sysDeptService.getById(user.getDeptId());
            jsonData.put("deptName", dept.getDeptName());
        }
        if (StrUtil.isNotBlank(user.getHeadImageUrl())) {
            jsonData.put(HEAD_IMAGE_URL_FIELD, user.getHeadImageUrl());
        }
        return jsonData;
    }

    private void appendResponseMenuAndPermCodeData(
            JSONObject responseData, Collection<SysMenu> allMenuList, Collection<String> menuCodeList) {
        allMenuList.stream()
                .filter(m -> m.getExtraObject() != null && StrUtil.isNotBlank(m.getExtraObject().getMenuCode()))
                .forEach(m -> CollUtil.addAll(menuCodeList, m.getExtraObject().getMenuCode()));
        List<SysMenu> menuList = allMenuList.stream()
                .filter(m -> m.getMenuType() <= SysMenuType.TYPE_MENU).collect(Collectors.toList());
        responseData.put("menuList", menuList);
        responseData.put("permCodeList", menuCodeList);
    }

    private TokenData buildTokenData(SysUser user, String sessionId, String deviceType) {
        TokenData tokenData = new TokenData();
        tokenData.setSessionId(sessionId);
        tokenData.setUserId(user.getUserId());
        tokenData.setDeptId(user.getDeptId());
        tokenData.setLoginName(user.getLoginName());
        tokenData.setShowName(user.getShowName());
        tokenData.setIsAdmin(user.getUserType().equals(SysUserType.TYPE_ADMIN));
        tokenData.setLoginIp(IpUtil.getRemoteIpAddress(ContextUtil.getHttpRequest()));
        tokenData.setLoginTime(new Date());
        tokenData.setDeviceType(deviceType);
        tokenData.setHeadImageUrl(user.getHeadImageUrl());
        List<SysUserPost> userPostList = sysPostService.getSysUserPostListByUserId(user.getUserId());
        if (CollUtil.isNotEmpty(userPostList)) {
            Set<Long> deptPostIdSet = userPostList.stream().map(SysUserPost::getDeptPostId).collect(Collectors.toSet());
            tokenData.setDeptPostIds(StrUtil.join(",", deptPostIdSet));
            Set<Long> postIdSet = userPostList.stream().map(SysUserPost::getPostId).collect(Collectors.toSet());
            tokenData.setPostIds(StrUtil.join(",", postIdSet));
        }
        List<SysUserRole> userRoleList = sysRoleService.getSysUserRoleListByUserId(user.getUserId());
        if (CollUtil.isNotEmpty(userRoleList)) {
            Set<Long> userRoleIdSet = userRoleList.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
            tokenData.setRoleIds(StrUtil.join(",", userRoleIdSet));
        }
        return tokenData;
    }

    private void putUserSysPermCache(String sessionId, Collection<String> permUrlSet) {
        if (CollUtil.isEmpty(permUrlSet)) {
            return;
        }
        String sessionPermKey = RedisKeyUtil.makeSessionPermIdKey(sessionId);
        RSet<String> redisPermSet = redissonClient.getSet(sessionPermKey);
        redisPermSet.addAll(permUrlSet);
        redisPermSet.expire(appConfig.getSessionExpiredSeconds(), TimeUnit.SECONDS);
    }

    private void putUserSysPermCodeCache(String sessionId, Collection<String> permCodeSet) {
        if (CollUtil.isEmpty(permCodeSet)) {
            return;
        }
        String sessionPermCodeKey = RedisKeyUtil.makeSessionPermCodeKey(sessionId);
        RSet<String> redisPermSet = redissonClient.getSet(sessionPermCodeKey);
        redisPermSet.addAll(permCodeSet);
        redisPermSet.expire(appConfig.getSessionExpiredSeconds(), TimeUnit.SECONDS);
    }
}
