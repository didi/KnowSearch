package com.didichuxing.datachannel.arius.admin.core.service.app.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.AppConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.AppDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppSearchTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.AppConfigPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.AppPO;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.event.app.AppAddEvent;
import com.didichuxing.datachannel.arius.admin.common.event.app.AppDeleteEvent;
import com.didichuxing.datachannel.arius.admin.common.event.app.AppEditEvent;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.util.VerifyCodeFactory;
import com.didichuxing.datachannel.arius.admin.core.component.ResponsibleConvertTool;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppUserInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.extend.employee.EmployeeService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateInfoService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.AppConfigDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.AppDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.APP;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.APP_CONFIG;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum.*;
import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.yesOrNo;

@Service
public class AppServiceImpl implements AppService {

    private static final ILog          LOGGER                      = LogFactory.getLog(AppServiceImpl.class);

    private static final Integer       VERIFY_CODE_LENGTH          = 15;

    private static final Integer       APP_QUERY_THRESHOLD_DEFAULT = 100;

    private static final String APP_NOT_EXIST = "应用不存在";

    @Autowired
    private AppDAO                      appDAO;

    @Autowired
    private EmployeeService             employeeService;

    @Autowired
    private OperateRecordService        operateRecordService;

    @Autowired
    private ResponsibleConvertTool      responsibleConvertTool;

    @Autowired
    private AppConfigDAO                appConfigDAO;

    @Autowired
    private AppUserInfoService          appUserInfoService;

    @Autowired
    private ClusterLogicService         clusterLogicService;

    @Autowired
    private IndexTemplateInfoService indexTemplateInfoService;

    @Autowired
    private AppClusterLogicAuthService logicClusterAuthService;

    private Cache<String, List<?>>      appListCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(100).build();


    /**
     * 查询app详细信息
     * @return 返回app列表
     */
    @Override
    public List<App> listApps() {
        return responsibleConvertTool.list2List(appDAO.listByCondition(new AppPO()), App.class);
    }

    @Override
    public List<App> listAppWithCache() {
        try {
            return (List<App>) appListCache.get("listApp", this::listApps);
        } catch (ExecutionException e) {
            return listApps();
        }
    }

    /**
     * 查询app详细信息
     * @return 返回app构成的map，key为appId, value为app
     */
    @Override
    public Map<Integer, App> getAppsMap() {
        return ConvertUtil.list2Map( listApps(), App::getId);
    }

    /**
     * 新建APP
     * @param appDTO   dto
     * @param operator 操作人 邮箱前缀
     * @return 成功 true  失败 false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> registerApp(AppDTO appDTO, String operator) {
        Result<Void> checkResult = validateApp(appDTO, ADD);
        if (checkResult.failed()) {
            LOGGER.warn("class=AppServiceImpl||method=addApp||fail msg={}", checkResult.getMessage());
            return Result.buildFrom(checkResult);
        }
        return addAppWithoutCheck(appDTO, operator);
    }

    /**
     * 验证APP参数是否合法
     *
     * @param appDTO         dto
     * @param operation 是否校验null参数;  新建的时候需要校验,编辑的时候不需要校验
     * @return 参数合法返回
     */
    @Override
    public Result<Void> validateApp(AppDTO appDTO, OperationEnum operation) {
        if (AriusObjUtils.isNull(appDTO)) {
            return Result.buildParamIllegal("应用信息为空");
        }
        Result<Void> validateAppFieldIsNullResult = validateAppFieldIsNull(appDTO);
        if (validateAppFieldIsNullResult.failed()) {return validateAppFieldIsNullResult;}

        if (AriusObjUtils.isNull(appDTO.getResponsible()) || employeeService.checkUsers(appDTO.getResponsible()).failed()) {
            return Result.buildParamIllegal("责任人非法");
        }

        if (ADD.equals(operation)) {
            if (AriusObjUtils.isNull(appDTO.getName())) {
                return Result.buildParamIllegal("应用名称为空");
            }
        } else if (EDIT.equals(operation)) {
            if (AriusObjUtils.isNull(appDTO.getId())) {
                return Result.buildParamIllegal("应用ID为空");
            }

            AppPO oldApp = appDAO.getById(appDTO.getId());
            if (AriusObjUtils.isNull(oldApp)) {
                return Result.buildNotExist(APP_NOT_EXIST);
            }
        }

        if (appDTO.getIsRoot() == null || !AdminConstant.yesOrNo(appDTO.getIsRoot())) {
            return Result.buildParamIllegal("超管标记非法");
        }

        AppSearchTypeEnum searchTypeEnum = AppSearchTypeEnum.valueOf(appDTO.getSearchType());
        if (searchTypeEnum.equals(AppSearchTypeEnum.UNKNOWN)) {
            return Result.buildParamIllegal("查询模式非法");
        }
        if (StringUtils.isBlank(appDTO.getVerifyCode())) {
            return Result.buildParamIllegal("校验码不能为空");
        }
        // 名字不能重复
        AppPO oldAppPO = getByName(appDTO.getName());
        if (oldAppPO != null && !oldAppPO.getId().equals(appDTO.getId())) {
            return Result.buildDuplicate("应用名称重复");
        }

        return Result.buildSucc();
    }

