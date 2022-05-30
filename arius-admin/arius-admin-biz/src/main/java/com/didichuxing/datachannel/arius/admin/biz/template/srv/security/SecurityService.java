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
     * @param projectId APPID
     * @param logicTemplateId 逻辑模板
     * @param authType 权限
     * @param retryCount 重试次数
     * @return result
     */
    Result<Void> saveProjectLogicTemplateAuth(Integer projectId, Integer logicTemplateId, Integer authType, int retryCount);

    /**
     * 为逻辑模板删除APP的指定权限
     * @param projectId APPID
     * @param logicTemplateId 逻辑模板
     * @param authType 权限
     * @param retryCount 重试次数
     * @return result
     */
    Result<Void> deleteProjectLogicTemplateAuth(Integer projectId, Integer logicTemplateId, Integer authType, int retryCount);

    /**
     * 修改逻辑模板的project
     * @param logicTemplateId 逻辑模板
     * @param srcProjectId 源project
     * @param tgtProjectId 现project
     * @param retryCount 重试次数
     * @return result
     */
    Result<Void> editLogicTemplateOwnProject(Integer logicTemplateId, Integer srcProjectId, Integer tgtProjectId, int retryCount);

    /**
     * 为物理模板创建APP的管理权限
     * @param templatePhysical 模板信息
     * @param projectId APPID
     * @param authType 权限
     * @param retryCount 重试次数
     * @return result
     */
    Result<Void> saveProjectPhysicalTemplateAuth(IndexTemplatePhy templatePhysical, Integer projectId, Integer authType,
                                                 int retryCount) throws ESOperateException;

    /**
     * 删除物理模板的project管理权限
     * @param templatePhysical 模板信息
     * @param projectId APPID
     * @param authType 权限
     * @param retryCount 重试次数
     * @return result
     */
    Result<Void> deleteProjectPhysicalTemplateAuth(IndexTemplatePhy templatePhysical, Integer projectId, Integer authType,
                                                   int retryCount) throws ESOperateException;

    /**
     * APP密码修改
     * @param projectId APPID
     * @param verifyCode 校验码
     * @param retryCount 重试次数
     * @return result
     */
    Result<Void> editProjectVerifyCode(Integer projectId, String verifyCode, int retryCount);

    /**
     * 元数据一致性保证
     * @param cluster 集群
     * @return result
     */
    void checkMeta(String cluster);
}