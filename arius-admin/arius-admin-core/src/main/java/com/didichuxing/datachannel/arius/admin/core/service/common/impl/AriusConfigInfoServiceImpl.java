package com.didichuxing.datachannel.arius.admin.core.service.common.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.AriusConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.config.AriusConfigInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.config.AriusConfigInfoPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.config.AriusConfigDimensionEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.config.AriusConfigStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.config.AriusConfigInfoDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.ProjectService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 *
 * @author d06679
 * @date 2019/3/14
 */
@Service
public class AriusConfigInfoServiceImpl implements AriusConfigInfoService {

    private static final ILog                LOGGER      = LogFactory.getLog(AriusConfigInfoServiceImpl.class);

    private static final String              NOT_EXIST   = "配置不存在";

    @Autowired
    private AriusConfigInfoDAO               configInfoDAO;

    @Autowired
    private OperateRecordService             operateRecordService;
    @Autowired
    private ProjectService projectService;

    private Cache<String, AriusConfigInfoPO> configCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(100).build();

    /**
     * 新增配置
     * @param configInfoDTO 配置信息
     * @param operator      操作人
     * @return 成功 true
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> addConfig(AriusConfigInfoDTO configInfoDTO, String operator) {
        Result<Void> checkResult = checkParam(configInfoDTO);
        if (checkResult.failed()) {
            LOGGER.warn("class=AriusConfigInfoServiceImpl||method=addConfig||msg={}||msg=check fail!",
                checkResult.getMessage());
            return Result.buildFrom(checkResult);
        }

        initConfig(configInfoDTO);

        AriusConfigInfoPO oldConfig = getByGroupAndNameFromDB(configInfoDTO.getValueGroup(),
            configInfoDTO.getValueName());
        if (oldConfig != null) {
            return Result.buildDuplicate("配置重复");
        }

        AriusConfigInfoPO param = ConvertUtil.obj2Obj(configInfoDTO, AriusConfigInfoPO.class);
        boolean succ = (1 == configInfoDAO.insert(param));
        if (succ) {
            operateRecordService.save(buildOperateRecord(configInfoDTO.getId(),operator,OperateTypeEnum.SETTING_ADD,
                    String.format("新增平台配置, 配置组:%s, 配置名称%s",
                    configInfoDTO.getValueGroup(), configInfoDTO.getValueName())));
        }
        return Result.build(succ, param.getId());
    }

    /**
     * 删除配置
     * @param configId 配置id
     * @param operator 操作人
     * @return 成功 true  失败 false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delConfig(Integer configId, String operator) {
        AriusConfigInfoPO configInfoPO = configInfoDAO.getById(configId);
        if (configInfoPO == null) {
            return Result.buildNotExist(NOT_EXIST);
        }

        boolean succ = (1 == configInfoDAO.updateByIdAndStatus(configId, AriusConfigStatusEnum.DELETED.getCode()));
        if (succ) {
            operateRecordService.save(buildOperateRecord(configId,operator,OperateTypeEnum.SETTING_DELETE,
                    String.format("删除平台配置, 配置组:%s, 配置名称%s", configInfoPO.getValueGroup(), configInfoPO.getValueName())));
        }

        return Result.build(succ);
    }

    /**
     * 编辑配置 只能编辑值  组和名称不能修改
     * @param configInfoDTO 配置内容
     * @param operator      操作人
     * @return 成功 true  失败 false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> editConfig(AriusConfigInfoDTO configInfoDTO, String operator) {
        if (AriusObjUtils.isNull(configInfoDTO.getId())) {
            return Result.buildParamIllegal("配置ID为空");
        }

        AriusConfigInfoPO configInfoPO = configInfoDAO.getById(configInfoDTO.getId());
        if (configInfoPO == null) {
            return Result.buildNotExist(NOT_EXIST);
        }

        boolean succ = (1 == configInfoDAO.update(ConvertUtil.obj2Obj(configInfoDTO, AriusConfigInfoPO.class)));

        if (succ) {
            operateRecordService.save(buildOperateRecord(configInfoPO.getId(),operator,OperateTypeEnum.SETTING_MODIFY
                    ,String.format("编辑平台配置，配置组：%s，配置名称%s，配置值：【%s】->【%s】", configInfoPO.getValueGroup(),
                            configInfoPO.getValueName(),configInfoPO.getValue(),configInfoDTO.getValue())));

        }

        return Result.build(succ);
    }

    /**
     * 使能配置
     * @param configId 配置id
     * @param status   状态
     * @param operator 操作人
     * @return 成功 true  失败 false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> switchConfig(Integer configId, Integer status, String operator) {
        AriusConfigInfoPO configInfoPO = configInfoDAO.getById(configId);
        if (configInfoPO == null) {
            return Result.buildNotExist(NOT_EXIST);
        }

        AriusConfigStatusEnum statusEnum = AriusConfigStatusEnum.valueOf(status);
        if (statusEnum == null) {
            return Result.buildParamIllegal("状态非法");
        }

        boolean succ = (1 == configInfoDAO.updateByIdAndStatus(configId, status));
        if (succ) {
            operateRecordService.save(buildOperateRecord(configInfoPO.getId(),operator,OperateTypeEnum.SETTING_MODIFY,String.format("平台配置%s, 配置组:%s, 配置名称%s",
                    statusEnum.getDesc(), configInfoPO.getValueGroup(), configInfoPO.getValueName())));

        }

        return Result.build(succ);
    }

    /**
     * 根据配置组获取配置项
     * @param group 配置组
     * @return 配置AriusConfigInfoPO列表  项目内部使用
     * <p>
     * 如果配置组不存在 返回空列表
     */
    @Override
    public List<AriusConfigInfo> getConfigByGroup(String group) {
        List<AriusConfigInfo> configInfos = Lists.newArrayList();

        List<AriusConfigInfoPO> configInfoPOList = configInfoDAO.listByGroup(group);
        if (CollectionUtils.isEmpty(configInfoPOList)) {
            return configInfos;
        }

        return ConvertUtil.list2List(configInfoPOList, AriusConfigInfo.class);
    }

