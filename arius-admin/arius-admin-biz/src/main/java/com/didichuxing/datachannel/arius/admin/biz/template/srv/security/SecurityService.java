package com.didichuxing.datachannel.arius.admin.biz.template.srv.security;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

/**
 * @author didi
 */
public interface SecurityService {

    /**
     * 为逻辑模板创建APP的指定权限
     * @param appId APPID
     * @param logicTemplateId 逻辑模板
     * @param authType 权限
     * @param retryCount 重试次数
     * @return result
     */
    Result<Void> saveAppLogicTemplateAuth(Integer appId, Integer logicTemplateId, Integer authType, int retryCount);

    /**
     * 为逻辑模板删除APP的指定权限
     * @param appId APPID
     * @param logicTemplateId 逻辑模板
     * @param authType 权限
     * @param retryCount 重试次数
     * @return result
     */
    Result<Void> deleteAppLogicTemplateAuth(Integer appId, Integer logicTemplateId, Integer authType, int retryCount);

    /**
     * 修改逻辑模板的APPID
     * @param logicTemplateId 逻辑模板
     * @param srcAppId 源APP
     * @param tgtAppId 现APP
     * @param retryCount 重试次数
     * @return result
     */
    Result<Void> editLogicTemplateOwnApp(Integer logicTemplateId, Integer srcAppId, Integer tgtAppId, int retryCount);

    /**
     * 为物理模板创建APP的管理权限
     * @param templatePhysical 模板信息
     * @param appId APPID
     * @param authType 权限
     * @param retryCount 重试次数
     * @return result
     */
    Result<Void> saveAppPhysicalTemplateAuth(IndexTemplatePhy templatePhysical, Integer appId, Integer authType,
                                       int retryCount) throws ESOperateException;

    /**
     * 删除物理模板的APP管理权限
     * @param templatePhysical 模板信息
     * @param appId APPID
     * @param authType 权限
     * @param retryCount 重试次数
     * @return result
     */
    Result<Void> deleteAppPhysicalTemplateAuth(IndexTemplatePhy templatePhysical, Integer appId, Integer authType,
                                         int retryCount) throws ESOperateException;

    /**
     * APP密码修改
     * @param appId APPID
     * @param verifyCode 校验码
     * @param retryCount 重试次数
     * @return result
     */
    Result<Void> editAppVerifyCode(Integer appId, String verifyCode, int retryCount);

    /**
     * 元数据一致性保证
     * @param cluster 集群
     * @return result
     */
    void checkMeta(String cluster);
}
