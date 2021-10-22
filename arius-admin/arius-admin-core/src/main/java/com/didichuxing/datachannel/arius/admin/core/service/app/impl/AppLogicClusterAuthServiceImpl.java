package com.didichuxing.datachannel.arius.admin.core.service.app.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppLogicClusterAuthDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppLogicClusterAuthEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppLogicClusterAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.AppLogicClusterAuthPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.LogicClusterPO;
import com.didichuxing.datachannel.arius.admin.common.event.auth.AppLogicClusterAuthAddEvent;
import com.didichuxing.datachannel.arius.admin.common.event.auth.AppLogicClusterAuthDeleteEvent;
import com.didichuxing.datachannel.arius.admin.common.event.auth.AppLogicClusterAuthEditEvent;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.core.component.ResponsibleConvertTool;
import com.didichuxing.datachannel.arius.admin.common.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicClusterAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.AppDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.AppLogicClusterAuthDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.LogicClusterDAO;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * APP 逻辑集群权限服务
 * @author wangshu
 * @date 2020/09/19
 */
@Service
public class AppLogicClusterAuthServiceImpl implements AppLogicClusterAuthService {

    private static final ILog      sLogger = LogFactory.getLog(AppLogicTemplateAuthServiceImpl.class);

    @Autowired
    private AppLogicClusterAuthDAO logicClusterAuthDAO;

    @Autowired
    private LogicClusterDAO        logicClusterDAO;

    @Autowired
    private AppDAO                 appDAO;

    @Autowired
    private OperateRecordService   operateRecordService;

    @Autowired
    private AriusUserInfoService   ariusUserInfoService;

    @Autowired
    private ResponsibleConvertTool responsibleConvertTool;

    /**
     * 设置APP对某逻辑集群的权限.
     * 封装了新增、更新、删除操作，调用接口时只需描述期望的权限状态
     * @param appId          APP的ID
     * @param logicClusterId 逻辑集群ID
     * @param auth           要设置的权限
     * @param responsible    责任人，逗号分隔的用户名列表
     * @param operator       操作人
     * @return 设置结果
     */
    @Override
    public Result ensureSetLogicClusterAuth(Integer appId, Long logicClusterId, AppLogicClusterAuthEnum auth,
                                            String responsible, String operator) {
        // 参数检查
        if (appId == null) {
            return Result.buildParamIllegal("未指定appId");
        }

        if (logicClusterId == null) {
            return Result.buildParamIllegal("未指定逻辑集群ID");
        }

        if (StringUtils.isBlank(operator)) {
            return Result.buildParamIllegal("未指定操作人");
        }

        // 获取已经存在的权限，可能来自于权限表（id不为null）和创建信息表（id为null）
        AppLogicClusterAuthDTO oldAuthDTO = getLogicClusterAuth(appId, logicClusterId);

        if (oldAuthDTO == null) {
            /*------------------之前无权限------------------*/
            // NO_PERMISSIONS不需添加
            if (auth == null || auth == AppLogicClusterAuthEnum.NO_PERMISSIONS) {
                return Result.buildSucc();
            }

            // 新增
            return addLogicClusterAuth(
                new AppLogicClusterAuthDTO(null, appId, logicClusterId, auth.getCode(), responsible), operator);
        } else {
            /*------------------之前有权限------------------*/
            if (oldAuthDTO.getId() != null) {
                /*------------------权限来自权限表------------------*/
                // 期望删除权限
                if (auth == AppLogicClusterAuthEnum.NO_PERMISSIONS) {
                    return deleteLogicClusterAuthById(oldAuthDTO.getId(), operator);
                }

                // 期望更新权限信息
                AppLogicClusterAuthDTO newAuthDTO = new AppLogicClusterAuthDTO(oldAuthDTO.getId(), null, null,
                    auth == null ? null : auth.getCode(), StringUtils.isBlank(responsible) ? null : responsible);
                return updateLogicClusterAuth(newAuthDTO, operator);
            } else {
                /*------------------权限来自于创建信息表（权限肯定为OWN）-----------------*/
                // 对于集群owner的app权限信息不能修改，只能增加大于OWN的权限
                if (auth != null
                    && AppLogicClusterAuthEnum.valueOf(auth.getCode()).higher(AppLogicClusterAuthEnum.OWN)) {
                    return addLogicClusterAuth(
                        new AppLogicClusterAuthDTO(null, appId, logicClusterId, auth.getCode(), responsible), operator);
                } else {
                    return Result.buildFail("不支持对集群owner的权限进行修改");
                }
            }
        }
    }