    /**
     * 根据查询条件返回AriusConfigInfoVO列表
     * @param param 查询条件
     * @return 配置列表
     *
     * 如果不存在,返回空列表
     */
    @Override
    public List<AriusConfigInfo> queryByCondition(AriusConfigInfoDTO param) {
        List<AriusConfigInfoPO> configInfoPOList = configInfoDAO
            .listByCondition(ConvertUtil.obj2Obj(param, AriusConfigInfoPO.class));
        return ConvertUtil.list2List(configInfoPOList, AriusConfigInfo.class);
    }

    /**
     * 查询指定配置
     * @param configId 配置id
     * @return 配置信息  不存在返回null
     */
    @Override
    public AriusConfigInfo getConfigById(Integer configId) {
        return ConvertUtil.obj2Obj(configInfoDAO.getById(configId), AriusConfigInfo.class);
    }

    /**
     * 获取int类型配置
     * @param group        配置组
     * @param name         配置项
     * @param defaultValue 默认值
     * @return 如果查到转换后返回, 转换报错或者没有查到则返回默认值
     */
    @Override
    public Integer intSetting(String group, String name, Integer defaultValue) {
        try {
            AriusConfigInfoPO configInfoPO = getByGroupAndName(group, name);
            if (configInfoPO == null || StringUtils.isBlank(configInfoPO.getValue())) {
                return defaultValue;
            }
            return Integer.valueOf(configInfoPO.getValue());
        } catch (NumberFormatException e) {
            if (!EnvUtil.isOnline()) {
                LOGGER.warn(
                    "class=AriusConfigInfoServiceImpl||method=intSetting||group={}||name={}||msg=get config error!",
                    group, name);
            }
        }
        return defaultValue;
    }

    /**
     * 获取long类型配置
     * @param group        配置组
     * @param name         配置项
     * @param defaultValue 默认值
     * @return 如果查到转换后返回, 转换报错或者没有查到则返回默认值
     */
    @Override
    public Long longSetting(String group, String name, Long defaultValue) {
        try {
            AriusConfigInfoPO configInfoPO = getByGroupAndName(group, name);
            if (configInfoPO == null || StringUtils.isBlank(configInfoPO.getValue())) {
                return defaultValue;
            }
            return Long.valueOf(configInfoPO.getValue());
        } catch (Exception e) {
            if (!EnvUtil.isOnline()) {
                LOGGER.warn(
                    "class=AriusConfigInfoServiceImpl||method=longSetting||group={}||name={}||msg=get config error!",
                    group, name);
            }
        }
        return defaultValue;
    }

    /**
     * 获取double类型配置
     * @param group        配置组
     * @param name         配置项
     * @param defaultValue 默认值
     * @return 如果查到转换后返回, 转换报错或者没有查到则返回默认值
     */
    @Override
    public Double doubleSetting(String group, String name, Double defaultValue) {
        try {
            AriusConfigInfoPO configInfoPO = getByGroupAndName(group, name);
            if (configInfoPO == null || StringUtils.isBlank(configInfoPO.getValue())) {
                return defaultValue;
            }
            return Double.valueOf(configInfoPO.getValue());
        } catch (Exception e) {
            if (!EnvUtil.isOnline()) {
                LOGGER.warn(
                    "class=AriusConfigInfoServiceImpl||method=doubleSetting||group={}||name={}||msg=get config error!",
                    group, name, e);
            }
        }
        return defaultValue;
    }