    /**
     * 编辑APP
     * @param appDTO   dto
     * @param operator 操作人 邮箱前缀
     * @return 成功 true  失败 false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> editApp(AppDTO appDTO, String operator) {
        Result<Void> checkResult = validateApp(appDTO, EDIT);
        if (checkResult.failed()) {
            LOGGER.warn("class=AppServiceImpl||method=updateApp||msg={}||msg=check fail", checkResult.getMessage());
            return checkResult;
        }
        return editAppWithoutCheck(appDTO, operator);
    }

    /**
     * 删除APP
     * @param appId    APPID
     * @param operator 操作人 邮箱前缀
     * @return 成功 true  失败 false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteAppById(int appId, String operator) {

        if (hasOwnLogicCluster(appId)) {
            return Result.build(ResultType.IN_USE_ERROR.getCode(), "APP申请了集群，不能删除");
        }

        if (hasOwnTemplate(appId)) {
            return Result.build(ResultType.IN_USE_ERROR.getCode(), "APP申请了模板，不能删除");
        }

        AppPO oldPO = appDAO.getById(appId);
        boolean succ = appDAO.delete(appId) == 1;
        if (succ) {
            operateRecordService.save(APP, DELETE, appId, "", operator);
            SpringTool.publish(new AppDeleteEvent(this, responsibleConvertTool.obj2Obj(oldPO, App.class)));
        }

        return Result.build(succ);
    }

    /**
     * 初始化APP配置
     *
     * @param appId APPID
     * @return 成功 true  失败false
     */
    @Override
    public Result<Void> initConfig(Integer appId) {
        AppConfigPO param = new AppConfigPO();
        param.setAppId(appId);
        param.setDslAnalyzeEnable(AdminConstant.YES);
        param.setIsSourceSeparated(AdminConstant.NO);
        param.setAggrAnalyzeEnable(AdminConstant.YES);
        param.setAnalyzeResponseEnable(AdminConstant.YES);

        return Result.build(appConfigDAO.update(param) == 1);
    }

    /**
     * 获取appid配置信息
     *
     * @param appId APPID
     * @return 配置信息
     */
    @Override
    public AppConfig getAppConfig(int appId) {
        AppPO oldApp = appDAO.getById(appId);
        if (oldApp == null) {
            LOGGER.warn("class=AppServiceImpl||method=getConfig||appId={}||msg=appid not exist!", appId);
            return null;
        }

        AppConfigPO oldConfigPO = appConfigDAO.getByAppId(appId);
        if (oldConfigPO == null) {
            initConfig(appId);
            oldConfigPO = appConfigDAO.getByAppId(appId);
        }

        return responsibleConvertTool.obj2Obj(oldConfigPO, AppConfig.class);
    }

    /**
     * 获取所有应用的配置
     *
     * @return list
     */
    @Override
    public List<AppConfig> listConfig() {
        return ConvertUtil.list2List(appConfigDAO.listAll(), AppConfig.class);
    }