    /**
     * 插入逻辑集群权限点
     * @param logicClusterAuth 逻辑集群权限点
     * @return
     */
    @Override
    public Result addLogicClusterAuth(AppLogicClusterAuthDTO logicClusterAuth, String operator) {

        Result checkResult = validateLogicClusterAuth(logicClusterAuth, OperationEnum.ADD);
        if (checkResult.failed()) {
            sLogger.warn("class=AppLogicClusterAuthServiceImpl||method=createLogicClusterAuth||msg={}||msg=check fail!",
                checkResult.getMessage());
            return checkResult;
        }

        return addLogicClusterAuthWithoutCheck(logicClusterAuth, operator);
    }

    /**
     * 更新逻辑集群权限点
     * @param logicClusterAuth 逻辑集群权限点
     * @return
     */
    @Override
    public Result updateLogicClusterAuth(AppLogicClusterAuthDTO logicClusterAuth, String operator) {
        // 只支持修改权限类型和责任人
        logicClusterAuth.setAppId(null);
        logicClusterAuth.setLogicClusterId(null);

        Result checkResult = validateLogicClusterAuth(logicClusterAuth, OperationEnum.EDIT);
        if (checkResult.failed()) {
            sLogger.warn("class=AppLogicClusterAuthServiceImpl||method=createLogicClusterAuth||msg={}||msg=check fail!",
                checkResult.getMessage());
            return checkResult;
        }

        return updateLogicClusterAuthWithoutCheck(logicClusterAuth, operator);
    }

    /**
     * 删除权限点
     * @param authId 权限点ID
     * @return
     */
    @Override
    public Result deleteLogicClusterAuthById(Long authId, String operator) {

        AppLogicClusterAuthPO oldAuthPO = logicClusterAuthDAO.getById(authId);
        if (oldAuthPO == null) {
            return Result.buildNotExist("权限不存在");
        }

        boolean succeed = 1 == logicClusterAuthDAO.delete(authId);
        if (succeed) {
            SpringTool.publish(new AppLogicClusterAuthDeleteEvent(this,
                responsibleConvertTool.obj2Obj(oldAuthPO, AppLogicClusterAuth.class)));

            operateRecordService.save(ModuleEnum.LOGIC_CLUSTER_PERMISSIONS, OperationEnum.DELETE, oldAuthPO.getId(),
                StringUtils.EMPTY, operator);
        }

        return Result.build(succeed);
    }

    /**
     * 获取APP所有权限点
     * @param appId 逻辑ID
     * @return
     */
    @Override
    public List<AppLogicClusterAuthDTO> getLogicClusterAuths(Integer appId) {

        if (appId == null) {
            return new ArrayList<>();
        }

        // 权限表
        List<AppLogicClusterAuthPO> authPOs = logicClusterAuthDAO.listByAppId(appId);
        List<AppLogicClusterAuthDTO> authDTOs = ConvertUtil.list2List(authPOs, AppLogicClusterAuthDTO.class);

        // 从逻辑集群表获取APP作为owner的集群
        List<LogicClusterPO> logicClusterPOs = logicClusterDAO.listByAppId(appId);
        authDTOs.addAll(
            logicClusterPOs.stream().map(this::buildLogicClusterAuthForClusterOwner).collect(Collectors.toList()));

        return authDTOs;
    }

    /**
     * 根据ID获取逻辑集群权限点
     * @param authId 权限点ID
     * @return
     */
    @Override
    public AppLogicClusterAuthDTO getLogicClusterAuthById(Long authId) {
        return ConvertUtil.obj2Obj(logicClusterAuthDAO.getById(authId), AppLogicClusterAuthDTO.class);
    }

    /**
     * 获取指定app对指定逻辑集群的权限.
     * @param appId          APP ID
     * @param logicClusterId 逻辑集群ID
     */
    @Override
    public AppLogicClusterAuthEnum getLogicClusterAuthEnum(Integer appId, Long logicClusterId) {
        if (appId == null || logicClusterId == null) {
            return AppLogicClusterAuthEnum.NO_PERMISSIONS;
        }

        AppLogicClusterAuthDTO authDTO = getLogicClusterAuth(appId, logicClusterId);
        return authDTO == null ? AppLogicClusterAuthEnum.NO_PERMISSIONS
            : AppLogicClusterAuthEnum.valueOf(authDTO.getType());
    }