    /**
     * 获取String类型配置
     * @param group        配置组
     * @param name         配置项
     * @param defaultValue 默认值
     * @return 如果查到转换后返回, 转换报错或者没有查到则返回默认值
     */
    @Override
    public String stringSetting(String group, String name, String defaultValue) {
        try {
            AriusConfigInfoPO configInfoPO = getByGroupAndName(group, name);
            if (configInfoPO == null || StringUtils.isBlank(configInfoPO.getValue())) {
                return defaultValue;
            }
            return configInfoPO.getValue();
        } catch (Exception e) {
            if (!EnvUtil.isOnline()) {
                LOGGER.warn(
                    "class=AriusConfigInfoServiceImpl||method=stringSetting||group={}||name={}||msg=get config error!",
                    group, name, e);
            }
        }
        return defaultValue;
    }

    /**
     * 获取String类型配置 用字符分割
     *
     * @param group        配置组
     * @param name         配置项
     * @param defaultValue 默认值
     * @param split        分隔符
     * @return 分隔符
     */
    @Override
    public Set<String> stringSettingSplit2Set(String group, String name, String defaultValue, String split) {
        String string = stringSetting(group, name, defaultValue);
        return Sets.newHashSet(string.split(split));
    }

    /**
     * 获取bool类型配置
     * @param group        配置组
     * @param name         配置项
     * @param defaultValue 默认值
     * @return 如果查到转换后返回, 转换报错或者没有查到则返回默认值
     */
    @Override
    public Boolean booleanSetting(String group, String name, Boolean defaultValue) {
        AriusConfigInfoPO configInfoPO = getByGroupAndName(group, name);
        if (configInfoPO == null || StringUtils.isBlank(configInfoPO.getValue())) {
            return defaultValue;
        }
        return Boolean.valueOf(configInfoPO.getValue());
    }

    /**
     * 获取Object类型配置
     * @param group        配置组
     * @param name         配置项
     * @param defaultValue 默认值
     * @param clazz        返回类型
     * @return 如果查到转换后返回, 转换报错或者没有查到则返回默认值
     */
    @Override
    public <T> T objectSetting(String group, String name, T defaultValue, Class<T> clazz) {
        try {
            AriusConfigInfoPO configInfoPO = getByGroupAndName(group, name);
            if (configInfoPO == null || StringUtils.isBlank(configInfoPO.getValue())) {
                return defaultValue;
            }
            return JSON.parseObject(configInfoPO.getValue(), clazz);
        } catch (Exception e) {
            if (!EnvUtil.isOnline()) {
                LOGGER.warn(
                    "class=AriusConfigInfoServiceImpl||method=objectSetting||group={}||name={}||msg=get config error!",
                    group, name, e);
            }
        }
        return defaultValue;
    }

    /******************************************* private method **************************************************/
    private Result<Void> checkParam(AriusConfigInfoDTO configInfoDTO) {
        if (AriusObjUtils.isNull(configInfoDTO)) {
            return Result.buildParamIllegal("配置信息为空");
        }
        if (AriusObjUtils.isNull(configInfoDTO.getValueGroup())) {
            return Result.buildParamIllegal("组为空");
        }
        if (AriusObjUtils.isNull(configInfoDTO.getValueName())) {
            return Result.buildParamIllegal("名字为空");
        }
        return Result.buildSucc();
    }

    private void initConfig(AriusConfigInfoDTO configInfoDTO) {

        if (configInfoDTO.getDimension() == null) {
            configInfoDTO.setDimension(AriusConfigDimensionEnum.UNKNOWN.getCode());
        }

        if (configInfoDTO.getStatus() == null) {
            configInfoDTO.setStatus(AriusConfigStatusEnum.NORMAL.getCode());
        }

        if (configInfoDTO.getValue() == null) {
            configInfoDTO.setValue("");
        }

        if (configInfoDTO.getMemo() == null) {
            configInfoDTO.setMemo("");
        }
    }

    private AriusConfigInfoPO getByGroupAndName(String group, String valueName) {
        try {
            return configCache.get(group + "@" + valueName, () -> getByGroupAndNameFromDB(group, valueName));
        } catch (Exception e) {
            return getByGroupAndNameFromDB(group, valueName);
        }
    }

    private AriusConfigInfoPO getByGroupAndNameFromDB(String group, String valueName) {
        return configInfoDAO.getByGroupAndName(group, valueName);
    }
    
    private OperateRecord buildOperateRecord(Object bizId, String operator, OperateTypeEnum operationTypeEnum, String content) {
        return new OperateRecord.Builder().content(content).bizId(bizId).operationTypeEnum(operationTypeEnum)
                .project(projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID))
                .userOperation(operator)
                .buildDefaultManualTrigger();
    }
}