    @Override
    public List<AppConfig> listConfigWithCache() {
        try {
            return (List<AppConfig>) appListCache.get("listConfig", this::listConfig);
        } catch (ExecutionException e) {
            return listConfig();
        }
    }

    /**
     * 修改APP配置
     * @param configDTO 配置信息
     * @param operator  操作人
     * @return 成功 true  失败  false
     * <p>
     * NotExistException
     * APP不存在
     * IllegalArgumentException
     * 参数不合理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateAppConfig(AppConfigDTO configDTO, String operator) {
        Result<Void> checkResult = checkConfigParam(configDTO);
        if (checkResult.failed()) {
            LOGGER.warn("class=AppServiceImpl||method=updateConfig||msg={}||msg=check fail!", checkResult.getMessage());
            return checkResult;
        }

        AppPO oldApp = appDAO.getById(configDTO.getAppId());
        if (oldApp == null) {
            return Result.buildNotExist(APP_NOT_EXIST);
        }

        AppConfigPO oldConfigPO = appConfigDAO.getByAppId(configDTO.getAppId());

        boolean succ = (1 == appConfigDAO.update(ConvertUtil.obj2Obj(configDTO, AppConfigPO.class)));
        if (succ) {
            operateRecordService.save(APP_CONFIG, EDIT, configDTO.getAppId(),
                AriusObjUtils.findChangedWithClear(oldConfigPO, configDTO), operator);
        }

        return Result.build(succ);
    }

    /**
     * 校验appid是否存在
     * @param appId 应用id
     * @return true/false
     */
    @Override
    public boolean isAppExists(Integer appId) {
        return appDAO.getById(appId) != null;
    }

    @Override
    public boolean isAppExists(App app) {
        return app != null;
    }

    @Override
    public boolean isSuperApp(Integer appId) {
        App appById = getAppById(appId);
        if (AriusObjUtils.isNull(appById)) {
            return false;
        }

        return appById.getIsRoot() == 1;
    }

    @Override
    public boolean isSuperApp(App app) {
        if (AriusObjUtils.isNull(app)) {
            return false;
        }

        return app.getIsRoot() == 1;
    }

    /**
     * 指定id查询
     * @param appId appID
     * @return app  如果不存在返回null
     */
    @Override
    public App getAppById(Integer appId) {
        return responsibleConvertTool.obj2Obj(appDAO.getById(appId), App.class);
    }

    @Override
    public String getAppName(Integer appId) {
        if (null == appId) { return null;}
        App app = getAppById(appId);
        return app == null ? null : app.getName();
    }

    /**
     * 用户登录接口
     *
     * @param appId      appId
     * @param verifyCode 校验码
     * @param operator   登陆人
     * @return result
     */
    @Override
    public Result<Void> login(Integer appId, String verifyCode, String operator) {
        AppPO appPO = appDAO.getById(appId);

        if (appPO == null) {
            return Result.buildNotExist(APP_NOT_EXIST);
        }

        if (StringUtils.isBlank(verifyCode) || !appPO.getVerifyCode().equals(verifyCode)) {
            return Result.buildParamIllegal("校验码错误");
        }

        if (StringUtils.isBlank(operator)) {
            return Result.buildParamIllegal("登陆人为空");
        }

        // 记录appid登陆的人员信息
        appUserInfoService.recordAppidAndUser(appId, operator);

        return Result.buildSucc();
    }

    /**
     * 校验验证码
     *
     * @param appId     app
     * @param verifyCode 验证码
     * @return result
     */
    @Override
    public Result<Void> verifyAppCode(Integer appId, String verifyCode) {
        AppPO appPO = appDAO.getById(appId);

        if (appPO == null) {
            return Result.buildNotExist(APP_NOT_EXIST);
        }

        if (StringUtils.isBlank(verifyCode) || !appPO.getVerifyCode().equals(verifyCode)) {
            return Result.buildParamIllegal("校验码错误");
        }

        return Result.buildSucc();
    }