    /**
     * 获取指定app对指定逻辑集群的权限，若没有权限则返回null.
     * 有权限时，返回结果中id不为null则为来自于权限表的数据，否则为来自于创建表的数据
     * @param appId          APP ID
     * @param logicClusterId 逻辑集群ID
     */
    @Override
    public AppLogicClusterAuthDTO getLogicClusterAuth(Integer appId, Long logicClusterId) {
        if (appId == null || logicClusterId == null) {
            return null;
        }

        // 从逻辑集群表获取创建信息
        LogicClusterPO logicClusterPO = logicClusterDAO.getById(logicClusterId);
        AppLogicClusterAuthEnum authFromCreateRecord = (logicClusterPO != null
                                                        && logicClusterPO.getAppId().equals(appId))
                                                            ? AppLogicClusterAuthEnum.OWN
                                                            : AppLogicClusterAuthEnum.NO_PERMISSIONS;

        // 从权限表获取权限信息
        AppLogicClusterAuthPO authPO = logicClusterAuthDAO.getByAppIdAndLogicCluseterId(appId, logicClusterId);
        AppLogicClusterAuthEnum authFromAuthRecord = (authPO != null)
            ? AppLogicClusterAuthEnum.valueOf(authPO.getType())
            : AppLogicClusterAuthEnum.NO_PERMISSIONS;

        // 都没有权限
        if (authFromCreateRecord == AppLogicClusterAuthEnum.NO_PERMISSIONS
            && authFromAuthRecord == AppLogicClusterAuthEnum.NO_PERMISSIONS) {
            return null;
        }

        // 选择权限高的构建AppLogicClusterAuthDTO，优先取权限表中的记录
        return authFromAuthRecord.higherOrEqual(authFromCreateRecord)
            ? ConvertUtil.obj2Obj(authPO, AppLogicClusterAuthDTO.class)
            : buildLogicClusterAuthForClusterOwner(logicClusterPO);

    }

    /**
     * 获取逻辑集群权限点列表
     * @param logicClusterId  逻辑集群ID
     * @param clusterAuthType 集群权限类型
     * @return
     */
    @Override
    public List<AppLogicClusterAuthDTO> getLogicClusterAuths(Long logicClusterId,
                                                             AppLogicClusterAuthEnum clusterAuthType) {

        AppLogicClusterAuthPO queryParams = new AppLogicClusterAuthPO();
        if (logicClusterId != null) {
            queryParams.setLogicClusterId(logicClusterId);
        }

        if (clusterAuthType != null) {
            queryParams.setType(clusterAuthType.getCode());
        }

        // 权限表
        List<AppLogicClusterAuthPO> authPOs = logicClusterAuthDAO.listByCondition(queryParams);
        List<AppLogicClusterAuthDTO> authDTOS = ConvertUtil.list2List(authPOs, AppLogicClusterAuthDTO.class);

        // 从逻辑集群表获取APP作为owner的集群
        if (logicClusterId != null && clusterAuthType == AppLogicClusterAuthEnum.OWN) {
            LogicClusterPO logicClusterPO = logicClusterDAO.getById(logicClusterId);
            if (logicClusterPO != null) {
                authDTOS.add(buildLogicClusterAuthForClusterOwner(logicClusterPO));
            }
        }

        return authDTOS;
    }

    @Override
    public boolean canCreateLogicTemplate(Integer appId, Long logicClusterId) {
        if (appId == null || logicClusterId == null) {
            return false;
        }

        AppLogicClusterAuthEnum authEnum = getLogicClusterAuthEnum(appId, logicClusterId);
        return authEnum.higherOrEqual(AppLogicClusterAuthEnum.ACCESS);
    }

    /**************************************** private method ****************************************************/
    /**
     * 增加权限  不做参数校验
     * @param authDTO  权限信息
     * @param operator 操作人
     * @return result
     */
    @Override
    public Result addLogicClusterAuthWithoutCheck(AppLogicClusterAuthDTO authDTO, String operator) {
        AppLogicClusterAuthPO authPO = responsibleConvertTool.obj2Obj(authDTO, AppLogicClusterAuthPO.class);

        boolean succeed = 1 == logicClusterAuthDAO.insert(authPO);
        if (succeed) {
            // 发送消息
            SpringTool.publish(new AppLogicClusterAuthAddEvent(this,
                responsibleConvertTool.obj2Obj(authPO, AppLogicClusterAuth.class)));

            // 记录操作
            operateRecordService.save(ModuleEnum.LOGIC_CLUSTER_PERMISSIONS, OperationEnum.ADD, authPO.getId(),
                JSON.toJSONString(authPO), operator);
        }

        return Result.build(succeed);
    }