    /**
     * 查询用户可以免密登陆的APP列表,包含校验码谨慎使用
     *
     * @param user 用户名
     * @return appList
     */
    @Override
    public List<App> getUserLoginWithoutCodeApps(String user) {
        List<AppUserInfo> userInfos = appUserInfoService.getByUser(user);

        if (CollectionUtils.isEmpty(userInfos)) {
            return Lists.newArrayList();
        }

        List<Integer> appIds = userInfos.stream().map(AppUserInfo::getAppId).collect(Collectors.toList());
        List<App> apps = responsibleConvertTool.list2List(appDAO.listByIds(appIds), App.class);

        // 按着用户的登陆时间降序排列
        Map<Integer, AppUserInfo> appId2appUserInfoMap = ConvertUtil.list2Map(userInfos, AppUserInfo::getAppId);
        apps.sort((o1, o2) -> {
            AppUserInfo o1UserInfo = appId2appUserInfoMap.get(o1.getId());
            AppUserInfo o2UserInfo = appId2appUserInfoMap.get(o2.getId());
            return o2UserInfo.getLastLoginTime().compareTo(o1UserInfo.getLastLoginTime());
        });

        return apps;
    }

    /**
     * 根据责任人查询
     *
     * @param responsible
     * @return
     */
    @Override
    public List<App> getAppsByResponsibleId(Long responsible) {
        return responsibleConvertTool.list2List(appDAO.listByResponsible(String.valueOf(responsible)), App.class);
    }

    @Override
    public App getAppByName(String name) {
        AppPO appPO = getByName(name);
        if (appPO == null) {
            return null;
        }
        return responsibleConvertTool.obj2Obj(appPO, App.class);
    }

    @Override
    public List<App> getAppsByLowestLogicClusterAuth(Long logicClusterId, AppClusterLogicAuthEnum logicClusterAuth) {
        if (logicClusterId == null || logicClusterAuth == null) {
            return new ArrayList<>();
        }

        // 要求的最低权限是无权限，所有APP否符合
        if (logicClusterAuth == AppClusterLogicAuthEnum.NO_PERMISSIONS) {
            return listApps();
        }

        // 获取集群的全部权限点，然后筛选
        List<AppClusterLogicAuth> auths = logicClusterAuthService.getLogicClusterAuths(logicClusterId, null);
        // 筛选出权限大于指定值的app
        List<Integer> appIds = auths.stream().filter(appLogicClusterAuth -> AppClusterLogicAuthEnum
            .valueOf(appLogicClusterAuth.getType()).higherOrEqual(logicClusterAuth))
            .map(AppClusterLogicAuth::getAppId).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(appIds)) {
            return new ArrayList<>();
        }

        // 取出符合条件的APP
        return responsibleConvertTool.list2List(appDAO.listByIds(appIds), App.class);
    }

    /**************************************** private method ****************************************************/
    private AppPO getByName(String name) {
        if (name == null) {
            return null;
        }

        List<AppPO> appPOs = appDAO.listByName(name);
        if (CollectionUtils.isEmpty(appPOs)) {
            return null;
        }
        return appPOs.get(0);
    }

    private void initParam(AppDTO appDTO) {
        // 默认不是root用户
        if (appDTO.getIsRoot() == null) {
            appDTO.setIsRoot(AdminConstant.NO);
        }

        if (StringUtils.isBlank(appDTO.getDataCenter())) {
            appDTO.setDataCenter(EnvUtil.getDC().getCode());
        }

        // 默认cluster=""
        if (appDTO.getCluster() == null) {
            appDTO.setCluster("");
        }

        if (appDTO.getDepartmentId() == null) {
            appDTO.setDepartmentId("");
        }

        if (appDTO.getDepartment() == null) {
            appDTO.setDepartment("");
        }

        // 默认索引模式
        if (appDTO.getSearchType() == null) {
            appDTO.setSearchType(AppSearchTypeEnum.TEMPLATE.getCode());
        }

        // 生成默认的校验码
        if (StringUtils.isBlank(appDTO.getVerifyCode())) {
            appDTO.setVerifyCode(VerifyCodeFactory.get(VERIFY_CODE_LENGTH));
        }

        // 设置默认查询限流值
        if (appDTO.getQueryThreshold() == null) {
            appDTO.setQueryThreshold(APP_QUERY_THRESHOLD_DEFAULT);
        }
    }

    /**
     * 新建APP  并不会校验参数是否合法  与validateApp配合使用
     * @param appDTO   dto
     * @param operator 操作人 邮箱前缀
     * @return 成功 true  失败 false
     */
    private Result<Integer> addAppWithoutCheck(AppDTO appDTO, String operator) {
        initParam(appDTO);

        AppPO param = responsibleConvertTool.obj2Obj(appDTO, AppPO.class);
        boolean succ = (appDAO.insert(param) == 1);
        if (succ) {
            // 默认配置
            if (initConfig(param.getId()).failed()) {
                LOGGER.warn("class=AppServiceImpl||method=addAppWithoutCheck||appid={}||msg=initConfig fail",
                        param.getId());
            }
            // 操作记录
            operateRecordService.save(APP, ADD, param.getId(), "", operator);
            appUserInfoService.recordAppidAndUser(param.getId(), appDTO.getResponsible());
            SpringTool.publish(
                    new AppAddEvent(this, responsibleConvertTool.obj2Obj(appDAO.getById(param.getId()), App.class)));
        }

        return Result.build(succ, param.getId());
    }

    /**
     * 编辑APP 并不会校验参数是否合法  与validateApp配合使用
     * @param appDTO   dto
     * @param operator 操作人 邮箱前缀
     * @return 成功 true  失败 false
     */
    private Result<Void> editAppWithoutCheck(AppDTO appDTO, String operator) {
        AppPO oldPO = appDAO.getById(appDTO.getId());
        AppPO param = responsibleConvertTool.obj2Obj(appDTO, AppPO.class);

        boolean succeed = (appDAO.update(param) == 1);
        if (succeed) {
            operateRecordService.save(APP, EDIT, appDTO.getId(), AriusObjUtils.findChangedWithClear(oldPO, param), operator);
            appUserInfoService.recordAppidAndUser(appDTO.getId(), appDTO.getResponsible());
            SpringTool.publish(new AppEditEvent(this, responsibleConvertTool.obj2Obj(oldPO, App.class),
                    responsibleConvertTool.obj2Obj(appDAO.getById(param.getId()), App.class)));
        }
        return Result.build(succeed);
    }


    private Result<Void> checkConfigParam(AppConfigDTO configDTO) {
        if (configDTO == null) {
            return Result.buildParamIllegal("配置信息为空");
        }
        if (configDTO.getAppId() == null) {
            return Result.buildParamIllegal("应用ID为空");
        }
        if (configDTO.getAnalyzeResponseEnable() != null && !yesOrNo(configDTO.getAnalyzeResponseEnable())) {
            return Result.buildParamIllegal("解析响应结果开关非法");
        }
        if (configDTO.getDslAnalyzeEnable() != null && !yesOrNo(configDTO.getDslAnalyzeEnable())) {
            return Result.buildParamIllegal("DSL分析开关非法");
        }
        if (configDTO.getAggrAnalyzeEnable() != null && !yesOrNo(configDTO.getAggrAnalyzeEnable())) {
            return Result.buildParamIllegal("聚合分析开关非法");
        }
        if (configDTO.getIsSourceSeparated() != null && !yesOrNo(configDTO.getIsSourceSeparated())) {
            return Result.buildParamIllegal("索引存储分离开关非法");
        }

        return Result.buildSucc();
    }

    private boolean hasOwnTemplate(int appId) {
        List<IndexTemplateInfo> templateLogics = indexTemplateInfoService.getAppLogicTemplatesByAppId(appId);
        return CollectionUtils.isNotEmpty(templateLogics);
    }

    private boolean hasOwnLogicCluster(int appId) {
        List<ClusterLogic> clusterLogics = clusterLogicService.getOwnedClusterLogicListByAppId(appId);
        return CollectionUtils.isNotEmpty(clusterLogics);
    }

    private Result<Void> validateAppFieldIsNull(AppDTO appDTO) {
        if (appDTO.getMemo() == null) {
            return Result.buildParamIllegal("备注为空");
        }
        return Result.buildSucc();
    }
}