    /**
     * 验证权限参数
     * @param authDTO   参数信息
     * @param operation 操作
     * @return result
     */
    private Result validateLogicClusterAuth(AppLogicClusterAuthDTO authDTO, OperationEnum operation) {
        if (!EnvUtil.isOnline()) {
            sLogger.info("class=AppAuthServiceImpl||method=validateTemplateAuth||authDTO={}||operator={}",
                JSON.toJSONString(authDTO), operation);
        }

        if (authDTO == null) {
            return Result.buildParamIllegal("权限信息为空");
        }

        Integer appId = authDTO.getAppId();
        Long logicClusterId = authDTO.getLogicClusterId();
        AppLogicClusterAuthEnum authEnum = AppLogicClusterAuthEnum.valueOf(authDTO.getType());

        if (OperationEnum.ADD.equals(operation)) {
            // 新增权限检查
            if (AriusObjUtils.isNull(appId)) {
                return Result.buildParamIllegal("appId为空");
            }

            if (AriusObjUtils.isNull(appDAO.getById(appId))) {
                return Result.buildParamIllegal(String.format("app[%d]不存在", appId));
            }

            if (AriusObjUtils.isNull(logicClusterId)) {
                return Result.buildParamIllegal("逻辑集群ID为空");
            }

            LogicClusterPO logicCluster = logicClusterDAO.getById(logicClusterId);
            if (AriusObjUtils.isNull(logicCluster)) {
                return Result.buildParamIllegal(String.format("逻辑集群[%d]不存在", logicClusterId));
            }

            if (AriusObjUtils.isNull(authDTO.getType())) {
                return Result.buildParamIllegal("权限类型为空");
            }

            if (AriusObjUtils.isNull(authDTO.getResponsible())) {
                return Result.buildParamIllegal("责任人为空");
            }

            // 重复添加不做幂等，抛出错误
            if (null != logicClusterAuthDAO.getByAppIdAndLogicCluseterId(appId, logicClusterId)) {
                return Result.buildDuplicate("权限已存在");
            }

            // APP是逻辑集群的owner，无需添加
            if (logicCluster.getAppId().equals(appId) && authEnum == AppLogicClusterAuthEnum.OWN) {
                return Result.buildDuplicate(String.format("APP[%d]已有管理权限", appId));
            }

        } else if (OperationEnum.EDIT.equals(operation)) {
            // 更新权限检查
            if (AriusObjUtils.isNull(authDTO.getId())) {
                return Result.buildParamIllegal("权限ID为空");
            }

            if (null == logicClusterAuthDAO.getById(authDTO.getId())) {
                return Result.buildNotExist("权限不存在");
            }
        }

        if (AppLogicClusterAuthEnum.NO_PERMISSIONS == authEnum) {
            // 不应该走到这一步，防御编码
            return Result.buildParamIllegal("无权限无需添加");
        }

        // 不能添加管理权限
        if (AppLogicClusterAuthEnum.ALL == authEnum) {
            return Result.buildParamIllegal("不支持添加超管权限");
        }

        // 校验责任人是否合法
        if (!AriusObjUtils.isNull(authDTO.getResponsible())
            && AriusObjUtils.isNull(ariusUserInfoService.getByDomainAccount(authDTO.getResponsible()))) {
            return Result.buildParamIllegal("责任人非法");
        }

        return Result.buildSucc();
    }

    /**
     * 由逻辑集群记录构建owner APP的权限数据
     * @param logicClusterPO 逻辑集群记录
     */
    private AppLogicClusterAuthDTO buildLogicClusterAuthForClusterOwner(LogicClusterPO logicClusterPO) {
        if (logicClusterPO == null) {
            return null;
        }
        AppLogicClusterAuthDTO appLogicClusterAuthDTO = new AppLogicClusterAuthDTO();
        appLogicClusterAuthDTO.setId(null);
        appLogicClusterAuthDTO.setAppId(logicClusterPO.getAppId());
        appLogicClusterAuthDTO.setLogicClusterId(logicClusterPO.getId());
        appLogicClusterAuthDTO.setType(AppLogicClusterAuthEnum.OWN.getCode());
        appLogicClusterAuthDTO.setResponsible(logicClusterPO.getResponsible());
        return appLogicClusterAuthDTO;
    }

    /**
     * 修改权限 可以修改权限类型和责任人 不校验参数
     * @param authDTO  参数
     * @param operator 操作人
     * @return result
     */
    private Result updateLogicClusterAuthWithoutCheck(AppLogicClusterAuthDTO authDTO, String operator) {

        AppLogicClusterAuthPO oldAuthPO = logicClusterAuthDAO.getById(authDTO.getId());
        AppLogicClusterAuthPO newAuthPO = responsibleConvertTool.obj2Obj(authDTO, AppLogicClusterAuthPO.class);
        boolean succeed = 1 == logicClusterAuthDAO.update(newAuthPO);
        if (succeed) {
            SpringTool.publish(new AppLogicClusterAuthEditEvent(this,
                responsibleConvertTool.obj2Obj(oldAuthPO, AppLogicClusterAuth.class), responsibleConvertTool
                    .obj2Obj(logicClusterAuthDAO.getById(authDTO.getId()), AppLogicClusterAuth.class)));

            operateRecordService.save(ModuleEnum.LOGIC_CLUSTER_PERMISSIONS, OperationEnum.EDIT, oldAuthPO.getId(),
                JSON.toJSONString(newAuthPO), operator);
        }

        return Result.build(succeed);
    }
}
